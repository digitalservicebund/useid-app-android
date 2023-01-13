package de.digitalService.useID.ui.screens.setup

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
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
import de.digitalService.useID.ui.components.ScreenWithTopBar
import de.digitalService.useID.ui.components.pin.InputType
import de.digitalService.useID.ui.components.pin.StandardNumberEntryScreen
import de.digitalService.useID.ui.coordinators.PinManagementCoordinator
import de.digitalService.useID.ui.screens.destinations.SetupTransportPinDestination
import de.digitalService.useID.ui.theme.UseIdTheme
import kotlinx.coroutines.delay
import javax.inject.Inject

@Destination(navArgsDelegate = SetupTransportPinNavArgs::class)
@Composable
fun SetupTransportPin(
    modifier: Modifier = Modifier,
    viewModel: SetupTransportPinViewModelInterface = hiltViewModel<SetupTransportPinViewModel>()
) {
    val icon = if (viewModel.retry) {
        NavigationIcon.Cancel
    } else {
        NavigationIcon.Back
    }

    val titleString = if (viewModel.retry) {
        stringResource(id = R.string.firstTimeUser_incorrectTransportPIN_title)
    } else {
        stringResource(id = R.string.firstTimeUser_transportPIN_title)
    }

    StandardNumberEntryScreen(
        title = titleString,
        attempts = if (viewModel.retry) 2 else null,
        navigationButton = NavigationButton(
            icon = icon,
            onClick = viewModel::onNavigationButtonClicked,
            confirmation = Flow.Setup.takeIf { viewModel.retry },
            contentDescription = titleString
        ),
        inputType = InputType.TransportPin,
        entryFieldDescription = "",
        onDone = viewModel::onDoneClicked)
}

data class SetupTransportPinNavArgs(
    val retry: Boolean
)

interface SetupTransportPinViewModelInterface {
    val retry: Boolean

    fun onDoneClicked(pin: String)
    fun onNavigationButtonClicked()
}

@HiltViewModel
class SetupTransportPinViewModel @Inject constructor(
    private val coordinator: PinManagementCoordinator,
    savedStateHandle: SavedStateHandle
) :
    ViewModel(), SetupTransportPinViewModelInterface {
    override val retry: Boolean

    init {
        retry = SetupTransportPinDestination.argsFrom(savedStateHandle).retry
    }

    override fun onDoneClicked(pin: String) {
        coordinator.setOldPin(pin)
    }

    override fun onNavigationButtonClicked() {
        if (retry) {
            coordinator.cancelPinManagement()
        } else {
            coordinator.onBack()
        }
    }
}

private class PreviewSetupTransportPinViewModel(
    override val retry: Boolean
) : SetupTransportPinViewModelInterface {
    override fun onDoneClicked(pin: String) {}
    override fun onNavigationButtonClicked() {}
}

@Preview(widthDp = 300, showBackground = true)
@Composable
private fun PreviewSetupTransportPinWithoutAttemptsNarrowDevice() {
    UseIdTheme {
        SetupTransportPin(viewModel = PreviewSetupTransportPinViewModel(false))
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewSetupTransportPinWithoutAttempts() {
    UseIdTheme {
        SetupTransportPin(viewModel = PreviewSetupTransportPinViewModel(false))
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewSetupTransportPinRetry() {
    UseIdTheme {
        SetupTransportPin(viewModel = PreviewSetupTransportPinViewModel(true))
    }
}
