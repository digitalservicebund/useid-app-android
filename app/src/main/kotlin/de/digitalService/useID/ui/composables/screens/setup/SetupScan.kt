package de.digitalService.useID.ui.composables.screens

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.digitalService.useID.R
import de.digitalService.useID.SecureStorageManagerInterface
import de.digitalService.useID.getLogger
import de.digitalService.useID.idCardInterface.EIDInteractionEvent
import de.digitalService.useID.idCardInterface.IDCardInteractionException
import de.digitalService.useID.idCardInterface.IDCardManager
import de.digitalService.useID.ui.composables.ScreenWithTopBar
import de.digitalService.useID.ui.coordinators.SetupCoordinator
import de.digitalService.useID.ui.theme.UseIDTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.annotation.Nullable
import javax.inject.Inject

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun SetupScan(viewModel: SetupScanViewModelInterface, modifier: Modifier = Modifier) {
    val context = LocalContext.current

    viewModel.errorState?.let {
        ErrorAlertDialog(error = it, onButtonTap = viewModel::onErrorDialogButtonTap)
    }

    AnimatedVisibility(
        visible = viewModel.attempts < 3,
        enter = scaleIn(tween(200)),
        exit = scaleOut(tween(100))
    ) {
        TransportPINDialog(
            attempts = viewModel.attempts,
            onCancel = viewModel::onCancel,
            onNewTransportPIN = { viewModel.onReEnteredTransportPIN(it, context) }
        )
    }

    Column(
        verticalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
            .fillMaxSize()
            .padding(vertical = 40.dp, horizontal = 20.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.eids),
            contentScale = ContentScale.Fit,
            contentDescription = ""
        )
        Text(
            stringResource(id = R.string.firstTimeUser_scan_title),
            style = MaterialTheme.typography.titleLarge
        )
        Text(
            stringResource(id = R.string.firstTimeUser_scan_body),
            style = MaterialTheme.typography.bodySmall
        )
        Button(
            onClick = viewModel::onHelpButtonTapped,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary
            ),
            shape = MaterialTheme.shapes.small,
            modifier = Modifier
                .height(40.dp)
        ) {
            Text(stringResource(id = R.string.firstTimeUser_scan_helpButton))
        }
    }

    LaunchedEffect(Unit) {
        viewModel.startSettingPIN(context)
    }
}

@Composable
private fun ErrorAlertDialog(error: SetupScanViewModelInterface.Error, onButtonTap: () -> Unit) {
    AlertDialog(
        onDismissRequest = { },
        confirmButton = {
            Button(onClick = onButtonTap) {
                Text(stringResource(id = R.string.firstTimeUser_scan_error_button))
            }
        },
        title = { Text(stringResource(id = error.titleResID)) },
        text = { Text(stringResource(id = error.textResID)) },
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    )
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
        ScreenWithTopBar(
            navigationIcon = {
                androidx.compose.material3.IconButton(onClick = onCancel) {
                    androidx.compose.material3.Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = stringResource(id = R.string.navigation_cancel)
                    )
                }
            },
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
    }
}

interface SetupScanViewModelInterface {
    sealed class Error {
        object PINSuspended : Error()
        object PINBlocked : Error()
        object IDDeactivated : Error()
        data class Other(val message: String?) : Error()

        val titleResID: Int
            get() {
                return when (this) {
                    PINSuspended -> R.string.firstTimeUser_scan_error_title_pin_suspended
                    PINBlocked -> R.string.firstTimeUser_scan_error_title_pin_blocked
                    IDDeactivated -> R.string.firstTimeUser_scan_error_title_id_deactivated
                    is Other -> R.string.firstTimeUser_scan_error_title_unknown
                }
            }

        val textResID: Int
            get() {
                return when (this) {
                    PINSuspended, PINBlocked, IDDeactivated -> R.string.firstTimeUser_scan_error_text_feature_unavailable
                    is Other -> R.string.firstTimeUser_scan_error_text_unknown
                }
            }
    }

    val attempts: Int
    val errorState: Error?

    fun startSettingPIN(context: Context)
    fun onReEnteredTransportPIN(transportPIN: String, context: Context)
    fun onHelpButtonTapped()

    fun onErrorDialogButtonTap()
    fun onCancel()
}

@HiltViewModel
class SetupScanViewModel @Inject constructor(
    private val coordinator: SetupCoordinator,
    private val secureStorageManager: SecureStorageManagerInterface,
    private val idCardManager: IDCardManager,
    @Nullable coroutineScope: CoroutineScope? = null
) :
    ViewModel(), SetupScanViewModelInterface {
    private val logger by getLogger()

    private val viewModelCoroutineScope: CoroutineScope = coroutineScope ?: viewModelScope
    private var firstPINCallback: Boolean = true

    override var attempts: Int by mutableStateOf(3)
        private set

    override var errorState: SetupScanViewModelInterface.Error? by mutableStateOf(null)
        private set

    override fun startSettingPIN(context: Context) {
        val transportPIN = secureStorageManager.loadTransportPIN() ?: run {
            logger.error("Transport PIN not available.")
            errorState = SetupScanViewModelInterface.Error.Other(null)
            return
        }
        executePINManagement(transportPIN, context)
    }

    override fun onReEnteredTransportPIN(transportPIN: String, context: Context) {
        firstPINCallback = true
        attempts = 3
        executePINManagement(transportPIN, context)
    }

    override fun onHelpButtonTapped() {}

    override fun onErrorDialogButtonTap() {
        coordinator.cancelSetup()
    }

    override fun onCancel() = coordinator.cancelSetup()

    private fun executePINManagement(transportPIN: String, context: Context) {
        val newPIN = secureStorageManager.loadPersonalPIN() ?: run {
            logger.error("Personal PIN not available.")
            errorState = SetupScanViewModelInterface.Error.Other(null)
            return
        }

        viewModelCoroutineScope.launch {
            logger.debug("Starting PIN management flow.")
            idCardManager.changePin(context).catch { exception ->
                errorState = when (exception) {
                    is IDCardInteractionException.CardDeactivated -> SetupScanViewModelInterface.Error.IDDeactivated
                    is IDCardInteractionException.CardBlocked -> SetupScanViewModelInterface.Error.PINBlocked
                    else -> SetupScanViewModelInterface.Error.Other(exception.message)
                }
            }.collect { event ->
                when (event) {
                    EIDInteractionEvent.CardInteractionComplete -> logger.debug("Card interaction complete.")
                    EIDInteractionEvent.CardRecognized -> logger.debug("Card recognized.")
                    EIDInteractionEvent.CardRemoved -> logger.debug("Card removed.")
                    EIDInteractionEvent.AuthenticationStarted -> logger.debug("Authentication started.")
                    EIDInteractionEvent.PINManagementStarted -> {
                        logger.debug("PIN management started.")
                    }
                    EIDInteractionEvent.ProcessCompletedSuccessfully -> {
                        logger.debug("Process completed successfully.")
                        coordinator.onSettingPINSucceeded()
                    }
                    EIDInteractionEvent.RequestCardInsertion -> logger.debug("Card insertion requested.")
                    is EIDInteractionEvent.RequestChangedPIN -> {
                        if (firstPINCallback) {
                            logger.debug("Changed PIN requested. Entering transport PIN and personal PIN")
                            event.pinCallback(transportPIN, newPIN)
                            firstPINCallback = false
                        } else {
                            logger.debug("Old and new PIN requested for a second time. The Transport-PIN seems to be incorrect.")
                            attempts = event.attempts ?: run {
                                logger.error("Old and new PIN requested without attempts.")
                                cancel()
                                return@collect
                            }
                            cancel()
                        }
                    }
                    is EIDInteractionEvent.RequestCANAndChangedPIN -> {
                        errorState =
                            SetupScanViewModelInterface.Error.PINSuspended
                        cancel()
                    }
                    is EIDInteractionEvent.RequestPUK -> {
                        errorState =
                            SetupScanViewModelInterface.Error.PINBlocked
                        cancel()
                    }
                    else -> {
                        logger.debug("Collected unexpected event: $event")
                        errorState = SetupScanViewModelInterface.Error.Other(null)
                        cancel()
                    }
                }
            }
        }
    }
}

class PreviewSetupScanViewModel(
    override val attempts: Int,
    override val errorState: SetupScanViewModelInterface.Error?
) :
    SetupScanViewModelInterface {
    override fun startSettingPIN(context: Context) {}
    override fun onReEnteredTransportPIN(transportPIN: String, context: Context) {}
    override fun onHelpButtonTapped() {}
    override fun onCancel() {}
    override fun onErrorDialogButtonTap() {}
}

@Preview(device = Devices.PIXEL_3A)
@Composable
fun PreviewSetupScanWithoutError() {
    UseIDTheme {
        SetupScan(PreviewSetupScanViewModel(3, errorState = null))
    }
}

@Preview(device = Devices.PIXEL_3A, showSystemUi = true)
@Preview(device = Devices.PIXEL_4_XL, showSystemUi = true)
@Composable
fun PreviewSetupScanInvalidTransportPIN() {
    UseIDTheme {
        SetupScan(PreviewSetupScanViewModel(2, errorState = null))
    }
}

@Preview(device = Devices.PIXEL_3A)
@Composable
fun PreviewSetupScanWithError() {
    UseIDTheme {
        SetupScan(
            PreviewSetupScanViewModel(
                3,
                errorState = SetupScanViewModelInterface.Error.PINSuspended
            )
        )
    }
}
