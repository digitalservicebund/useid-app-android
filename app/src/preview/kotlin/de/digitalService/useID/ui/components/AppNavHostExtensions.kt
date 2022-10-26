package de.digitalService.useID.ui.components

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import de.digitalService.useID.ui.screens.PreviewIdentificationFetchMetadata
import de.digitalService.useID.ui.screens.PreviewIdentificationScan
import de.digitalService.useID.ui.screens.PreviewSetupScan

@Composable
fun ConfigSpecificSetupScan() {
    PreviewSetupScan(hiltViewModel(), hiltViewModel())
}

@Composable
fun ConfigSpecificIdentificationFetchMetadata() {
    PreviewIdentificationFetchMetadata(hiltViewModel())
}

@Composable
fun ConfigSpecificIdentificationScan() {
    PreviewIdentificationScan(hiltViewModel(), hiltViewModel())
}
