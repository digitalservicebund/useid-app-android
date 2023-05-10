package de.digitalService.useID.ui.screens.error.viewModel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import de.digitalService.useID.ui.coordinators.ChangePinCoordinator
import de.digitalService.useID.ui.coordinators.CheckPinCoordinator
import javax.inject.Inject

@HiltViewModel
class CheckCardErrorViewModel @Inject constructor(
    private val checkPinCoordinator: CheckPinCoordinator
) : ViewModel() {

    fun onNavigationButtonClicked() {
        checkPinCoordinator.cancelPinCheck()
    }

    fun onButtonClicked() {
        checkPinCoordinator.goToPinbrief()
    }
}
