package de.digitalService.useID.ui.screens.setup

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
import de.digitalService.useID.ui.coordinators.SetupCoordinator
import de.digitalService.useID.ui.theme.UseIDTheme
import javax.inject.Inject

@Destination(
    route = "firstTimeUser/PINLetter"
)
@Composable
fun SetupPINLetter(viewModel: SetupPINLetterScreenViewModelInterface = hiltViewModel<SetupPINLetterViewModel>()) {
    ScreenWithTopBar(
        navigationButton = NavigationButton(icon = NavigationIcon.Back, onClick = viewModel::onBackButtonTapped)
    ) { topPadding ->
        StandardStaticComposition(
            title = stringResource(id = R.string.firstTimeUser_pinLetter_title),
            body = stringResource(id = R.string.firstTimeUser_pinLetter_body),
            imageID = R.drawable.pin_letter,
            imageScaling = ContentScale.FillWidth,
            primaryButton = BundButtonConfig(
                title = stringResource(id = R.string.firstTimeUser_pinLetter_letterPresent),
                action = viewModel::onTransportPINAvailable
            ),
            secondaryButton = BundButtonConfig(
                title = stringResource(id = R.string.firstTimeUser_pinLetter_requestLetter),
                action = viewModel::onNoPINAvailable
            ),
            modifier = Modifier.padding(top = topPadding)
        )
    }
}

interface SetupPINLetterScreenViewModelInterface {
    fun onTransportPINAvailable()
    fun onNoPINAvailable()
    fun onBackButtonTapped()
}

@HiltViewModel
class SetupPINLetterViewModel @Inject constructor(private val coordinator: SetupCoordinator) :
    ViewModel(),
    SetupPINLetterScreenViewModelInterface {
    override fun onTransportPINAvailable() = coordinator.setupWithPINLetter()
    override fun onNoPINAvailable() = coordinator.setupWithoutPINLetter()
    override fun onBackButtonTapped() = coordinator.onBackTapped()
}

//region Preview
private class PreviewSetupPINLetterScreenViewModel : SetupPINLetterScreenViewModelInterface {
    override fun onTransportPINAvailable() { }
    override fun onNoPINAvailable() { }
    override fun onBackButtonTapped() { }
}

@Composable
@Preview
fun PreviewSetupPINLetterScreen() {
    UseIDTheme {
        SetupPINLetter(PreviewSetupPINLetterScreenViewModel())
    }
}
//endregion
