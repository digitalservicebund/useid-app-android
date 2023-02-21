package de.digitalService.useID.ui.navigation

import androidx.navigation.NavController
import com.ramcosta.composedestinations.spec.Direction

interface Navigator {
    val isAtRoot: Boolean

    fun setNavController(navController: NavController)

    fun navigate(route: Direction)
    fun navigatePopping(route: Direction)
    fun popUpTo(route: Direction)
    fun popUpToOrNavigate(route: Direction, navigatePopping: Boolean)
    fun pop()
    fun popToRoot()
}
