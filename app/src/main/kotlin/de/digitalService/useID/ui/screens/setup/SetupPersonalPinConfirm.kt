package de.digitalService.useID.ui.screens.setup

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import com.ramcosta.composedestinations.annotation.Destination
import dagger.hilt.android.lifecycle.HiltViewModel
import de.digitalService.useID.R
import de.digitalService.useID.ui.components.NavigationButton
import de.digitalService.useID.ui.components.NavigationIcon
import de.digitalService.useID.ui.components.pin.InputType
import de.digitalService.useID.ui.components.pin.StandardNumberEntryScreen
import de.digitalService.useID.ui.coordinators.PinManagementCoordinator
import de.digitalService.useID.ui.dialogs.StandardDialog
import de.digitalService.useID.ui.theme.UseIdTheme
import javax.inject.Inject

@Destination
@Composable
fun SetupPersonalPinConfirm(viewModel: SetupPersonalPinConfirmViewModelInterface = hiltViewModel<SetupPersonalPinConfirmViewModel>()) {
    StandardNumberEntryScreen(
        title = stringResource(id = R.string.firstTimeUser_personalPIN_confirmation_title),
        body = stringResource(id = R.string.firstTimeUser_personalPIN_confirmation_body),
        entryFieldDescription = stringResource(id = R.string.firstTimeUser_personalPIN_textFieldLabel_second),
        navigationButton = NavigationButton(
            icon = NavigationIcon.Back,
            onClick = viewModel::onBack,
            confirmation = null
        ),
        inputType = InputType.Pin,
        onDone = viewModel::onDoneClicked,
        delayFocusRequest = false
    )

    AnimatedVisibility(visible = viewModel.shouldShowError) {
        StandardDialog(
            title = {
                Text(
                    text = stringResource(id = R.string.firstTimeUser_personalPIN_error_mismatch_title),
                    style = UseIdTheme.typography.headingL
                )
            },
            text = {},
            confirmButtonText = stringResource(id = R.string.identification_fetchMetadataError_retry),
            onConfirmButtonClick = viewModel::onErrorDialogButtonClicked
        )
    }
}

interface SetupPersonalPinConfirmViewModelInterface {
    val shouldShowError: Boolean

    fun onDoneClicked(pin: String)
    fun onErrorDialogButtonClicked()
    fun onBack()
}

@HiltViewModel
class SetupPersonalPinConfirmViewModel @Inject constructor(
    private val pinManagementCoordinator: PinManagementCoordinator
) : ViewModel(), SetupPersonalPinConfirmViewModelInterface {
    override var shouldShowError: Boolean by mutableStateOf(false)

    override fun onDoneClicked(pin: String) {
        shouldShowError = !pinManagementCoordinator.confirmNewPin(pin)
    }

    override fun onErrorDialogButtonClicked() {
        pinManagementCoordinator.onConfirmPinMismatchError()
    }

    override fun onBack() {
        pinManagementCoordinator.onBack()
    }
}

private class PreviewSetupPersonalPinConfirmViewModel : SetupPersonalPinConfirmViewModelInterface {
    override val shouldShowError: Boolean = false

    override fun onDoneClicked(pin: String) {}
    override fun onErrorDialogButtonClicked() {}
    override fun onBack() {}
}

@Preview(showBackground = true)
@Composable
private fun PreviewSetupPersonalPinConfirm() {
    UseIdTheme {
        SetupPersonalPinConfirm(PreviewSetupPersonalPinConfirmViewModel())
    }
}
