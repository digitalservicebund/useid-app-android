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
fun SetupPINLetter(viewModel: SetupPINLetterScreenViewModelInterface) {
    StandardScreen(
        title = stringResource(id = R.string.firstTimeUser_pinLetter_title),
        body = stringResource(id = R.string.firstTimeUser_pinLetter_body),
        imageID = R.drawable.pin_brief,
        imageScaling = ContentScale.FillWidth,
        primaryButton = BundButtonConfig(title = stringResource(id = R.string.firstTimeUser_pinLetter_no), action = viewModel::onNoPINAvailable),
        secondaryButton = BundButtonConfig(title = stringResource(id = R.string.firstTimeUser_pinLetter_yes), action = viewModel::onTransportPINAvailable)
    )
}

interface SetupPINLetterScreenViewModelInterface {
    fun onTransportPINAvailable()
    fun onNoPINAvailable()
}

class SetupPINLetterScreenViewModel(val navController: NavController): ViewModel(), SetupPINLetterScreenViewModelInterface {
    override fun onTransportPINAvailable() { navController.navigate(Screen.SetupTransportPIN.parameterizedRoute()) }
    override fun onNoPINAvailable() { navController.navigate(Screen.ResetPIN.parameterizedRoute()) }
}

//region Preview
private class PreviewSetupPINLetterScreenViewModel: SetupPINLetterScreenViewModelInterface {
    override fun onTransportPINAvailable() { }
    override fun onNoPINAvailable() { }
}

@Composable
@Preview
fun PreviewSetupPINLetterScreen() {
    UseIDTheme {
        SetupPINLetter(PreviewSetupPINLetterScreenViewModel())
    }
}
//endregion