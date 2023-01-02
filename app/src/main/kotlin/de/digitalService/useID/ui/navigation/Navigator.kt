package de.digitalService.useID.ui.navigation

import androidx.navigation.NavController
import com.ramcosta.composedestinations.spec.Direction
import de.digitalService.useID.ui.screens.destinations.Destination

interface Navigator {
    val isAtRoot: Boolean

    fun setNavController(navController: NavController)

    fun navigate(route: Direction)
    fun navigatePopping(route: Direction)
    fun popUpTo(direction: Destination)
    fun pop()
    fun popToRoot()
}
