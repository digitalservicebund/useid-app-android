package de.digitalService.useID.ui.components.screens

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
import de.digitalService.useID.StorageManagerType
import de.digitalService.useID.analytics.TrackerManagerType
import de.digitalService.useID.models.ScanError
import de.digitalService.useID.ui.coordinators.AppCoordinator
import de.digitalService.useID.ui.coordinators.SetupCoordinator
import de.digitalService.useID.ui.previewMocks.PreviewTrackerManager
import de.digitalService.useID.ui.screens.setup.SetupScan
import de.digitalService.useID.ui.screens.setup.SetupScanViewModelInterface
import de.digitalService.useID.ui.theme.UseIDTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@Composable
fun PreviewSetupScan(viewModel: PreviewSetupScanViewModel) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth()
    ) {
        Column(modifier = Modifier.weight(1f)) {
            SetupScan(modifier = Modifier, viewModel.innerViewModel)
        }

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
            Button(onClick = { viewModel.simulateCardDeactivated() }) { Text("📵") }
        }
    }
}

@HiltViewModel
class PreviewSetupScanViewModel @Inject constructor(
    private val coordinator: SetupCoordinator,
    private val trackerManager: TrackerManagerType
) : ViewModel() {
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
        trackerManager.trackScreen("firstTimeUser/incorrectTransportPIN")
    }

    fun simulateCANRequired() {
        viewModelScope.launch {
            innerViewModel.injectShouldShowProgress(true)
            delay(3000L)
            innerViewModel.injectShouldShowProgress(false)
            innerViewModel.injectShouldShowError(ScanError.PINSuspended)
        }
        trackerManager.trackScreen("firstTimeUser/cardSuspended")
    }

    fun simulatePUKRequired() {
        viewModelScope.launch {
            innerViewModel.injectShouldShowProgress(true)
            delay(3000L)
            innerViewModel.injectShouldShowProgress(false)
            innerViewModel.injectShouldShowError(ScanError.PINBlocked)
        }
        trackerManager.trackScreen("firstTimeUser/cardBlocked")
    }

    fun simulateCardDeactivated() {
        viewModelScope.launch {
            innerViewModel.injectShouldShowProgress(true)
            delay(3000L)
            innerViewModel.injectShouldShowProgress(false)
            innerViewModel.injectShouldShowError(ScanError.CardDeactivated)
        }
        trackerManager.trackScreen("firstTimeUser/cardDeactivated")
    }

    val innerViewModel = object : SetupScanViewModelInterfaceExtension {
        override var shouldShowProgress: Boolean by mutableStateOf(false)
        override var errorState: ScanError? by mutableStateOf(null)
        override fun startSettingPIN(context: Context) {}
        override fun onReEnteredTransportPIN(transportPIN: String, context: Context) {
            injectShouldShowError(null)
        }

        override fun onHelpButtonTapped() = trackerManager.trackScreen("firstTimeUser/scanHelp")
        override fun onNfcButtonTapped() = trackerManager.trackEvent("firstTimeUser", "alertShown", "NFCInfo")
        override fun onBackButtonTapped() = coordinator.onBackTapped()
        override fun onCancelConfirm() {
            coordinator.cancelSetup()
        }

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
fun PreviewPreviewSetupScan() {
    val fakeStorageManager = (
        object : StorageManagerType {
            override fun getIsFirstTimeUser(): Boolean = false
            override fun setIsNotFirstTimeUser() {}
        }
        )

    UseIDTheme {
        PreviewSetupScan(
            PreviewSetupScanViewModel(SetupCoordinator(AppCoordinator(fakeStorageManager)), PreviewTrackerManager())
        )
    }
}
