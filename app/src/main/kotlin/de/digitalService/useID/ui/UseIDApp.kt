package de.digitalService.useID.ui

import android.net.Uri
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.ramcosta.composedestinations.spec.Direction
import de.digitalService.useID.analytics.MockTrackerManager
import de.digitalService.useID.analytics.TrackerManagerType
import de.digitalService.useID.models.NfcAvailability
import de.digitalService.useID.ui.coordinators.AppCoordinatorType
import de.digitalService.useID.ui.screens.destinations.*
import de.digitalService.useID.ui.screens.noNfc.NfcDeactivatedScreen
import de.digitalService.useID.ui.screens.noNfc.NoNfcScreen
import de.digitalService.useID.ui.theme.UseIDTheme
import io.sentry.compose.withSentryObservableEffect

@Composable
fun UseIDApp(appCoordinator: AppCoordinatorType, trackerManager: TrackerManagerType) {
    val navController = rememberNavController().withSentryObservableEffect()

    appCoordinator.setNavController(navController)

    val context = LocalContext.current
    trackerManager.initTracker(context)

    navController.addOnDestinationChangedListener { _, destination, _ ->
        fun trackerRoute(flow: String, screen: String) = "$flow/$screen"

        fun firstTimeUserRoute(screen: String) = trackerRoute("firstTimeUser", screen)
        fun identificationRoute(screen: String) = trackerRoute("identification", screen)

        val trackerRoute = when (destination.route) {
            HomeScreenDestination.route -> "/"
            SetupIntroDestination.route -> firstTimeUserRoute("intro")
            SetupPINLetterDestination.route -> firstTimeUserRoute("PINLetter")
            SetupResetPersonalPINDestination.route -> firstTimeUserRoute("missingPINLetter")
            SetupTransportPINDestination.route -> firstTimeUserRoute("transportPIN")
            SetupPersonalPINIntroDestination.route -> firstTimeUserRoute("personalPINIntro")
            SetupPersonalPINDestination.route -> firstTimeUserRoute("personalPIN")
            SetupScanDestination.route -> firstTimeUserRoute("scan")
            SetupFinishDestination.route -> firstTimeUserRoute("done")

            IdentificationFetchMetadataDestination.route -> identificationRoute("fetchMetadata")
            IdentificationAttributeConsentDestination.route -> identificationRoute("attributes")
            IdentificationPersonalPINDestination.route -> identificationRoute("personalPIN")
            IdentificationScanDestination.route -> identificationRoute("scan")
            IdentificationSuccessDestination.route -> identificationRoute("done")

            ImprintScreenDestination.route -> "imprint"
            AccessibilityScreenDestination.route -> "accessibility"
            DependenciesScreenDestination.route -> "thirdPartyDependencies"
            PrivacyScreenDestination.route -> "privacy"

            else -> "unknown"
        }
        trackerManager.trackScreen(trackerRoute)
    }

    UseIDTheme {
        if (appCoordinator.nfcAvailability.value == NfcAvailability.NoNfc) {
            NoNfcScreen()
            return@UseIDTheme
        }

        AppNavHost(
            navController = navController,
            modifier = Modifier
                .fillMaxWidth()
        )

        if (appCoordinator.nfcAvailability.value == NfcAvailability.Deactivated) {
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

private class PreviewAppCoordinator(override val nfcAvailability: State<NfcAvailability>) : AppCoordinatorType {
    override fun setNavController(navController: NavController) {}
    override fun navigate(route: Direction) {}
    override fun pop() {}
    override fun popToRoot() {}
    override fun startIdSetup(tcTokenURL: String?) {}
    override fun startIdentification(tcTokenURL: String) {}
    override fun homeScreenLaunched() {}
    override fun setNfcAvailability(availability: NfcAvailability) {}
    override fun setIsNotFirstTimeUser() {}
    override fun handleDeepLink(uri: Uri) {}
}

@Preview(name = "Small", showSystemUi = true, device = Devices.NEXUS_5)
@Preview(name = "Large", showSystemUi = true, device = Devices.PIXEL_4_XL)
@Composable
private fun Preview1() {
    UseIDApp(appCoordinator = PreviewAppCoordinator(rememberUpdatedState(newValue = NfcAvailability.Available)), MockTrackerManager())
}

@Preview(name = "Small", showSystemUi = true, device = Devices.NEXUS_5)
@Preview(name = "Large", showSystemUi = true, device = Devices.PIXEL_4_XL)
@Composable
private fun Preview2() {
    UseIDApp(appCoordinator = PreviewAppCoordinator(rememberUpdatedState(newValue = NfcAvailability.NoNfc)), MockTrackerManager())
}

@Preview(name = "Small", showSystemUi = true, device = Devices.NEXUS_5)
@Preview(name = "Large", showSystemUi = true, device = Devices.PIXEL_4_XL)
@Composable
private fun Preview3() {
    UseIDApp(appCoordinator = PreviewAppCoordinator(rememberUpdatedState(newValue = NfcAvailability.Deactivated)), MockTrackerManager())
}
