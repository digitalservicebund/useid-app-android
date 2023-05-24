package de.digitalService.useID.ui.screens.setup

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import com.ramcosta.composedestinations.annotation.Destination
import dagger.hilt.android.lifecycle.HiltViewModel
import de.digitalService.useID.R
import de.digitalService.useID.ui.components.CardScreen
import de.digitalService.useID.ui.components.CardScreenType
import de.digitalService.useID.ui.coordinators.SetupCoordinator
import de.digitalService.useID.ui.theme.UseIdTheme
import javax.inject.Inject

@Destination
@Composable
fun AlreadySetupConfirmation(viewModel: AlreadySetupConfirmationScreenViewModelInterface = hiltViewModel<AlreadySetupConfirmationScreenViewModel>()) {
    CardScreen(
        type = CardScreenType.SUCCESS,
        title = R.string.firstTimeUser_alreadySetupConfirmation_title,
        cardText = R.string.firstTimeUser_alreadySetupConfirmation_box,
        buttonText = R.string.firstTimeUser_alreadySetupConfirmation_close,
        confirmation = null,
        onBack = viewModel::onBackButtonClicked,
        onConfirm = viewModel::onConfirmationButtonClicked
    )
}

interface AlreadySetupConfirmationScreenViewModelInterface {
    fun onConfirmationButtonClicked()
    fun onBackButtonClicked()
}

@HiltViewModel
class AlreadySetupConfirmationScreenViewModel @Inject constructor(private val coordinator: SetupCoordinator) :
    ViewModel(),
    AlreadySetupConfirmationScreenViewModelInterface {
    override fun onConfirmationButtonClicked() = coordinator.confirmAlreadySetUp()
    override fun onBackButtonClicked() = coordinator.onBackClicked()
}

//region Preview
private class PreviewAlreadySetupConfirmationScreenViewModel : AlreadySetupConfirmationScreenViewModelInterface {
    override fun onConfirmationButtonClicked() {}
    override fun onBackButtonClicked() {}
}

@Composable
@Preview
fun PreviewSetupAlreadyCompletedScreen() {
    UseIdTheme {
        AlreadySetupConfirmation(PreviewAlreadySetupConfirmationScreenViewModel())
    }
}
//endregion
