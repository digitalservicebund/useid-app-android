package de.digitalService.useID.ui.composables.screens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import dagger.hilt.android.lifecycle.HiltViewModel
import de.digitalService.useID.R
import de.digitalService.useID.ui.composables.PINEntryField
import de.digitalService.useID.ui.coordinators.TransportPINCoordinator
import de.digitalService.useID.ui.theme.UseIDTheme
import javax.inject.Inject

@Composable
fun SetupTransportPIN(viewModel: SetupTransportPINViewModelInterface) {
    val focusRequester = remember { FocusRequester() }
    val resources = LocalContext.current.resources

    val pinEntryFieldDescription = stringResource(
        id = R.string.firstTimeUser_transportPIN_PINTextFieldDescription,
        viewModel.transportPIN.map { "$it " })

    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        Text(
            text = stringResource(id = R.string.firstTimeUser_transportPIN_title),
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(modifier = Modifier.height(40.dp))
        Box(modifier = Modifier.padding(horizontal = 20.dp)) {
            Image(
                painter = painterResource(id = R.drawable.transport_pin),
                contentDescription = null,
                alignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
            )
            PINEntryField(
                value = viewModel.transportPIN,
                onValueChanged = viewModel::onInputChanged,
                digitCount = 5,
                spacerPosition = null,
                contentDescription = pinEntryFieldDescription,
                focusRequester = focusRequester,
                onDone = viewModel::onDoneTapped,
                modifier = Modifier
                    .align(Alignment.Center)
                    .width(240.dp)
                    .height(56.dp)
            )
        }

        if (viewModel.shouldShowTransportPINError) {
            val attempts = viewModel.displayedAttempts
            Spacer(modifier = Modifier.height(40.dp))
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    stringResource(id = R.string.firstTimeUser_transportPIN_error_incorrectPIN),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    stringResource(id = R.string.firstTimeUser_transportPIN_error_tryAgain),
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center
                )
                val attemptString = if (attempts > 0) {
                    resources.getQuantityString(
                        R.plurals.firstTimeUser_transportPIN_remainingAttempts,
                        attempts,
                        attempts
                    )
                } else {
                    stringResource(id = R.string.firstTimeUser_transportPIN_error_noAttemptLeft)
                }
                Text(
                    attemptString,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center
                )
            }
        }
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}

interface SetupTransportPINViewModelInterface {
    val transportPIN: String

    val shouldShowTransportPINError: Boolean
    val displayedAttempts: Int

    fun onInputChanged(value: String)
    fun onDoneTapped()
}

@HiltViewModel
class SetupTransportPINViewModel @Inject constructor(
    private val coordinator: TransportPINCoordinator,
    savedStateHandle: SavedStateHandle
) :
    ViewModel(), SetupTransportPINViewModelInterface {

    private val attempts: Int

    init {
        attempts = Screen.SetupTransportPIN.attempts(savedStateHandle)
    }

    override var transportPIN: String by mutableStateOf("")
        private set

    override val shouldShowTransportPINError: Boolean = attempts < 3
    override val displayedAttempts: Int
        get() = attempts

    override fun onInputChanged(value: String) {
        transportPIN = value
    }

    override fun onDoneTapped() {
        if (transportPIN.length == 5) {
            coordinator.finishTransportPINEntry(transportPIN)
        } else {
            Log.d("DEBUG", "Transport PIN too short.")
        }
    }
}

//region Preview
private class PreviewSetupTransportPINViewModel(
    override val transportPIN: String,
    override val shouldShowTransportPINError: Boolean,
    override val displayedAttempts: Int
) : SetupTransportPINViewModelInterface {
    override fun onInputChanged(value: String) {}
    override fun onDoneTapped() {}
}

@Preview
@Composable
fun PreviewSetupTransportPINWithoutAttempts() {
    UseIDTheme {
        SetupTransportPIN(PreviewSetupTransportPINViewModel("12", false, 0))
    }
}

@Preview
@Composable
fun PreviewSetupTransportPINNullAttempts() {
    UseIDTheme {
        SetupTransportPIN(PreviewSetupTransportPINViewModel("12", true, 0))
    }
}

@Preview
@Composable
fun PreviewSetupTransportPINOneAttempt() {
    UseIDTheme {
        SetupTransportPIN(PreviewSetupTransportPINViewModel("12", true, 1))
    }
}

@Preview
@Composable
fun PreviewSetupTransportPINTwoAttempts() {
    UseIDTheme {
        SetupTransportPIN(PreviewSetupTransportPINViewModel("12", true, 2))
    }
}
//endregion