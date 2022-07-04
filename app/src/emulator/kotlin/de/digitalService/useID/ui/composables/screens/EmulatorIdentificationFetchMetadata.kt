package de.digitalService.useID.ui.composables.screens

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
import androidx.lifecycle.viewmodel.compose.viewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import de.digitalService.useID.ui.AppCoordinator
import de.digitalService.useID.ui.composables.screens.identification.IdentificationFetchMetadata
import de.digitalService.useID.ui.composables.screens.identification.IdentificationFetchMetadata
import de.digitalService.useID.ui.composables.screens.identification.IdentificationFetchMetadataViewModelInterface
import de.digitalService.useID.ui.coordinators.IdentificationCoordinator
import de.digitalService.useID.ui.theme.UseIDTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@Composable
fun EmulatorIdentificationFetchMetadata(viewModel: EmulatorIdentificationFetchMetadataViewModel) {
    Column(verticalArrangement = Arrangement.Top, modifier = Modifier.fillMaxSize()) {
        IdentificationFetchMetadata(modifier = Modifier.fillMaxHeight(0.9f), viewModel.innerViewModel)
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
class EmulatorIdentificationFetchMetadataViewModel @Inject constructor(private val coordinator: IdentificationCoordinator) : ViewModel() {
    fun simulateSuccess() { coordinator.startIdentificationProcess() }
    fun simulateConnectionError() {
        viewModelScope.launch {
            innerViewModel.injectShouldShowProgress(false)
            innerViewModel.injectShouldShowError(true)
        }
    }

    val innerViewModel = object : IdentificationFetchMetadataViewModelInterfaceExtension {
        override var shouldShowProgressIndicator: Boolean by mutableStateOf(false)
        override var shouldShowError: Boolean by mutableStateOf(false)

        override fun fetchMetadata() {}
        override fun onErrorCancel() {}
        override fun onErrorRetry() {
            coordinator.startIdentificationProcess()
            injectShouldShowProgress(true)
            injectShouldShowError(false)
        }

        override fun injectShouldShowProgress(show: Boolean) {
            shouldShowProgressIndicator = show
        }

        override fun injectShouldShowError(show: Boolean) {
            shouldShowError = show
        }
    }

    interface IdentificationFetchMetadataViewModelInterfaceExtension : IdentificationFetchMetadataViewModelInterface {
        fun injectShouldShowProgress(show: Boolean)
        fun injectShouldShowError(show: Boolean)
    }
}

@Preview(device = Devices.PIXEL_3A)
@Composable
fun PreviewEmulatorIdentificationFetchMetadata() {
    UseIDTheme {
        EmulatorIdentificationFetchMetadata(
            EmulatorIdentificationFetchMetadataViewModel(
                IdentificationCoordinator(
                    AppCoordinator()
                )
            )
        )
    }
}
