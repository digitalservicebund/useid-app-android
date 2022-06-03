package de.digitalService.useID.ui.coordinators

import de.digitalService.useID.ui.AppCoordinator
import de.digitalService.useID.ui.composables.screens.Screen
import javax.inject.Inject

class PersonalPINCoordinator @Inject constructor(private val appCoordinator: AppCoordinator) {
    fun finishPersonalPINEntry() {
        appCoordinator.navigate(Screen.SetupScan.parameterizedRoute())
    }
}
