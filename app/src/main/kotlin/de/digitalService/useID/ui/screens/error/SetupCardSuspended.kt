package de.digitalService.useID.ui.screens.error

import androidx.compose.runtime.Composable
import com.ramcosta.composedestinations.annotation.Destination
import de.digitalService.useID.R
import de.digitalService.useID.idCardInterface.IDCardManager
import de.digitalService.useID.ui.components.ScanErrorScreen
import de.digitalService.useID.ui.coordinators.SetupCoordinator

@Destination
@Composable
fun SetupCardSuspended(setupCoordinator: SetupCoordinator, idCardManager: IDCardManager) {
    ScanErrorScreen(
        titleResId = R.string.scanError_cardSuspended_title,
        bodyResId = R.string.scanError_cardSuspended_body,
        buttonTitleResId = R.string.scanError_close,
        onNavigationButtonTapped = {
            idCardManager.cancelTask()
            setupCoordinator.cancelSetup()
        },
        onButtonTapped = {
            idCardManager.cancelTask()
            setupCoordinator.cancelSetup()
        },
    )
}

