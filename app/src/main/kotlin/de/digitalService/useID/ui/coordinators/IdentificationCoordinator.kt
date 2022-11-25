package de.digitalService.useID.ui.coordinators

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import de.digitalService.useID.analytics.IssueTrackerManagerType
import de.digitalService.useID.analytics.TrackerManagerType
import de.digitalService.useID.getLogger
import de.digitalService.useID.idCardInterface.EIDInteractionEvent
import de.digitalService.useID.idCardInterface.IDCardInteractionException
import de.digitalService.useID.idCardInterface.IDCardManager
import de.digitalService.useID.models.ScanError
import de.digitalService.useID.ui.screens.destinations.*
import de.digitalService.useID.ui.screens.identification.ScanEvent
import de.digitalService.useID.util.CoroutineContextProviderType
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IdentificationCoordinator @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appCoordinator: AppCoordinator,
    private val idCardManager: IDCardManager,
    private val trackerManager: TrackerManagerType,
    private val issueTrackerManager: IssueTrackerManagerType,
    private val coroutineContextProvider: CoroutineContextProviderType
) {
    private val logger by getLogger()

    private val _scanEventFlow: MutableStateFlow<ScanEvent> = MutableStateFlow(ScanEvent.CardRequested)
    val scanEventFlow: Flow<ScanEvent>
        get() = _scanEventFlow

    private var requestAuthenticationEvent: EIDInteractionEvent.RequestAuthenticationRequestConfirmation? = null
    private var pinCallback: ((String) -> Unit)? = null

    private var reachedScanState = false
    private var incorrectPin: Boolean = false

    private var identificationFlowCoroutineScope: Job? = null

    var didSetup: Boolean = false
        private set

    fun startIdentificationProcess(tcTokenURL: String, didSetup: Boolean) {
        logger.debug("Start identification process.")
        this.didSetup = didSetup

        idCardManager.cancelTask()
        reachedScanState = false
        CoroutineScope(coroutineContextProvider.IO).launch {
            _scanEventFlow.emit(ScanEvent.CardRequested)
        }
        startIdentification(tcTokenURL)
    }

    fun confirmAttributesForIdentification() {
        val requestAuthenticationEvent = requestAuthenticationEvent ?: run {
            logger.debug("No confirmation event saved. Attributes might have been confirmed before.")
            appCoordinator.navigate(IdentificationPersonalPINDestination(null))
            return
        }

        val requiredAttributes = requestAuthenticationEvent.request.readAttributes.filterValues { it }
        requestAuthenticationEvent.confirmationCallback(requiredAttributes)
        this.requestAuthenticationEvent = null
    }

    fun onPINEntered(pin: String) {
        if (incorrectPin) {
            appCoordinator.pop()
        }

        val pinCallback = pinCallback ?: run {
            logger.error("Cannot process PIN because there isn't any pin callback saved.")
            return
        }
        logger.debug("Executing PIN callback.")
        pinCallback(pin)
        this.pinCallback = null
        incorrectPin = false
    }

    private fun onIncorrectPersonalPIN(attempts: Int) {
        incorrectPin = true
        appCoordinator.navigate(IdentificationPersonalPINDestination(attempts))
    }

    fun pop() {
        appCoordinator.pop()
    }

    fun cancelIdentification() {
        logger.debug("Cancel identification process.")
        appCoordinator.stopNFCTagHandling()
        identificationFlowCoroutineScope?.cancel()
        CoroutineScope(Dispatchers.Main).launch {
            if (didSetup && !reachedScanState) {
                appCoordinator.popUpTo(SetupIntroDestination)
            } else {
                appCoordinator.popToRoot()
            }
        }
        reachedScanState = false
        incorrectPin = false
        idCardManager.cancelTask()
    }

    private fun finishIdentification() {
        logger.debug("Finish identification process.")
        identificationFlowCoroutineScope?.cancel()

        appCoordinator.setIsNotFirstTimeUser()
        CoroutineScope(Dispatchers.Main).launch {
            appCoordinator.popToRoot()
        }
        reachedScanState = false
        incorrectPin = false
        trackerManager.trackEvent(category = "identification", action = "success", name = "")
    }

    private fun startIdentification(tcTokenURL: String) {
        identificationFlowCoroutineScope?.cancel()

        val fullURL = Uri
            .Builder()
            .scheme("http")
            .encodedAuthority("127.0.0.1:24727")
            .appendPath("eID-Client")
            .appendQueryParameter("tcTokenURL", tcTokenURL)
            .build()
            .toString()

        identificationFlowCoroutineScope = CoroutineScope(coroutineContextProvider.IO).launch {
            idCardManager.identify(context, fullURL).catch { error ->
                logger.error("Identification error: $error")

                when (error) {
                    IDCardInteractionException.CardDeactivated -> {
                        trackerManager.trackScreen("identification/cardDeactivated")

                        _scanEventFlow.emit(ScanEvent.Error(ScanError.CardDeactivated))
                        appCoordinator.navigate(IdentificationCardDeactivatedDestination)
                    }
                    IDCardInteractionException.CardBlocked -> {
                        trackerManager.trackScreen("identification/cardBlocked")

                        _scanEventFlow.emit(ScanEvent.Error(ScanError.PINBlocked))
                        appCoordinator.navigate(IdentificationCardBlockedDestination)
                    }
                    is IDCardInteractionException.ProcessFailed -> {
                        if (reachedScanState) {
                            val scanEvent = if (error.redirectUrl != null) {
                                appCoordinator.navigate(IdentificationCardUnreadableDestination(true, error.redirectUrl))
                                ScanEvent.Error(ScanError.CardErrorWithRedirect(error.redirectUrl))
                            } else {
                                appCoordinator.navigate(IdentificationCardUnreadableDestination(true, null))
                                ScanEvent.Error(ScanError.CardErrorWithoutRedirect)
                            }
                            _scanEventFlow.emit(scanEvent)
                        } else {
                            appCoordinator.navigate(IdentificationOtherErrorDestination(tcTokenURL))
                        }
                    }
                    else -> {
                        appCoordinator.navigate(IdentificationOtherErrorDestination(tcTokenURL))
                        _scanEventFlow.emit(ScanEvent.Error(ScanError.Other(null)))

                        if (pinCallback == null && !reachedScanState) {
                            trackerManager.trackEvent(category = "identification", action = "loadingFailed", name = "attributes")

                            (error as? IDCardInteractionException)?.redacted?.let {
                                issueTrackerManager.capture(it)
                            }
                        }
                    }
                }
            }.collect { event ->
                when (event) {
                    EIDInteractionEvent.AuthenticationStarted -> logger.debug("Authentication started")
                    is EIDInteractionEvent.RequestAuthenticationRequestConfirmation -> {
                        logger.debug(
                            "Requesting authentication confirmation:\n" +
                                "${event.request.subject}\n" +
                                "Read attributes: ${event.request.readAttributes.keys}"
                        )

                        requestAuthenticationEvent = event

                        appCoordinator.navigate(IdentificationAttributeConsentDestination(event.request))
                    }
                    is EIDInteractionEvent.RequestPIN -> {
                        logger.debug("Requesting PIN")

                        pinCallback = event.pinCallback

                        if (event.attempts == null) {
                            logger.debug("PIN request without attempts")
                            appCoordinator.navigate(IdentificationPersonalPINDestination(null))
                        } else {
                            logger.debug("PIN request with ${event.attempts} attempts")
                            _scanEventFlow.emit(ScanEvent.CardRequested)
                            onIncorrectPersonalPIN(event.attempts)
                        }
                    }
                    is EIDInteractionEvent.RequestCAN -> {
                        logger.debug("Requesting CAN")
                        _scanEventFlow.emit(ScanEvent.Error(ScanError.PINSuspended))
                        appCoordinator.navigate(IdentificationCardSuspendedDestination)

                        trackerManager.trackScreen("identification/cardSuspended")
                        cancel()
                    }
                    is EIDInteractionEvent.RequestPINAndCAN -> {
                        logger.debug("Requesting PIN and CAN")
                        _scanEventFlow.emit(ScanEvent.Error(ScanError.PINSuspended))
                        appCoordinator.navigate(IdentificationCardSuspendedDestination)

                        trackerManager.trackScreen("identification/cardSuspended")
                        cancel()
                    }
                    is EIDInteractionEvent.RequestPUK -> {
                        logger.debug("Requesting PUK")
                        _scanEventFlow.emit(ScanEvent.Error(ScanError.PINBlocked))
                        appCoordinator.navigate(IdentificationCardBlockedDestination)

                        trackerManager.trackScreen("identification/cardBlocked")
                        cancel()
                    }
                    EIDInteractionEvent.RequestCardInsertion -> {
                        logger.debug("Requesting ID card")
                        if (!reachedScanState) {
                            appCoordinator.navigate(IdentificationScanDestination)
                        }
                        appCoordinator.startNFCTagHandling()
                    }
                    EIDInteractionEvent.CardRecognized -> {
                        logger.debug("Card recognized")
                        _scanEventFlow.emit(ScanEvent.CardAttached)
                        reachedScanState = true
                    }
                    is EIDInteractionEvent.ProcessCompletedSuccessfullyWithRedirect -> {
                        logger.debug("Process completed successfully")
                        _scanEventFlow.emit(ScanEvent.Finished(event.redirectURL))

                        finishIdentification()
                    }
                    is EIDInteractionEvent.CardInteractionComplete -> {
                        logger.debug("Card interaction complete.")
                        appCoordinator.stopNFCTagHandling()
                    }
                    else -> {
                        logger.debug("Unhandled authentication event: $event")
                        issueTrackerManager.capture(event.redacted)
                    }
                }
            }
        }
    }
}
