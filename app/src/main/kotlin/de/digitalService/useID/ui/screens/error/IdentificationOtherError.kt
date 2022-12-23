package de.digitalService.useID.ui.screens.error

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.ramcosta.composedestinations.annotation.Destination
import dagger.hilt.android.lifecycle.HiltViewModel
import de.digitalService.useID.R
import de.digitalService.useID.ui.components.ScanErrorScreen
import de.digitalService.useID.ui.coordinators.AppCoordinator
import de.digitalService.useID.ui.coordinators.IdentificationCoordinator
import de.digitalService.useID.ui.screens.destinations.IdentificationOtherErrorDestination
import javax.inject.Inject

@Destination
@Composable
fun IdentificationOtherError(viewModel: IdentificationOtherErrorViewModel = hiltViewModel()) {
    ScanErrorScreen(
        titleResId = R.string.scanError_unknown_title,
        bodyResId = R.string.scanError_unknown_body,
        buttonTitleResId = R.string.identification_fetchMetadataError_retry,
        confirmNavigationButtonDialog = true,
        onNavigationButtonClicked = viewModel::onCancelButtonClicked,
        onButtonClicked = viewModel::onRetryButtonClicked,
        isIdentification = true
    )
}

@HiltViewModel
class IdentificationOtherErrorViewModel @Inject constructor(
    private val coordinator: IdentificationCoordinator
) : ViewModel() {

    fun onRetryButtonClicked() {
        coordinator.retryIdentification()
    }

    fun onCancelButtonClicked() {
        coordinator.cancelIdentification()
    }
}
