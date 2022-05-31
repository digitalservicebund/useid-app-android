package de.digitalService.useID.ui.composables

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun ConfigSpecificSetupScan() {
    EmulatorSetupScan(hiltViewModel())
}
