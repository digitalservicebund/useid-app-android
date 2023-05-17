package de.digitalService.useID.ui.coordinators

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import de.digitalService.useID.StorageManagerType
import de.digitalService.useID.analytics.IssueTrackerManagerType
import de.digitalService.useID.analytics.TrackerManagerType
import de.digitalService.useID.flows.CanStateMachine
import de.digitalService.useID.flows.IdentificationStateMachine
import de.digitalService.useID.getLogger
import de.digitalService.useID.idCardInterface.EidInteractionEvent
import de.digitalService.useID.idCardInterface.EidInteractionException
import de.digitalService.useID.idCardInterface.EidInteractionManager
import de.digitalService.useID.idCardInterface.IdentificationAttributes
import de.digitalService.useID.ui.navigation.Navigator
import de.digitalService.useID.ui.screens.destinations.IdentificationAttributeConsentDestination
import de.digitalService.useID.ui.screens.destinations.IdentificationCardBlockedDestination
import de.digitalService.useID.ui.screens.destinations.IdentificationCardDeactivatedDestination
import de.digitalService.useID.ui.screens.destinations.IdentificationCardUnreadableDestination
import de.digitalService.useID.ui.screens.destinations.IdentificationFetchMetadataDestination
import de.digitalService.useID.ui.screens.destinations.IdentificationOtherErrorDestination
import de.digitalService.useID.ui.screens.destinations.IdentificationPersonalPinDestination
import de.digitalService.useID.ui.screens.destinations.IdentificationScanDestination
import de.digitalService.useID.util.CoroutineContextProviderType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IdentificationCoordinator @Inject constructor(
    @ApplicationContext private val context: Context,
    private val canCoordinator: CanCoordinator,
    private val navigator: Navigator,
    private val eidInteractionManager: EidInteractionManager,
    private val storageManager: StorageManagerType,
    private val trackerManager: TrackerManagerType,
    private val flowStateMachine: IdentificationStateMachine,
    private val canStateMachine: CanStateMachine,
    private val coroutineContextProvider: CoroutineContextProviderType,
    private val issueTrackerManager: IssueTrackerManagerType
) {
    private val logger by getLogger()

    private val _scanInProgress: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val scanInProgress: Flow<Boolean>
        get() = _scanInProgress

    private var stateMachineCoroutineScope: Job? = null
    private var eIdEventFlowCoroutineScope: Job? = null
    private var canEventFlowCoroutineScope: Job? = null

    private val _stateFlow: MutableStateFlow<SubCoordinatorState> = MutableStateFlow(SubCoordinatorState.FINISHED)
    val stateFlow: StateFlow<SubCoordinatorState>
        get() = _stateFlow

    private fun collectStateMachineEvents() {
        if (stateMachineCoroutineScope != null) {
            return
        }

        stateMachineCoroutineScope = CoroutineScope(coroutineContextProvider.Default).launch {
            flowStateMachine.state.collect { eventAndPair ->
                if (eventAndPair.first is IdentificationStateMachine.Event.Back) {
                    navigator.pop()
                    if (eventAndPair.second == IdentificationStateMachine.State.Invalid) {
                        resetCoordinatorState()
                    }
                } else {
                    when (val state = eventAndPair.second) {
                        is IdentificationStateMachine.State.StartIdentification -> {
                            executeIdentification(state.tcTokenUrl)
                            navigator.popUpToOrNavigate(IdentificationFetchMetadataDestination(state.backingDownAllowed), false)
                        }

                        is IdentificationStateMachine.State.FetchingMetadata -> {
                            // this state is currently necessary for proper error handling after onAuthenticationStarted callback
                            // and should be removed once USEID-1055 is fixed
                        }

                        is IdentificationStateMachine.State.FetchingMetadataFailed -> {
                            eidInteractionManager.cancelTask()
                            navigator.navigate(IdentificationOtherErrorDestination)
                        }

                        is IdentificationStateMachine.State.RequestCertificate -> eidInteractionManager.getCertificate()
                        is IdentificationStateMachine.State.CertificateDescriptionReceived ->
                            navigator.navigatePopping(IdentificationAttributeConsentDestination(IdentificationAttributes(state.identificationRequest.requiredAttributes, state.certificateDescription), state.backingDownAllowed))

                        is IdentificationStateMachine.State.PinInput -> navigator.navigate(IdentificationPersonalPinDestination(false))
                        is IdentificationStateMachine.State.PinInputRetry -> navigator.navigate(IdentificationPersonalPinDestination(true))
                        is IdentificationStateMachine.State.PinEntered -> {
                            navigator.popUpToOrNavigate(IdentificationScanDestination, false)
                            if (state.firstTime) eidInteractionManager.acceptAccessRights() else eidInteractionManager.providePin(state.pin)
                        }

                        is IdentificationStateMachine.State.CanRequested -> startCanFlow(state.pin)
                        is IdentificationStateMachine.State.PinRequested -> eidInteractionManager.providePin(state.pin)
                        is IdentificationStateMachine.State.Finished -> finishIdentification(state.redirectUrl)

                        is IdentificationStateMachine.State.CardDeactivated -> navigator.navigate(IdentificationCardDeactivatedDestination)
                        is IdentificationStateMachine.State.CardBlocked -> navigator.navigate(IdentificationCardBlockedDestination)
                        is IdentificationStateMachine.State.CardUnreadable -> navigator.navigate(IdentificationCardUnreadableDestination(true, state.redirectUrl))

                        IdentificationStateMachine.State.Invalid -> logger.debug("Ignoring transition to state INVALID.")
                    }
                }
            }
        }
    }

    fun startIdentificationProcess(tcTokenUrl: String, setupSkipped: Boolean) {
        collectStateMachineEvents()
        _stateFlow.value = SubCoordinatorState.ACTIVE

        flowStateMachine.transition(IdentificationStateMachine.Event.Initialize(setupSkipped, Uri.parse(tcTokenUrl)))
        canStateMachine.transition(CanStateMachine.Event.Invalidate)
    }

    fun confirmAttributesForIdentification() {
        flowStateMachine.transition(IdentificationStateMachine.Event.ConfirmAttributes)
    }

    fun setPin(pin: String) {
        flowStateMachine.transition(IdentificationStateMachine.Event.EnterPin(pin))
    }

    private fun startCanFlow(pin: String?) {
        if (canCoordinator.stateFlow.value != SubCoordinatorState.ACTIVE) {
            canEventFlowCoroutineScope = CoroutineScope(coroutineContextProvider.IO).launch {
                canCoordinator.startIdentCanFlow(pin).collect { state ->
                    when (state) {
                        SubCoordinatorState.CANCELLED -> cancelIdentification()
                        else -> logger.debug("Ignoring sub flow state: $state")
                    }
                }
            }
        } else {
            logger.debug("Don't start CAN flow as it is already active.")
        }
    }

    fun onBack() {
        flowStateMachine.transition(IdentificationStateMachine.Event.Back)
    }

    fun retryIdentification() {
        flowStateMachine.transition(IdentificationStateMachine.Event.RetryAfterError)
    }

    fun cancelIdentification() {
        logger.debug("Cancel identification process.")

        navigator.popToRoot()
        _stateFlow.value = SubCoordinatorState.CANCELLED
        resetCoordinatorState()
    }

    private fun finishIdentification(redirectUrl: String) {
        logger.debug("Finish identification process.")

        navigator.popToRoot()
        _stateFlow.value = SubCoordinatorState.FINISHED

        storageManager.setIsNotFirstTimeUser()
        trackerManager.trackEvent(category = "identification", action = "success", name = "")

        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(redirectUrl))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        ContextCompat.startActivity(context, intent, null)

        resetCoordinatorState()
    }

    private fun resetCoordinatorState() {
        _scanInProgress.value = false
        eIdEventFlowCoroutineScope?.cancel()
        canEventFlowCoroutineScope?.cancel()
        eidInteractionManager.cancelTask()
        flowStateMachine.transition(IdentificationStateMachine.Event.Invalidate)
    }

    private fun executeIdentification(tcTokenUrl: Uri) {
        eidInteractionManager.cancelTask()
        eIdEventFlowCoroutineScope?.cancel()
        eIdEventFlowCoroutineScope = CoroutineScope(coroutineContextProvider.IO).launch {
            eidInteractionManager.eidFlow.catch { exception ->
                logger.error("Error: $exception")
                _scanInProgress.value = false
                eidInteractionManager.cancelTask()
                navigator.navigate(IdentificationOtherErrorDestination)
            }.collect { event ->
                when (event) {
                    is EidInteractionEvent.IdentificationStarted -> {
                        logger.debug("Authentication started.")
                        flowStateMachine.transition(IdentificationStateMachine.Event.StartedFetchingMetadata)
                    }

                    is EidInteractionEvent.IdentificationRequestConfirmationRequested -> {
                        logger.debug("Requesting authentication confirmation")
                        flowStateMachine.transition(IdentificationStateMachine.Event.FrameworkRequestsAttributeConfirmation(event.request))
                    }

                    is EidInteractionEvent.CertificateDescriptionReceived -> {
                        logger.debug("Certificate description received")
                        flowStateMachine.transition(IdentificationStateMachine.Event.CertificateDescriptionReceived(event.certificateDescription))
                    }

                    is EidInteractionEvent.PinRequested -> {
                        logger.debug("Requesting PIN")
                        if (canCoordinator.stateFlow.value == SubCoordinatorState.ACTIVE) {
                            logger.debug("Ignoring event because CAN flow is active.")
                            return@collect
                        }
                        flowStateMachine.transition(IdentificationStateMachine.Event.FrameworkRequestsPin(event.attempts == 3))
                    }

                    EidInteractionEvent.CardInsertionRequested -> {
                        logger.debug("Card insertion requested.")
                    }

                    EidInteractionEvent.CardRecognized -> {
                        logger.debug("Card recognized.")
                        _scanInProgress.value = true
                    }

                    EidInteractionEvent.CardRemoved -> {
                        logger.debug("Card removed.")
                        _scanInProgress.value = false
                    }

                    is EidInteractionEvent.IdentificationSucceededWithRedirect -> {
                        logger.debug("Process completed successfully.")
                        flowStateMachine.transition(IdentificationStateMachine.Event.Finish(event.redirectURL))
                    }

                    is EidInteractionEvent.CanRequested -> {
                        logger.debug("PIN and CAN requested.")
                        if (canCoordinator.stateFlow.value == SubCoordinatorState.ACTIVE) {
                            logger.debug("Ignoring event because CAN flow is active.")
                            return@collect
                        }
                        flowStateMachine.transition(IdentificationStateMachine.Event.FrameworkRequestsCan)
                    }

                    is EidInteractionEvent.PukRequested -> {
                        logger.debug("PUK requested.")
                        issueTrackerManager.captureMessage("${EidInteractionException.CardBlocked}")
                        flowStateMachine.transition(IdentificationStateMachine.Event.Error(EidInteractionException.CardBlocked))
                    }

                    is EidInteractionEvent.Error -> {
                        logger.error("Identification error: ${event.exception}")
                        event.exception.redacted?.let { issueTrackerManager.capture(it) } ?: issueTrackerManager.captureMessage("${event.exception}")
                        flowStateMachine.transition(IdentificationStateMachine.Event.Error(event.exception))
                    }

                    else -> logger.debug("Ignoring event: $event")
                }
            }
        }

        eidInteractionManager.identify(context, tcTokenUrl)
    }
}
