package de.digitalService.useID.ui.coordinators

import com.ramcosta.composedestinations.navigation.navigate
import de.digitalService.useID.ui.AppCoordinator
import de.digitalService.useID.ui.composables.screens.destinations.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SetupCoordinator @Inject constructor(private val appCoordinator: AppCoordinator) {
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
