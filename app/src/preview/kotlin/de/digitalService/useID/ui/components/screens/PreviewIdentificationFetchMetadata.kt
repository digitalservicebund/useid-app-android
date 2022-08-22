package de.digitalService.useID.ui.components.screens

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
import de.digitalService.useID.analytics.MockTrackerManager
import de.digitalService.useID.ui.coordinators.AppCoordinator
import de.digitalService.useID.ui.coordinators.IdentificationCoordinator
import de.digitalService.useID.ui.screens.identification.IdentificationFetchMetadata
import de.digitalService.useID.ui.screens.identification.IdentificationFetchMetadataViewModelInterface
import de.digitalService.useID.ui.theme.UseIDTheme
import kotlinx.coroutines.launch
import javax.inject.Inject

@Composable
fun PreviewIdentificationFetchMetadata(viewModel: PreviewIdentificationFetchMetadataViewModel) {
    Column(verticalArrangement = Arrangement.Top, modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.weight(1f)) {
            IdentificationFetchMetadata(modifier = Modifier.fillMaxHeight(0.9f), viewModel.innerViewModel)
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
class PreviewIdentificationFetchMetadataViewModel @Inject constructor(private val coordinator: IdentificationCoordinator) : ViewModel() {
    fun simulateSuccess() {
        coordinator.startIdentificationProcess("")
    }

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
        override fun onErrorRetry() {
            injectShouldShowProgress(true)
            injectShouldShowError(false)
        }

        override fun onCancelButtonTapped() = coordinator.cancelIdentification()

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
fun PreviewPreviewIdentificationFetchMetadata() {
    UseIDTheme {
        PreviewIdentificationFetchMetadata(
            PreviewIdentificationFetchMetadataViewModel(
                IdentificationCoordinator(
                    AppCoordinator((object : StorageManagerType {
                        override fun getIsFirstTimeUser(): Boolean = false
                        override fun setIsNotFirstTimeUser() {}
                    }))
                , MockTrackerManager())
            )
        )
    }
}
