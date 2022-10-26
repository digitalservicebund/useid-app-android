package de.digitalService.useID.ui.screens.error

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.ramcosta.composedestinations.annotation.Destination
import dagger.hilt.android.lifecycle.HiltViewModel
import de.digitalService.useID.R
import de.digitalService.useID.idCardInterface.IDCardManager
import de.digitalService.useID.ui.components.ScanErrorScreen
import de.digitalService.useID.ui.coordinators.SetupCoordinator
import de.digitalService.useID.ui.screens.destinations.SetupCardUnreadableDestination
import javax.inject.Inject

@Destination(navArgsDelegate = SetupCardUnreadableNavArgs::class)
@Composable
fun SetupCardUnreadable(
    viewModel: SetupCardUnreadableViewModel = hiltViewModel()
) {
    ScanErrorScreen(
        titleResId = R.string.scanError_cardUnreadable_title,
        bodyResId = R.string.scanError_cardUnreadable_body,
        buttonTitleResId = R.string.scanError_close,
        showErrorCard = viewModel.errorCard,
        onNavigationButtonTapped = viewModel::onCancelButtonPressed,
        onButtonTapped = viewModel::onCancelButtonPressed
    )
}

@HiltViewModel
class SetupCardUnreadableViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val setupCoordinator: SetupCoordinator,
    private val idCardManager: IDCardManager
) : ViewModel() {
    val errorCard: Boolean

    init {
        val args = SetupCardUnreadableDestination.argsFrom(savedStateHandle)
        errorCard = args.errorCard
    }

    fun onCancelButtonPressed() {
        idCardManager.cancelTask()
        setupCoordinator.cancelSetup()
    }
}

data class SetupCardUnreadableNavArgs(
    val errorCard: Boolean,
)
