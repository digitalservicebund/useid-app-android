package de.digitalService.useID.ui.composables

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import de.digitalService.useID.ui.composables.screens.*
import de.digitalService.useID.ui.composables.screens.identification.*

sealed class NavigationException : Exception() {
    object MissingArgumentException : NavigationException()
}

@Composable
fun AppNavHost(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(
        navController = navController,
        startDestination = Screen.IdentificationFetchMetadata.routeTemplate,
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

        composable(Screen.SetupTransportPIN.routeTemplate) {
            SetupTransportPIN(hiltViewModel<SetupTransportPINViewModel>())
        }

        composable(
            Screen.SetupPersonalPINIntro.routeTemplate
        ) {
            SetupPersonalPINIntro(hiltViewModel<SetupPersonalPINIntroViewModel>())
        }

        composable(
            Screen.SetupPersonalPIN.routeTemplate
        ) {
            SetupPersonalPIN(hiltViewModel<SetupPersonalPINViewModel>())
        }

        composable(Screen.SetupScan.routeTemplate) {
            ConfigSpecificSetupScan()
        }

        composable(Screen.SetupFinish.routeTemplate) {
            SetupFinish(hiltViewModel<SetupFinishViewModel>())
        }

        composable(Screen.IdentificationFetchMetadata.routeTemplate) {
            IdentificationFetchMetadata(hiltViewModel<IdentificationFetchMetadataViewModel>())
        }

        composable(
            route = Screen.IdentificationAttributeConsent.routeTemplate,
            arguments = Screen.IdentificationAttributeConsent.namedNavArguments
        ) {
            IdentificationAttributeConsent(hiltViewModel<IdentificationAttributeConsentViewModel>())
        }
    }
}
