package de.digitalService.useID.ui.coordinators

import de.digitalService.useID.ui.AppCoordinator
import de.digitalService.useID.ui.composables.screens.Screen
import javax.inject.Inject

class PINLetterCoordinator @Inject constructor(private val appCoordinator: AppCoordinator) {
    fun letterAvailable() {
        appCoordinator.navigate(Screen.SetupTransportPIN.parameterizedRoute())
    }

    fun letterNotAvailable() {
        appCoordinator.navigate(Screen.ResetPIN.parameterizedRoute())
    }
}
