package de.digitalService.useID.ui.navigation

import androidx.navigation.NavController
import androidx.navigation.NavDestination
import com.ramcosta.composedestinations.navigation.navigate
import com.ramcosta.composedestinations.spec.Direction
import de.digitalService.useID.getLogger
import de.digitalService.useID.ui.screens.destinations.Destination
import de.digitalService.useID.ui.screens.destinations.HomeScreenDestination
import de.digitalService.useID.util.CoroutineContextProviderType
import kotlinx.coroutines.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.truncate

@Singleton
class AppNavigator @Inject constructor(private val coroutineContextProvider: CoroutineContextProviderType) : Navigator {
    private val logger by getLogger()

    private lateinit var navController: NavController

    override val isAtRoot: Boolean
        get() = navController.previousBackStackEntry == null

    override fun setNavController(navController: NavController) {
        this.navController = navController
    }

    override fun navigate(route: Direction) {
        CoroutineScope(coroutineContextProvider.Main).launch { navController.navigate(route) }
    }

    override fun navigatePopping(route: Direction) {
        CoroutineScope(coroutineContextProvider.Main).launch {
            navController.navigate(route) {
                navController.popBackStack()
            }
        }
    }

    override fun pop() {
        CoroutineScope(coroutineContextProvider.Main).launch { navController.popBackStack() }
    }

    override fun popUpTo(route: Direction) {
        CoroutineScope(coroutineContextProvider.Main).launch { navController.popBackStack(route = route.route, inclusive = false) }
    }

    override fun popUpToOrNavigate(route: Direction, navigatePopping: Boolean) {
        CoroutineScope(coroutineContextProvider.Main).launch {
            if (!popBackStackWithWorkaround(route, inclusive = false)) {
                navController.navigate(route) {
                    if (navigatePopping) {
                        navController.popBackStack()
                    }
                }
                logger.debug("Pushed direction ${route.route}")
            } else {
                logger.debug("Popped back stack to direction ${route.route}")
            }
        }
    }

    override fun popToRoot() {
        CoroutineScope(coroutineContextProvider.Main).launch {
            if (navController.currentBackStackEntry != null) {
                navController.popBackStack(route = HomeScreenDestination.route, inclusive = false)
            }
        }
    }

    // As we can't use the destinations navigator here we need to pop manually
    private fun popBackStackWithWorkaround(direction: Direction, inclusive: Boolean): Boolean {
        var destinationId: Int? = null
        val truncatedDirectionRoute = direction.truncatedRoute()

        for (entry in navController.backQueue) {
            if (entry.destination.truncatedRoute() == truncatedDirectionRoute) {
                destinationId = entry.destination.id
                break
            }
        }

        return if (destinationId != null) {
            navController.popBackStack(destinationId, true)
            if (!inclusive) {
                navController.navigate(direction)
            }
            true
        } else {
            false
        }
    }
}

fun NavDestination.truncatedRoute() = route?.split("/")?.first() ?: ""
fun Direction.truncatedRoute() = route.split("/").first()
