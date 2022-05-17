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
            FirstTimeUserCheckScreen(
                firstTimeUserHandler = {
                    navController.navigate(
                        Screens.FIRST_TIME_USER_PIN_LETTER_CHECK.name
                    )
                },
                experiencedUserHandler = { }
            )
        }

        composable(Screens.FIRST_TIME_USER_PIN_LETTER_CHECK.name) {
            FirstTimeUserPINLetterScreen(
                transportPINAvailableHandler = { navController.navigate(Screens.TRANSPORT_PIN_SCREEN.name) },
                noPINAvailable = { navController.navigate(Screens.RESET_PIN_SCREEN.name) })
        }

        composable(Screens.RESET_PIN_SCREEN.name) {
            ResetPINScreen()
        }

        composable(Screens.TRANSPORT_PIN_SCREEN.name) {
            TransportPINScreen(null)
        }
    }
}