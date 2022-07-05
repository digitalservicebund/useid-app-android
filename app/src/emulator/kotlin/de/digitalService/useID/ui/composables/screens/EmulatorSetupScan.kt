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
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.digitalService.useID.SecureStorageManagerInterface
import de.digitalService.useID.ui.AppCoordinator
import de.digitalService.useID.ui.ScanError
import de.digitalService.useID.ui.composables.screens.SetupScan
import de.digitalService.useID.ui.composables.screens.SetupScanViewModelInterface
import de.digitalService.useID.ui.coordinators.SetupCoordinator
import de.digitalService.useID.ui.theme.UseIDTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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
    fun simulateSuccess() {
        viewModelScope.launch {
            innerViewModel.injectShouldShowProgress(true)
            delay(3000L)
            innerViewModel.injectShouldShowProgress(false)
            coordinator.onSettingPINSucceeded()
        }
    }
    fun simulateIncorrectTransportPIN() {
        viewModelScope.launch {
            innerViewModel.injectShouldShowProgress(true)
            delay(3000L)
            innerViewModel.injectShouldShowProgress(false)
            innerViewModel.injectShouldShowError(ScanError.IncorrectPIN(2))
        }
    }
    fun simulateCANRequired() {
        viewModelScope.launch {
            innerViewModel.injectShouldShowProgress(true)
            delay(3000L)
            innerViewModel.injectShouldShowProgress(false)
            innerViewModel.injectShouldShowError(ScanError.PINSuspended)
        }
    }
    fun simulatePUKRequired() {
        viewModelScope.launch {
            innerViewModel.injectShouldShowProgress(true)
            delay(3000L)
            innerViewModel.injectShouldShowProgress(false)
            innerViewModel.injectShouldShowError(ScanError.PINBlocked)
        }
    }

    val innerViewModel = object : SetupScanViewModelInterfaceExtension {
        override var shouldShowProgress: Boolean by mutableStateOf(false)
        override var errorState: ScanError? by mutableStateOf(null)
        override fun startSettingPIN(context: Context) {}
        override fun onReEnteredTransportPIN(transportPIN: String, context: Context) { injectShouldShowError(null) }
        override fun onHelpButtonTapped() {}
        override fun onCancel() { coordinator.cancelSetup() }

        override fun injectShouldShowProgress(show: Boolean) {
            shouldShowProgress = show
        }
        override fun injectShouldShowError(error: ScanError?) {
            errorState = error
        }
    }

    interface SetupScanViewModelInterfaceExtension : SetupScanViewModelInterface {
        fun injectShouldShowProgress(show: Boolean)
        fun injectShouldShowError(error: ScanError?)
    }
}

@Preview(device = Devices.PIXEL_3A)
@Composable
fun PreviewEmulatorSetupScan() {
    UseIDTheme {
        EmulatorSetupScan(EmulatorSetupScanViewModel(SetupCoordinator(AppCoordinator())))
    }
}
