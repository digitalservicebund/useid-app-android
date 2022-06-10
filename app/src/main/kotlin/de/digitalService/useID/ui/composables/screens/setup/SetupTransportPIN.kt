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
import dagger.hilt.android.lifecycle.HiltViewModel
import de.digitalService.useID.R
import de.digitalService.useID.SecureStorageManager
import de.digitalService.useID.SecureStorageManagerInterface
import de.digitalService.useID.getLogger
import de.digitalService.useID.ui.composables.PINEntryField
import de.digitalService.useID.ui.composables.TransportPINEntryField
import de.digitalService.useID.ui.coordinators.SetupCoordinator
import de.digitalService.useID.ui.theme.UseIDTheme
import javax.inject.Inject

@Composable
fun SetupTransportPIN(
    viewModel: SetupTransportPINViewModelInterface,
    modifier: Modifier = Modifier
) {
    val focusRequester = remember { FocusRequester() }

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
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}

interface SetupTransportPINViewModelInterface {
    val transportPIN: String

    fun onInputChanged(value: String)
    fun onDoneTapped()
}

@HiltViewModel
class SetupTransportPINViewModel(
    private val secureStorageManager: SecureStorageManagerInterface,
    private val onDone: () -> Unit
) :
    ViewModel(), SetupTransportPINViewModelInterface {
    private val logger by getLogger()

    @Inject
    constructor(
        coordinator: SetupCoordinator,
        secureStorageManager: SecureStorageManager
    ) : this(
        secureStorageManager = secureStorageManager,
        onDone = coordinator::onTransportPINEntered
    )

    override var transportPIN: String by mutableStateOf("")
        private set

    override fun onInputChanged(value: String) {
        transportPIN = value
    }

    override fun onDoneTapped() {
        if (transportPIN.length == 5) {
            secureStorageManager.setTransportPIN(transportPIN)
            transportPIN = ""
            onDone()
        } else {
            logger.debug("Transport PIN too short.")
        }
    }
}

//region Preview
private class PreviewSetupTransportPINViewModel(
    override val transportPIN: String
) : SetupTransportPINViewModelInterface {
    override fun onInputChanged(value: String) {}
    override fun onDoneTapped() {}
}

@Preview(widthDp = 300)
@Composable
fun PreviewSetupTransportPINWithoutAttemptsNarrowDevice() {
    UseIDTheme {
        SetupTransportPIN(PreviewSetupTransportPINViewModel("12"))
    }
}

@Preview
@Composable
fun PreviewSetupTransportPINWithoutAttempts() {
    UseIDTheme {
        SetupTransportPIN(PreviewSetupTransportPINViewModel("12"))
    }
}

@Preview
@Composable
fun PreviewSetupTransportPINNullAttempts() {
    UseIDTheme {
        SetupTransportPIN(PreviewSetupTransportPINViewModel("12"))
    }
}

@Preview
@Composable
fun PreviewSetupTransportPINOneAttempt() {
    UseIDTheme {
        SetupTransportPIN(PreviewSetupTransportPINViewModel("12"))
    }
}

@Preview
@Composable
fun PreviewSetupTransportPINTwoAttempts() {
    UseIDTheme {
        SetupTransportPIN(PreviewSetupTransportPINViewModel("12"))
    }
}
//endregion
