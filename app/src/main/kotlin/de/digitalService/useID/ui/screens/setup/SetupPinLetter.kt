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

@Destination
@Composable
fun SetupPinLetter(viewModel: SetupPinLetterScreenViewModelInterface = hiltViewModel<SetupPinLetterViewModel>()) {
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
                action = viewModel::onTransportPinAvailable
            ),
            secondaryButton = BundButtonConfig(
                title = stringResource(id = R.string.firstTimeUser_pinLetter_requestLetter),
                action = viewModel::onNoPinAvailable
            ),
            modifier = Modifier.padding(top = topPadding)
        )
    }
}

interface SetupPinLetterScreenViewModelInterface {
    fun onTransportPinAvailable()
    fun onNoPinAvailable()
    fun onBackButtonTapped()
}

@HiltViewModel
class SetupPinLetterViewModel @Inject constructor(private val coordinator: SetupCoordinator) :
    ViewModel(),
    SetupPinLetterScreenViewModelInterface {
    override fun onTransportPinAvailable() = coordinator.setupWithPinLetter()
    override fun onNoPinAvailable() = coordinator.setupWithoutPinLetter()
    override fun onBackButtonTapped() = coordinator.onBackTapped()
}

//region Preview
private class PreviewSetupPinLetterScreenViewModel : SetupPinLetterScreenViewModelInterface {
    override fun onTransportPinAvailable() { }
    override fun onNoPinAvailable() { }
    override fun onBackButtonTapped() { }
}

@Composable
@Preview
fun PreviewSetupPinLetterScreen() {
    UseIDTheme {
        SetupPinLetter(PreviewSetupPinLetterScreenViewModel())
    }
}
//endregion
