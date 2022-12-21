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
import de.digitalService.useID.ui.components.pin.StandardNumberEntryScreen
import de.digitalService.useID.ui.coordinators.SetupCoordinator
import de.digitalService.useID.ui.theme.UseIdTheme
import javax.inject.Inject

@Destination
@Composable
fun SetupPersonalPinInput(viewModel: SetupPersonalPinInputViewModelInterface = hiltViewModel<SetupPersonalPinInputViewModel>()) {
    StandardNumberEntryScreen(
        title = stringResource(id = R.string.firstTimeUser_personalPIN_title),
        body = stringResource(id = R.string.firstTimeUser_personalPIN_body),
        entryFieldDescription = stringResource(id = R.string.firstTimeUser_personalPIN_textFieldLabel_first),
        onNavigationButtonBackClick = viewModel::onNavigationButtonClicked,
        obfuscation = true,
        onDone = viewModel::onDoneClicked
    )
}

interface SetupPersonalPinInputViewModelInterface {
    fun onDoneClicked(pin: String)
    fun onNavigationButtonClicked()
}

@HiltViewModel
class SetupPersonalPinInputViewModel @Inject constructor(
    private val setupCoordinator: SetupCoordinator
) : ViewModel(), SetupPersonalPinInputViewModelInterface {
    override fun onDoneClicked(pin: String) {
        setupCoordinator.onPersonalPinInput(pin)
    }

    override fun onNavigationButtonClicked() {
        setupCoordinator.onBackClicked()
    }
}

private class PreviewSetupPersonalPinInputViewModel : SetupPersonalPinInputViewModelInterface {
    override fun onDoneClicked(pin: String) {}
    override fun onNavigationButtonClicked() {}
}

@Preview(showBackground = true)
@Composable
private fun PreviewSetupPersonalPinInput() {
    UseIdTheme {
        SetupPersonalPinInput(PreviewSetupPersonalPinInputViewModel())
    }
}
