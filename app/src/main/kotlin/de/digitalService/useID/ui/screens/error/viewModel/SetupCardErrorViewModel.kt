package de.digitalService.useID.ui.screens.error.viewModel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import de.digitalService.useID.ui.coordinators.ChangePinCoordinator
import javax.inject.Inject

@HiltViewModel
class SetupCardErrorViewModel @Inject constructor(
    private val changePinCoordinator: ChangePinCoordinator
) : ViewModel() {

    fun onNavigationButtonClicked() {
        changePinCoordinator.cancelPinManagement()
    }

    fun onButtonClicked() {
        changePinCoordinator.cancelPinManagement()
    }
}
