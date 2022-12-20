package de.digitalService.useID.ui.screens.can

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
            shouldShowConfirmDialog = true,
            onClick = viewModel::onCancelIdentification,
            isIdentification = true
        )
    ) { topPadding ->
        StandardStaticComposition(
            title = stringResource(id = R.string.identification_can_pinForgotten_title),
            body = stringResource(id = R.string.identification_can_pinForgotten_body),
            imageID = R.drawable.illustration_id_confused,
            imageScaling = ContentScale.Inside,
            primaryButton = BundButtonConfig(
                title = stringResource(id = R.string.identification_can_pinForgotten_orderNewPin),
                action = viewModel::onNewPin
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
    fun onNewPin()
    fun onRetry()
}

@HiltViewModel
class IdentificationCanPinForgottenViewModel @Inject constructor(
    val coordinator: CanCoordinator
): ViewModel(), IdentificationCanPinForgottenViewModelInterface {

    override fun onCancelIdentification() {
        coordinator.onCancelIdentification()
    }

    override fun onNewPin() {
        coordinator.onNewPin()
    }

    override fun onRetry() {
        coordinator.startCanFlow()
    }
}

private class PreviewIdentificationCanPinForgottenViewModel: IdentificationCanPinForgottenViewModelInterface {
    override fun onCancelIdentification() {}
    override fun onNewPin() {}
    override fun onRetry() {}
}

@Preview
@Composable
private fun Preview() {
    UseIdTheme {
        IdentificationCanPinForgotten(PreviewIdentificationCanPinForgottenViewModel())
    }
}
