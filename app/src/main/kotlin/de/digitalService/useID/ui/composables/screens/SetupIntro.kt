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
fun SetupIntro(viewModel: SetupIntroViewModelInterface) {
    StandardScreen(
        title = stringResource(id = R.string.firstTimeUser_intro_title),
        body = stringResource(id = R.string.firstTimeUser_intro_body),
        imageID = R.drawable.eids,
        imageScaling = ContentScale.Inside,
        primaryButton = BundButtonConfig(title = stringResource(id = R.string.firstTimeUser_intro_no), action = viewModel::onFirstTimeUsage),
        secondaryButton = BundButtonConfig(title = stringResource(id = R.string.firstTimeUser_intro_yes), action = viewModel::onNonFirstTimeUsage)
    )
}

interface SetupIntroViewModelInterface {
    fun onFirstTimeUsage()
    fun onNonFirstTimeUsage()
}

class SetupIntroViewModel(val navController: NavController): ViewModel(), SetupIntroViewModelInterface {
    override fun onFirstTimeUsage() {
        navController.navigate(Screen.SetupPINLetter.parameterizedRoute())
    }

    override fun onNonFirstTimeUsage() {
    }
}

//region Preview
private class PreviewSetupIntroViewModel(): SetupIntroViewModelInterface {
    override fun onFirstTimeUsage() { }
    override fun onNonFirstTimeUsage() { }
}

@Composable
@Preview
fun PreviewSetupIntro() {
    UseIDTheme {
        SetupIntro(PreviewSetupIntroViewModel())
    }
}
//endregion