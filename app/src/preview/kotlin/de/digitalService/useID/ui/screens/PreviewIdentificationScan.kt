package de.digitalService.useID.ui.screens

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
import de.digitalService.useID.ui.screens.identification.IdentificationScan
import de.digitalService.useID.ui.screens.identification.IdentificationScanViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.openecard.mobile.activation.ActivationResultCode
import javax.inject.Inject

@Composable
fun PreviewIdentificationScan(viewModel: PreviewIdentificationScanViewModel, identificationScanViewModel: IdentificationScanViewModel) {
    Column(verticalArrangement = Arrangement.Top, modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.weight(1f)) {
            IdentificationScan(modifier = Modifier.fillMaxHeight(0.9f), identificationScanViewModel)
        }
        val context = LocalContext.current

        LazyRow(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp)
        ) {
            item { Button(onClick = { viewModel.simulateSuccess(context) }) { Text("✅") } }
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
    private val trackerManager: TrackerManagerType,
    private val idCardManager: IDCardManager
) : ViewModel() {
    fun simulateSuccess(context: Context) {
        viewModelScope.launch(Dispatchers.Main) {
            simulateWaiting()

            idCardManager.injectIdentifyEvent(EIDInteractionEvent.ProcessCompletedSuccessfullyWithRedirect("https://digitalservice.bund.de"))
        }
    }

    fun simulateIncorrectPIN() {
        viewModelScope.launch(Dispatchers.Main) {
            simulateWaiting()

            idCardManager.injectIdentifyEvent(EIDInteractionEvent.RequestPIN(2) {})
        }
    }

    fun simulateReadingErrorWithoutRedirect() {
        viewModelScope.launch(Dispatchers.Main) {
            simulateWaiting()

            idCardManager.injectIdentifyException(IDCardInteractionException.ProcessFailed(ActivationResultCode.BAD_REQUEST, null, null))
        }
    }

    fun simulateReadingErrorWithRedirect() {
        viewModelScope.launch(Dispatchers.Main) {
            simulateWaiting()

            idCardManager.injectIdentifyException(
                IDCardInteractionException.ProcessFailed(
                    ActivationResultCode.BAD_REQUEST,
                    "https://digitalservice.bund.de",
                    null
                )
            )
        }
    }

    fun simulateCANRequired() {
        viewModelScope.launch(Dispatchers.Main) {
            simulateWaiting()

            idCardManager.injectIdentifyEvent(EIDInteractionEvent.RequestCAN({}))
        }
        trackerManager.trackScreen("identification/cardSuspended")
    }

    fun simulatePUKRequired() {
        viewModelScope.launch(Dispatchers.Main) {
            simulateWaiting()

            idCardManager.injectIdentifyEvent(EIDInteractionEvent.RequestPUK({}))
        }
        trackerManager.trackScreen("identification/cardBlocked")
    }

    fun simulateCardDeactivated() {
        viewModelScope.launch(Dispatchers.Main) {
            simulateWaiting()

            idCardManager.injectIdentifyException(IDCardInteractionException.CardDeactivated)
        }
        trackerManager.trackScreen("identification/cardDeactivated")
    }

    private suspend fun simulateWaiting() {
        idCardManager.injectIdentifyEvent(EIDInteractionEvent.CardRecognized)
        delay(3000L)
        idCardManager.injectIdentifyEvent(EIDInteractionEvent.CardRemoved)
    }
}

@Preview(device = Devices.PIXEL_3A)
@Composable
fun PreviewPreviewIdentificationScan() {
//    UseIDTheme {
//        PreviewIdentificationScan(
//            PreviewIdentificationScanViewModel(
//                IdentificationCoordinator(
//                    AppCoordinator(object : StorageManagerType {
//                        override fun getIsFirstTimeUser(): Boolean = false
//                        override fun setIsNotFirstTimeUser() {}
//                    }),
//                    PreviewTrackerManager()
//                ),
//                PreviewTrackerManager()
//            )
//        )
//    }
}
