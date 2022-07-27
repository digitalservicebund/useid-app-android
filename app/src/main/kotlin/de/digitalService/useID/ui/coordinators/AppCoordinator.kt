package de.digitalService.useID.ui

import androidx.navigation.NavController
import com.ramcosta.composedestinations.navigation.navigate
import com.ramcosta.composedestinations.spec.Direction
import de.digitalService.useID.ui.composables.screens.destinations.IdentificationFetchMetadataDestination
import de.digitalService.useID.ui.composables.screens.destinations.SetupIntroDestination
import de.digitalService.useID.ui.composables.screens.destinations.SetupPINLetterDestination
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppCoordinator @Inject constructor() {
    private lateinit var navController: NavController

    fun setNavController(navController: NavController) {
        this.navController = navController
    }

    fun navigate(route: Direction) = navController.navigate(route)

    fun popToRoot() {
        navController.popBackStack(route = SetupIntroDestination.route, inclusive = false)
    }

    fun startIdentification(tcTokenURL: String) = navController.navigate(IdentificationFetchMetadataDestination(tcTokenURL))
}
