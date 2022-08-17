@file:OptIn(ExperimentalComposeUiApi::class)

package de.digitalService.useID.ui

import android.os.Bundle
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.compose.rememberNavController
import com.ramcosta.composedestinations.spec.Direction
import de.digitalService.useID.R
import de.digitalService.useID.models.NfcAvailability
import de.digitalService.useID.ui.composables.ScreenWithTopBar
import de.digitalService.useID.ui.screens.noNfc.NfcDeactivatedScreen
import de.digitalService.useID.ui.screens.noNfc.NoNfcScreen
import de.digitalService.useID.ui.theme.UseIDTheme
import io.sentry.compose.withSentryObservableEffect

@Composable
fun UseIDApp(appCoordinator: AppCoordinatorType) {
    val navController = rememberNavController().withSentryObservableEffect()
    var shouldShowBackButton by remember { mutableStateOf(false) }

    appCoordinator.setNavController(navController)

    navController.addOnDestinationChangedListener(object : NavController.OnDestinationChangedListener {
        override fun onDestinationChanged(
            controller: NavController,
            destination: NavDestination,
            arguments: Bundle?
        ) {
            shouldShowBackButton = controller.previousBackStackEntry != null
        }
    })

    UseIDTheme {
        if (appCoordinator.nfcAvailability.value == NfcAvailability.NoNfc) {
            NoNfcScreen()
            return@UseIDTheme
        }

        ScreenWithTopBar(navigationIcon = {
            if (shouldShowBackButton) {
                IconButton(modifier = Modifier.testTag("backButton"), onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = stringResource(id = R.string.navigation_back)
                    )
                }
            }
        }) { topBarPadding ->
            AppNavHost(
                navController = navController,
                modifier = Modifier
                    .padding(top = topBarPadding)
                    .fillMaxWidth()
            )
        }

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
    override fun popToRoot() {}
    override fun startIdentification(tcTokenURL: String) {}
    override fun homeScreenLaunched(token: String?) {}
    override fun setNfcAvailability(availability: NfcAvailability) {}
    override fun setIsNotFirstTimeUser() {}
}

@Preview(name = "Small", showSystemUi = true, device = Devices.NEXUS_5)
@Preview(name = "Large", showSystemUi = true, device = Devices.PIXEL_4_XL)
@Composable
private fun Preview1() {
    UseIDApp(appCoordinator = PreviewAppCoordinator(rememberUpdatedState(newValue = NfcAvailability.Available)))
}

@Preview(name = "Small", showSystemUi = true, device = Devices.NEXUS_5)
@Preview(name = "Large", showSystemUi = true, device = Devices.PIXEL_4_XL)
@Composable
private fun Preview2() {
    UseIDApp(appCoordinator = PreviewAppCoordinator(rememberUpdatedState(newValue = NfcAvailability.NoNfc)))
}

@Preview(name = "Small", showSystemUi = true, device = Devices.NEXUS_5)
@Preview(name = "Large", showSystemUi = true, device = Devices.PIXEL_4_XL)
@Composable
private fun Preview3() {
    UseIDApp(appCoordinator = PreviewAppCoordinator(rememberUpdatedState(newValue = NfcAvailability.Deactivated)))
}
