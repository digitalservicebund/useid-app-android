package de.digitalService.useID.ui.composables

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import de.digitalService.useID.ui.composables.screens.*

@Composable
fun AppNavHost(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(navController = navController, startDestination = Screens.FIRST_TIME_USER_CHECK.name, modifier = modifier) {
        composable(Screens.FIRST_TIME_USER_CHECK.name) {
            FirstTimeUserCheckScreen(FirstTimeUserCheckScreenViewModel(navController))
        }

        composable(Screens.FIRST_TIME_USER_PIN_LETTER_CHECK.name) {
            FirstTimeUserPINLetterScreen(FirstTimeUserPINLetterScreenViewModel(navController))
        }

        composable(Screens.RESET_PIN_SCREEN.name) {
            ResetPINScreen()
        }

        composable(Screens.TRANSPORT_PIN_SCREEN.name) {
            val viewModel = TransportPINScreenViewModel(navController, attempts = null)
            TransportPINScreen(viewModel)
        }
    }
}