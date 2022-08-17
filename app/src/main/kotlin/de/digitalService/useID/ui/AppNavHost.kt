package de.digitalService.useID.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.manualcomposablecalls.composable
import de.digitalService.useID.ui.composables.ConfigSpecificIdentificationFetchMetadata
import de.digitalService.useID.ui.composables.ConfigSpecificIdentificationScan
import de.digitalService.useID.ui.composables.ConfigSpecificSetupScan
import de.digitalService.useID.ui.screens.NavGraphs
import de.digitalService.useID.ui.screens.destinations.IdentificationFetchMetadataDestination
import de.digitalService.useID.ui.screens.destinations.IdentificationScanDestination
import de.digitalService.useID.ui.screens.destinations.SetupScanDestination

@Composable
fun AppNavHost(navController: NavHostController, modifier: Modifier = Modifier) {
    DestinationsNavHost(
        navGraph = NavGraphs.root,
        navController = navController,
        modifier = modifier
    ) {
        composable(SetupScanDestination) {
            ConfigSpecificSetupScan()
        }

        composable(IdentificationFetchMetadataDestination) {
            ConfigSpecificIdentificationFetchMetadata()
        }

        composable(IdentificationScanDestination) {
            ConfigSpecificIdentificationScan()
        }
    }
}
