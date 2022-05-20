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
fun SetPINIntroScreen(viewModel: SetPINIntroScreenViewModelInterface) {
    OnboardingScreen(
        title = stringResource(id = R.string.firstTimeUser_personalPINIntro_title),
        body = stringResource(id = R.string.firstTimeUser_personalPINIntro_body),
        imageID = R.drawable.eids,
        imageScaling = ContentScale.Inside,
        primaryButton = BundButtonConfig(title = stringResource(id = R.string.firstTimeUser_personalPINIntro_continue), action = viewModel::onSetPIN)
    )
}

interface SetPINIntroScreenViewModelInterface {
    fun onSetPIN()
}

class SetPINIntroScreenViewModel(val navController: NavController, val transportPIN: String): ViewModel(), SetPINIntroScreenViewModelInterface {
    override fun onSetPIN() {
        navController.navigate(Screen.SetPIN.routeTemplate)
    }
}

//region Preview
private class PreviewSetPINIntroScreenViewModel: SetPINIntroScreenViewModelInterface {
    override fun onSetPIN() { }
}

@Preview
@Composable
fun PreviewSetPINIntroScreen() {
    UseIDTheme {
        SetPINIntroScreen(PreviewSetPINIntroScreenViewModel())
    }
}
//endregion