package de.digitalService.useID.ui.screens.error

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import com.ramcosta.composedestinations.annotation.Destination
import dagger.hilt.android.lifecycle.HiltViewModel
import de.digitalService.useID.R
import de.digitalService.useID.ui.components.ScanErrorScreen
import de.digitalService.useID.ui.coordinators.PinManagementCoordinator
import javax.inject.Inject

@Destination(navArgsDelegate = SetupCardUnreadableNavArgs::class)
@Composable
fun SetupCardUnreadable(viewModel: SetupCardUnreadableViewModel = hiltViewModel()) {
    ScanErrorScreen(
        titleResId = R.string.scanError_cardUnreadable_title,
        bodyResId = R.string.scanError_cardUnreadable_body,
        buttonTitleResId = R.string.scanError_close,
        showErrorCard = false,
        onNavigationButtonClicked = viewModel::onRetryClicked,
        onButtonClicked = viewModel::onRetryClicked
    )
}

@HiltViewModel
class SetupCardUnreadableViewModel @Inject constructor(
    private val setupCoordinator: PinManagementCoordinator
) : ViewModel() {

    fun onRetryClicked() {
        setupCoordinator.retryPinManagement()
    }
}

data class SetupCardUnreadableNavArgs(
    val errorCard: Boolean
)
