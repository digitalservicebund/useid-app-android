package de.digitalService.useID.ui.composables.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import de.digitalService.useID.R
import de.digitalService.useID.ui.theme.UseIDTheme

@Composable
fun FirstTimeUserPINLetterScreen(viewModel: FirstTimeUserPINLetterScreenViewModelInterface) {
    OnboardingScreen(
        title = stringResource(id = R.string.firstTimeUser_pinLetter_title),
        body = stringResource(id = R.string.firstTimeUser_pinLetter_body),
        imageID = R.drawable.pin_brief,
        imageScaling = ContentScale.FillWidth,
        primaryButton = BundButtonConfig(title = stringResource(id = R.string.firstTimeUser_pinLetter_no), action = viewModel::onNoPINAvailable),
        secondaryButton = BundButtonConfig(title = stringResource(id = R.string.firstTimeUser_pinLetter_yes), action = viewModel::onTransportPINAvailable)
    )
}

interface FirstTimeUserPINLetterScreenViewModelInterface {
    fun onTransportPINAvailable()
    fun onNoPINAvailable()
}

class FirstTimeUserPINLetterScreenViewModel(val navController: NavController): ViewModel(), FirstTimeUserPINLetterScreenViewModelInterface {
    override fun onTransportPINAvailable() { navController.navigate(Screens.TRANSPORT_PIN_SCREEN.name) }
    override fun onNoPINAvailable() { navController.navigate(Screens.RESET_PIN_SCREEN.name) }
}

//region Preview
class PreviewFirstTimeUserPINLetterScreenViewModel: FirstTimeUserPINLetterScreenViewModelInterface {
    override fun onTransportPINAvailable() { }
    override fun onNoPINAvailable() { }
}

@Composable
@Preview
fun PreviewFirstTimeUserPINLetterScreen() {
    UseIDTheme {
        FirstTimeUserPINLetterScreen(PreviewFirstTimeUserPINLetterScreenViewModel())
    }
}
//endregion