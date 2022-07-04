package de.digitalService.useID.ui.composables

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import de.digitalService.useID.ui.composables.screens.SetupScan
import de.digitalService.useID.ui.composables.screens.SetupScanViewModel
import de.digitalService.useID.ui.composables.screens.identification.IdentificationScan
import de.digitalService.useID.ui.composables.screens.identification.IdentificationScanViewModel

@Composable
fun ConfigSpecificSetupScan() {
    SetupScan(viewModel = hiltViewModel<SetupScanViewModel>())
}

@Composable
fun ConfigSpecificIdentificationScan() {
    IdentificationScan(viewModel = hiltViewModel<IdentificationScanViewModel>())
}
