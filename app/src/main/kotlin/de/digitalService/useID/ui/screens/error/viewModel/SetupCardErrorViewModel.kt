package de.digitalService.useID.ui.screens.error.viewModel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import de.digitalService.useID.ui.coordinators.SetupCoordinator
import javax.inject.Inject

@HiltViewModel
class SetupCardErrorViewModel @Inject constructor(
    private val setupCoordinator: SetupCoordinator
) : ViewModel() {

    fun onNavigationButtonClicked() {
        setupCoordinator.cancelSetup()
    }

    fun onButtonClicked() {
        setupCoordinator.cancelSetup()
    }
}
