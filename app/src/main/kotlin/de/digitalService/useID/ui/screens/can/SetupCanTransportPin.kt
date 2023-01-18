package de.digitalService.useID.ui.screens.can

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
import de.digitalService.useID.ui.coordinators.CanCoordinator
import de.digitalService.useID.ui.coordinators.SetupCoordinator
import de.digitalService.useID.ui.theme.UseIdTheme
import javax.inject.Inject

@Destination
@Composable
fun SetupCanTransportPin(
    modifier: Modifier = Modifier,
    viewModel: SetupCanTransportPinViewModelInterface = hiltViewModel<SetupCanTransportPinViewModel>()
) {
    StandardNumberEntryScreen(
        title = stringResource(id = R.string.firstTimeUser_transportPIN_title),
        body = stringResource(id = R.string.firstTimeUser_transportPIN_body),
        attempts = 1,
        navigationButton = NavigationButton(
            icon = NavigationIcon.Back,
            onClick = viewModel::onBack,
            confirmation = null,
        ),
        inputType = InputType.TransportPin,
        entryFieldDescription = "",
        onDone = viewModel::onDoneClicked)
}

interface SetupCanTransportPinViewModelInterface {
    val identificationPending: Boolean

    fun onDoneClicked(pin: String)
    fun onBack()
}

@HiltViewModel
class SetupCanTransportPinViewModel @Inject constructor(
    private val canCoordinator: CanCoordinator,
    private val setupCoordinator: SetupCoordinator
) :
    ViewModel(), SetupCanTransportPinViewModelInterface {

    override val identificationPending: Boolean
        get() = setupCoordinator.identificationPending

    override fun onDoneClicked(pin: String) {
        canCoordinator.onPinEntered(pin)
    }

    override fun onBack() {
        canCoordinator.onBack()
    }
}

private class PreviewSetupCanTransportPinViewModel(
    override val identificationPending: Boolean
): SetupCanTransportPinViewModelInterface {
    override fun onDoneClicked(pin: String) {}
    override fun onBack() {}
}

@Preview(widthDp = 300, showBackground = true)
@Composable
private fun PreviewSetupCanTransportPinWithoutAttemptsNarrowDevice() {
    UseIdTheme {
        SetupCanTransportPin(viewModel = PreviewSetupCanTransportPinViewModel(false))
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewSetupCanTransportPinWithoutAttempts() {
    UseIdTheme {
        SetupCanTransportPin(viewModel = PreviewSetupCanTransportPinViewModel(false))
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewSetupCanTransportPinRetry() {
    UseIdTheme {
        SetupCanTransportPin(viewModel = PreviewSetupCanTransportPinViewModel(false))
    }
}
