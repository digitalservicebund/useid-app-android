package de.digitalService.useID.ui.screens.can

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
import de.digitalService.useID.getLogger
import de.digitalService.useID.ui.components.pin.StandardNumberEntryScreen
import de.digitalService.useID.ui.coordinators.CanCoordinator
import de.digitalService.useID.ui.theme.UseIdTheme
import javax.inject.Inject

@Destination
@Composable
fun IdentificationCanInput(viewModel: IdentificationCanInputViewModelInterface = hiltViewModel<IdentificationCanInputViewModel>()) {
    StandardNumberEntryScreen(
        title = stringResource(id = R.string.identification_can_input_title),
        body = stringResource(id = R.string.identification_can_input_body),
        entryFieldDescription = stringResource(id = R.string.identification_can_input_canInputLabel),
        onNavigationButtonBackClick = viewModel::onBack,
        obfuscation = false,
        onDone = viewModel::onDone)
}

interface IdentificationCanInputViewModelInterface {
    fun onBack()
    fun onDone(can: String)
}

@HiltViewModel
class IdentificationCanInputViewModel @Inject constructor(
    val coordinator: CanCoordinator
): ViewModel(), IdentificationCanInputViewModelInterface {
    override fun onBack() {
        coordinator.onBack()
    }

    override fun onDone(can: String) {
        coordinator.onCanEntered(can)
    }
}

private class PreviewIdentificationCanInputViewModel: IdentificationCanInputViewModelInterface {
    override fun onBack() {}
    override fun onDone(can: String) {}
}

@Preview
@Composable
private fun Preview() {
    UseIdTheme {
        IdentificationCanInput(PreviewIdentificationCanInputViewModel())
    }
}
