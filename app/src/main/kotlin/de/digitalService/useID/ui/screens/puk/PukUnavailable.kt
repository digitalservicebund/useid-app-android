package de.digitalService.useID.ui.screens.puk

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import com.ramcosta.composedestinations.annotation.Destination
import dagger.hilt.android.lifecycle.HiltViewModel
import de.digitalService.useID.R
import de.digitalService.useID.ui.components.*
import de.digitalService.useID.ui.coordinators.PukCoordinator
import de.digitalService.useID.ui.theme.UseIdTheme
import javax.inject.Inject

@Destination
@Composable
fun PukUnavailable(viewModel: PukUnavailableScreenViewModelInterface = hiltViewModel<PukUnavailableScreenViewModel>()) {
    CardScreen(
        type = CardScreenType.INFO,
        title = R.string.puk_unavailable_title,
        cardText = R.string.puk_unavailable_body,
        buttonText = R.string.puk_orderNewPin,
        confirmation = null,
        onBack = viewModel::onBackButtonClicked,
        onConfirm = viewModel::onConfirmationButtonClicked
    )
}

interface PukUnavailableScreenViewModelInterface {
    fun onConfirmationButtonClicked()
    fun onBackButtonClicked()
}

@HiltViewModel
class PukUnavailableScreenViewModel @Inject constructor(private val coordinator: PukCoordinator) :
    ViewModel(),
    PukUnavailableScreenViewModelInterface {
    override fun onConfirmationButtonClicked() = coordinator.onOrderNewPin()
    override fun onBackButtonClicked() = coordinator.onBack()
}

//region Preview
private class PreviewPukUnavailableScreenViewModel : PukUnavailableScreenViewModelInterface {
    override fun onConfirmationButtonClicked() {}
    override fun onBackButtonClicked() {}
}

@Composable
@Preview
fun PreviewSetupAlreadyCompletedScreen() {
    UseIdTheme {
        PukUnavailable(PreviewPukUnavailableScreenViewModel())
    }
}
//endregion
