package de.digitalService.useID.ui.screens.can

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import com.ramcosta.composedestinations.annotation.Destination
import dagger.hilt.android.lifecycle.HiltViewModel
import de.digitalService.useID.ui.coordinators.CanCoordinator
import de.digitalService.useID.ui.screens.setup.ResetPersonalPin
import javax.inject.Inject

@Destination
@Composable
fun CanResetPersonalPin(viewModel: CanResetPersonalPinViewModel = hiltViewModel()) {
    ResetPersonalPin(viewModel::onBack)
}

@HiltViewModel
class CanResetPersonalPinViewModel @Inject constructor(
    private val canCoordinator: CanCoordinator
): ViewModel() {
    fun onBack() = canCoordinator.onBack()
}
