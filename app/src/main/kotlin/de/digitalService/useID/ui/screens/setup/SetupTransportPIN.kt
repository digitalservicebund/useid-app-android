package de.digitalService.useID.ui.screens.setup

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.ramcosta.composedestinations.annotation.Destination
import dagger.hilt.android.lifecycle.HiltViewModel
import de.digitalService.useID.R
import de.digitalService.useID.getLogger
import de.digitalService.useID.ui.components.NavigationButton
import de.digitalService.useID.ui.components.NavigationIcon
import de.digitalService.useID.ui.components.ScreenWithTopBar
import de.digitalService.useID.ui.components.pin.TransportPINEntryField
import de.digitalService.useID.ui.coordinators.SetupCoordinator
import de.digitalService.useID.ui.screens.destinations.SetupTransportPINDestination
import de.digitalService.useID.ui.theme.UseIDTheme
import kotlinx.coroutines.delay
import javax.inject.Inject

@Destination(navArgsDelegate = SetupTransportPINNavArgs::class)
@Composable
fun SetupTransportPIN(
    modifier: Modifier = Modifier,
    viewModel: SetupTransportPINViewModelInterface = hiltViewModel<SetupTransportPINViewModel>()
) {
    val resources = LocalContext.current.resources
    var showCancelDialog by remember { mutableStateOf(false) }

    val icon = if (viewModel.attempts == null) {
        NavigationIcon.Back
    } else {
        NavigationIcon.Cancel
    }

    ScreenWithTopBar(
        navigationButton = NavigationButton(
            icon = icon,
            onClick = { if (viewModel.attempts == null) viewModel.onBackButtonTapped() else showCancelDialog = true }
        )
    ) { topPadding ->
        val focusRequester = remember { FocusRequester() }

        LaunchedEffect(Unit) {
            delay(100)
            focusRequester.requestFocus()
        }

        Column(
            modifier = modifier
                .padding(horizontal = 20.dp)
                .padding(top = topPadding)
                .verticalScroll(rememberScrollState())
        ) {
            val titleString = if (viewModel.attempts == null) {
                stringResource(id = R.string.firstTimeUser_transportPIN_title)
            } else {
                stringResource(id = R.string.firstTimeUser_incorrectTransportPIN_title)
            }

            Text(
                text = titleString,
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(id = R.string.firstTimeUser_transportPIN_body),
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(24.dp))

            TransportPINEntryField(
                value = viewModel.transportPIN,
                onValueChanged = viewModel::onInputChanged,
                onDone = viewModel::onDoneTapped,
                focusRequester = focusRequester
            )

            Spacer(modifier = Modifier.height(24.dp))

            viewModel.attempts?.let { attempts ->
                val attemptString = resources.getQuantityString(
                    R.plurals.firstTimeUser_transportPIN_remainingAttempts,
                    attempts,
                    attempts
                )
                Text(
                    attemptString,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.align(androidx.compose.ui.Alignment.CenterHorizontally)
                )
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
                    Text(text = stringResource(R.string.firstTimeUser_scan_cancelDialog_body), style = MaterialTheme.typography.bodySmall)
                },
                confirmButton = {
                    TextButton(onClick = viewModel::onCancelTapped) {
                        Text(
                            text = stringResource(R.string.firstTimeUser_scan_cancelDialog_confirm),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCancelDialog = false }) {
                        Text(
                            text = stringResource(R.string.firstTimeUser_scan_cancelDialog_dismiss),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                containerColor = MaterialTheme.colorScheme.background,
                shape = RoundedCornerShape(10.dp)
            )
        }
    }
}

data class SetupTransportPINNavArgs(
    val attempts: Int?
)

interface SetupTransportPINViewModelInterface {
    val transportPIN: String
    val attempts: Int?

    fun onInputChanged(value: String)
    fun onDoneTapped()
    fun onCancelTapped()
    fun onBackButtonTapped()
}

@HiltViewModel
class SetupTransportPINViewModel @Inject constructor(
    private val coordinator: SetupCoordinator,
    savedStateHandle: SavedStateHandle
) :
    ViewModel(), SetupTransportPINViewModelInterface {
    private val logger by getLogger()

    override val attempts: Int?

    override var transportPIN: String by mutableStateOf("")
        private set

    init {
        attempts = SetupTransportPINDestination.argsFrom(savedStateHandle).attempts
    }

    override fun onInputChanged(value: String) {
        transportPIN = value
    }

    override fun onDoneTapped() {
        if (transportPIN.length == 5) {
            coordinator.onTransportPINEntered(transportPIN)
            transportPIN = ""
        } else {
            logger.debug("Transport PIN too short.")
        }
    }

    override fun onCancelTapped() {
        coordinator.cancelSetup()
    }

    override fun onBackButtonTapped() {
        coordinator.onBackTapped()
    }
}

//region Preview
private class PreviewSetupTransportPINViewModel(
    override val transportPIN: String,
    override val attempts: Int?
) : SetupTransportPINViewModelInterface {
    override fun onInputChanged(value: String) {}
    override fun onDoneTapped() {}
    override fun onCancelTapped() {}
    override fun onBackButtonTapped() {}
}

@Preview(widthDp = 300, showBackground = true)
@Composable
fun PreviewSetupTransportPINWithoutAttemptsNarrowDevice() {
    UseIDTheme {
        SetupTransportPIN(viewModel = PreviewSetupTransportPINViewModel("12", null))
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewSetupTransportPINWithoutAttempts() {
    UseIDTheme {
        SetupTransportPIN(viewModel = PreviewSetupTransportPINViewModel("12", null))
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewSetupTransportPINOneAttempt() {
    UseIDTheme {
        SetupTransportPIN(viewModel = PreviewSetupTransportPINViewModel("12", attempts = 1))
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewSetupTransportPINTwoAttempts() {
    UseIDTheme {
        SetupTransportPIN(viewModel = PreviewSetupTransportPINViewModel("12", attempts = 2))
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewSetupTransportPINCancelDialog() {
    UseIDTheme {
        SetupTransportPIN(viewModel = PreviewSetupTransportPINViewModel("12", attempts = 2))
    }
}
//endregion
