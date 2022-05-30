package de.digitalService.useID.ui.composables

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
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
            SetupIntro(hiltViewModel<SetupIntroViewModel>())
        }

        composable(Screen.SetupPINLetter.routeTemplate) {
            SetupPINLetter(hiltViewModel<SetupPINLetterViewModel>())
        }

        composable(Screen.ResetPIN.routeTemplate) {
            SetupResetPersonalPIN()
        }

        composable(
            Screen.SetupTransportPIN.routeTemplate,
            arguments = Screen.SetupTransportPIN.namedNavArguments
        ) {
            SetupTransportPIN(hiltViewModel<SetupTransportPINViewModel>())
        }

        composable(
            Screen.SetupPersonalPINIntro.routeTemplate,
        ) {
            SetupPersonalPINIntro(hiltViewModel<SetupPersonalPINIntroViewModel>())
        }

        composable(
            Screen.SetupPersonalPIN.routeTemplate,
        ) {
            SetupPersonalPIN(hiltViewModel<SetupPersonalPINViewModel>())
        }

        composable(
            Screen.SetupScan.routeTemplate,
            arguments = Screen.SetupScan.namedNavArguments
        ) {
            SetupScan(hiltViewModel<SetupScanViewModel>())
        }
    }
}