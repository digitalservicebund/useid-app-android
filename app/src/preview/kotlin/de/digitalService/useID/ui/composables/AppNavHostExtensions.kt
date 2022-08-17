package de.digitalService.useID.ui.composables

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import de.digitalService.useID.ui.composables.screens.PreviewIdentificationFetchMetadata
import de.digitalService.useID.ui.composables.screens.PreviewIdentificationScan

@Composable
fun ConfigSpecificSetupScan() {
    PreviewSetupScan(hiltViewModel())
}

@Composable
fun ConfigSpecificIdentificationFetchMetadata() {
    PreviewIdentificationFetchMetadata(hiltViewModel())
}

@Composable
fun ConfigSpecificIdentificationScan() {
    PreviewIdentificationScan(hiltViewModel())
}
