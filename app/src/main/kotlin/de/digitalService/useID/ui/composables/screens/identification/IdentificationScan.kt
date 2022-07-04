package de.digitalService.useID.ui.composables.screens.identification

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
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
import de.digitalService.useID.getLogger
import de.digitalService.useID.ui.composables.ScreenWithTopBar
import de.digitalService.useID.ui.composables.screens.SetupReEnterTransportPIN
import de.digitalService.useID.ui.composables.screens.SetupReEnterTransportPINViewModel
import de.digitalService.useID.ui.coordinators.IdentificationCoordinator
import de.digitalService.useID.ui.theme.UseIDTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalAnimationApi::class)
@Destination
@Composable
fun IdentificationScan(modifier: Modifier = Modifier, viewModel: IdentificationScanViewModelInterface = hiltViewModel<IdentificationScanViewModel>()) {
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
                stringResource(id = R.string.identification_scan_title),
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                stringResource(id = R.string.identification_scan_body),
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

    AnimatedVisibility(visible = viewModel.shouldShowProgress) {
        Dialog(onDismissRequest = {}) {
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .background(
                        color = MaterialTheme.colorScheme.background,
                        shape = RoundedCornerShape(10.dp)
                    )
            ) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 10.dp,
                    modifier = Modifier
                        .size(70.dp)
                        .align(Alignment.Center)
                )
            }
        }
    }

    viewModel.errorState?.let {
        when (it) {
            IdentificationScanViewModelInterface.Error.IDDeactivated -> TODO()
            is IdentificationScanViewModelInterface.Error.IncorrectPIN -> {
                PINDialog(attempts = it.attempts, onCancel = viewModel::onCancelReEnterPersonalPIN, onNewPINEntered = viewModel::onNewPersonalPINEntered)
            }
            is IdentificationScanViewModelInterface.Error.Other -> TODO()
            IdentificationScanViewModelInterface.Error.PINBlocked -> TODO()
            IdentificationScanViewModelInterface.Error.PINSuspended -> TODO()
        }
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
    object CardRequested: ScanEvent()
    object CardAttached: ScanEvent()
    object Finished: ScanEvent()
    data class IncorrectPIN(val attempts: Int): ScanEvent()
}

interface IdentificationScanViewModelInterface {
    sealed class Error {
        data class IncorrectPIN(val attempts: Int) : Error()
        object PINSuspended : Error()
        object PINBlocked : Error()
        object IDDeactivated : Error()
        data class Other(val message: String?) : Error()

        val titleResID: Int
            get() {
                return when (this) {
                    is PINSuspended -> R.string.firstTimeUser_scan_error_title_pin_suspended
                    is PINBlocked -> R.string.firstTimeUser_scan_error_title_pin_blocked
                    is IDDeactivated -> R.string.firstTimeUser_scan_error_title_id_deactivated
                    is Other -> R.string.firstTimeUser_scan_error_title_unknown
                    else -> throw IllegalArgumentException()
                }
            }

        val textResID: Int
            get() {
                return when (this) {
                    PINSuspended, PINBlocked, IDDeactivated -> R.string.firstTimeUser_scan_error_text_feature_unavailable
                    is Other -> R.string.firstTimeUser_scan_error_text_unknown
                    else -> throw IllegalArgumentException()
                }
            }
    }

    val shouldShowProgress: Boolean
    val errorState: Error?

    fun onHelpButtonTapped()
    fun onCancelReEnterPersonalPIN()
    fun onNewPersonalPINEntered(pin: String)
}

@HiltViewModel
class IdentificationScanViewModel @Inject constructor(private val coordinator: IdentificationCoordinator): ViewModel(), IdentificationScanViewModelInterface {
    private val logger by getLogger()

    override var shouldShowProgress: Boolean by mutableStateOf(false)
        private set
    override var errorState: IdentificationScanViewModelInterface.Error? by mutableStateOf(null)
        private set

    init {
        collectScanEvents()
    }

    override fun onHelpButtonTapped() {
    }

    override fun onCancelReEnterPersonalPIN() {
        coordinator.cancelIdentification()
    }

    override fun onNewPersonalPINEntered(pin: String) {
        errorState = null
        coordinator.onPINEntered(pin)
    }

    private fun collectScanEvents() {
        viewModelScope.launch {
            coordinator.scanEventFlow.collect { event ->
                when(event) {
                    ScanEvent.CardRequested -> shouldShowProgress = false
                    ScanEvent.CardAttached -> shouldShowProgress = true
                    ScanEvent.Finished -> shouldShowProgress = false
                    is ScanEvent.IncorrectPIN -> {
                        logger.debug("Set ERROR")
                        shouldShowProgress = false
                        errorState = IdentificationScanViewModelInterface.Error.IncorrectPIN(event.attempts)
                    }
                }
            }
        }
    }
}

private class PreviewIdentificationScanViewModel(override val shouldShowProgress: Boolean,
                                                 override val errorState: IdentificationScanViewModelInterface.Error?
): IdentificationScanViewModelInterface {
    override fun onHelpButtonTapped() {}
    override fun onCancelReEnterPersonalPIN() {}
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
