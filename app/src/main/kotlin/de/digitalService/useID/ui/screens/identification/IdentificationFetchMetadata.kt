package de.digitalService.useID.ui.screens.identification

import androidx.compose.foundation.layout.*
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ramcosta.composedestinations.annotation.Destination
import dagger.hilt.android.lifecycle.HiltViewModel
import de.digitalService.useID.R
import de.digitalService.useID.ui.components.NavigationButton
import de.digitalService.useID.ui.components.NavigationIcon
import de.digitalService.useID.ui.components.ScreenWithTopBar
import de.digitalService.useID.ui.coordinators.IdentificationCoordinator
import de.digitalService.useID.ui.screens.destinations.IdentificationFetchMetadataDestination
import de.digitalService.useID.ui.theme.UseIDTheme
import kotlinx.coroutines.launch
import javax.inject.Inject

@Destination(
    navArgsDelegate = IdentificationFetchMetadataNavArgs::class
)
@Composable
fun IdentificationFetchMetadata(
    modifier: Modifier = Modifier,
    viewModel: IdentificationFetchMetadataViewModelInterface = hiltViewModel<IdentificationFetchMetadataViewModel>()
) {
    ScreenWithTopBar(
        navigationButton = NavigationButton(
            icon = if (viewModel.didSetup) NavigationIcon.Back else NavigationIcon.Cancel,
            shouldShowConfirmDialog = !viewModel.didSetup,
            onClick = viewModel::onCancelButtonTapped
        )
    ) { topPadding ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier
                .padding(20.dp)
                .padding(top = topPadding)
                .fillMaxSize()
        ) {
            Spacer(modifier = Modifier.weight(.5f))
            Box {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 10.dp,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(70.dp)
                )
            }
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(.5f)
            ) {
                Text(
                    stringResource(id = R.string.identification_fetchMetadata_pleaseWait),
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    stringResource(id = R.string.identification_fetchMetadata_loadingData),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.fetchMetadata()
    }
}

data class IdentificationFetchMetadataNavArgs(
    val tcTokenURL: String,
    val didSetup: Boolean
)

enum class FetchMetadataEvent {
    Started, Finished, Error
}

interface IdentificationFetchMetadataViewModelInterface {
    val shouldShowProgressIndicator: Boolean

    fun fetchMetadata()
    fun onCancelButtonTapped()
    val didSetup: Boolean
}

@HiltViewModel
class IdentificationFetchMetadataViewModel @Inject constructor(
    private val coordinator: IdentificationCoordinator,
    savedStateHandle: SavedStateHandle
) : ViewModel(), IdentificationFetchMetadataViewModelInterface {
    override var shouldShowProgressIndicator: Boolean by mutableStateOf(false)
        private set

    override val didSetup: Boolean
    private val tcTokenURL: String

    init {
        tcTokenURL = IdentificationFetchMetadataDestination.argsFrom(savedStateHandle).tcTokenURL
        didSetup = IdentificationFetchMetadataDestination.argsFrom(savedStateHandle).didSetup
        collectFetchMetadataEvents()
    }

    override fun fetchMetadata() {
        coordinator.startIdentificationProcess(tcTokenURL, didSetup)
    }

    override fun onCancelButtonTapped() {
        coordinator.cancelIdentification()
    }

    private fun collectFetchMetadataEvents() {
        viewModelScope.launch {
            coordinator.fetchMetadataEventFlow.collect { event: FetchMetadataEvent ->
                shouldShowProgressIndicator = when (event) {
                    FetchMetadataEvent.Started -> true
                    FetchMetadataEvent.Finished -> false
                    FetchMetadataEvent.Error -> false
                }
            }
        }
    }
}

class PreviewIdentificationFetchMetadataViewModel(
    override val shouldShowProgressIndicator: Boolean
) : IdentificationFetchMetadataViewModelInterface {
    override fun fetchMetadata() {}
    override fun onCancelButtonTapped() {}
    override val didSetup: Boolean = false
}

@Preview(device = Devices.PIXEL_3A, showBackground = true)
@Composable
fun PreviewIdentificationFetchMetadata() {
    UseIDTheme {
        IdentificationFetchMetadata(
            viewModel = PreviewIdentificationFetchMetadataViewModel(
                shouldShowProgressIndicator = false
            )
        )
    }
}
