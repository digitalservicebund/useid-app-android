package de.digitalService.useID.ui.screens.error.viewModel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import de.digitalService.useID.ui.coordinators.IdentificationCoordinator
import javax.inject.Inject

@HiltViewModel
class IdentificationCardErrorViewModel @Inject constructor(
    private val coordinator: IdentificationCoordinator
) : ViewModel() {

    fun onNavigationButtonClicked() {
        coordinator.cancelIdentification()
    }

    fun onButtonClicked() {
        coordinator.cancelIdentification()
    }
}
