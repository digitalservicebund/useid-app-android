package de.digitalService.useID.ui.screens.identification

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.ramcosta.composedestinations.annotation.Destination
import dagger.hilt.android.lifecycle.HiltViewModel
import de.digitalService.useID.R
import de.digitalService.useID.ui.components.Flow
import de.digitalService.useID.ui.components.NavigationButton
import de.digitalService.useID.ui.components.NavigationIcon
import de.digitalService.useID.ui.components.pin.StandardNumberEntryScreen
import de.digitalService.useID.ui.coordinators.IdentificationCoordinator
import de.digitalService.useID.ui.screens.destinations.IdentificationPersonalPinDestination
import de.digitalService.useID.ui.theme.UseIdTheme
import javax.inject.Inject

@Destination(navArgsDelegate = IdentificationPersonalPinNavArgs::class)
@Composable
fun IdentificationPersonalPin(
    viewModel: IdentificationPersonalPinViewModelInterface = hiltViewModel<IdentificationPersonalPinViewModel>()
) {
    StandardNumberEntryScreen(
        title = stringResource(id = R.string.identification_personalPIN_title),
        errorMessage = stringResource(id = R.string.identification_personalPIN_error_incorrectPIN).takeIf { viewModel.retry },
        entryFieldDescription = stringResource(id = R.string.identification_personalPIN_PINTextFieldDescription),
        navigationButton = NavigationButton(
            icon = if (viewModel.retry) NavigationIcon.Cancel else NavigationIcon.Back,
            onClick = viewModel::onNavigationButtonClicked,
            confirmation = Flow.Identification.takeIf { viewModel.retry }
        ),
        attempts = 2.takeIf { viewModel.retry },
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
    private val identificationCoordinator: IdentificationCoordinator,
    savedStateHandle: SavedStateHandle
) : ViewModel(), IdentificationPersonalPinViewModelInterface {

    override val retry: Boolean

    init {
        retry = IdentificationPersonalPinDestination.argsFrom(savedStateHandle).retry
    }

    override fun onDone(pin: String) {
        identificationCoordinator.setPin(pin)
    }

    override fun onNavigationButtonClicked() {
        if (retry) {
            identificationCoordinator.cancelIdentification()
        } else {
            identificationCoordinator.onBack()
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
