package de.digitalService.useID.ui.coordinators

import de.digitalService.useID.ui.AppCoordinator
import de.digitalService.useID.ui.composables.screens.Screen
import javax.inject.Inject

class PersonalPINIntroCoordinator @Inject constructor(private val appCoordinator: AppCoordinator) {
    fun finishIntro() {
        appCoordinator.navigate(Screen.SetupPersonalPIN.parameterizedRoute())
    }
}
