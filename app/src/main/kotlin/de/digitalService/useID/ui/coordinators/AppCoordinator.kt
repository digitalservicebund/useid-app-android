package de.digitalService.useID.ui

import android.util.Log
import androidx.navigation.NavController
import de.digitalService.useID.getLogger
import de.digitalService.useID.ui.composables.screens.Screen
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppCoordinator @Inject constructor() {
    private lateinit var navController: NavController

    fun setNavController(navController: NavController) {
        this.navController = navController
    }

    fun navigate(route: String) = navController.navigate(route)

    fun popToRoot() {
        navController.popBackStack(route = Screen.SetupIntro.routeTemplate, inclusive = false)
    }

    fun startSetupIDCard() = navController.navigate(Screen.SetupPINLetter.parameterizedRoute())
    fun startIdentification() = navController.navigate(Screen.IdentificationFetchMetadata.parameterizedRoute())
}
