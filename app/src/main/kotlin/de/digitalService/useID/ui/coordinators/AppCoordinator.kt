package de.digitalService.useID.ui

import androidx.navigation.NavController
import com.ramcosta.composedestinations.navigation.navigate
import com.ramcosta.composedestinations.spec.Direction
import de.digitalService.useID.StorageManagerType
import de.digitalService.useID.ui.composables.screens.destinations.IdentificationFetchMetadataDestination
import de.digitalService.useID.ui.composables.screens.destinations.SetupIntroDestination
import javax.inject.Inject
import javax.inject.Singleton

interface AppCoordinatorType {
    fun setNavController(navController: NavController)
    fun navigate(route: Direction)
    fun popToRoot()
    fun startIdentification(tcTokenURL: String)
    fun homeScreenLaunched(token: String?)
}

@Singleton
class AppCoordinator @Inject constructor(
    private val storageManager: StorageManagerType
) : AppCoordinatorType {
    private lateinit var navController: NavController
    private var firstTimeLaunch: Boolean = true

    override fun setNavController(navController: NavController) {
        this.navController = navController
    }

    override fun navigate(route: Direction) = navController.navigate(route)

    override fun popToRoot() {
        navController.popBackStack(route = SetupIntroDestination.route, inclusive = false)
    }

    override fun startIdentification(tcTokenURL: String) = navController.navigate(IdentificationFetchMetadataDestination(tcTokenURL))

    override fun homeScreenLaunched(token: String?) {
        if (!firstTimeLaunch) {
            return
        }
        firstTimeLaunch = false

        if (storageManager.getIsFirstTimeUser()) {
            navigate(SetupIntroDestination(token))
        }
    }
}
