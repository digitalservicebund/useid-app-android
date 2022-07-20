package de.digitalService.useID.ui.composables

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import de.digitalService.useID.SecureStorageManagerInterface
import de.digitalService.useID.ui.AppCoordinator
import de.digitalService.useID.ui.composables.screens.SetupScan
import de.digitalService.useID.ui.composables.screens.SetupScanViewModelInterface
import de.digitalService.useID.ui.coordinators.SetupCoordinator
import de.digitalService.useID.ui.theme.UseIDTheme
import javax.inject.Inject

@Composable
fun EmulatorSetupScan(viewModel: EmulatorSetupScanViewModel) {
    Column(verticalArrangement = Arrangement.Top, modifier = Modifier.fillMaxSize()) {
        SetupScan(modifier = Modifier.fillMaxHeight(0.9f), viewModel.innerViewModel)
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp)
        ) {
            Button(onClick = { viewModel.simulateSuccess() }) { Text("✅") }
            Button(onClick = { viewModel.simulateIncorrectTransportPIN() }) { Text("❌") }
            Button(onClick = { viewModel.simulateCANRequired() }) { Text("CAN") }
            Button(onClick = { viewModel.simulatePUKRequired() }) { Text("PUK") }
        }
    }
}

@HiltViewModel
class EmulatorSetupScanViewModel @Inject constructor(private val coordinator: SetupCoordinator) : ViewModel() {
    fun simulateSuccess() { coordinator.onSettingPINSucceeded() }
    fun simulateIncorrectTransportPIN() { innerViewModel.injectAttempts(innerViewModel.attempts - 1) }
    fun simulateCANRequired() { innerViewModel.injectShouldShowError(SetupScanViewModelInterface.Error.PINSuspended) }
    fun simulatePUKRequired() { innerViewModel.injectShouldShowError(SetupScanViewModelInterface.Error.IDDeactivated) }

    val innerViewModel = object : SetupScanViewModelInterfaceExtension {
        override var attempts: Int by mutableStateOf(3)
        override var errorState: SetupScanViewModelInterface.Error? by mutableStateOf(null)
        override fun startSettingPIN(context: Context) {}
        override fun onReEnteredTransportPIN(transportPIN: String, context: Context) { attempts = 3 }
        override fun onHelpButtonTapped() {}
        override fun onCancel() { coordinator.cancelSetup() }
        override fun onErrorDialogButtonTap() { coordinator.cancelSetup() }

        override fun injectAttempts(newAttempts: Int) {
            this.attempts = newAttempts
        }

        override fun injectShouldShowError(error: SetupScanViewModelInterface.Error) {
            errorState = error
        }
    }

    interface SetupScanViewModelInterfaceExtension : SetupScanViewModelInterface {
        fun injectAttempts(newAttempts: Int)
        fun injectShouldShowError(error: SetupScanViewModelInterface.Error)
    }
}

@Preview(device = Devices.PIXEL_3A)
@Composable
fun PreviewEmulatorSetupScan() {
    UseIDTheme {
        EmulatorSetupScan(EmulatorSetupScanViewModel(SetupCoordinator(AppCoordinator())))
    }
}
