package de.digitalService.useID.ui.screens.can

import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.text.isDigitsOnly
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import com.ramcosta.composedestinations.annotation.Destination
import dagger.hilt.android.lifecycle.HiltViewModel
import de.digitalService.useID.R
import de.digitalService.useID.ui.components.NavigationButton
import de.digitalService.useID.ui.components.NavigationIcon
import de.digitalService.useID.ui.components.pin.StandardNumberEntryScreen
import de.digitalService.useID.ui.coordinators.CanCoordinator
import de.digitalService.useID.ui.theme.UseIdTheme
import javax.inject.Inject

@Destination
@Composable
fun IdentificationCanPinInput(viewModel: IdentificationCanPinInputViewModelInterface = hiltViewModel<IdentificationCanPinInputViewModel>()) {
    StandardNumberEntryScreen(
        title = stringResource(id = R.string.identification_personalPIN_title),
        attempts = 1,
        entryFieldDescription = stringResource(id = R.string.identification_personalPIN_PINTextFieldDescription),
        navigationButton = NavigationButton(
            icon = NavigationIcon.Back,
            onClick = viewModel::onBack,
            confirmation = null
        ),
        obfuscation = true,
        onDone = viewModel::onDone
    )
}

interface IdentificationCanPinInputViewModelInterface {
    fun onBack()
    fun onDone(pin: String)
}

@HiltViewModel
class IdentificationCanPinInputViewModel @Inject constructor(
    val coordinator: CanCoordinator
) : ViewModel(), IdentificationCanPinInputViewModelInterface {
    override fun onBack() {
        coordinator.onBack()
    }

    override fun onDone(pin: String) {
        coordinator.onPinEntered(pin)
    }

    private fun checkPinString(value: String): Boolean = value.length < 7 && value.isDigitsOnly()
}

private class PreviewIdentificationCanPinInputViewModel : IdentificationCanPinInputViewModelInterface {
    override fun onBack() {}
    override fun onDone(pin: String) {}
}

@Preview
@Composable
private fun Preview() {
    UseIdTheme {
        IdentificationCanPinInput(PreviewIdentificationCanPinInputViewModel())
    }
}
