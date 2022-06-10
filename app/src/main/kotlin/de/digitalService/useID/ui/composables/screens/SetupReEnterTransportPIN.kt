package de.digitalService.useID.ui.composables.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import de.digitalService.useID.R
import de.digitalService.useID.SecureStorageManager
import de.digitalService.useID.SecureStorageManagerInterface
import de.digitalService.useID.getLogger
import de.digitalService.useID.ui.composables.PINEntryField
import de.digitalService.useID.ui.composables.TransportPINEntryField
import de.digitalService.useID.ui.theme.UseIDTheme
import javax.inject.Inject

@Composable
fun SetupReEnterTransportPIN(
    viewModel: SetupReEnterTransportPINViewModelInterface,
    modifier: Modifier = Modifier
) {
    val focusRequester = remember { FocusRequester() }
    val resources = LocalContext.current.resources

    Column(modifier = modifier.padding(horizontal = 20.dp)) {
        Text(
            text = stringResource(id = R.string.firstTimeUser_transportPIN_title),
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(modifier = Modifier.height(40.dp))
        TransportPINEntryField(
            value = viewModel.transportPIN,
            onValueChanged = viewModel::onInputChanged,
            onDone = viewModel::onDoneTapped,
            focusRequester = focusRequester
        )

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
            val attemptString = if (viewModel.attempts > 0) {
                resources.getQuantityString(
                    R.plurals.firstTimeUser_transportPIN_remainingAttempts,
                    viewModel.attempts,
                    viewModel.attempts
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

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}

interface SetupReEnterTransportPINViewModelInterface {
    val transportPIN: String
    val attempts: Int

    fun onInputChanged(value: String)
    fun onDoneTapped()
}

class SetupReEnterTransportPINViewModel(
    override val attempts: Int,
    private val onDone: (String) -> Unit
) :
    ViewModel(), SetupReEnterTransportPINViewModelInterface {
    private val logger by getLogger()

    override var transportPIN: String by mutableStateOf("")
        private set

    override fun onInputChanged(value: String) {
        transportPIN = value
    }

    override fun onDoneTapped() {
        if (transportPIN.length == 5) {
            onDone(transportPIN)
        } else {
            logger.debug("Transport PIN too short.")
        }
    }
}

//region Preview
private class PreviewSetupReEnterTransportPINViewModel(
    override val transportPIN: String,
    override val attempts: Int
) : SetupReEnterTransportPINViewModelInterface {
    override fun onInputChanged(value: String) {}
    override fun onDoneTapped() {}
}

@Preview(widthDp = 300)
@Composable
fun PreviewSetupReEnterTransportPINWithoutAttemptsNarrowDevice() {
    UseIDTheme {
        SetupReEnterTransportPIN(PreviewSetupReEnterTransportPINViewModel("12", 0))
    }
}

@Preview
@Composable
fun PreviewSetupReEnterTransportPINWithoutAttempts() {
    UseIDTheme {
        SetupReEnterTransportPIN(PreviewSetupReEnterTransportPINViewModel("12", 0))
    }
}

@Preview
@Composable
fun PreviewSetupReEnterTransportPINNullAttempts() {
    UseIDTheme {
        SetupReEnterTransportPIN(PreviewSetupReEnterTransportPINViewModel("12", 0))
    }
}

@Preview
@Composable
fun PreviewSetupReEnterTransportPINOneAttempt() {
    UseIDTheme {
        SetupReEnterTransportPIN(PreviewSetupReEnterTransportPINViewModel("12", 1))
    }
}

@Preview
@Composable
fun PreviewSetupReEnterTransportPINTwoAttempts() {
    UseIDTheme {
        SetupReEnterTransportPIN(PreviewSetupReEnterTransportPINViewModel("12", 2))
    }
}
//endregion
