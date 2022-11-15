package de.digitalService.useID.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.digitalService.useID.analytics.TrackerManagerType
import de.digitalService.useID.getLogger
import de.digitalService.useID.idCardInterface.*
import de.digitalService.useID.ui.previewMocks.PreviewTrackerManager
import de.digitalService.useID.ui.screens.identification.IdentificationFetchMetadata
import de.digitalService.useID.ui.screens.identification.IdentificationFetchMetadataViewModel
import de.digitalService.useID.ui.screens.identification.IdentificationFetchMetadataViewModelInterface
import de.digitalService.useID.ui.theme.UseIDTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.openecard.mobile.activation.ActivationResultCode
import javax.inject.Inject

@Composable
fun PreviewIdentificationFetchMetadata(
    viewModel: PreviewIdentificationFetchMetadataViewModel,
    identificationFetchMetadataViewModelInterface: IdentificationFetchMetadataViewModelInterface = hiltViewModel<IdentificationFetchMetadataViewModel>()
) {
    Column(verticalArrangement = Arrangement.Top, modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.weight(1f)) {
            IdentificationFetchMetadata(modifier = Modifier.fillMaxHeight(0.9f), identificationFetchMetadataViewModelInterface)
        }

        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp)
        ) {
            Button(onClick = { viewModel.simulateSuccess() }) { Text("✅") }
            Button(onClick = { viewModel.simulateConnectionError() }) { Text("❌") }
        }
    }
}

@HiltViewModel
class PreviewIdentificationFetchMetadataViewModel @Inject constructor(
    private val idCardManager: IDCardManager,
    private val trackerManager: TrackerManagerType
) : ViewModel() {
    fun simulateSuccess() {
        viewModelScope.launch(Dispatchers.Main) {
            idCardManager.injectIdentifyEvent(
                EIDInteractionEvent.RequestAuthenticationRequestConfirmation(
                    EIDAuthenticationRequest(
                        "issuer",
                        "issueURL",
                        "subject",
                        "subjectURL",
                        "validity",
                        AuthenticationTerms.Text(""),
                        "transactionInfo",
                        readAttributes = IDCardAttribute.values().associateWith { true }
                    )
                ) {
                    viewModelScope.launch {
                        getLogger().value.debug("request pin")
                        idCardManager.injectIdentifyEvent(EIDInteractionEvent.RequestPIN(null) {
                            viewModelScope.launch {
                                idCardManager.injectIdentifyEvent(EIDInteractionEvent.RequestCardInsertion)
                            }
                        })
                    }
                }
            )
        }
    }

    fun simulateConnectionError() {
        viewModelScope.launch(Dispatchers.Main) {
            idCardManager.injectIdentifyException(IDCardInteractionException.ProcessFailed(ActivationResultCode.BAD_REQUEST, null, null))
        }
        trackerManager.trackEvent("identification", "loadingFailed", "attributes")
    }
}

@Preview(device = Devices.PIXEL_3A)
@Composable
fun PreviewPreviewIdentificationFetchMetadata() {
    UseIDTheme {
        PreviewIdentificationFetchMetadata(
            PreviewIdentificationFetchMetadataViewModel(IDCardManager(), PreviewTrackerManager()),
            object : IdentificationFetchMetadataViewModelInterface {
                override val shouldShowProgressIndicator: Boolean = false
                override fun fetchMetadata() {}
                override fun onCancelButtonTapped() {}
                override val didSetup: Boolean = false
            }
        )
    }
}
