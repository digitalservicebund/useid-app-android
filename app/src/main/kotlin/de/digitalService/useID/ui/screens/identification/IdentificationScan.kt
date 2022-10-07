package de.digitalService.useID.ui.screens.identification

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.AlertDialog
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ramcosta.composedestinations.annotation.Destination
import dagger.hilt.android.lifecycle.HiltViewModel
import de.digitalService.useID.R
import de.digitalService.useID.analytics.TrackerManagerType
import de.digitalService.useID.models.ScanError
import de.digitalService.useID.ui.components.NavigationButton
import de.digitalService.useID.ui.components.NavigationIcon
import de.digitalService.useID.ui.components.ScreenWithTopBar
import de.digitalService.useID.ui.coordinators.IdentificationCoordinator
import de.digitalService.useID.ui.screens.ScanScreen
import de.digitalService.useID.ui.theme.Blue600
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
    val context = LocalContext.current
    var showCancelDialog by remember { mutableStateOf(false) }

    ScreenWithTopBar(
        navigationButton = NavigationButton(
            icon = NavigationIcon.Cancel,
            onClick = { showCancelDialog = true }
        )
    ) { topPadding ->
        ScanScreen(
            title = stringResource(id = R.string.identification_scan_title),
            body = stringResource(id = R.string.identification_scan_body),
            errorState = viewModel.errorState,
            onIncorrectPIN = { attempts ->
                PINDialog(
                    attempts = attempts,
                    onCancel = { showCancelDialog = true },
                    onNewPINEntered = viewModel::onNewPersonalPINEntered
                )
            },
            onErrorDialogButtonTap = { viewModel.onErrorDialogButtonTapped(context) },
            onHelpDialogShown = viewModel::onHelpButtonTapped,
            showProgress = viewModel.shouldShowProgress,
            modifier = modifier.padding(top = topPadding)
        )
    }

    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            properties = DialogProperties(),
            title = {
                Text(
                    text = stringResource(R.string.identification_scan_cancelDialog_title),
                    style = MaterialTheme.typography.titleMedium
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.identification_scan_cancelDialog_body)
                )
            },
            confirmButton = {
                TextButton(onClick = viewModel::onCancelIdentification) {
                    Text(
                        text = stringResource(R.string.identification_scan_cancelDialog_confirm),
                        color = Blue600
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) {
                    Text(
                        text = stringResource(R.string.identification_scan_cancelDialog_dismiss),
                        color = Blue600
                    )
                }
            }
        )
    }
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
            navigationButton = NavigationButton(NavigationIcon.Cancel, onClick = onCancel),
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
    fun onNfcButtonTapped()
    fun onErrorDialogButtonTapped(context: Context)
    fun onCancelIdentification()
    fun onNewPersonalPINEntered(pin: String)
}

@HiltViewModel
class IdentificationScanViewModel @Inject constructor(
    private val coordinator: IdentificationCoordinator,
    private val coroutineContextProvider: CoroutineContextProviderType,
    private val trackerManager: TrackerManagerType
) : ViewModel(), IdentificationScanViewModelInterface {
    override var shouldShowProgress: Boolean by mutableStateOf(false)
        private set

    override var errorState: ScanError? by mutableStateOf(null)
        private set

    init {
        collectScanEvents()
    }

    override fun onHelpButtonTapped() {
        trackerManager.trackScreen("identification/scanHelp")
    }

    override fun onNfcButtonTapped() {
        trackerManager.trackEvent("identification", "alertShown", "NFCInfo")
    }

    override fun onErrorDialogButtonTapped(context: Context) {
        errorState?.let { error ->
            if (error is ScanError.CardErrorWithRedirect) {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(error.redirectUrl))
                ContextCompat.startActivity(context, intent, null)
            }
        }

        coordinator.cancelIdentification()
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
            coordinator.scanEventFlow.collect { event: ScanEvent ->
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
    override fun onNfcButtonTapped() {}
    override fun onErrorDialogButtonTapped(context: Context) {}
    override fun onCancelIdentification() {}
    override fun onNewPersonalPINEntered(pin: String) {}
}

@Preview(showBackground = true)
@Composable
fun PreviewIdentificationScan() {
    UseIDTheme {
        IdentificationScan(viewModel = PreviewIdentificationScanViewModel(false, null))
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewIdentificationScanWithProgress() {
    UseIDTheme {
        IdentificationScan(viewModel = PreviewIdentificationScanViewModel(true, null))
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewIdentificationScanWithError() {
    UseIDTheme {
        IdentificationScan(viewModel = PreviewIdentificationScanViewModel(true, ScanError.CardDeactivated))
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewIdentificationScanWithCancelDialog() {
    UseIDTheme {
        IdentificationScan(viewModel = PreviewIdentificationScanViewModel(false, null))
    }
}
