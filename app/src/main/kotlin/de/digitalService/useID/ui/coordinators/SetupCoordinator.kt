package de.digitalService.useID.ui.coordinators

import de.digitalService.useID.ui.AppCoordinator
import de.digitalService.useID.ui.composables.screens.destinations.*
import javax.inject.Inject

class SetupCoordinator @Inject constructor(private val appCoordinator: AppCoordinator) {
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
        appCoordinator.startIdentification()
    }

    fun cancelSetup() {
        appCoordinator.popToRoot()
    }
}
