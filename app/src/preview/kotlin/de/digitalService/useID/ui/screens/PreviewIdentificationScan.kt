package de.digitalService.useID.ui.screens

import android.content.Context
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
import de.digitalService.useID.getLogger
import de.digitalService.useID.idCardInterface.EidInteractionEvent
import de.digitalService.useID.idCardInterface.IdCardInteractionException
import de.digitalService.useID.idCardInterface.IdCardManager
import de.digitalService.useID.models.ScanError
import de.digitalService.useID.ui.previewMocks.PreviewTrackerManager
import de.digitalService.useID.ui.screens.identification.IdentificationScan
import de.digitalService.useID.ui.screens.identification.IdentificationScanViewModelInterface
import de.digitalService.useID.ui.theme.UseIdTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
//import org.openecard.mobile.activation.ActivationResultCode
import javax.inject.Inject

@Composable
fun PreviewIdentificationScan(
    viewModel: PreviewIdentificationScanViewModel,
    identificationScanViewModel: IdentificationScanViewModelInterface
) {
    Column(verticalArrangement = Arrangement.Top, modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.weight(1f)) {
            IdentificationScan(modifier = Modifier.fillMaxHeight(0.9f), identificationScanViewModel)
        }

        LazyRow(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp)
        ) {
            item { Button(onClick = { viewModel.simulateSuccess() }) { Text("✅") } }
            item { Button(onClick = { viewModel.simulateIncorrectPin() }) { Text("PIN") } }
            item { Button(onClick = { viewModel.simulateReadingErrorWithRedirect() }) { Text("❌➰") } }
            item { Button(onClick = { viewModel.simulateReadingErrorWithoutRedirect() }) { Text("❌") } }
            item { Button(onClick = { viewModel.simulateCanRequired() }) { Text("CAN") } }
            item { Button(onClick = { viewModel.simulatePUKRequired() }) { Text("PUK") } }
            item { Button(onClick = { viewModel.simulateCardDeactivated() }) { Text("📵") } }
        }
    }
}

@HiltViewModel
class PreviewIdentificationScanViewModel @Inject constructor(
    private val trackerManager: TrackerManagerType,
    private val idCardManager: IdCardManager
) : ViewModel() {
    fun simulateSuccess() {
        viewModelScope.launch(Dispatchers.Main) {
            simulateWaiting()

            idCardManager.injectEvent(EidInteractionEvent.ProcessCompletedSuccessfullyWithRedirect("https://digitalservice.bund.de"))
        }
    }

    fun simulateIncorrectPin() {
        viewModelScope.launch(Dispatchers.Main) {
            simulateWaiting()

            idCardManager.injectEvent(EidInteractionEvent.RequestPin(2) {
                viewModelScope.launch(Dispatchers.Main) {
                    idCardManager.injectEvent(EidInteractionEvent.RequestCardInsertion)
                }
            })
        }
    }

    fun simulateReadingErrorWithoutRedirect() {
        viewModelScope.launch(Dispatchers.Main) {
            simulateWaiting()

            idCardManager.injectException(IdCardInteractionException.ProcessFailed)
        }
    }

    fun simulateReadingErrorWithRedirect() {
        viewModelScope.launch(Dispatchers.Main) {
            simulateWaiting()

            idCardManager.injectException(IdCardInteractionException.ProcessFailed)
        }
    }

    fun simulateCanRequired() {
        viewModelScope.launch(Dispatchers.Main) {
            simulateWaiting()

            idCardManager.injectEvent(EidInteractionEvent.RequestPinAndCan { _, _ ->
                CoroutineScope(Dispatchers.Main).launch {
                    idCardManager.injectEvent(EidInteractionEvent.RequestCardInsertion)
                }
            })
        }
        trackerManager.trackScreen("identification/cardSuspended")
    }

    fun simulatePUKRequired() {
        viewModelScope.launch(Dispatchers.Main) {
            simulateWaiting()

            idCardManager.injectEvent(EidInteractionEvent.CardInteractionComplete)
            idCardManager.injectException(IdCardInteractionException.CardBlocked)
        }
        trackerManager.trackScreen("identification/cardBlocked")
    }

    fun simulateCardDeactivated() {
        viewModelScope.launch(Dispatchers.Main) {
            simulateWaiting()

            idCardManager.injectException(IdCardInteractionException.CardDeactivated)
        }
        trackerManager.trackScreen("identification/cardDeactivated")
    }

    private suspend fun simulateWaiting() {
        idCardManager.injectEvent(EidInteractionEvent.CardRecognized)
        delay(3000L)
        idCardManager.injectEvent(EidInteractionEvent.CardRemoved)
    }
}

@Preview(device = Devices.PIXEL_3A)
@Composable
fun PreviewPreviewIdentificationScan() {
    UseIdTheme {
        PreviewIdentificationScan(
            PreviewIdentificationScanViewModel(
                PreviewTrackerManager(),
                IdCardManager()
            ),
            object : IdentificationScanViewModelInterface {
                override val shouldShowProgress: Boolean = false

                override fun onHelpButtonClicked() {}
                override fun onNfcButtonClicked() {}
                override fun onCancelIdentification() {}
            }
        )
    }
}
