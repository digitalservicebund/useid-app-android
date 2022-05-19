package de.digitalService.useID.ui.composables

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import de.digitalService.useID.ui.composables.screens.*

sealed class NavigationException: Exception() {
    object MissingArgumentException: NavigationException()
}

@Composable
fun AppNavHost(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(navController = navController, startDestination = Screen.SetPIN.routeTemplate, modifier = modifier) {
        composable(Screen.FirstTimeUserCheck.routeTemplate) {
            FirstTimeUserCheckScreen(FirstTimeUserCheckScreenViewModel(navController))
        }

        composable(Screen.FirstTimeUserPINLetterCheck.routeTemplate) {
            FirstTimeUserPINLetterScreen(FirstTimeUserPINLetterScreenViewModel(navController))
        }

        composable(Screen.ResetPIN.routeTemplate) {
            ResetPINScreen()
        }

        composable(Screen.TransportPIN.routeTemplate) {
            val viewModel = TransportPINScreenViewModel(navController, attempts = null)
            TransportPINScreen(viewModel)
        }

        composable(Screen.SetPINIntro.routeTemplate,
            arguments = Screen.SetPINIntro.namedNavArguments
        ) { entry ->
            val arguments = entry.arguments ?: throw NavigationException.MissingArgumentException

            val pin = Screen.SetPINIntro.pin(arguments)
            val viewModel = SetPINIntroScreenViewModel(navController, pin)
            SetPINIntroScreen(viewModel)
        }

        composable(Screen.SetPIN.routeTemplate) {
            val viewModel = SetPINScreenViewModel()
            SetPINScreen(viewModel)
        }
    }
}