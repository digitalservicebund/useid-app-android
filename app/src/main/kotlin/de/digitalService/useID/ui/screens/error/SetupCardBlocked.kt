package de.digitalService.useID.ui.screens.error

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import com.ramcosta.composedestinations.annotation.Destination
import de.digitalService.useID.R
import de.digitalService.useID.ui.components.ScanErrorScreen
import de.digitalService.useID.ui.screens.error.viewModel.SetupCardErrorViewModel

@Destination
@Composable
fun SetupCardBlocked(viewModel: SetupCardErrorViewModel = hiltViewModel()) {
    ScanErrorScreen(
        titleResId = R.string.scanError_cardBlocked_title,
        bodyResId = R.string.scanError_cardBlocked_body,
        buttonTitleResId = R.string.identification_fetchMetadataError_retry,
        onNavigationButtonTapped = viewModel::onNavigationButtonTapped,
        onButtonTapped = viewModel::onButtonTapped
    )
}
