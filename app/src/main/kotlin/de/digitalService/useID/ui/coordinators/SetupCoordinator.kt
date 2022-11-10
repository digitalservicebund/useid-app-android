package de.digitalService.useID.ui.coordinators

import de.digitalService.useID.models.ScanError
import de.digitalService.useID.ui.screens.destinations.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SetupCoordinator @Inject constructor(
    private val appCoordinator: AppCoordinator
) {
    private var tcTokenURL: String? = null
    private var incorrectTransportPin: Boolean = false

    var transportPin: String? = null
        private set

    var personalPin: String? = null
        private set

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
            appCoordinator.pop()
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
        if (personalPin == newPersonalPin) {
            appCoordinator.navigatePopping(SetupScanDestination)
            appCoordinator.startNFCTagHandling()
            return true
        }

        personalPin = null
        return false
    }

    fun onPersonalPinErrorTryAgain() {
        personalPin = null
        appCoordinator.pop()
    }

    fun onIncorrectTransportPIN(attempts: Int) {
        incorrectTransportPin = true
        appCoordinator.navigate(SetupTransportPINDestination(attempts))
    }

    fun onSettingPINSucceeded() {
        appCoordinator.setIsNotFirstTimeUser()
        appCoordinator.navigate(SetupFinishDestination)
        appCoordinator.stopNFCTagHandling()
    }

    fun onScanError(scanError: ScanError) {
        when (scanError) {
            ScanError.PINSuspended -> appCoordinator.navigate(SetupCardSuspendedDestination)
            ScanError.PINBlocked -> appCoordinator.navigate(SetupCardBlockedDestination)
            ScanError.CardDeactivated -> appCoordinator.navigate(SetupCardDeactivatedDestination)
            is ScanError.CardErrorWithRedirect -> appCoordinator.navigate(SetupCardUnreadableDestination(true))
            ScanError.CardErrorWithoutRedirect -> appCoordinator.navigate(SetupCardUnreadableDestination(true))
            is ScanError.Other -> appCoordinator.navigate(SetupOtherErrorDestination)
            else -> {}
        }
    }

    fun onSetupFinished() {
        handleSetupEnded(true)
    }

    fun onBackToHome() {
        appCoordinator.popToRoot()
    }

    fun onBackTapped() {
        appCoordinator.pop()
    }

    fun onSkipSetup() {
        handleSetupEnded(false)
    }

    fun cancelSetup() {
        appCoordinator.stopNFCTagHandling()
        transportPin = null
        personalPin = null
        incorrectTransportPin = false
        appCoordinator.popToRoot()
        tcTokenURL = null
    }

    private fun handleSetupEnded(didSetup: Boolean) {
        transportPin = null
        personalPin = null
        incorrectTransportPin = false

        tcTokenURL?.let {
            appCoordinator.startIdentification(it, didSetup)
            tcTokenURL = null
        } ?: run {
            appCoordinator.popToRoot()
        }
    }
}
