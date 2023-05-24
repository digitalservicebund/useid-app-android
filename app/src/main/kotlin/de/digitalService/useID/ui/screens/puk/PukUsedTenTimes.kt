package de.digitalService.useID.ui.screens.puk

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import com.ramcosta.composedestinations.annotation.Destination
import dagger.hilt.android.lifecycle.HiltViewModel
import de.digitalService.useID.R
import de.digitalService.useID.ui.components.CardScreen
import de.digitalService.useID.ui.components.CardScreenType
import de.digitalService.useID.ui.coordinators.PukCoordinator
import de.digitalService.useID.ui.theme.UseIdTheme
import javax.inject.Inject

@Destination
@Composable
fun PukUsedTenTimes(viewModel: PukUsedTenTimesScreenViewModelInterface = hiltViewModel<PukUsedTenTimesScreenViewModel>()) {
    CardScreen(
        type = CardScreenType.ERROR,
        title = R.string.puk_usedTenTimes_title,
        cardText = R.string.puk_usedTenTimes_body,
        buttonText = R.string.puk_orderNewPin,
        confirmation = null,
        onBack = viewModel::onBackButtonClicked,
        onConfirm = viewModel::onConfirmationButtonClicked
    )
}

interface PukUsedTenTimesScreenViewModelInterface {
    fun onConfirmationButtonClicked()
    fun onBackButtonClicked()
}

@HiltViewModel
class PukUsedTenTimesScreenViewModel @Inject constructor(private val coordinator: PukCoordinator) :
    ViewModel(),
    PukUsedTenTimesScreenViewModelInterface {
    override fun onConfirmationButtonClicked() = coordinator.onOrderNewPin()
    override fun onBackButtonClicked() = coordinator.onBack()
}

//region Preview
private class PreviewPukUsedTenTimesScreenViewModel : PukUsedTenTimesScreenViewModelInterface {
    override fun onConfirmationButtonClicked() {}
    override fun onBackButtonClicked() {}
}

@Composable
@Preview
fun PreviewPukUsedTenTimesScreen() {
    UseIdTheme {
        PukUsedTenTimes(PreviewPukUsedTenTimesScreenViewModel())
    }
}
//endregion
