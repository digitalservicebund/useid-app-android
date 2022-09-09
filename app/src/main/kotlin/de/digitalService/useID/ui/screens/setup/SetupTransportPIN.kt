package de.digitalService.useID.ui.screens.setup

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import com.ramcosta.composedestinations.annotation.Destination
import dagger.hilt.android.lifecycle.HiltViewModel
import de.digitalService.useID.R
import de.digitalService.useID.SecureStorageManager
import de.digitalService.useID.SecureStorageManagerInterface
import de.digitalService.useID.getLogger
import de.digitalService.useID.ui.components.NavigationButton
import de.digitalService.useID.ui.components.NavigationIcon
import de.digitalService.useID.ui.components.ScreenWithTopBar
import de.digitalService.useID.ui.components.pin.TransportPINEntryField
import de.digitalService.useID.ui.coordinators.SetupCoordinator
import de.digitalService.useID.ui.theme.UseIDTheme
import javax.inject.Inject

@Destination
@Composable
fun SetupTransportPIN(
    modifier: Modifier = Modifier,
    viewModel: SetupTransportPINViewModelInterface = hiltViewModel<SetupTransportPINViewModel>()
) {
    ScreenWithTopBar(
        navigationButton = NavigationButton(icon = NavigationIcon.Back, onClick = viewModel::onBackButtonTapped)
    ) { topPadding ->
        val focusRequester = remember { FocusRequester() }

        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
        }

        Column(modifier = modifier.padding(horizontal = 20.dp).padding(top = topPadding)) {
            Text(
                text = stringResource(id = R.string.firstTimeUser_transportPIN_title),
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
        }
    }
}

interface SetupTransportPINViewModelInterface {
    val transportPIN: String

    fun onInputChanged(value: String)
    fun onDoneTapped()
    fun onBackButtonTapped()
}

@HiltViewModel
class SetupTransportPINViewModel(
    private val coordinator: SetupCoordinator,
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
        coordinator = coordinator,
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

    override fun onBackButtonTapped() = coordinator.onBackTapped()
}

//region Preview
private class PreviewSetupTransportPINViewModel(
    override val transportPIN: String
) : SetupTransportPINViewModelInterface {
    override fun onInputChanged(value: String) {}
    override fun onDoneTapped() {}
    override fun onBackButtonTapped() {}
}

@Preview(widthDp = 300, showBackground = true)
@Composable
fun PreviewSetupTransportPINWithoutAttemptsNarrowDevice() {
    UseIDTheme {
        SetupTransportPIN(viewModel = PreviewSetupTransportPINViewModel("12"))
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewSetupTransportPINWithoutAttempts() {
    UseIDTheme {
        SetupTransportPIN(viewModel = PreviewSetupTransportPINViewModel("12"))
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewSetupTransportPINNullAttempts() {
    UseIDTheme {
        SetupTransportPIN(viewModel = PreviewSetupTransportPINViewModel("12"))
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewSetupTransportPINOneAttempt() {
    UseIDTheme {
        SetupTransportPIN(viewModel = PreviewSetupTransportPINViewModel("12"))
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewSetupTransportPINTwoAttempts() {
    UseIDTheme {
        SetupTransportPIN(viewModel = PreviewSetupTransportPINViewModel("12"))
    }
}
//endregion
