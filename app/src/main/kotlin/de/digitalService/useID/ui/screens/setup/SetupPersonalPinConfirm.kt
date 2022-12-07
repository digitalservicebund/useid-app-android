package de.digitalService.useID.ui.screens.setup

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.text.isDigitsOnly
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import com.ramcosta.composedestinations.annotation.Destination
import dagger.hilt.android.lifecycle.HiltViewModel
import de.digitalService.useID.R
import de.digitalService.useID.ui.components.pin.StandardPinScreen
import de.digitalService.useID.ui.coordinators.SetupCoordinator
import de.digitalService.useID.ui.dialogs.StandardDialog
import de.digitalService.useID.ui.theme.UseIdTheme
import javax.inject.Inject

@Destination
@Composable
fun SetupPersonalPinConfirm(viewModel: SetupPersonalPinConfirmViewModelInterface = hiltViewModel<SetupPersonalPinConfirmViewModel>()) {

    val pinEntryFieldDescription = stringResource(
        id = R.string.firstTimeUser_personalPIN_textFieldLabel_second,
        viewModel.pin.map { "$it " }
    )

    StandardPinScreen(
        header = stringResource(id = R.string.firstTimeUser_personalPIN_confirmation_title),
        description = stringResource(id = R.string.firstTimeUser_personalPIN_confirmation_body),
        pinEntryDescription = pinEntryFieldDescription,
        pin = viewModel.pin,
        onNavigationButtonBackClick = viewModel::onNavigationButtonClicked,
        onInitialize = viewModel::onInitialize,
        onValueChanged = viewModel::userInputPin,
        onDone = viewModel::onDoneClicked
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
    UseIdTheme {
        SetupPersonalPinConfirm(PreviewSetupPersonalPinConfirmViewModel())
    }
}
