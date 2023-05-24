package de.digitalService.useID.ui.screens.puk

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
import de.digitalService.useID.ui.coordinators.PukCoordinator
import de.digitalService.useID.ui.theme.UseIdTheme
import javax.inject.Inject

@Destination
@Composable
fun PukPinForgotten(viewModel: PukPinForgottenViewModelInterface = hiltViewModel<PukPinForgottenViewModel>()) {
    ScreenWithTopBar(
        navigationButton = NavigationButton(
            icon = NavigationIcon.Cancel,
            confirmation = Flow.Identification,
            onClick = viewModel::onCancelIdentification
        )
    ) { topPadding ->
        StandardStaticComposition(
            title = stringResource(id = R.string.puk_pinForgotten_title),
            body = stringResource(id = R.string.puk_pinForgotten_body),
            imageId = R.drawable.illustration_id_confused,
            imageScaling = ContentScale.Inside,
            primaryButton = BundButtonConfig(
                title = stringResource(id = R.string.puk_pinForgotten_orderNewPin),
                action = viewModel::onOrderNewPin
            ),
            secondaryButton = BundButtonConfig(
                title = stringResource(id = R.string.puk_pinForgotten_retry),
                action = viewModel::onRetry
            ),
            modifier = Modifier.padding(top = topPadding)
        )
    }
}

interface PukPinForgottenViewModelInterface {
    fun onCancelIdentification()
    fun onOrderNewPin()
    fun onRetry()
}

@HiltViewModel
class PukPinForgottenViewModel @Inject constructor(
    private val coordinator: PukCoordinator
) : ViewModel(), PukPinForgottenViewModelInterface {

    override fun onCancelIdentification() {
        coordinator.cancelPukFlow()
    }

    override fun onOrderNewPin() {
        coordinator.onOrderNewPin()
    }

    override fun onRetry() {
        coordinator.onProceedWithPukFlow()
    }
}

private class PreviewPukPinForgottenViewModel : PukPinForgottenViewModelInterface {
    override fun onCancelIdentification() {}
    override fun onOrderNewPin() {}
    override fun onRetry() {}
}

@Preview
@Composable
private fun PukPinForgottenPreview() {
    UseIdTheme {
        PukPinForgotten(PreviewPukPinForgottenViewModel())
    }
}
