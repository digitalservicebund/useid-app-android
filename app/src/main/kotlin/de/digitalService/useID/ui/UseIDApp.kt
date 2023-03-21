package de.digitalService.useID.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.ramcosta.composedestinations.spec.Direction
import de.digitalService.useID.analytics.TrackerManagerType
import de.digitalService.useID.models.NfcAvailability
import de.digitalService.useID.ui.navigation.AppNavHost
import de.digitalService.useID.ui.navigation.Navigator
import de.digitalService.useID.ui.previewMocks.PreviewTrackerManager
import de.digitalService.useID.ui.screens.destinations.*
import de.digitalService.useID.ui.screens.noNfc.NfcDeactivatedScreen
import de.digitalService.useID.ui.screens.noNfc.NoNfcScreen
import de.digitalService.useID.ui.theme.UseIdTheme
import io.sentry.compose.withSentryObservableEffect

@Composable
fun UseIDApp(nfcAvailability: NfcAvailability, navigator: Navigator, trackerManager: TrackerManagerType) {
    val navController = rememberNavController().withSentryObservableEffect()

    navigator.setNavController(navController)

    val context = LocalContext.current
    trackerManager.initTracker(context)

    navController.addOnDestinationChangedListener { _, destination, _ ->
        fun trackerRoute(flow: String, screen: String) = "$flow/$screen"

        fun firstTimeUserRoute(screen: String) = trackerRoute("firstTimeUser", screen)
        fun identificationRoute(screen: String) = trackerRoute("identification", screen)

        val trackerRoute = when (destination.route) {
            HomeScreenDestination.route -> "/"
            SetupResetPersonalPinDestination.route, CanResetPersonalPinDestination.route -> "missingPINLetter"

            SetupIntroDestination.route -> firstTimeUserRoute("intro")
            SetupPinLetterDestination.route -> firstTimeUserRoute("PINLetter")
            SetupTransportPinDestination.route -> firstTimeUserRoute("transportPIN")
            SetupPersonalPinIntroDestination.route -> firstTimeUserRoute("personalPINIntro")
            SetupPersonalPinInputDestination.route -> firstTimeUserRoute("personalPINInput")
            SetupPersonalPinConfirmDestination.route -> firstTimeUserRoute("personalPINConfirm")
            SetupScanDestination.route -> firstTimeUserRoute("scan")
            SetupFinishDestination.route -> firstTimeUserRoute("done")

            SetupCardDeactivatedDestination.route -> firstTimeUserRoute("cardDeactivated")
            SetupCardSuspendedDestination.route -> firstTimeUserRoute("cardSuspended")
            SetupCardBlockedDestination.route -> firstTimeUserRoute("cardBlocked")
            SetupCardUnreadableDestination.route -> firstTimeUserRoute("cardUnreadable")

            SetupCanConfirmTransportPinDestination.route -> "confirmTransportPIN"
            SetupCanAlreadySetupDestination.route -> "alreadySetup"
            SetupCanTransportPinDestination.route -> "transportPIN"

            IdentificationFetchMetadataDestination.route -> identificationRoute("fetchMetadata")
            IdentificationAttributeConsentDestination.route -> identificationRoute("attributes")
            IdentificationPersonalPinDestination.route -> identificationRoute("personalPIN")
            IdentificationScanDestination.route -> identificationRoute("scan")
            IdentificationCanPinForgottenDestination.route -> "canPINForgotten"

            IdentificationCardDeactivatedDestination.route -> identificationRoute("cardDeactivated")
            IdentificationCardBlockedDestination.route -> identificationRoute("cardBlocked")
            IdentificationCardUnreadableDestination.route -> identificationRoute("cardUnreadable")
            IdentificationOtherErrorDestination.route -> identificationRoute("other")

            SetupCanIntroDestination.route, IdentificationCanIntroDestination.route -> "canIntro"
            CanInputDestination.route -> "canInput"
            IdentificationCanPinInputDestination.route -> identificationRoute("personalPIN")

            ImprintScreenDestination.route -> "imprint"
            AccessibilityScreenDestination.route -> "accessibility"
            DependenciesScreenDestination.route -> "thirdPartyDependencies"
            PrivacyScreenDestination.route -> "privacy"

            else -> "unknown"
        }
        trackerManager.trackScreen(trackerRoute)
    }

    UseIdTheme {
        if (nfcAvailability == NfcAvailability.NoNfc) {
            NoNfcScreen()
            return@UseIdTheme
        }

        AppNavHost(
            navController = navController,
            modifier = Modifier
                .fillMaxWidth()
        )

        if (nfcAvailability == NfcAvailability.Deactivated) {
            Dialog(
                onDismissRequest = {},
                properties = DialogProperties(
                    dismissOnBackPress = false,
                    dismissOnClickOutside = false
                )
            ) {
                NfcDeactivatedScreen()
            }
        }
    }
}

private class PreviewAppNavigator : Navigator {
    override val isAtRoot: Boolean = false
    override fun setNavController(navController: NavController) {}
    override fun navigate(route: Direction) {}
    override fun pop() {}
    override fun popToRoot() {}
    override fun navigatePopping(route: Direction) {}
    override fun popUpTo(route: Direction) {}
    override fun popUpToOrNavigate(route: Direction, navigatePopping: Boolean) {}
}

@Preview(name = "Small", showSystemUi = true, device = Devices.NEXUS_5)
@Preview(name = "Large", showSystemUi = true, device = Devices.PIXEL_4_XL)
@Composable
private fun PreviewNfc() {
    UseIDApp(
        NfcAvailability.Available,
        PreviewAppNavigator(),
        PreviewTrackerManager()
    )
}

@Preview(name = "Small", showSystemUi = true, device = Devices.NEXUS_5)
@Preview(name = "Large", showSystemUi = true, device = Devices.PIXEL_4_XL)
@Composable
private fun PreviewNoNfc() {
    UseIDApp(
        NfcAvailability.NoNfc,
        PreviewAppNavigator(),
        PreviewTrackerManager()
    )
}

@Preview(name = "Small", showSystemUi = true, device = Devices.NEXUS_5)
@Preview(name = "Large", showSystemUi = true, device = Devices.PIXEL_4_XL)
@Composable
private fun PreviewNfcDeactivated() {
    UseIDApp(
        NfcAvailability.Deactivated,
        PreviewAppNavigator(),
        PreviewTrackerManager()
    )
}
