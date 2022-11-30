package de.digitalService.useID.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
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
import de.digitalService.useID.analytics.TrackerManagerType
import de.digitalService.useID.idCardInterface.EIDInteractionEvent
import de.digitalService.useID.idCardInterface.IDCardInteractionException
import de.digitalService.useID.idCardInterface.IDCardManager
import de.digitalService.useID.ui.previewMocks.PreviewTrackerManager
import de.digitalService.useID.ui.screens.setup.SetupScan
import de.digitalService.useID.ui.screens.setup.SetupScanViewModelInterface
import de.digitalService.useID.ui.theme.UseIDTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.openecard.mobile.activation.ActivationResultCode
import javax.inject.Inject

@Composable
fun PreviewSetupScan(viewModel: PreviewSetupScanViewModel, viewModelInner: SetupScanViewModelInterface) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth()
    ) {
        Column(modifier = Modifier.weight(1f)) {
            SetupScan(modifier = Modifier, viewModel = viewModelInner)
        }

        LazyRow(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp)
        ) {
            item { Button(onClick = { viewModel.simulateSuccess() }) { Text("📌👍") } }
            item { Button(onClick = { viewModel.simulateIncorrectTransportPIN() }) { Text("📌👎") } }
            item { Button(onClick = { viewModel.simulateCardUnreadable() }) { Text("🪪⚡️") } }
            item { Button(onClick = { viewModel.simulateCANRequired() }) { Text("CAN") } }
            item { Button(onClick = { viewModel.simulatePUKRequired() }) { Text("PUK") } }
            item { Button(onClick = { viewModel.simulateCardDeactivated() }) { Text("🪪📵") } }
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

    fun simulateCardUnreadable() {
        viewModelScope.launch {
            simulateWaiting()

            idCardManager.injectChangePinException(IDCardInteractionException.ProcessFailed(ActivationResultCode.INTERRUPTED, null, null))
        }
        trackerManager.trackScreen("firstTimeUser/cardUnreadable")
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
    UseIDTheme {
        PreviewSetupScan(
            PreviewSetupScanViewModel(PreviewTrackerManager(), IDCardManager()),
            object : SetupScanViewModelInterface {
                override val shouldShowProgress: Boolean = false

                override fun onHelpButtonTapped() {}
                override fun onNfcButtonTapped() {}
                override fun onCancelConfirm() {}
            }
        )
    }
}
