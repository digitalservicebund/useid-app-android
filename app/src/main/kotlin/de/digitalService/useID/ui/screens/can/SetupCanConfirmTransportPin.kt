package de.digitalService.useID.ui.screens.can

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.ramcosta.composedestinations.annotation.Destination
import dagger.hilt.android.lifecycle.HiltViewModel
import de.digitalService.useID.R
import de.digitalService.useID.ui.components.*
import de.digitalService.useID.ui.coordinators.CanCoordinator
import de.digitalService.useID.ui.coordinators.SetupCoordinator
import de.digitalService.useID.ui.screens.destinations.SetupCanConfirmTransportPinDestination
import de.digitalService.useID.ui.theme.UseIdTheme
import javax.inject.Inject

@Destination(navArgsDelegate = SetupCanConfirmTransportPinNavArgs::class)
@Composable
fun SetupCanConfirmTransportPin(viewModel: SetupCanConfirmTransportPinViewModelInterface = hiltViewModel<SetupCanConfirmTransportPinViewModel>()) {
    ScreenWithTopBar(
        navigationButton = NavigationButton(
            icon = NavigationIcon.Cancel,
            confirmation = if (viewModel.identificationPending) Flow.Identification else Flow.Setup,
            onClick = viewModel::onCancel
        )
    ) { topPadding ->
        StandardStaticComposition(
            title = stringResource(id = R.string.firstTimeUser_can_confirmTransportPIN_title, viewModel.transportPin),
            body = stringResource(id = R.string.firstTimeUser_can_confirmTransportPIN_body, viewModel.transportPin),
            primaryButton = BundButtonConfig(title = stringResource(id = R.string.firstTimeUser_can_confirmTransportPIN_confirmInput), viewModel::onConfirm),
            secondaryButton = BundButtonConfig(title = stringResource(id = R.string.firstTimeUser_can_confirmTransportPIN_incorrectInput), viewModel::onReenterTransportPin),
            modifier = Modifier.padding(top = topPadding)
        )
    }
}

data class SetupCanConfirmTransportPinNavArgs(
    val transportPin: String
)

interface SetupCanConfirmTransportPinViewModelInterface {
    val identificationPending: Boolean
    val transportPin: String

    fun onConfirm()
    fun onReenterTransportPin()
    fun onCancel()
}

@HiltViewModel
class SetupCanConfirmTransportPinViewModel @Inject constructor(
    private val canCoordinator: CanCoordinator,
    private val setupCoordinator: SetupCoordinator,
    savedStateHandle: SavedStateHandle
): ViewModel(), SetupCanConfirmTransportPinViewModelInterface {
    override val transportPin: String
    override val identificationPending: Boolean
        get() = setupCoordinator.identificationPending

    init {
        val args = SetupCanConfirmTransportPinDestination.argsFrom(savedStateHandle)
        transportPin = args.transportPin
    }

    override fun onConfirm() {
        canCoordinator.confirmPinInput()
    }

    override fun onReenterTransportPin() {
        canCoordinator.proceedWithThirdAttempt()
    }

    override fun onCancel() {
        canCoordinator.cancelCanFlow()
    }
}

private class PreviewSetupCanConfirmTransportPinViewModel: SetupCanConfirmTransportPinViewModelInterface {
    override val identificationPending: Boolean = false
    override val transportPin: String = "12345"
    override fun onConfirm() {}
    override fun onReenterTransportPin() {}
    override fun onCancel() {}
}

@Preview
@Composable
private fun Preview() {
    UseIdTheme {
        SetupCanConfirmTransportPin(PreviewSetupCanConfirmTransportPinViewModel())
    }
}
