package de.digitalService.useID.ui.screens.pincheck

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import com.ramcosta.composedestinations.annotation.Destination
import dagger.hilt.android.lifecycle.HiltViewModel
import de.digitalService.useID.ui.coordinators.CheckPinCoordinator
import de.digitalService.useID.ui.coordinators.SetupCoordinator
import de.digitalService.useID.ui.screens.setup.ResetPersonalPin
import javax.inject.Inject

@Destination
@Composable
fun CheckResetPersonalPin(viewModel: SetupResetPersonalPinViewModel = hiltViewModel()) {
    ResetPersonalPin(viewModel::onBack)
}

@HiltViewModel
class SetupResetPersonalPinViewModel @Inject constructor(
    private val checkPinCoordinator: CheckPinCoordinator
) : ViewModel() {
    fun onBack() = checkPinCoordinator.onBack()
}
