package de.digitalService.useID.ui.screens.setup

import androidx.compose.runtime.Composable
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
import de.digitalService.useID.ui.coordinators.ChangePinCoordinator
import de.digitalService.useID.ui.theme.UseIdTheme
import javax.inject.Inject

@Destination
@Composable
fun SetupPersonalPinInput(viewModel: SetupPersonalPinInputViewModelInterface = hiltViewModel<SetupPersonalPinInputViewModel>()) {
    StandardNumberEntryScreen(
        title = stringResource(id = R.string.firstTimeUser_personalPIN_title),
        body = stringResource(id = R.string.firstTimeUser_personalPIN_body),
        entryFieldDescription = stringResource(id = R.string.firstTimeUser_personalPIN_textFieldLabel_first),
        navigationButton = NavigationButton(
            icon = NavigationIcon.Back,
            onClick = viewModel::onBack,
            confirmation = null
        ),
        inputType = InputType.Pin,
        onDone = viewModel::onDoneClicked
    )
}

interface SetupPersonalPinInputViewModelInterface {
    fun onDoneClicked(pin: String)
    fun onBack()
}

@HiltViewModel
class SetupPersonalPinInputViewModel @Inject constructor(
    private val changePinCoordinator: ChangePinCoordinator
) : ViewModel(), SetupPersonalPinInputViewModelInterface {
    override fun onDoneClicked(pin: String) = changePinCoordinator.onNewPinEntered(pin)
    override fun onBack() = changePinCoordinator.onBack()
}

private class PreviewSetupPersonalPinInputViewModel : SetupPersonalPinInputViewModelInterface {
    override fun onDoneClicked(pin: String) {}
    override fun onBack() {}
}

@Preview(showBackground = true)
@Composable
private fun PreviewSetupPersonalPinInput() {
    UseIdTheme {
        SetupPersonalPinInput(PreviewSetupPersonalPinInputViewModel())
    }
}
