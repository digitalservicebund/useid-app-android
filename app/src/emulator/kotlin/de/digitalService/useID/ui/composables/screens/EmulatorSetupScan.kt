package de.digitalService.useID.ui.composables

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import de.digitalService.useID.ui.AppCoordinator
import de.digitalService.useID.ui.composables.screens.SetupScan
import de.digitalService.useID.ui.composables.screens.SetupScanViewModelInterface
import de.digitalService.useID.ui.coordinators.SetupScanCoordinator
import de.digitalService.useID.ui.theme.UseIDTheme
import javax.inject.Inject

@Composable
fun EmulatorSetupScan(viewModel: EmulatorSetupScanViewModel) {
    val innerViewModel = object: SetupScanViewModelInterface {
        override fun onUIInitialized(context: Context) {}
        override fun onHelpButtonTapped() {}
    }

    Column(verticalArrangement = Arrangement.Top, modifier = Modifier.fillMaxSize()) {
        SetupScan(innerViewModel, modifier = Modifier.fillMaxHeight(0.9f))
        Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp)) {
            Button(onClick = { viewModel.simulateSuccess() }) { Text("✅") }
            Button(onClick = { viewModel.simulateIncorrectTransportPIN() }) { Text("❌") }
            Button(onClick = { viewModel.simulateCANRequired() }) { Text("CAN") }
            Button(onClick = { viewModel.simulatePUKRequired() }) { Text("PUK") }
        }
    }
}

@HiltViewModel
class EmulatorSetupScanViewModel @Inject constructor(private val coordinator: SetupScanCoordinator): ViewModel() {
    fun simulateSuccess() { coordinator.settingPINSucceeded() }
    fun simulateIncorrectTransportPIN() { coordinator.settingPINFailed(attempts = 2) }
    fun simulateCANRequired() { }
    fun simulatePUKRequired() { }
}

@Preview(device = Devices.PIXEL_3A)
@Composable
fun PreviewEmulatorSetupScan() {
    UseIDTheme {
        EmulatorSetupScan(EmulatorSetupScanViewModel(SetupScanCoordinator(AppCoordinator())))
    }
}