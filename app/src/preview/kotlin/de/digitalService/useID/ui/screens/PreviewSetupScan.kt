package de.digitalService.useID.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.digitalService.useID.StorageManagerType
import de.digitalService.useID.analytics.TrackerManagerType
import de.digitalService.useID.idCardInterface.EIDInteractionEvent
import de.digitalService.useID.idCardInterface.IDCardInteractionException
import de.digitalService.useID.idCardInterface.IDCardManager
import de.digitalService.useID.ui.screens.setup.SetupScan
import de.digitalService.useID.ui.screens.setup.SetupScanViewModel
import de.digitalService.useID.ui.theme.UseIDTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@Composable
fun PreviewSetupScan(viewModel: PreviewSetupScanViewModel, viewModelInner: SetupScanViewModel) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth()
    ) {
        Column(modifier = Modifier.weight(1f)) {
            SetupScan(modifier = Modifier, viewModelInner)
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
    private val trackerManager: TrackerManagerType,
    private val idCardManager: IDCardManager
) : ViewModel() {
    fun simulateSuccess() {
        viewModelScope.launch {
            simulateWaiting()

            idCardManager.injectChangePinEvent(EIDInteractionEvent.ProcessCompletedSuccessfullyWithoutResult)
        }
    }

    fun simulateIncorrectTransportPIN() {
        viewModelScope.launch {
            simulateWaiting()

            idCardManager.injectChangePinEvent(EIDInteractionEvent.RequestChangedPIN(2, { _, _ -> }))
            idCardManager.injectChangePinEvent(EIDInteractionEvent.RequestChangedPIN(2, { _, _ -> }))
        }
        trackerManager.trackScreen("firstTimeUser/incorrectTransportPIN")
    }

    fun simulateCANRequired() {
        viewModelScope.launch {
            simulateWaiting()

            idCardManager.injectChangePinEvent(EIDInteractionEvent.RequestCANAndChangedPIN(pinCallback = { _, _, _ -> }))
        }
        trackerManager.trackScreen("firstTimeUser/cardSuspended")
    }

    fun simulatePUKRequired() {
        viewModelScope.launch {
            simulateWaiting()

            idCardManager.injectChangePinEvent(EIDInteractionEvent.RequestPUK { _ -> })
        }
        trackerManager.trackScreen("firstTimeUser/cardBlocked")
    }

    fun simulateCardDeactivated() {
        viewModelScope.launch {
            simulateWaiting()

            idCardManager.injectChangePinException(IDCardInteractionException.CardDeactivated)
        }
        trackerManager.trackScreen("firstTimeUser/cardDeactivated")
    }

    private suspend fun simulateWaiting() {
        idCardManager.injectChangePinEvent(EIDInteractionEvent.CardRecognized)
        delay(3000L)
        idCardManager.injectChangePinEvent(EIDInteractionEvent.CardRemoved)
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
//        PreviewSetupScan(
//            PreviewSetupScanViewModel(SetupCoordinator(AppCoordinator(fakeStorageManager)), PreviewTrackerManager(), IDCardManager())
//        )
    }
}
