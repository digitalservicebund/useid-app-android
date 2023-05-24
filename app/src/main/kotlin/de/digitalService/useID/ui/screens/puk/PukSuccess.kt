package de.digitalService.useID.ui.screens.puk

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.ramcosta.composedestinations.annotation.Destination
import dagger.hilt.android.lifecycle.HiltViewModel
import de.digitalService.useID.R
import de.digitalService.useID.ui.components.CardScreen
import de.digitalService.useID.ui.components.CardScreenType
import de.digitalService.useID.ui.components.Flow
import de.digitalService.useID.ui.coordinators.PukCoordinator
import de.digitalService.useID.ui.screens.destinations.PukInputDestination
import de.digitalService.useID.ui.screens.destinations.PukSuccessDestination
import de.digitalService.useID.ui.theme.UseIdTheme
import javax.inject.Inject

@Destination(navArgsDelegate = PukSuccessNavArgs::class)
@Composable
fun PukSuccess(viewModel: PukSuccessScreenViewModelInterface = hiltViewModel<PukSuccessScreenViewModel>()) {
    CardScreen(
        type = CardScreenType.INFO,
        title = R.string.puk_success_title,
        cardText = R.string.puk_success_body,
        buttonText = R.string.puk_success_enterPinButton,
        confirmation = Flow.Identification,
        onBack = viewModel::onBackButtonClicked,
        onConfirm = viewModel::onConfirmationButtonClicked
    )
}

data class PukSuccessNavArgs(
    val flow: Flow
)

interface PukSuccessScreenViewModelInterface {
    val flow: Flow
    fun onConfirmationButtonClicked()
    fun onBackButtonClicked()
}

@HiltViewModel
class PukSuccessScreenViewModel @Inject constructor(
    private val coordinator: PukCoordinator,
    savedStateHandle: SavedStateHandle
) : ViewModel(), PukSuccessScreenViewModelInterface {

    override val flow: Flow = Flow.Identification

    init {
//        flow = PukSuccessDestination.argsFrom(savedStateHandle).flow
    }

    override fun onConfirmationButtonClicked() = coordinator.onOrderNewPin()
    override fun onBackButtonClicked() = coordinator.onBack()
}

//region Preview
private class PreviewPukSuccessScreenViewModel : PukSuccessScreenViewModelInterface {
    override val flow: Flow = Flow.Identification
    override fun onConfirmationButtonClicked() {}
    override fun onBackButtonClicked() {}
}

@Composable
@Preview
fun PreviewPukSuccessScreen() {
    UseIdTheme {
        PukSuccess(PreviewPukSuccessScreenViewModel())
    }
}
//endregion
