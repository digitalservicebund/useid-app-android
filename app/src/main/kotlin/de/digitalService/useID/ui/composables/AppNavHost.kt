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
    NavHost(navController = navController, startDestination = Screen.SetupIntro.routeTemplate, modifier = modifier) {
        composable(Screen.SetupIntro.routeTemplate) {
            SetupIntro(SetupIntroViewModel(navController))
        }

        composable(Screen.SetupPINLetter.routeTemplate) {
            SetupPINLetter(SetupPINLetterScreenViewModel(navController))
        }

        composable(Screen.ResetPIN.routeTemplate) {
            ReSetupPersonalPIN()
        }

        composable(Screen.TransportPIN.routeTemplate) {
            val viewModel = SetupTransportPINViewModel(navController, attempts = null)
            SetupTransportPIN(viewModel)
        }

        composable(Screen.SetPINIntro.routeTemplate,
            arguments = Screen.SetPINIntro.namedNavArguments
        ) { entry ->
            val arguments = entry.arguments ?: throw NavigationException.MissingArgumentException

            val pin = Screen.SetPINIntro.pin(arguments)
            val viewModel = SetupPersonalPINIntroViewModel(navController, pin)
            SetupPersonalPINIntro(viewModel)
        }

        composable(Screen.SetPIN.routeTemplate) {
            val viewModel = SetupPersonalPINViewModel()
            SetupPersonalPIN(viewModel)
        }
    }
}