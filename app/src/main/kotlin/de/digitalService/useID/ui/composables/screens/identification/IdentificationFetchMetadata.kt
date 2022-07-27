package de.digitalService.useID.ui.composables.screens.identification

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.*
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootNavGraph
import dagger.hilt.android.lifecycle.HiltViewModel
import de.digitalService.useID.R
import de.digitalService.useID.ui.composables.ScreenWithTopBar
import de.digitalService.useID.ui.composables.screens.destinations.IdentificationFetchMetadataDestination
import de.digitalService.useID.ui.composables.screens.destinations.IdentificationSuccessDestination
import de.digitalService.useID.ui.coordinators.IdentificationCoordinator
import de.digitalService.useID.ui.theme.UseIDTheme
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalAnimationApi::class)
@Destination(
    navArgsDelegate = IdentificationFetchMetadataNavArgs::class
)
@Composable
fun IdentificationFetchMetadata(
    modifier: Modifier = Modifier,
    viewModel: IdentificationFetchMetadataViewModelInterface = hiltViewModel<IdentificationFetchMetadataViewModel>()
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .padding(20.dp)
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

    AnimatedVisibility(
        visible = viewModel.shouldShowError,
        enter = scaleIn(tween(200)),
        exit = scaleOut(tween(100))
    ) {
        ConnectionErrorDialog(
            onCancel = viewModel::onErrorCancel,
            onRetry = viewModel::onErrorRetry
        )
    }

    LaunchedEffect(Unit) {
        viewModel.fetchMetadata()
    }
}

@Composable
fun ConnectionErrorDialog(
    onCancel: () -> Unit,
    onRetry: () -> Unit
) {
    Dialog(
        onDismissRequest = { },
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        ScreenWithTopBar(
            navigationIcon = {
                IconButton(onClick = onCancel) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = stringResource(id = R.string.navigation_cancel)
                    )
                }
            },
            modifier = Modifier.fillMaxSize().padding(vertical = 20.dp)
        ) { topPadding ->
            IdentificationFetchMetadataError(
                onRetry = onRetry,
                modifier = Modifier.padding(top = topPadding, start = 20.dp, end = 20.dp)
            )
        }
    }
}

data class IdentificationFetchMetadataNavArgs(
    val tcTokenURL: String
)

enum class FetchMetadataEvent {
    Started, Finished, Error
}

interface IdentificationFetchMetadataViewModelInterface {
    val shouldShowProgressIndicator: Boolean
    val shouldShowError: Boolean

    fun fetchMetadata()
    fun onErrorCancel()
    fun onErrorRetry()
}

@HiltViewModel
class IdentificationFetchMetadataViewModel @Inject constructor(
    private val coordinator: IdentificationCoordinator,
    savedStateHandle: SavedStateHandle
) : ViewModel(), IdentificationFetchMetadataViewModelInterface {
    override var shouldShowProgressIndicator: Boolean by mutableStateOf(false)
        private set

    override var shouldShowError: Boolean by mutableStateOf(false)
        private set

    private val tcTokenURL: String

    init {
        tcTokenURL = IdentificationFetchMetadataDestination.argsFrom(savedStateHandle).tcTokenURL
        collectFetchMetadataEvents()
    }

    override fun fetchMetadata() {
        coordinator.startIdentificationProcess(tcTokenURL)
    }

    override fun onErrorCancel() {
        coordinator.cancelIdentification()
    }

    override fun onErrorRetry() {
        coordinator.startIdentificationProcess(tcTokenURL)
    }

    private fun collectFetchMetadataEvents() {
        viewModelScope.launch {
            coordinator.fetchMetadataEventFlow.collect { event ->
                when (event) {
                    FetchMetadataEvent.Started -> {
                        shouldShowProgressIndicator = true
                        shouldShowError = false
                    }
                    FetchMetadataEvent.Finished -> shouldShowProgressIndicator = false
                    FetchMetadataEvent.Error -> {
                        shouldShowProgressIndicator = false
                        shouldShowError = true
                    }
                }
            }
        }
    }
}

class PreviewIdentificationFetchMetadataViewModel(
    override val shouldShowProgressIndicator: Boolean,
    override val shouldShowError: Boolean
) : IdentificationFetchMetadataViewModelInterface {
    override fun fetchMetadata() {}
    override fun onErrorCancel() {}
    override fun onErrorRetry() {}
}

@Preview(device = Devices.PIXEL_3A)
@Composable
fun PreviewIdentificationFetchMetadata() {
    UseIDTheme {
        IdentificationFetchMetadata(
            viewModel = PreviewIdentificationFetchMetadataViewModel(
                shouldShowProgressIndicator = true,
                shouldShowError = false
            )
        )
    }
}
