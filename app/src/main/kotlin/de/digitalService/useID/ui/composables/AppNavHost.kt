package de.digitalService.useID.ui.composables

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.manualcomposablecalls.composable
import de.digitalService.useID.ui.composables.screens.*
import de.digitalService.useID.ui.composables.screens.destinations.IdentificationFetchMetadataDestination
import de.digitalService.useID.ui.composables.screens.destinations.IdentificationScanDestination
import de.digitalService.useID.ui.composables.screens.destinations.SetupScanDestination
import de.digitalService.useID.ui.composables.screens.identification.*

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
