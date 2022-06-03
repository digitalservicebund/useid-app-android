package de.digitalService.useID.ui.coordinators

import de.digitalService.useID.ui.AppCoordinator
import de.digitalService.useID.ui.composables.screens.Screen
import javax.inject.Inject

class TransportPINCoordinator @Inject constructor(private val appCoordinator: AppCoordinator) {
    fun finishTransportPINEntry() {
        appCoordinator.navigate(Screen.SetupPersonalPINIntro.parameterizedRoute())
    }
}
