package de.digitalService.useID.ui

import android.net.Uri
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.rememberUpdatedState
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
import de.digitalService.useID.ui.coordinators.AppCoordinatorType
import de.digitalService.useID.ui.previewMocks.PreviewTrackerManager
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
            SetupPersonalPinInputDestination.route -> firstTimeUserRoute("personalPINInput")
            SetupPersonalPinConfirmDestination.route -> firstTimeUserRoute("personalPINConfirm")
            SetupScanDestination.route -> firstTimeUserRoute("scan")
            SetupFinishDestination.route -> firstTimeUserRoute("done")

            IdentificationFetchMetadataDestination.route -> identificationRoute("fetchMetadata")
            IdentificationAttributeConsentDestination.route -> identificationRoute("attributes")
            IdentificationPersonalPINDestination.route -> identificationRoute("personalPIN")
            IdentificationScanDestination.route -> identificationRoute("scan")

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

private class PreviewAppCoordinator(
    override val nfcAvailability: State<NfcAvailability>,
    override val currentlyHandlingNFCTags: Boolean
) : AppCoordinatorType {
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
    override fun startNFCTagHandling() {}
    override fun stopNFCTagHandling() {}
    override fun navigatePopping(route: Direction) {}
}

@Preview(name = "Small", showSystemUi = true, device = Devices.NEXUS_5)
@Preview(name = "Large", showSystemUi = true, device = Devices.PIXEL_4_XL)
@Composable
private fun PreviewNfc() {
    UseIDApp(
        appCoordinator = PreviewAppCoordinator(rememberUpdatedState(newValue = NfcAvailability.Available), false),
        PreviewTrackerManager()
    )
}

@Preview(name = "Small", showSystemUi = true, device = Devices.NEXUS_5)
@Preview(name = "Large", showSystemUi = true, device = Devices.PIXEL_4_XL)
@Composable
private fun PreviewNoNfc() {
    UseIDApp(appCoordinator = PreviewAppCoordinator(rememberUpdatedState(newValue = NfcAvailability.NoNfc), false), PreviewTrackerManager())
}

@Preview(name = "Small", showSystemUi = true, device = Devices.NEXUS_5)
@Preview(name = "Large", showSystemUi = true, device = Devices.PIXEL_4_XL)
@Composable
private fun PreviewNfcDeactivated() {
    UseIDApp(
        appCoordinator = PreviewAppCoordinator(rememberUpdatedState(newValue = NfcAvailability.Deactivated), false),
        PreviewTrackerManager()
    )
}
