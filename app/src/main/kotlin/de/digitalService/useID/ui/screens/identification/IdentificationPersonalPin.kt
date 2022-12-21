package de.digitalService.useID.ui.screens.identification

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.text.isDigitsOnly
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.ramcosta.composedestinations.annotation.Destination
import dagger.hilt.android.lifecycle.HiltViewModel
import de.digitalService.useID.R
import de.digitalService.useID.ui.components.pin.StandardNumberEntryScreen
import de.digitalService.useID.ui.coordinators.IdentificationCoordinator
import de.digitalService.useID.ui.screens.destinations.IdentificationPersonalPinDestination
import de.digitalService.useID.ui.theme.UseIdTheme
import javax.inject.Inject

@Destination(navArgsDelegate = IdentificationPersonalPinNavArgs::class)
@Composable
fun IdentificationPersonalPin(
    modifier: Modifier = Modifier,
    viewModel: IdentificationPersonalPinViewModelInterface = hiltViewModel<IdentificationPersonalPinViewModel>()
) {
    StandardNumberEntryScreen(
        title = stringResource(id = R.string.identification_personalPIN_title),
        errorMessage = if (viewModel.retry) stringResource(id = R.string.identification_personalPIN_error_incorrectPIN) else null,
        entryFieldDescription = stringResource(id = R.string.identification_personalPIN_PINTextFieldDescription),
        onNavigationButtonBackClick = viewModel::onNavigationButtonClicked,
        obfuscation = true,
        onDone = viewModel::onDone
    )
}

data class IdentificationPersonalPinNavArgs(
    val retry: Boolean
)

interface IdentificationPersonalPinViewModelInterface {
    val retry: Boolean

    fun onDone(pin: String)
    fun onNavigationButtonClicked()
}

@HiltViewModel
class IdentificationPersonalPinViewModel @Inject constructor(
    private val coordinator: IdentificationCoordinator,
    savedStateHandle: SavedStateHandle
) : ViewModel(), IdentificationPersonalPinViewModelInterface {

    override val retry: Boolean

    init {
        retry = IdentificationPersonalPinDestination.argsFrom(savedStateHandle).retry
    }

    override fun onDone(pin: String) {
        coordinator.onPinEntered(pin)
    }

    override fun onNavigationButtonClicked() {
        if (retry) {
            coordinator.cancelIdentification()
        } else {
            coordinator.pop()
        }
    }
}

class PreviewIdentificationPersonalPinViewModel(
    override val retry: Boolean
) : IdentificationPersonalPinViewModelInterface {
    override fun onDone(pin: String) {}
    override fun onNavigationButtonClicked() {}
}

@Preview
@Composable
fun PreviewIdentificationPersonalPin() {
    UseIdTheme {
        IdentificationPersonalPin(viewModel = PreviewIdentificationPersonalPinViewModel(false))
    }
}

@Preview
@Composable
fun PreviewIdentificationPersonalPinRetry() {
    UseIdTheme {
        IdentificationPersonalPin(viewModel = PreviewIdentificationPersonalPinViewModel(true))
    }
}
