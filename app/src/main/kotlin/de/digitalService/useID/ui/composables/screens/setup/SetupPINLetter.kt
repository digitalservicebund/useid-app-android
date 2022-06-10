package de.digitalService.useID.ui.composables.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import de.digitalService.useID.R
import de.digitalService.useID.ui.coordinators.SetupCoordinator
import de.digitalService.useID.ui.theme.UseIDTheme
import javax.inject.Inject

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

@HiltViewModel
class SetupPINLetterViewModel @Inject constructor(private val coordinator: SetupCoordinator) : ViewModel(), SetupPINLetterScreenViewModelInterface {
    override fun onTransportPINAvailable() { coordinator.setupWithPINLetter() }
    override fun onNoPINAvailable() { coordinator.setupWithoutPINLetter() }
}

//region Preview
private class PreviewSetupPINLetterScreenViewModel : SetupPINLetterScreenViewModelInterface {
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
