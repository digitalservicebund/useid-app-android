package de.digitalService.useID.ui.screens.can

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.ramcosta.composedestinations.annotation.Destination
import dagger.hilt.android.lifecycle.HiltViewModel
import de.digitalService.useID.R
import de.digitalService.useID.ui.components.NavigationButton
import de.digitalService.useID.ui.components.NavigationIcon
import de.digitalService.useID.ui.components.pin.InputType
import de.digitalService.useID.ui.components.pin.StandardNumberEntryScreen
import de.digitalService.useID.ui.coordinators.CanCoordinator
import de.digitalService.useID.ui.screens.destinations.CanInputDestination
import de.digitalService.useID.ui.theme.UseIdTheme
import javax.inject.Inject

@Destination(navArgsDelegate = CanInputNavArgs::class)
@Composable
fun CanInput(viewModel: IdentificationCanInputViewModelInterface = hiltViewModel<IdentificationCanInputViewModel>()) {
    StandardNumberEntryScreen(
        title = stringResource(id = R.string.identification_can_input_title),
        body = stringResource(id = R.string.identification_can_input_body),
        entryFieldDescription = stringResource(id = R.string.identification_can_input_canInputLabel),
        errorMessage = stringResource(id = R.string.identification_can_incorrectInput_error_incorrect_body).takeIf { viewModel.retry },
        navigationButton = NavigationButton(
            icon = NavigationIcon.Back,
            onClick = viewModel::onBack,
            confirmation = null
        ),
        inputType = InputType.Can,
        onDone = viewModel::onDone)
}

data class CanInputNavArgs(
    val retry: Boolean
)

interface IdentificationCanInputViewModelInterface {
    val retry: Boolean

    fun onBack()
    fun onDone(can: String)
}

@HiltViewModel
class IdentificationCanInputViewModel @Inject constructor(
    val coordinator: CanCoordinator,
    savedStateHandle: SavedStateHandle
): ViewModel(), IdentificationCanInputViewModelInterface {
    override val retry: Boolean

    init {
        retry = CanInputDestination.argsFrom(savedStateHandle).retry
    }

    override fun onBack() {
        coordinator.onBack()
    }

    override fun onDone(can: String) {
        coordinator.onCanEntered(can)
    }
}

private class PreviewIdentificationCanInputViewModel: IdentificationCanInputViewModelInterface {
    override val retry: Boolean = false
    override fun onBack() {}
    override fun onDone(can: String) {}
}

@Preview
@Composable
private fun Preview() {
    UseIdTheme {
        CanInput(PreviewIdentificationCanInputViewModel())
    }
}
