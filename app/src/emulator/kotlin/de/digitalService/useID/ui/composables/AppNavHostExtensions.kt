package de.digitalService.useID.ui.composables

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import de.digitalService.useID.ui.composables.screens.EmulatorIdentificationFetchMetadata
import de.digitalService.useID.ui.composables.screens.EmulatorIdentificationScan
import de.digitalService.useID.ui.composables.screens.identification.IdentificationFetchMetadata
import de.digitalService.useID.ui.composables.screens.identification.IdentificationFetchMetadataViewModel

@Composable
fun ConfigSpecificSetupScan() {
    EmulatorSetupScan(hiltViewModel())
}

@Composable
fun ConfigSpecificIdentificationFetchMetadata() {
    EmulatorIdentificationFetchMetadata(hiltViewModel())
}

@Composable
fun ConfigSpecificIdentificationScan() {
    EmulatorIdentificationScan(hiltViewModel())
}
