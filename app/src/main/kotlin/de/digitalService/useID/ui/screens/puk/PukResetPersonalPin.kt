package de.digitalService.useID.ui.screens.puk

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import com.ramcosta.composedestinations.annotation.Destination
import dagger.hilt.android.lifecycle.HiltViewModel
import de.digitalService.useID.ui.coordinators.PukCoordinator
import de.digitalService.useID.ui.screens.ResetPersonalPin
import javax.inject.Inject

@Destination
@Composable
fun PukResetPersonalPin(viewModel: PukResetPersonalPinViewModel = hiltViewModel()) {
    ResetPersonalPin(viewModel::onBack)
}

@HiltViewModel
class PukResetPersonalPinViewModel @Inject constructor(
    private val coordinator: PukCoordinator
) : ViewModel() {
    fun onBack() = coordinator.onBack()
}
