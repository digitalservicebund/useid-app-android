package de.digitalService.useID.ui.navigation

import androidx.navigation.NavController
import com.ramcosta.composedestinations.navigation.navigate
import com.ramcosta.composedestinations.spec.Direction
import de.digitalService.useID.ui.screens.destinations.Destination
import de.digitalService.useID.ui.screens.destinations.HomeScreenDestination
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppNavigator @Inject constructor() : Navigator {
    private lateinit var navController: NavController

    override val isAtRoot: Boolean
        get() = navController.previousBackStackEntry == null

    override fun setNavController(navController: NavController) {
        this.navController = navController
    }

    override fun navigate(route: Direction) {
        CoroutineScope(Dispatchers.Main).launch { navController.navigate(route) }
    }

    override fun navigatePopping(route: Direction) {
        CoroutineScope(Dispatchers.Main).launch {
            navController.navigate(route) {
                pop()
            }
        }
    }

    override fun pop() {
        navController.popBackStack()
    }

    override fun popUpTo(direction: Destination) {
        CoroutineScope(Dispatchers.Main).launch { navController.popBackStack(route = direction.route, inclusive = false) }
    }

    override fun popToRoot() {
        CoroutineScope(Dispatchers.Main).launch { navController.popBackStack(route = HomeScreenDestination.route, inclusive = false) }
    }
}
