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
import de.digitalService.useID.idCardInterface.EidInteractionEvent
import de.digitalService.useID.idCardInterface.IdCardInteractionException
import de.digitalService.useID.idCardInterface.IdCardManager
import de.digitalService.useID.ui.previewMocks.PreviewTrackerManager
import de.digitalService.useID.ui.screens.setup.SetupScan
import de.digitalService.useID.ui.screens.setup.SetupScanViewModelInterface
import de.digitalService.useID.ui.theme.UseIdTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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
            item { Button(onClick = { viewModel.simulateIncorrectTransportPin() }) { Text("📌👎") } }
            item { Button(onClick = { viewModel.simulateCardUnreadable() }) { Text("🪪⚡️") } }
            item { Button(onClick = { viewModel.simulateCanRequired() }) { Text("CAN") } }
            item { Button(onClick = { viewModel.simulatePUKRequired() }) { Text("PUK") } }
            item { Button(onClick = { viewModel.simulateCardDeactivated() }) { Text("🪪📵") } }
        }
    }
}

@HiltViewModel
class PreviewSetupScanViewModel @Inject constructor(
    private val trackerManager: TrackerManagerType,
    private val idCardManager: IdCardManager
) : ViewModel() {
    fun simulateSuccess() {
        viewModelScope.launch {
            simulateWaiting()

            idCardManager.injectEvent(EidInteractionEvent.ProcessCompletedSuccessfullyWithoutResult)
            delay(100L)
            idCardManager.injectEvent(EidInteractionEvent.Idle)
        }
    }

    fun simulateIncorrectTransportPin() {
        viewModelScope.launch {
            simulateWaiting()

            idCardManager.injectEvent(EidInteractionEvent.RequestChangedPin(2) { _, _ ->
                viewModelScope.launch {
                    idCardManager.injectEvent(EidInteractionEvent.RequestChangedPin(2, { _, _ -> }))
                }
            })
        }
        trackerManager.trackScreen("firstTimeUser/incorrectTransportPIN")
    }

    fun simulateCardUnreadable() {
        viewModelScope.launch {
            simulateWaiting()

            idCardManager.injectException(IdCardInteractionException.ProcessFailed(ActivationResultCode.INTERRUPTED, null, null))
        }
        trackerManager.trackScreen("firstTimeUser/cardUnreadable")
    }

    fun simulateCanRequired() {
        viewModelScope.launch {
            simulateWaiting()

            idCardManager.injectEvent(EidInteractionEvent.RequestCanAndChangedPin(pinCallback = { _, _, _ ->
                CoroutineScope(Dispatchers.Main).launch {
                    idCardManager.injectEvent(EidInteractionEvent.RequestCardInsertion)
                }
            }))
        }
        trackerManager.trackScreen("firstTimeUser/cardSuspended")
    }

    fun simulatePUKRequired() {
        viewModelScope.launch {
            simulateWaiting()

            idCardManager.injectEvent(EidInteractionEvent.RequestPuk { _ -> })
        }
        trackerManager.trackScreen("firstTimeUser/cardBlocked")
    }

    fun simulateCardDeactivated() {
        viewModelScope.launch {
            simulateWaiting()

            idCardManager.injectException(IdCardInteractionException.CardDeactivated)
        }
        trackerManager.trackScreen("firstTimeUser/cardDeactivated")
    }

    private suspend fun simulateWaiting() {
        idCardManager.injectEvent(EidInteractionEvent.CardRecognized)
        delay(3000L)
        idCardManager.injectEvent(EidInteractionEvent.CardRemoved)
    }
}

@Preview(device = Devices.PIXEL_3A)
@Composable
fun PreviewPreviewSetupScan() {
    UseIdTheme {
        PreviewSetupScan(
            PreviewSetupScanViewModel(PreviewTrackerManager(), IdCardManager()),
            object : SetupScanViewModelInterface {
                override val backAllowed: Boolean = false
                override val identificationPending: Boolean = false
                override val shouldShowProgress: Boolean = false

                override fun onHelpButtonClicked() {}
                override fun onNfcButtonClicked() {}
                override fun onNavigationButtonClicked() {}
            }
        )
    }
}
