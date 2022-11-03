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

    var transportPin: String? = null
        private set

    var personalPin: String? = null
        private set

    private var insertedPersonalPin: String? = null

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
        appCoordinator.navigate(SetupTransportPINDestination)
    }

    fun setupWithoutPINLetter() {
        appCoordinator.navigate(SetupResetPersonalPINDestination)
    }

    fun onTransportPINEntered(newTransportPin: String) {
        transportPin = newTransportPin
        appCoordinator.navigate(SetupPersonalPINIntroDestination)
    }

    fun onPersonalPINIntroFinished() {
        appCoordinator.navigate(SetupPersonalPinInputDestination(false))
    }

    fun onPersonalPinInput(newPersonalPin: String) {
        insertedPersonalPin = newPersonalPin

        appCoordinator.navigate(SetupPersonalPinConfirmDestination)
    }

    fun onPersonalPinConfirm(newPersonalPin: String) {
        if (insertedPersonalPin != newPersonalPin) {
            insertedPersonalPin = null
            appCoordinator.navigate(SetupPersonalPinInputDestination(true))
            return
        }

        personalPin = newPersonalPin
        appCoordinator.navigate(SetupScanDestination)
        appCoordinator.startNFCTagHandling()
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
        handleSetupEnded()
    }

    fun onBackToHome() {
        appCoordinator.popToRoot()
    }

    fun onBackTapped() {
        appCoordinator.pop()
    }

    fun onSkipSetup() {
        handleSetupEnded()
    }

    fun cancelSetup() {
        appCoordinator.stopNFCTagHandling()
        transportPin = null
        personalPin = null
        appCoordinator.popToRoot()
        tcTokenURL = null
    }

    private fun handleSetupEnded() {
        transportPin = null
        personalPin = null

        tcTokenURL?.let {
            appCoordinator.startIdentification(it)
            tcTokenURL = null
        } ?: run {
            appCoordinator.popToRoot()
        }
    }
}
