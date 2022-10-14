package de.digitalService.useID.ui.screens.setup

import android.content.Context
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
import androidx.compose.ui.tooling.preview.Devices
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
import de.digitalService.useID.analytics.IssueTrackerManagerType
import de.digitalService.useID.analytics.TrackerManagerType
import de.digitalService.useID.getLogger
import de.digitalService.useID.idCardInterface.EIDInteractionEvent
import de.digitalService.useID.idCardInterface.IDCardInteractionException
import de.digitalService.useID.idCardInterface.IDCardManager
import de.digitalService.useID.models.ScanError
import de.digitalService.useID.ui.components.NavigationButton
import de.digitalService.useID.ui.components.NavigationIcon
import de.digitalService.useID.ui.components.ScreenWithTopBar
import de.digitalService.useID.ui.coordinators.SetupCoordinator
import de.digitalService.useID.ui.screens.ScanScreen
import de.digitalService.useID.ui.theme.Blue600
import de.digitalService.useID.ui.theme.UseIDTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.annotation.Nullable
import javax.inject.Inject

@Destination
@Composable
fun SetupScan(
    modifier: Modifier = Modifier,
    viewModel: SetupScanViewModelInterface = hiltViewModel<SetupScanViewModel>()
) {
    val context = LocalContext.current

    ScreenWithTopBar(
        navigationButton = NavigationButton(icon = NavigationIcon.Back, onClick = viewModel::onBackButtonTapped)
    ) { topPadding ->
        ScanScreen(
            title = stringResource(id = R.string.firstTimeUser_scan_title),
            body = stringResource(id = R.string.firstTimeUser_scan_body),
            errorState = viewModel.errorState,
            onIncorrectPIN = { attempts ->
                TransportPINDialog(
                    attempts = attempts,
                    onCancel = viewModel::onCancelConfirm,
                    onNewTransportPIN = { viewModel.onReEnteredTransportPIN(it, context) }
                )
            },
            onErrorDialogButtonTap = viewModel::onCancelConfirm,
            onHelpDialogShown = viewModel::onHelpButtonTapped,
            onNfcDialogShown = viewModel::onNfcButtonTapped,
            showProgress = viewModel.shouldShowProgress,
            modifier = modifier.padding(top = topPadding)
        )
    }

    LaunchedEffect(Unit) {
        viewModel.startSettingPIN(context)
    }
}

@Composable
private fun TransportPINDialog(
    attempts: Int,
    onCancel: () -> Unit,
    onNewTransportPIN: (String) -> Unit
) {
    Dialog(
        onDismissRequest = { },
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        var showCancelDialog by remember { mutableStateOf(false) }

        ScreenWithTopBar(
            navigationButton = NavigationButton(NavigationIcon.Cancel) { showCancelDialog = true },
            modifier = Modifier.height(500.dp)
        ) { topPadding ->
            val focusManager = LocalFocusManager.current

            SetupReEnterTransportPIN(
                viewModel = SetupReEnterTransportPINViewModel(
                    attempts,
                    onNewTransportPIN
                ),
                modifier = Modifier.padding(top = topPadding)
            )

            LaunchedEffect(Unit) {
                delay(100L) // Workaround for https://issuetracker.google.com/issues/204502668
                focusManager.moveFocus(FocusDirection.Next)
            }
        }

        if (showCancelDialog) {
            AlertDialog(
                onDismissRequest = { showCancelDialog = false },
                properties = DialogProperties(),
                title = {
                    Text(
                        text = stringResource(R.string.firstTimeUser_scan_cancelDialog_title),
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                text = {
                    Text(text = stringResource(R.string.firstTimeUser_scan_cancelDialog_body))
                },
                confirmButton = {
                    TextButton(onClick = onCancel) {
                        Text(
                            text = stringResource(R.string.firstTimeUser_scan_cancelDialog_confirm),
                            color = Blue600
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCancelDialog = false }) {
                        Text(
                            text = stringResource(R.string.firstTimeUser_scan_cancelDialog_dismiss),
                            color = Blue600
                        )
                    }
                }
            )
        }
    }
}

interface SetupScanViewModelInterface {
    val shouldShowProgress: Boolean
    val errorState: ScanError?

    fun startSettingPIN(context: Context)
    fun onReEnteredTransportPIN(transportPIN: String, context: Context)
    fun onHelpButtonTapped()
    fun onNfcButtonTapped()
    fun onBackButtonTapped()
    fun onCancelConfirm()
}

@HiltViewModel
class SetupScanViewModel @Inject constructor(
    private val coordinator: SetupCoordinator,
    private val idCardManager: IDCardManager,
    private val trackerManager: TrackerManagerType,
    private val issueTrackerManager: IssueTrackerManagerType,
    @Nullable coroutineScope: CoroutineScope? = null
) :
    ViewModel(), SetupScanViewModelInterface {
    private val logger by getLogger()

    private val viewModelCoroutineScope: CoroutineScope = coroutineScope ?: viewModelScope
    private var firstPINCallback: Boolean = true

    override var shouldShowProgress: Boolean by mutableStateOf(false)
        private set

    override var errorState: ScanError? by mutableStateOf(null)
        private set

    override fun startSettingPIN(context: Context) {
        val transportPIN = coordinator.transportPin ?: run {
            logger.error("Transport PIN not available.")
            errorState = ScanError.Other(null)
            return
        }
        executePINManagement(transportPIN, context)
    }

    override fun onReEnteredTransportPIN(transportPIN: String, context: Context) {
        firstPINCallback = true
        executePINManagement(transportPIN, context)
    }

    override fun onHelpButtonTapped() {
        trackerManager.trackScreen("firstTimeUser/scanHelp")
    }

    override fun onNfcButtonTapped() {
        trackerManager.trackEvent("firstTimeUser", "alertShown", "NFCInfo")
    }

    override fun onBackButtonTapped() {
        idCardManager.cancelTask()
        coordinator.onBackTapped()
    }

    override fun onCancelConfirm() {
        idCardManager.cancelTask()
        coordinator.cancelSetup()
    }

    private fun finishSetup() {
        coordinator.onSettingPINSucceeded()
    }

    private fun executePINManagement(transportPIN: String, context: Context) {
        val newPIN = coordinator.personalPin ?: run {
            logger.error("Personal PIN not available.")
            errorState = ScanError.Other(null)
            return
        }

        viewModelCoroutineScope.launch {
            logger.debug("Starting PIN management flow.")
            idCardManager.changePin(context).catch { exception ->
                errorState = when (exception) {
                    is IDCardInteractionException.CardDeactivated -> {
                        trackerManager.trackScreen("firstTimeUser/cardDeactivated")
                        ScanError.CardDeactivated
                    }
                    is IDCardInteractionException.CardBlocked -> ScanError.PINBlocked
                    else -> {
                        trackerManager.trackScreen("firstTimeUser/cardUnreadable")

                        (exception as? IDCardInteractionException)?.redacted?.let {
                            issueTrackerManager.capture(it)
                        }

                        ScanError.Other(exception.message)
                    }
                }
            }.collect { event ->
                when (event) {
                    EIDInteractionEvent.CardInteractionComplete -> logger.debug("Card interaction complete.")
                    EIDInteractionEvent.CardRecognized -> {
                        logger.debug("Card recognized.")
                        shouldShowProgress = true
                    }
                    EIDInteractionEvent.CardRemoved -> {
                        logger.debug("Card removed.")
                        shouldShowProgress = false
                    }
                    EIDInteractionEvent.AuthenticationStarted -> logger.debug("Authentication started.")
                    EIDInteractionEvent.PINManagementStarted -> {
                        logger.debug("PIN management started.")
                    }
                    is EIDInteractionEvent.ProcessCompletedSuccessfullyWithoutResult -> {
                        logger.debug("Process completed successfully.")
                        finishSetup()
                    }
                    EIDInteractionEvent.RequestCardInsertion -> logger.debug("Card insertion requested.")
                    is EIDInteractionEvent.RequestChangedPIN -> {
                        if (firstPINCallback) {
                            logger.debug("Changed PIN requested. Entering transport PIN and personal PIN")
                            event.pinCallback(transportPIN, newPIN)
                            firstPINCallback = false
                        } else {
                            logger.debug("Old and new PIN requested for a second time. The Transport-PIN seems to be incorrect.")
                            val attempts = event.attempts ?: run {
                                logger.error("Old and new PIN requested without attempts.")
                                cancel()
                                return@collect
                            }
                            errorState = ScanError.IncorrectPIN(attempts)
                            trackerManager.trackScreen("firstTimeUser/incorrectTransportPIN")
                            cancel()
                        }
                    }
                    is EIDInteractionEvent.RequestCANAndChangedPIN -> {
                        errorState = ScanError.PINSuspended
                        trackerManager.trackScreen("firstTimeUser/cardSuspended")
                        cancel()
                    }
                    is EIDInteractionEvent.RequestPUK -> {
                        errorState = ScanError.PINBlocked
                        trackerManager.trackScreen("firstTimeUser/cardBlocked")
                        cancel()
                    }
                    else -> {
                        logger.debug("Collected unexpected event: $event")
                        errorState = ScanError.Other(null)
                        issueTrackerManager.capture(event.redacted)
                        cancel()
                    }
                }
            }
        }
    }
}

class PreviewSetupScanViewModel(
    override val shouldShowProgress: Boolean,
    override val errorState: ScanError?
) :
    SetupScanViewModelInterface {
    override fun startSettingPIN(context: Context) {}
    override fun onReEnteredTransportPIN(transportPIN: String, context: Context) {}
    override fun onHelpButtonTapped() {}
    override fun onNfcButtonTapped() {}
    override fun onBackButtonTapped() {}
    override fun onCancelConfirm() {}
}

@Preview(device = Devices.PIXEL_3A)
@Composable
fun PreviewSetupScanWithoutError() {
    UseIDTheme {
        SetupScan(viewModel = PreviewSetupScanViewModel(false, errorState = null))
    }
}

@Preview(device = Devices.PIXEL_3A, showSystemUi = true)
@Preview(device = Devices.PIXEL_4_XL, showSystemUi = true)
@Composable
fun PreviewSetupScanInvalidTransportPIN() {
    UseIDTheme {
        SetupScan(viewModel = PreviewSetupScanViewModel(false, errorState = ScanError.IncorrectPIN(2)))
    }
}

@Preview(device = Devices.PIXEL_3A)
@Composable
fun PreviewSetupScanWithError() {
    UseIDTheme {
        SetupScan(
            viewModel = PreviewSetupScanViewModel(
                false,
                ScanError.PINSuspended
            )
        )
    }
}
