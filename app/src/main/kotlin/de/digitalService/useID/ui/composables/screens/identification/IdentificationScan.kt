package de.digitalService.useID.ui.composables.screens.identification

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ramcosta.composedestinations.annotation.Destination
import dagger.hilt.android.lifecycle.HiltViewModel
import de.digitalService.useID.R
import de.digitalService.useID.ui.ScanError
import de.digitalService.useID.ui.composables.ScreenWithTopBar
import de.digitalService.useID.ui.composables.screens.ScanScreen
import de.digitalService.useID.ui.coordinators.IdentificationCoordinator
import de.digitalService.useID.ui.theme.UseIDTheme
import de.digitalService.useID.util.CoroutineContextProviderType
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@Destination
@Composable
fun IdentificationScan(
    modifier: Modifier = Modifier,
    viewModel: IdentificationScanViewModelInterface = hiltViewModel<IdentificationScanViewModel>()
) {
    ScanScreen(
        title = stringResource(id = R.string.identification_scan_title),
        body = stringResource(id = R.string.identification_scan_body),
        errorState = viewModel.errorState,
        onIncorrectPIN = { attempts ->
            PINDialog(
                attempts = attempts,
                onCancel = viewModel::onCancelIdentification,
                onNewPINEntered = viewModel::onNewPersonalPINEntered
            )
        },
        onCancel = viewModel::onCancelIdentification,
        showProgress = viewModel.shouldShowProgress,
        modifier = modifier
    )
}

@Composable
private fun PINDialog(
    attempts: Int,
    onCancel: () -> Unit,
    onNewPINEntered: (String) -> Unit
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
            modifier = Modifier.height(500.dp)
        ) { topPadding ->
            val focusManager = LocalFocusManager.current

            val viewModel = IdentificationReEnterPersonalPINViewModel(attempts, onNewPINEntered)
            IdentificationReEnterPersonalPIN(modifier = Modifier.padding(top = topPadding), viewModel = viewModel)

            LaunchedEffect(Unit) {
                delay(100L) // Workaround for https://issuetracker.google.com/issues/204502668
                focusManager.moveFocus(FocusDirection.Next)
            }
        }
    }
}

sealed class ScanEvent {
    object CardRequested : ScanEvent()
    object CardAttached : ScanEvent()
    object Finished : ScanEvent()
    data class Error(val error: ScanError) : ScanEvent()
}

interface IdentificationScanViewModelInterface {
    val shouldShowProgress: Boolean
    val errorState: ScanError?

    fun onHelpButtonTapped()
    fun onCancelIdentification()
    fun onNewPersonalPINEntered(pin: String)
}

@HiltViewModel
class IdentificationScanViewModel @Inject constructor(
    private val coordinator: IdentificationCoordinator,
    private val coroutineContextProvider: CoroutineContextProviderType
) : ViewModel(), IdentificationScanViewModelInterface {
    override var shouldShowProgress: Boolean by mutableStateOf(false)
        private set
    override var errorState: ScanError? by mutableStateOf(null)
        private set

    init {
        collectScanEvents()
    }

    override fun onHelpButtonTapped() {
    }

    override fun onCancelIdentification() {
        coordinator.cancelIdentification()
    }

    override fun onNewPersonalPINEntered(pin: String) {
        errorState = null
        coordinator.onPINEntered(pin)
    }

    private fun collectScanEvents() {
        viewModelScope.launch(coroutineContextProvider.IO) {
            coordinator.scanEventFlow.collect { event ->
                launch(coroutineContextProvider.Main) {
                    when (event) {
                        ScanEvent.CardRequested -> shouldShowProgress = false
                        ScanEvent.CardAttached -> shouldShowProgress = true
                        ScanEvent.Finished -> shouldShowProgress = false
                        is ScanEvent.Error -> {
                            shouldShowProgress = false
                            errorState = event.error
                        }
                    }
                }
            }
        }
    }
}

private class PreviewIdentificationScanViewModel(
    override val shouldShowProgress: Boolean,
    override val errorState: ScanError?
) : IdentificationScanViewModelInterface {
    override fun onHelpButtonTapped() {}
    override fun onCancelIdentification() {}
    override fun onNewPersonalPINEntered(pin: String) {}
}

@Preview
@Composable
fun PreviewIdentificationScan() {
    UseIDTheme {
        IdentificationScan(viewModel = PreviewIdentificationScanViewModel(false, null))
    }
}

@Preview
@Composable
fun PreviewIdentificationScanWithProgress() {
    UseIDTheme {
        IdentificationScan(viewModel = PreviewIdentificationScanViewModel(true, null))
    }
}

@Preview
@Composable
fun PreviewIdentificationScanWithError() {
    UseIDTheme {
        IdentificationScan(viewModel = PreviewIdentificationScanViewModel(true, ScanError.CardDeactivated))
    }
}
