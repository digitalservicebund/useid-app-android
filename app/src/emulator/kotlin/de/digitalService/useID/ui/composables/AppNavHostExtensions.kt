package de.digitalService.useID.ui.composables

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import de.digitalService.useID.ui.composables.screens.EmulatorIdentificationScan

@Composable
fun ConfigSpecificSetupScan() {
    EmulatorSetupScan(hiltViewModel())
}

@Composable
fun ConfigSpecificIdentificationScan() {
    EmulatorIdentificationScan(hiltViewModel())
}
