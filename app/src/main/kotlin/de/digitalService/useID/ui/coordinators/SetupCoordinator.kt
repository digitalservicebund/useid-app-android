package de.digitalService.useID.ui.coordinators

import de.digitalService.useID.StorageManagerType
import de.digitalService.useID.ui.AppCoordinator
import de.digitalService.useID.ui.composables.screens.destinations.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SetupCoordinator @Inject constructor(
    private val appCoordinator: AppCoordinator,
    private val storageManager: StorageManagerType
) {
    private var tcTokenURL: String? = null

    fun setTCTokenURL(tcTokenURL: String) {
        this.tcTokenURL = tcTokenURL
    }

    fun startSetupIDCard() = appCoordinator.navigate(SetupPINLetterDestination)

    fun setupWithPINLetter() {
        appCoordinator.navigate(SetupTransportPINDestination)
    }

    fun setupWithoutPINLetter() {
        appCoordinator.navigate(SetupResetPersonalPINDestination)
    }

    fun onTransportPINEntered() {
        appCoordinator.navigate(SetupPersonalPINIntroDestination)
    }

    fun onPersonalPINIntroFinished() {
        appCoordinator.navigate(SetupPersonalPINDestination)
    }

    fun onPersonalPINEntered() {
        appCoordinator.navigate(SetupScanDestination)
    }

    fun onSettingPINSucceeded() {
        appCoordinator.navigate(SetupFinishDestination)
    }

    fun onSetupFinished() {
        storageManager.setIsNotFirstTimeUser()

        tcTokenURL?.let {
            appCoordinator.startIdentification(it)
            tcTokenURL = null
        }
    }

    fun cancelSetup() {
        appCoordinator.popToRoot()
        tcTokenURL = null
    }
}
