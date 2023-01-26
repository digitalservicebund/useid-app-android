package de.digitalService.useID.ui.coordinators

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import de.digitalService.useID.StorageManagerType
import de.digitalService.useID.analytics.IssueTrackerManagerType
import de.digitalService.useID.analytics.TrackerManagerType
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
import kotlinx.coroutines.flow.catch
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.properties.Delegates

@Singleton
class IdentificationCoordinator @Inject constructor(
    @ApplicationContext private val context: Context,
    private val canCoordinator: CanCoordinator,
    private val navigator: Navigator,
    private val idCardManager: IdCardManager,
    private val storageManager: StorageManagerType,
    private val trackerManager: TrackerManagerType,
    private val issueTrackerManager: IssueTrackerManagerType,
    private val coroutineContextProvider: CoroutineContextProviderType
) {
    private val logger by getLogger()

    private val _scanInProgress: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val scanInProgress: Flow<Boolean>
        get() = _scanInProgress

    private var requestAuthenticationEvent: EidInteractionEvent.RequestAuthenticationRequestConfirmation? = null
    private var pinCallback: ((String) -> Unit)? = null

    private var reachedScanState = false
    private var startedWithThreeAttempts = false
    private var pin: String? = null

    private var setupSkipped by Delegates.notNull<Boolean>()
    private lateinit var identificationUrl: String

    private var eIdEventFlowCoroutineScope: Job? = null
    private var canEventFlowCoroutineScope: Job? = null

    val stateFlow: MutableStateFlow<SubCoordinatorState> = MutableStateFlow(SubCoordinatorState.FINISHED)

    fun startIdentificationProcess(tcTokenUrl: String, setupSkipped: Boolean) {
        this.setupSkipped = setupSkipped
        identificationUrl = Uri
            .Builder()
            .scheme("http")
            .encodedAuthority("127.0.0.1:24727")
            .appendPath("eID-Client")
            .appendQueryParameter("tcTokenURL", tcTokenUrl)
            .build()
            .toString()

        executeIdentification()
    }

    private fun executeIdentification() {
        logger.debug("Start identification process.")

        stateFlow.value = SubCoordinatorState.ACTIVE

        eIdEventFlowCoroutineScope?.cancel()
        canEventFlowCoroutineScope?.cancel()
        idCardManager.cancelTask()
        reachedScanState = false
        startedWithThreeAttempts = false
        pin = null
        requestAuthenticationEvent = null
        pinCallback = null

        collectEidEvents()
        idCardManager.identify(context, identificationUrl)
    }

    fun confirmAttributesForIdentification() {
        requestAuthenticationEvent?.let { requestAuthenticationEvent ->
            val requiredAttributes =
                requestAuthenticationEvent.request.readAttributes.filterValues { it }
            requestAuthenticationEvent.confirmationCallback(requiredAttributes)
            this.requestAuthenticationEvent = null
        } ?: run {
            logger.debug("No confirmation event saved. Attributes have been confirmed before.")

            navigator.navigate(IdentificationPersonalPinDestination(false))
        }
    }

    fun setPin(pin: String) {
        val pinCallback = pinCallback ?: run {
            logger.error("Cannot process PIN because there isn't any pin callback saved.")
            return
        }

        this.pin = pin

        logger.debug("Executing PIN callback.")
        pinCallback(pin)
        this.pinCallback = null
    }

    fun onBack() {
        navigator.pop()
    }

    fun retryIdentification() {
        executeIdentification()
    }

    fun cancelIdentification() {
        logger.debug("Cancel identification process.")

        eIdEventFlowCoroutineScope?.cancel()
        canEventFlowCoroutineScope?.cancel()
        idCardManager.cancelTask()

        navigator.popToRoot()

        stateFlow.value = SubCoordinatorState.CANCELLED
    }

    private fun finishIdentification(redirectUrl: String) {
        logger.debug("Finish identification process.")
        eIdEventFlowCoroutineScope?.cancel()

        storageManager.setIsNotFirstTimeUser()
        navigator.popToRoot()
        trackerManager.trackEvent(category = "identification", action = "success", name = "")

        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(redirectUrl))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        ContextCompat.startActivity(context, intent, null)

        stateFlow.value = SubCoordinatorState.FINISHED
    }

    private fun collectEidEvents() {
        eIdEventFlowCoroutineScope?.cancel()
        eIdEventFlowCoroutineScope = CoroutineScope(coroutineContextProvider.IO).launch {
            idCardManager.eidFlow.catch { exception ->
                logger.error("Error: $exception")
                _scanInProgress.value = false
                idCardManager.cancelTask()
                navigator.navigate(IdentificationOtherErrorDestination)
            }.collect { event ->
                when (event) {
                    is EidInteractionEvent.AuthenticationStarted -> navigator.navigate(IdentificationFetchMetadataDestination(setupSkipped))
                    is EidInteractionEvent.RequestAuthenticationRequestConfirmation -> {
                        logger.debug("Requesting authentication confirmation")
                        requestAuthenticationEvent = event

                        navigator.navigatePopping(IdentificationAttributeConsentDestination(event.request, setupSkipped))
                    }
                    is EidInteractionEvent.RequestPin -> {
                        logger.debug("Requesting PIN")

                        pinCallback = event.pinCallback

                        if (event.attempts == null) {
                            logger.debug("PIN request without attempts.")
                            navigator.navigate(IdentificationPersonalPinDestination(false))
                        } else {
                            logger.debug("PIN request with ${event.attempts} attempts.")
                            _scanInProgress.value = false
                            startedWithThreeAttempts = true
                            navigator.navigate(IdentificationPersonalPinDestination(true))
                        }
                    }
                    EidInteractionEvent.RequestCardInsertion -> {
                        logger.debug("Card insertion requested.")
                        if (reachedScanState) {
                            logger.debug("Requested card insertion again. Popping to scan screen.")
                            navigator.popUpTo(IdentificationScanDestination)
                        } else {
                            logger.debug("Requested card insertion for the first time. Pushing scan screen.")
                            navigator.navigate(IdentificationScanDestination)
                            reachedScanState = true
                        }
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
                        _scanInProgress.value = false
                        finishIdentification(event.redirectURL)
                    }
                    is EidInteractionEvent.RequestPinAndCan -> {
                        logger.debug("PIN and CAN requested.")
                        _scanInProgress.value = false

                        val pin = pin ?: run {
                            logger.error("No PIN saved.")
                            return@collect
                        }

                        if (canCoordinator.stateFlow.value != SubCoordinatorState.ACTIVE) {
                            canEventFlowCoroutineScope = CoroutineScope(coroutineContextProvider.IO).launch {
                                canCoordinator.startIdentCanFlow(pin.takeIf { !startedWithThreeAttempts }).collect { state ->
                                    when (state) {
                                        SubCoordinatorState.CANCELLED -> cancelIdentification()
                                        else -> logger.debug("Ignoring sub flow event: $state")
                                    }
                                }
                            }
                        } else {
                            logger.debug("Ignoring PIN and CAN request because CAN flow is already active.")
                        }
                    }
                    is EidInteractionEvent.RequestPuk -> {
                        logger.debug("PUK requested.")
                        _scanInProgress.value = false
                        navigator.navigate(IdentificationCardBlockedDestination)
                        idCardManager.cancelTask()
                        stateFlow.value = SubCoordinatorState.CANCELLED
                        cancel()
                    }
                    is EidInteractionEvent.Error -> {
                        logger.error("Identification error: ${event.exception}")
                        _scanInProgress.value = false

                        val destination = when (event.exception) {
                            IdCardInteractionException.CardDeactivated -> {
                                trackerManager.trackScreen("identification/cardDeactivated")

                                IdentificationCardDeactivatedDestination
                            }
                            IdCardInteractionException.CardBlocked -> {
                                trackerManager.trackScreen("identification/cardBlocked")

                                IdentificationCardBlockedDestination
                            }
                            is IdCardInteractionException.ProcessFailed -> {
                                if (reachedScanState) {
                                    if (event.exception.redirectUrl != null) {
                                        IdentificationCardUnreadableDestination(true, event.exception.redirectUrl)
                                    } else {
                                        IdentificationCardUnreadableDestination(true, null)
                                    }
                                } else {
                                    IdentificationOtherErrorDestination
                                }
                            }
                            else -> {
                                if (pinCallback == null && !reachedScanState) {
                                    trackerManager.trackEvent(
                                        category = "identification",
                                        action = "loadingFailed",
                                        name = "attributes"
                                    )

                                    (event.exception as? IdCardInteractionException)?.redacted?.let {
                                        issueTrackerManager.capture(it)
                                    }
                                }

                                IdentificationOtherErrorDestination
                            }
                        }

                        navigator.navigate(destination)
                        idCardManager.cancelTask()
                        cancel()
                    }
                    else -> logger.debug("Ignoring event: $event")
                }
            }
        }
    }
}
