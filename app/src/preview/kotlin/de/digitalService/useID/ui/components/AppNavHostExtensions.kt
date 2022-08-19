package de.digitalService.useID.ui.components

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import de.digitalService.useID.ui.components.screens.PreviewIdentificationFetchMetadata
import de.digitalService.useID.ui.components.screens.PreviewIdentificationScan
import de.digitalService.useID.ui.components.screens.PreviewSetupScan

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
