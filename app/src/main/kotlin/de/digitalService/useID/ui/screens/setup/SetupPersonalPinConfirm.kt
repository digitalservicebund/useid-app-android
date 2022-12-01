package de.digitalService.useID.ui.screens.setup

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.text.isDigitsOnly
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import com.ramcosta.composedestinations.annotation.Destination
import dagger.hilt.android.lifecycle.HiltViewModel
import de.digitalService.useID.R
import de.digitalService.useID.ui.components.NavigationButton
import de.digitalService.useID.ui.components.NavigationIcon
import de.digitalService.useID.ui.components.ScreenWithTopBar
import de.digitalService.useID.ui.components.pin.PinEntryField
import de.digitalService.useID.ui.coordinators.SetupCoordinator
import de.digitalService.useID.ui.dialogs.StandardDialog
import de.digitalService.useID.ui.theme.UseIDTheme
import javax.inject.Inject

@Destination
@Composable
fun SetupPersonalPinConfirm(viewModel: SetupPersonalPinConfirmViewModelInterface = hiltViewModel<SetupPersonalPinConfirmViewModel>()) {
    ScreenWithTopBar(
        navigationButton = NavigationButton(icon = NavigationIcon.Back, onClick = viewModel::onNavigationButtonClicked)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(top = it, start = 16.dp, end = 16.dp)
                .verticalScroll(rememberScrollState())

        ) {
            val focusRequesterPin = remember { FocusRequester() }

            LaunchedEffect(Unit) {
                viewModel.onInitialize()
                focusRequesterPin.requestFocus()
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(id = R.string.firstTimeUser_personalPIN_confirmation_title),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = stringResource(id = R.string.firstTimeUser_personalPIN_confirmation_body),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))

            val pinEntryFieldDescription = stringResource(
                id = R.string.firstTimeUser_personalPIN_textFieldLabel_second,
                viewModel.pin.map { "$it " }
            )

            PinEntryField(
                value = viewModel.pin,
                digitCount = 6,
                obfuscation = true,
                spacerPosition = 3,
                onValueChanged = viewModel::userInputPin,
                onDone = viewModel::onDoneClicked,
                contentDescription = pinEntryFieldDescription,
                focusRequester = focusRequesterPin,
                backgroundColor = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier
                    .width(240.dp)
                    .height(56.dp)
            )
        }
    }

    AnimatedVisibility(visible = viewModel.shouldShowError) {
        StandardDialog(
            title = {
                Text(
                    text = stringResource(id = R.string.firstTimeUser_personalPIN_error_mismatch_title),
                    style = MaterialTheme.typography.titleMedium
                )
            },
            text = {},
            confirmButtonText = stringResource(id = R.string.identification_fetchMetadataError_retry),
            onConfirmButtonClick = viewModel::onErrorDialogButtonClicked
        )
    }
}

interface SetupPersonalPinConfirmViewModelInterface {
    val pin: String
    val shouldShowError: Boolean

    fun onInitialize()
    fun userInputPin(value: String)
    fun onDoneClicked()
    fun onErrorDialogButtonClicked()
    fun onNavigationButtonClicked()
}

@HiltViewModel
class SetupPersonalPinConfirmViewModel @Inject constructor(
    private val setupCoordinator: SetupCoordinator
) : ViewModel(), SetupPersonalPinConfirmViewModelInterface {
    override var pin: String by mutableStateOf("")
    override var shouldShowError: Boolean by mutableStateOf(false)

    override fun onInitialize() {
        pin = ""
    }

    override fun userInputPin(value: String) {
        if (!value.isDigitsOnly()) {
            return
        }

        pin = value
    }

    override fun onDoneClicked() {
        if (pin.length == 6) {
            shouldShowError = !setupCoordinator.confirmPersonalPin(pin)
        }
    }

    override fun onErrorDialogButtonClicked() {
        setupCoordinator.onPersonalPinErrorTryAgain()
    }

    override fun onNavigationButtonClicked() {
        setupCoordinator.onBackClicked()
    }
}

private class PreviewSetupPersonalPinConfirmViewModel : SetupPersonalPinConfirmViewModelInterface {
    override val pin: String = ""
    override val shouldShowError: Boolean = false

    override fun onInitialize() {}
    override fun userInputPin(value: String) {}
    override fun onDoneClicked() {}
    override fun onErrorDialogButtonClicked() {}
    override fun onNavigationButtonClicked() {}
}

@Preview(showBackground = true)
@Composable
private fun PreviewSetupPersonalPinConfirm() {
    UseIDTheme {
        SetupPersonalPinConfirm(PreviewSetupPersonalPinConfirmViewModel())
    }
}
