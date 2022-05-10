package de.digitalService.useID.ui.composables

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import de.digitalService.useID.ui.composables.screens.FirstTimeUserCheckScreen
import de.digitalService.useID.ui.composables.screens.FirstTimeUserPINLetterScreen
import de.digitalService.useID.ui.composables.screens.ResetPINScreen
import de.digitalService.useID.ui.composables.screens.Screens

@Composable
fun AppNavHost(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(navController = navController, startDestination = Screens.FIRST_TIME_USER_CHECK.name, modifier = modifier) {
        composable(Screens.FIRST_TIME_USER_CHECK.name) {
            FirstTimeUserCheckScreen(firstTimeUserHandler = { }, experiencedUserHandler = {
                navController.navigate(
                    Screens.FIRST_TIME_USER_PIN_LETTER_CHECK.name
                )
            })
        }

        composable(Screens.FIRST_TIME_USER_PIN_LETTER_CHECK.name) {
            FirstTimeUserPINLetterScreen(
                transportPINAvailableHandler = { },
                noPINAvailable = { navController.navigate(Screens.RESET_PIN_SCREEN.name) })
        }

        composable(Screens.RESET_PIN_SCREEN.name) {
            ResetPINScreen()
        }
    }
}