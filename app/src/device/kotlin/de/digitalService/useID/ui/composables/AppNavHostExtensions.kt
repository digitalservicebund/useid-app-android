package de.digitalService.useID.ui.composables

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import de.digitalService.useID.ui.screens.SetupScan
import de.digitalService.useID.ui.screens.SetupScanViewModel
import de.digitalService.useID.ui.screens.identification.IdentificationFetchMetadata
import de.digitalService.useID.ui.screens.identification.IdentificationFetchMetadataViewModel
import de.digitalService.useID.ui.screens.identification.IdentificationScan
import de.digitalService.useID.ui.screens.identification.IdentificationScanViewModel

@Composable
fun ConfigSpecificSetupScan() {
    SetupScan(viewModel = hiltViewModel<SetupScanViewModel>())
}

@Composable
fun ConfigSpecificIdentificationFetchMetadata() {
    IdentificationFetchMetadata(viewModel = hiltViewModel<IdentificationFetchMetadataViewModel>())
}

@Composable
fun ConfigSpecificIdentificationScan() {
    IdentificationScan(viewModel = hiltViewModel<IdentificationScanViewModel>())
}
