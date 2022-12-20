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
fun IdentificationCanIntro(viewModel: IdentificationCanIntroViewModelInterface = hiltViewModel<IdentificationCanIntroViewModel>()) {
    ScreenWithTopBar(
        navigationButton = NavigationButton(
            icon = NavigationIcon.Back,
            shouldShowConfirmDialog = false,
            onClick = viewModel::onBack,
            isIdentification = true
        )
    ) { topPadding ->
        StandardStaticComposition(
            title = stringResource(id = R.string.identification_can_intro_title),
            body = stringResource(id = R.string.identification_can_intro_body),
            imageID = R.drawable.illustration_id_can,
            imageScaling = ContentScale.Inside,
            primaryButton = BundButtonConfig(
                title = stringResource(id = R.string.identification_can_intro_continue),
                action = viewModel::onContinue
            ),
            modifier = Modifier.padding(top = topPadding)
        )
    }
}

interface IdentificationCanIntroViewModelInterface {
    fun onBack()
    fun onContinue()
}

@HiltViewModel
class IdentificationCanIntroViewModel @Inject constructor(
    val coordinator: CanCoordinator
): ViewModel(), IdentificationCanIntroViewModelInterface {

    override fun onBack() {
        coordinator.onBack()
    }

    override fun onContinue() {
        coordinator.continueAfterIntro()
    }
}

private class PreviewIdentificationCanIntroViewModel: IdentificationCanIntroViewModelInterface {
    override fun onBack() {}
    override fun onContinue() {}
}

@Preview
@Composable
private fun Preview() {
    UseIdTheme {
        IdentificationCanIntro(PreviewIdentificationCanIntroViewModel())
    }
}
