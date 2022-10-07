package de.digitalService.useID.ui.components.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.digitalService.useID.StorageManagerType
import de.digitalService.useID.analytics.TrackerManagerType
import de.digitalService.useID.models.ScanError
import de.digitalService.useID.ui.coordinators.AppCoordinator
import de.digitalService.useID.ui.coordinators.IdentificationCoordinator
import de.digitalService.useID.ui.previewMocks.PreviewTrackerManager
import de.digitalService.useID.ui.screens.identification.IdentificationScan
import de.digitalService.useID.ui.screens.identification.IdentificationScanViewModelInterface
import de.digitalService.useID.ui.theme.UseIDTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@Composable
fun PreviewIdentificationScan(viewModel: PreviewIdentificationScanViewModel) {
    Column(verticalArrangement = Arrangement.Top, modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.weight(1f)) {
            IdentificationScan(modifier = Modifier.fillMaxHeight(0.9f), viewModel.innerViewModel)
        }

        LazyRow(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp)
        ) {
            item { Button(onClick = { viewModel.simulateSuccess() }) { Text("✅") } }
            item { Button(onClick = { viewModel.simulateIncorrectPIN() }) { Text("PIN") } }
            item { Button(onClick = { viewModel.simulateReadingErrorWithRedirect() }) { Text("❌➰") } }
            item { Button(onClick = { viewModel.simulateReadingErrorWithoutRedirect() }) { Text("❌") } }
            item { Button(onClick = { viewModel.simulateCANRequired() }) { Text("CAN") } }
            item { Button(onClick = { viewModel.simulatePUKRequired() }) { Text("PUK") } }
            item { Button(onClick = { viewModel.simulateCardDeactivated() }) { Text("📵") } }
        }
    }
}

@HiltViewModel
class PreviewIdentificationScanViewModel @Inject constructor(
    private val coordinator: IdentificationCoordinator,
    private val trackerManager: TrackerManagerType
) :
    ViewModel() {
    fun simulateSuccess() {
        viewModelScope.launch {
            innerViewModel.injectShouldShowProgress(true)
            delay(3000L)
            innerViewModel.injectShouldShowProgress(false)
            coordinator.onIDInteractionFinishedSuccessfully()
        }
    }
    fun simulateIncorrectPIN() {
        viewModelScope.launch {
            innerViewModel.injectShouldShowProgress(true)
            delay(3000L)
            innerViewModel.injectShouldShowProgress(false)
            innerViewModel.injectErrorState(ScanError.IncorrectPIN(2))
        }
    }
    fun simulateReadingErrorWithoutRedirect() {
        viewModelScope.launch {
            innerViewModel.injectShouldShowProgress(true)
            delay(3000L)
            innerViewModel.injectShouldShowProgress(false)
            innerViewModel.injectErrorState(ScanError.CardErrorWithoutRedirect)
        }
    }
    fun simulateReadingErrorWithRedirect() {
        viewModelScope.launch {
            innerViewModel.injectShouldShowProgress(true)
            delay(3000L)
            innerViewModel.injectShouldShowProgress(false)
            innerViewModel.injectErrorState(ScanError.CardErrorWithRedirect("https://digitalservice.bund.de"))
        }
    }
    fun simulateCANRequired() {
        viewModelScope.launch {
            innerViewModel.injectShouldShowProgress(true)
            delay(3000L)
            innerViewModel.injectShouldShowProgress(false)
            innerViewModel.injectErrorState(ScanError.PINSuspended)
        }
        trackerManager.trackScreen("identification/cardSuspended")
    }
    fun simulatePUKRequired() {
        viewModelScope.launch {
            innerViewModel.injectShouldShowProgress(true)
            delay(3000L)
            innerViewModel.injectShouldShowProgress(false)
            innerViewModel.injectErrorState(ScanError.PINBlocked)
        }
        trackerManager.trackScreen("identification/cardBlocked")
    }

    fun simulateCardDeactivated() {
        viewModelScope.launch {
            innerViewModel.injectShouldShowProgress(true)
            delay(3000L)
            innerViewModel.injectShouldShowProgress(false)
            innerViewModel.injectErrorState(ScanError.CardDeactivated)
        }
        trackerManager.trackScreen("identification/cardDeactivated")
    }

    val innerViewModel = object : IdentificationScanViewModelInterfaceExtension {
        override var shouldShowProgress by mutableStateOf(false)
        override var errorState: ScanError? by mutableStateOf(null)
        override fun onHelpButtonTapped() = trackerManager.trackScreen("identification/scanHelp")
        override fun onNfcButtonTapped() = trackerManager.trackEvent("identification", "alertShown", "NFCInfo")
        override fun onErrorDialogButtonTapped(context: Context) {
            errorState?.let { error ->
                if (error is ScanError.CardErrorWithRedirect) {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(error.redirectUrl))
                    ContextCompat.startActivity(context, intent, null)
                }
            }

            coordinator.cancelIdentification()
        }
        override fun onCancelIdentification() { coordinator.cancelIdentification() }
        override fun onNewPersonalPINEntered(pin: String) {
            errorState = null
        }

        override fun injectShouldShowProgress(show: Boolean) {
            shouldShowProgress = show
        }

        override fun injectErrorState(state: ScanError?) {
            errorState = state
        }
    }

    interface IdentificationScanViewModelInterfaceExtension : IdentificationScanViewModelInterface {
        fun injectShouldShowProgress(show: Boolean)
        fun injectErrorState(state: ScanError?)
    }
}

@Preview(device = Devices.PIXEL_3A)
@Composable
fun PreviewPreviewIdentificationScan() {
    UseIDTheme {
        PreviewIdentificationScan(
            PreviewIdentificationScanViewModel(
                IdentificationCoordinator(
                    AppCoordinator(object : StorageManagerType {
                        override fun getIsFirstTimeUser(): Boolean = false
                        override fun setIsNotFirstTimeUser() {}
                    }),
                    PreviewTrackerManager()
                ),
                PreviewTrackerManager()
            )
        )
    }
}
