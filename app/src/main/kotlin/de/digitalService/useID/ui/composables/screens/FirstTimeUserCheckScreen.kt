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
fun FirstTimeUserCheckScreen(viewModel: FirstTimeUserCheckScreenViewModelInterface) {
    OnboardingScreen(
        title = stringResource(id = R.string.firstTimeUser_intro_title),
        body = stringResource(id = R.string.firstTimeUser_intro_body),
        imageID = R.drawable.eids,
        imageScaling = ContentScale.Inside,
        primaryButton = BundButtonConfig(title = stringResource(id = R.string.firstTimeUser_intro_no), action = viewModel::onFirstTimeUsage),
        secondaryButton = BundButtonConfig(title = stringResource(id = R.string.firstTimeUser_intro_yes), action = viewModel::onNonFirstTimeUsage)
    )
}

interface FirstTimeUserCheckScreenViewModelInterface {
    fun onFirstTimeUsage()
    fun onNonFirstTimeUsage()
}

class FirstTimeUserCheckScreenViewModel(val navController: NavController): ViewModel(), FirstTimeUserCheckScreenViewModelInterface {
    override fun onFirstTimeUsage() {
        navController.navigate(Screen.FirstTimeUserPINLetterCheck.parameterizedRoute())
    }

    override fun onNonFirstTimeUsage() {
    }
}

//region Preview
private class PreviewFirstTimeUserCheckScreenViewModel(): FirstTimeUserCheckScreenViewModelInterface {
    override fun onFirstTimeUsage() { }
    override fun onNonFirstTimeUsage() { }
}

@Composable
@Preview
fun PreviewFirstTimeUserCheckScreen() {
    UseIDTheme {
        FirstTimeUserCheckScreen(PreviewFirstTimeUserCheckScreenViewModel())
    }
}
//endregion