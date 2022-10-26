package de.digitalService.useID.ui.screens.error.viewModel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import de.digitalService.useID.idCardInterface.IDCardManager
import de.digitalService.useID.ui.coordinators.SetupCoordinator
import javax.inject.Inject

@HiltViewModel
class SetupCardErrorViewModel @Inject constructor(
    private val setupCoordinator: SetupCoordinator,
    private val idCardManager: IDCardManager
) : ViewModel() {

    fun onNavigationButtonTapped() {
        idCardManager.cancelTask()
        setupCoordinator.cancelSetup()
    }

    fun onButtonTapped() {
        idCardManager.cancelTask()
        setupCoordinator.cancelSetup()
    }
}
