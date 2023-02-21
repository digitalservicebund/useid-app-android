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
import de.digitalService.useID.idCardInterface.IdCardInteractionException
import de.digitalService.useID.idCardInterface.IdCardManager
import de.digitalService.useID.ui.navigation.Navigator
import de.digitalService.useID.ui.screens.destinations.*
import de.digitalService.useID.util.CoroutineContextProviderType
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IdentificationCoordinator @Inject constructor(
    @ApplicationContext private val context: Context,
    private val canCoordinator: CanCoordinator,
    private val navigator: Navigator,
    private val idCardManager: IdCardManager,
    private val storageManager: StorageManagerType,
    private val trackerManager: TrackerManagerType,
    private val flowStateMachine: IdentificationStateMachine,
    private val canStateMachine: CanStateMachine,
    private val coroutineContextProvider: CoroutineContextProviderType
) {
    private val logger by getLogger()

    private val _scanInProgress: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val scanInProgress: Flow<Boolean>
        get() = _scanInProgress

    private var eIdEventFlowCoroutineScope: Job? = null
    private var canEventFlowCoroutineScope: Job? = null

    private val _stateFlow: MutableStateFlow<SubCoordinatorState> = MutableStateFlow(SubCoordinatorState.FINISHED)
    val stateFlow: StateFlow<SubCoordinatorState>
        get() = _stateFlow

    init {
        CoroutineScope(coroutineContextProvider.Default).launch {
            flowStateMachine.state.collect { eventAndPair ->
                if (eventAndPair.first is IdentificationStateMachine.Event.Back) {
                    navigator.pop()
                    if (eventAndPair.second == IdentificationStateMachine.State.Invalid) {
                        resetCoordinatorState()
                    }
                } else {
                    when (val state = eventAndPair.second) {
                        is IdentificationStateMachine.State.StartIdentification -> executeIdentification(state.tcTokenUrl)
                        is IdentificationStateMachine.State.FetchingMetadata -> navigator.popUpToOrNavigate(IdentificationFetchMetadataDestination(state.backingDownAllowed), false)
                        is IdentificationStateMachine.State.FetchingMetadataFailed -> navigator.navigate(IdentificationOtherErrorDestination)
                        is IdentificationStateMachine.State.RequestAttributeConfirmation -> navigator.navigatePopping(IdentificationAttributeConsentDestination(state.request, state.backingDownAllowed))
                        is IdentificationStateMachine.State.SubmitAttributeConfirmation -> state.confirmationCallback(state.request.readAttributes.filterValues { it })
                        is IdentificationStateMachine.State.PinInput -> navigator.navigate(IdentificationPersonalPinDestination(false))
                        is IdentificationStateMachine.State.PinInputRetry -> navigator.navigate(IdentificationPersonalPinDestination(true))
                        is IdentificationStateMachine.State.RevisitAttributes -> navigator.pop()
                        is IdentificationStateMachine.State.PinEntered -> state.callback(state.pin)
                        is IdentificationStateMachine.State.CanRequested -> startCanFlow(state.pin)
                        is IdentificationStateMachine.State.WaitingForCardAttachment -> navigator.popUpToOrNavigate(IdentificationScanDestination, false)
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
        _stateFlow.value = SubCoordinatorState.ACTIVE

        val normalizedTcTokenUrl = Uri
            .Builder()
            .scheme("http")
            .encodedAuthority("127.0.0.1:24727")
            .appendPath("eID-Client")
            .appendQueryParameter("tcTokenURL", tcTokenUrl)
            .build()
            .toString()

        flowStateMachine.transition(IdentificationStateMachine.Event.Initialize(setupSkipped, normalizedTcTokenUrl))
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
        idCardManager.cancelTask()
        flowStateMachine.transition(IdentificationStateMachine.Event.Invalidate)
    }

    private fun executeIdentification(tcTokenUrl: String) {
        eIdEventFlowCoroutineScope?.cancel()
        idCardManager.cancelTask()
        eIdEventFlowCoroutineScope = CoroutineScope(coroutineContextProvider.IO).launch {
            idCardManager.eidFlow.catch { exception ->
                logger.error("Error: $exception")
                _scanInProgress.value = false
                idCardManager.cancelTask()
                navigator.navigate(IdentificationOtherErrorDestination)
            }.collect { event ->
                when (event) {
                    is EidInteractionEvent.AuthenticationStarted -> {
                        logger.debug("Authentication started.")
                        flowStateMachine.transition(IdentificationStateMachine.Event.StartedFetchingMetadata)
                    }
                    is EidInteractionEvent.RequestAuthenticationRequestConfirmation -> {
                        logger.debug("Requesting authentication confirmation")
                        flowStateMachine.transition(IdentificationStateMachine.Event.FrameworkRequestsAttributeConfirmation(event.request, event.confirmationCallback))
                    }
                    is EidInteractionEvent.RequestPin -> {
                        logger.debug("Requesting PIN")
                        flowStateMachine.transition(IdentificationStateMachine.Event.FrameworkRequestsPin(event.pinCallback))
                    }
                    EidInteractionEvent.RequestCardInsertion -> {
                        logger.debug("Card insertion requested.")
                        flowStateMachine.transition(IdentificationStateMachine.Event.RequestCardInsertion)
                    }
                    EidInteractionEvent.CardRecognized -> {
                        logger.debug("Card recognized.")
                        _scanInProgress.value = true
                    }
                    EidInteractionEvent.CardRemoved -> {
                        logger.debug("Card removed.")
                        _scanInProgress.value = false
                    }
                    is EidInteractionEvent.ProcessCompletedSuccessfullyWithRedirect -> {
                        logger.debug("Process completed successfully.")
                        flowStateMachine.transition(IdentificationStateMachine.Event.Finish(event.redirectURL))
                    }
                    is EidInteractionEvent.RequestPinAndCan -> {
                        logger.debug("PIN and CAN requested.")
                        flowStateMachine.transition(IdentificationStateMachine.Event.FrameworkRequestsCan)
                    }
                    is EidInteractionEvent.RequestPuk -> {
                        logger.debug("PUK requested.")
                        flowStateMachine.transition(IdentificationStateMachine.Event.Error(IdCardInteractionException.CardBlocked))
                    }
                    is EidInteractionEvent.Error -> {
                        logger.error("Identification error: ${event.exception}")
                        flowStateMachine.transition(IdentificationStateMachine.Event.Error(event.exception))
                    }
                    else -> logger.debug("Ignoring event: $event")
                }
            }
        }

        idCardManager.identify(context, tcTokenUrl)
    }
}
