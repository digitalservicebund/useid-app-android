package de.digitalService.useID.ui.screens.setup

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
import de.digitalService.useID.ui.theme.UseIdTheme
import javax.inject.Inject

@Destination
@Composable
fun SetupPersonalPinInput(viewModel: SetupPersonalPinInputViewModelInterface = hiltViewModel<SetupPersonalPinInputViewModel>()) {

    val pinEntryFieldDescription = stringResource(
        id = R.string.firstTimeUser_personalPIN_textFieldLabel_first,
        viewModel.pin.map { "$it " }
    )

    StandardPinScreen(
        header = stringResource(id = R.string.firstTimeUser_personalPIN_title),
        description = stringResource(id = R.string.firstTimeUser_personalPIN_body),
        pinEntryDescription = pinEntryFieldDescription,
        pin = viewModel.pin,
        onNavigationButtonBackClick = viewModel::onNavigationButtonClicked,
        onInitialize = viewModel::onInitialize,
        onValueChanged = viewModel::userInputPin,
        onDone = viewModel::onDoneClicked
    )
}

interface SetupPersonalPinInputViewModelInterface {
    val pin: String

    fun onInitialize()
    fun onDoneClicked()
    fun userInputPin(value: String)
    fun onNavigationButtonClicked()
}

@HiltViewModel
class SetupPersonalPinInputViewModel @Inject constructor(
    private val setupCoordinator: SetupCoordinator
) : ViewModel(), SetupPersonalPinInputViewModelInterface {
    override var pin: String by mutableStateOf("")

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
            setupCoordinator.onPersonalPinInput(pin)
        }
    }

    override fun onNavigationButtonClicked() {
        setupCoordinator.onBackClicked()
    }
}

private class PreviewSetupPersonalPinInputViewModel : SetupPersonalPinInputViewModelInterface {
    override val pin: String = ""
    override fun onInitialize() {}
    override fun onDoneClicked() {}
    override fun userInputPin(value: String) {}
    override fun onNavigationButtonClicked() {}
}

@Preview(showBackground = true)
@Composable
private fun PreviewSetupPersonalPinInput() {
    UseIdTheme {
        SetupPersonalPinInput(PreviewSetupPersonalPinInputViewModel())
    }
}
