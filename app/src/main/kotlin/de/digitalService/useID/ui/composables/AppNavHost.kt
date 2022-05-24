package de.digitalService.useID.ui.composables

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import de.digitalService.useID.ui.composables.screens.*

sealed class NavigationException : Exception() {
    object MissingArgumentException : NavigationException()
}

@Composable
fun AppNavHost(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(
        navController = navController,
        startDestination = Screen.SetupIntro.routeTemplate,
        modifier = modifier
    ) {
        composable(Screen.SetupIntro.routeTemplate) {
            SetupIntro(SetupIntroViewModel(navController))
        }

        composable(Screen.SetupPINLetter.routeTemplate) {
            SetupPINLetter(SetupPINLetterScreenViewModel(navController))
        }

        composable(Screen.ResetPIN.routeTemplate) {
            ReSetupPersonalPIN()
        }

        composable(Screen.SetupTransportPIN.routeTemplate) {
            val viewModel = SetupTransportPINViewModel(navController, attempts = null)
            SetupTransportPIN(viewModel)
        }

        composable(
            Screen.SetupPersonalPINIntro.routeTemplate,
            arguments = Screen.SetupPersonalPINIntro.namedNavArguments
        ) { entry ->
            val arguments = entry.arguments ?: throw NavigationException.MissingArgumentException

            val transportPIN = Screen.SetupPersonalPINIntro.transportPIN(arguments)
            val viewModel = SetupPersonalPINIntroViewModel(navController, transportPIN)

            SetupPersonalPINIntro(viewModel)
        }

        composable(
            Screen.SetupPersonalPIN.routeTemplate,
            arguments = Screen.SetupPersonalPIN.namedNavArguments
        ) { entry ->
            val arguments = entry.arguments ?: throw NavigationException.MissingArgumentException

            val transportPIN = Screen.SetupPersonalPINIntro.transportPIN(arguments)
            val viewModel = SetupPersonalPINViewModel(navController, transportPIN)

            SetupPersonalPIN(viewModel)
        }

        composable(
            Screen.SetupScan.routeTemplate,
            arguments = Screen.SetupScan.namedNavArguments
        ) { entry ->
            val arguments = entry.arguments ?: throw NavigationException.MissingArgumentException

            val transportPIN = Screen.SetupScan.transportPIN(arguments)
            val personalPIN = Screen.SetupScan.personalPIN(arguments)
            val viewModel = SetupScanViewModel(navController, transportPIN, personalPIN)

            SetupScan(viewModel)
        }
    }
}