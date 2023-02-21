package de.digitalService.useID.ui.screens.setup

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import com.ramcosta.composedestinations.annotation.Destination
import dagger.hilt.android.lifecycle.HiltViewModel
import de.digitalService.useID.ui.coordinators.SetupCoordinator
import javax.inject.Inject

@Destination
@Composable
fun SetupResetPersonalPin(viewModel: SetupResetPersonalPinViewModel = hiltViewModel()) {
    ResetPersonalPin(viewModel::onBack)
}

@HiltViewModel
class SetupResetPersonalPinViewModel @Inject constructor(
    private val setupCoordinator: SetupCoordinator
): ViewModel() {
    fun onBack() = setupCoordinator.onBackClicked()
}
