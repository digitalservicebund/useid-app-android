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
fun PukIntro(viewModel: PukIntroScreenViewModelInterface = hiltViewModel<PukIntroViewModel>()) {
    ScreenWithTopBar(
        navigationButton = NavigationButton(icon = NavigationIcon.Back, onClick = viewModel::onBackButtonClicked, confirmation = null)
    ) { topPadding ->
        StandardStaticComposition(
            title = stringResource(id = R.string.puk_intro_title),
            body = stringResource(id = R.string.puk_intro_body),
            imageId = R.drawable.pin_letter,
            imageScaling = ContentScale.FillWidth,
            primaryButton = BundButtonConfig(
                title = stringResource(id = R.string.puk_intro_pukAvailable),
                action = viewModel::onPukAvailable
            ),
            secondaryButton = BundButtonConfig(
                title = stringResource(id = R.string.puk_intro_pukUnavailable),
                action = viewModel::onUnavailable
            ),
            modifier = Modifier.padding(top = topPadding)
        )
    }
}

interface PukIntroScreenViewModelInterface {
    fun onPukAvailable()
    fun onUnavailable()
    fun onBackButtonClicked()
}

@HiltViewModel
class PukIntroViewModel @Inject constructor(private val coordinator: PukCoordinator) :
    ViewModel(),
    PukIntroScreenViewModelInterface {
    override fun onPukAvailable() = coordinator.onPukAvailable()
    override fun onUnavailable() = coordinator.onPukUnavailable()
    override fun onBackButtonClicked() = coordinator.onBack()
}

//region Preview
private class PreviewPukIntroScreenViewModel : PukIntroScreenViewModelInterface {
    override fun onPukAvailable() { }
    override fun onUnavailable() { }
    override fun onBackButtonClicked() { }
}

@Composable
@Preview
fun PreviewPukIntroScreen() {
    UseIdTheme {
        PukIntro(PreviewPukIntroScreenViewModel())
    }
}
//endregion
