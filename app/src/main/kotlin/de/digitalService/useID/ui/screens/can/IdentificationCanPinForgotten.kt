package de.digitalService.useID.ui.screens.can

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import com.ramcosta.composedestinations.annotation.Destination
import dagger.hilt.android.lifecycle.HiltViewModel
import de.digitalService.useID.R
import de.digitalService.useID.ui.components.*
import de.digitalService.useID.ui.coordinators.CanCoordinator
import de.digitalService.useID.ui.theme.UseIdTheme
import javax.inject.Inject

@Destination
@Composable
fun IdentificationCanPinForgotten(viewModel: IdentificationCanPinForgottenViewModelInterface = hiltViewModel<IdentificationCanPinForgottenViewModel>()) {
    ScreenWithTopBar(
        navigationButton = NavigationButton(
            icon = NavigationIcon.Cancel,
            confirmation = Flow.Identification,
            onClick = viewModel::onCancelIdentification
        )
    ) { topPadding ->
        StandardStaticComposition(
            title = stringResource(id = R.string.identification_can_pinForgotten_title),
            body = stringResource(id = R.string.identification_can_pinForgotten_body),
            imageId = R.drawable.illustration_id_confused,
            imageScaling = ContentScale.Inside,
            primaryButton = BundButtonConfig(
                title = stringResource(id = R.string.identification_can_pinForgotten_orderNewPin),
                action = viewModel::onResetPin
            ),
            secondaryButton = BundButtonConfig(
                title = stringResource(id = R.string.identification_can_pinForgotten_retry),
                action = viewModel::onRetry
            ),
            modifier = Modifier.padding(top = topPadding)
        )
    }
}

interface IdentificationCanPinForgottenViewModelInterface {
    fun onCancelIdentification()
    fun onResetPin()
    fun onRetry()
}

@HiltViewModel
class IdentificationCanPinForgottenViewModel @Inject constructor(
    private val coordinator: CanCoordinator
) : ViewModel(), IdentificationCanPinForgottenViewModelInterface {

    override fun onCancelIdentification() {
        coordinator.cancelCanFlow()
    }

    override fun onResetPin() {
        coordinator.onResetPin()
    }

    override fun onRetry() {
        coordinator.proceedWithThirdAttempt()
    }
}

private class PreviewIdentificationCanPinForgottenViewModel : IdentificationCanPinForgottenViewModelInterface {
    override fun onCancelIdentification() {}
    override fun onResetPin() {}
    override fun onRetry() {}
}

@Preview
@Composable
private fun Preview() {
    UseIdTheme {
        IdentificationCanPinForgotten(PreviewIdentificationCanPinForgottenViewModel())
    }
}
