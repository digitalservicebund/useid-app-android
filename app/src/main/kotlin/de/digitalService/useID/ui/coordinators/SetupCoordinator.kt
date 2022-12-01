package de.digitalService.useID.ui.coordinators

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import de.digitalService.useID.analytics.IssueTrackerManagerType
import de.digitalService.useID.getLogger
import de.digitalService.useID.idCardInterface.EidInteractionEvent
import de.digitalService.useID.idCardInterface.IdCardInteractionException
import de.digitalService.useID.idCardInterface.IdCardManager
import de.digitalService.useID.ui.screens.destinations.*
import de.digitalService.useID.util.CoroutineContextProviderType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SetupCoordinator @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appCoordinator: AppCoordinator,
    private val idCardManager: IdCardManager,
    private val issueTrackerManager: IssueTrackerManagerType,
    private val coroutineContextProvider: CoroutineContextProviderType
) {
    private val logger by getLogger()

    private var tcTokenURL: String? = null
    private var incorrectTransportPin: Boolean = false

    private val _scanInProgress: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val scanInProgress: Flow<Boolean>
        get() = _scanInProgress

    private var transportPin: String? = null
    private var personalPin: String? = null

    private var changePinFlowCoroutineScope: Job? = null

    fun setTCTokenURL(tcTokenURL: String) {
        this.tcTokenURL = tcTokenURL
    }

    fun identificationPending(): Boolean {
        return this.tcTokenURL != null
    }

    fun startSetupIDCard() {
        transportPin = null
        personalPin = null
        appCoordinator.navigate(SetupPinLetterDestination)
    }

    fun setupWithPinLetter() {
        appCoordinator.navigate(SetupTransportPinDestination(null))
    }

    fun setupWithoutPinLetter() {
        appCoordinator.navigate(SetupResetPersonalPinDestination)
    }

    fun onTransportPinEntered(newTransportPin: String) {
        transportPin = newTransportPin
        if (incorrectTransportPin) {
            val personalPin = personalPin ?: run {
                logger.error("Personal PIN not set.")
                throw IllegalStateException()
            }
            setPin(newTransportPin, personalPin)
        } else {
            appCoordinator.navigate(SetupPersonalPinIntroDestination)
        }
        incorrectTransportPin = false
    }

    fun onPersonalPinIntroFinished() {
        appCoordinator.navigate(SetupPersonalPinInputDestination)
    }

    fun onPersonalPinInput(newPersonalPin: String) {
        personalPin = newPersonalPin

        appCoordinator.navigate(SetupPersonalPinConfirmDestination)
    }

    fun confirmPersonalPin(newPersonalPin: String): Boolean {
        val transportPin = transportPin ?: run {
            logger.error("Transport PIN not set.")
            throw IllegalStateException()
        }

        if (personalPin == newPersonalPin) {
            startSettingPin(transportPin, newPersonalPin)
            return true
        }

        return false
    }

    private fun startSettingPin(transportPin: String, personalPin: String) {
        appCoordinator.startNfcTagHandling()
        setPin(transportPin, personalPin)
    }

    fun retrySettingPin() {
        val transportPin = transportPin ?: run {
            logger.error("Transport PIN not set.")
            throw IllegalStateException()
        }

        val personalPin = personalPin ?: run {
            logger.error("Personal PIN not set.")
            throw IllegalStateException()
        }

        startSettingPin(transportPin, personalPin)
    }

    private fun setPin(transportPin: String, pin: String) {
        var firstTransportPinRequest = true

        changePinFlowCoroutineScope = CoroutineScope(coroutineContextProvider.IO).launch {
            idCardManager.changePin(context).catch { exception ->
                _scanInProgress.value = false

                when (exception) {
                    is IdCardInteractionException.CardDeactivated -> {
                        appCoordinator.navigate(SetupCardDeactivatedDestination)
                    }
                    is IdCardInteractionException.CardBlocked -> {
                        appCoordinator.navigate(SetupCardBlockedDestination)
                    }
                    else -> {
                        (exception as? IdCardInteractionException)?.redacted?.let {
                            issueTrackerManager.capture(it)
                        }

                        appCoordinator.navigatePopping(SetupCardUnreadableDestination(false))
                    }
                }
            }.collect { event ->
                when (event) {
                    EidInteractionEvent.RequestCardInsertion -> {
                        logger.debug("Card insertion requested.")
                        appCoordinator.navigatePopping(SetupScanDestination)
                    }

                    EidInteractionEvent.CardInteractionComplete -> logger.debug("Card interaction complete.")
                    EidInteractionEvent.AuthenticationStarted -> logger.debug("Authentication started.")
                    EidInteractionEvent.PinManagementStarted -> logger.debug("PIN management started.")
                    EidInteractionEvent.CardRecognized -> {
                        logger.debug("Card recognized.")
                        _scanInProgress.value = true
                    }
                    EidInteractionEvent.CardRemoved -> {
                        logger.debug("Card removed.")
                        _scanInProgress.value = false
                    }
                    is EidInteractionEvent.ProcessCompletedSuccessfullyWithoutResult -> {
                        logger.debug("Process completed successfully.")
                        _scanInProgress.value = false
                        onSettingPinSucceeded()
                    }
                    is EidInteractionEvent.RequestChangedPin -> {
                        if (firstTransportPinRequest) {
                            logger.debug("Changed PIN requested for the first time. Entering transport PIN and personal PIN")
                            firstTransportPinRequest = false
                            event.pinCallback(transportPin, pin)
                        } else {
                            val attempts = event.attempts ?: run {
                                logger.error("Number of attempts not provided by framework.")
                                appCoordinator.navigate(SetupOtherErrorDestination)
                                return@collect
                            }

                            logger.debug("Old and new PIN requested for a second time. The Transport-PIN seems to be incorrect.")
                            _scanInProgress.value = false
                            onIncorrectTransportPin(attempts)
                            cancel()
                        }
                    }
                    is EidInteractionEvent.RequestCanAndChangedPin -> {
                        _scanInProgress.value = false
                        appCoordinator.navigate(SetupCardSuspendedDestination)
                        cancel()
                    }
                    is EidInteractionEvent.RequestPUK -> {
                        _scanInProgress.value = false
                        appCoordinator.navigate(SetupCardBlockedDestination)
                        cancel()
                    }
                    else -> {
                        logger.debug("Collected unexpected event: $event")
                        _scanInProgress.value = false
                        appCoordinator.navigate(SetupOtherErrorDestination)

                        issueTrackerManager.capture(event.redacted)
                        cancel()
                    }
                }
            }
        }
    }

    fun onPersonalPinErrorTryAgain() {
        personalPin = null
        appCoordinator.pop()
    }

    private fun onIncorrectTransportPin(attempts: Int) {
        incorrectTransportPin = true
        appCoordinator.navigate(SetupTransportPinDestination(attempts))
        appCoordinator.stopNfcTagHandling()
    }

    private fun onSettingPinSucceeded() {
        appCoordinator.setIsNotFirstTimeUser()
        appCoordinator.navigate(SetupFinishDestination)
        appCoordinator.stopNfcTagHandling()
    }

    fun onBackClicked() {
        changePinFlowCoroutineScope?.cancel()

        idCardManager.cancelTask()
        appCoordinator.pop()
    }

    fun cancelSetup() {
        changePinFlowCoroutineScope?.cancel()

        appCoordinator.stopNfcTagHandling()
        idCardManager.cancelTask()
        transportPin = null
        personalPin = null
        incorrectTransportPin = false
        appCoordinator.popToRoot()
        tcTokenURL = null
    }

    fun finishSetup() {
        transportPin = null
        personalPin = null
        incorrectTransportPin = false

        tcTokenURL?.let {
            appCoordinator.startIdentification(it, true)
            tcTokenURL = null
        } ?: run {
            appCoordinator.popToRoot()
        }
    }
}
