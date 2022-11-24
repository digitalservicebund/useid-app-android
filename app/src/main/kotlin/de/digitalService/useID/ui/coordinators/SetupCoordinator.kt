package de.digitalService.useID.ui.coordinators

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import de.digitalService.useID.analytics.IssueTrackerManagerType
import de.digitalService.useID.getLogger
import de.digitalService.useID.idCardInterface.EIDInteractionEvent
import de.digitalService.useID.idCardInterface.IDCardInteractionException
import de.digitalService.useID.idCardInterface.IDCardManager
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
    private val idCardManager: IDCardManager,
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
        appCoordinator.navigate(SetupPINLetterDestination)
    }

    fun setupWithPINLetter() {
        appCoordinator.navigate(SetupTransportPINDestination(null))
    }

    fun setupWithoutPINLetter() {
        appCoordinator.navigate(SetupResetPersonalPINDestination)
    }

    fun onTransportPINEntered(newTransportPin: String) {
        transportPin = newTransportPin
        if (incorrectTransportPin) {
            val personalPin = personalPin ?: run {
                logger.error("Personal PIN not set.")
                throw IllegalStateException()
            }
            setPin(newTransportPin, personalPin)
        } else {
            appCoordinator.navigate(SetupPersonalPINIntroDestination)
        }
        incorrectTransportPin = false
    }

    fun onPersonalPINIntroFinished() {
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
            appCoordinator.startNFCTagHandling()
            setPin(transportPin, newPersonalPin)
            return true
        }

        personalPin = null
        return false
    }

    private fun setPin(transportPin: String, pin: String) {
        var firstTransportPinRequest = true

        changePinFlowCoroutineScope = CoroutineScope(coroutineContextProvider.IO).launch {
            idCardManager.changePin(context).catch { exception ->
                _scanInProgress.value = false

                when (exception) {
                    is IDCardInteractionException.CardDeactivated -> {
                        appCoordinator.navigate(SetupCardDeactivatedDestination)
                    }
                    is IDCardInteractionException.CardBlocked -> {
                        appCoordinator.navigate(SetupCardBlockedDestination)
                    }
                    else -> {
                        (exception as? IDCardInteractionException)?.redacted?.let {
                            issueTrackerManager.capture(it)
                        }

                        appCoordinator.navigate(SetupCardUnreadableDestination(false))
                    }
                }
            }.collect { event ->
                when (event) {
                    EIDInteractionEvent.RequestCardInsertion -> {
                        logger.debug("Card insertion requested.")
                        appCoordinator.navigatePopping(SetupScanDestination)
                    }

                    EIDInteractionEvent.CardInteractionComplete -> logger.debug("Card interaction complete.")
                    EIDInteractionEvent.AuthenticationStarted -> logger.debug("Authentication started.")
                    EIDInteractionEvent.PINManagementStarted -> logger.debug("PIN management started.")
                    EIDInteractionEvent.CardRecognized -> {
                        logger.debug("Card recognized.")
                        _scanInProgress.value = true
                    }
                    EIDInteractionEvent.CardRemoved -> {
                        logger.debug("Card removed.")
                        _scanInProgress.value = false
                    }
                    is EIDInteractionEvent.ProcessCompletedSuccessfullyWithoutResult -> {
                        logger.debug("Process completed successfully.")
                        _scanInProgress.value = false
                        onSettingPINSucceeded()
                    }
                    is EIDInteractionEvent.RequestChangedPIN -> {
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
                            onIncorrectTransportPIN(attempts)
                            cancel()
                        }
                    }
                    is EIDInteractionEvent.RequestCANAndChangedPIN -> {
                        _scanInProgress.value = false
                        appCoordinator.navigate(SetupCardSuspendedDestination)
                        cancel()
                    }
                    is EIDInteractionEvent.RequestPUK -> {
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

    private fun onIncorrectTransportPIN(attempts: Int) {
        incorrectTransportPin = true
        appCoordinator.navigate(SetupTransportPINDestination(attempts))
        appCoordinator.stopNFCTagHandling()
    }

    private fun onSettingPINSucceeded() {
        appCoordinator.setIsNotFirstTimeUser()
        appCoordinator.navigate(SetupFinishDestination)
        appCoordinator.stopNFCTagHandling()
    }

    fun onBackTapped() {
        changePinFlowCoroutineScope?.cancel()

        idCardManager.cancelTask()
        appCoordinator.pop()
    }

    fun cancelSetup() {
        changePinFlowCoroutineScope?.cancel()

        appCoordinator.stopNFCTagHandling()
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
