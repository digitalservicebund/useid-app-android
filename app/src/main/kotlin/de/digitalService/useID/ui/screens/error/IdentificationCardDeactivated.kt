package de.digitalService.useID.ui.screens.error

import androidx.compose.runtime.Composable
import com.ramcosta.composedestinations.annotation.Destination
import de.digitalService.useID.R
import de.digitalService.useID.idCardInterface.IDCardManager
import de.digitalService.useID.ui.components.ScanErrorScreen
import de.digitalService.useID.ui.coordinators.IdentificationCoordinator

@Destination
@Composable
fun IdentificationCardDeactivated(coordinator: IdentificationCoordinator, idCardManager: IDCardManager) {
    ScanErrorScreen(
        titleResId = R.string.scanError_cardDeactivated_title,
        bodyResId = R.string.scanError_cardDeactivated_body,
        buttonTitleResId = R.string.scanError_close,
        onNavigationButtonTapped = coordinator::cancelIdentification,
        onButtonTapped = coordinator::cancelIdentification
    )
}

