package de.digitalService.useID.ui.composables.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import com.ramcosta.composedestinations.annotation.Destination
import dagger.hilt.android.lifecycle.HiltViewModel
import de.digitalService.useID.R
import de.digitalService.useID.ui.coordinators.SetupCoordinator
import de.digitalService.useID.ui.theme.UseIDTheme
import javax.inject.Inject

@Destination
@Composable
fun SetupPersonalPINIntro(viewModel: SetupPersonalPINIntroViewModelInterface = hiltViewModel<SetupPersonalPINIntroViewModel>()) {
    StandardStaticComposition(
        title = stringResource(id = R.string.firstTimeUser_personalPINIntro_title),
        body = stringResource(id = R.string.firstTimeUser_personalPINIntro_body),
        imageID = R.drawable.eids,
        imageScaling = ContentScale.Inside,
        primaryButton = BundButtonConfig(title = stringResource(id = R.string.firstTimeUser_personalPINIntro_continue), action = viewModel::onSetPIN)
    )
}

interface SetupPersonalPINIntroViewModelInterface {
    fun onSetPIN()
}

@HiltViewModel
class SetupPersonalPINIntroViewModel @Inject constructor(private val coordinator: SetupCoordinator) : ViewModel(), SetupPersonalPINIntroViewModelInterface {
    override fun onSetPIN() {
        coordinator.onPersonalPINIntroFinished()
    }
}

//region Preview
private class PreviewSetupPersonalPINIntroViewModel : SetupPersonalPINIntroViewModelInterface {
    override fun onSetPIN() { }
}

@Preview
@Composable
fun PreviewSetupPersonalPINIntro() {
    UseIDTheme {
        SetupPersonalPINIntro(PreviewSetupPersonalPINIntroViewModel())
    }
}
//endregion
