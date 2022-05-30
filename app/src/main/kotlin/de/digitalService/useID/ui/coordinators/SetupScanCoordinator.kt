package de.digitalService.useID.ui.coordinators

import de.digitalService.useID.ui.AppCoordinator
import de.digitalService.useID.ui.composables.screens.Screen
import javax.inject.Inject

class SetupScanCoordinator @Inject constructor(private val appCoordinator: AppCoordinator) {
    fun settingPINSucceeded() {
        appCoordinator.navigate(Screen.SetupFinish.parameterizedRoute())
    }

    fun settingPINFailed(attempts: Int) {

    }
}