package de.digitalService.useID.ui.coordinators

import de.digitalService.useID.ui.AppCoordinator
import de.digitalService.useID.ui.composables.screens.Screen
import javax.inject.Inject

class SetupCoordinator @Inject constructor(private val appCoordinator: AppCoordinator) {
    fun setupWithPINLetter() {
        appCoordinator.navigate(Screen.SetupTransportPIN.parameterizedRoute())
    }

    fun setupWithoutPINLetter() {
        appCoordinator.navigate(Screen.ResetPIN.parameterizedRoute())
    }

    fun onTransportPINEntered() {
        appCoordinator.navigate(Screen.SetupPersonalPINIntro.parameterizedRoute())
    }

    fun onPersonalPINIntroFinished() {
        appCoordinator.navigate(Screen.SetupPersonalPIN.parameterizedRoute())
    }

    fun onPersonalPINEntered() {
        appCoordinator.navigate(Screen.SetupScan.parameterizedRoute())
    }

    fun onSettingPINSucceeded() {
        appCoordinator.navigate(Screen.SetupFinish.parameterizedRoute())
    }

    fun onSetupFinished() {
        appCoordinator.startIdentification()
    }

    fun cancelSetup() {
        appCoordinator.popToRoot()
    }
}
