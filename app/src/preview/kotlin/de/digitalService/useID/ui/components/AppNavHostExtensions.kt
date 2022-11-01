package de.digitalService.useID.ui.components

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import de.digitalService.useID.ui.screens.PreviewIdentificationFetchMetadata
import de.digitalService.useID.ui.screens.PreviewIdentificationScan
import de.digitalService.useID.ui.screens.PreviewSetupScan
import de.digitalService.useID.ui.screens.identification.IdentificationScanViewModel
import de.digitalService.useID.ui.screens.setup.SetupScanViewModel

@Composable
fun ConfigSpecificSetupScan() {
    PreviewSetupScan(hiltViewModel(), hiltViewModel<SetupScanViewModel>())
}

@Composable
fun ConfigSpecificIdentificationFetchMetadata() {
    PreviewIdentificationFetchMetadata(hiltViewModel())
}

@Composable
fun ConfigSpecificIdentificationScan() {
    PreviewIdentificationScan(hiltViewModel(), hiltViewModel<IdentificationScanViewModel>())
}
