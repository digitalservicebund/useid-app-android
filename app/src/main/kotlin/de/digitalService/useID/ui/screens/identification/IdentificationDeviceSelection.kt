package de.digitalService.useID.ui.screens.identification

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import com.ramcosta.composedestinations.annotation.Destination
import dagger.hilt.android.lifecycle.HiltViewModel
import de.digitalService.useID.ui.components.*
import de.digitalService.useID.ui.coordinators.IdentificationCoordinator
import javax.inject.Inject

@Destination
@Composable
fun IdentificationDeviceSelection(viewModel: IdentificationDeviceSelectionViewModelInterface = hiltViewModel<IdentificationDeviceSelectionViewModel>()) {
    ScreenWithTopBar(
        navigationButton = NavigationButton(
            icon = NavigationIcon.Cancel,
            confirmation = Flow.Identification,
            onClick = viewModel::onCancel
        )
    ) { topPadding ->
        StandardStaticComposition(
            title = "Auf welchem Gerät möchten Sie fortfahren?",
            body = "Um die Identifizierung abschließen, können Sie mit dem Vorgang entweder auf diesem Gerät fortfahren oder auf ein anderes Gerät wechseln.",
            primaryButton = BundButtonConfig(
                title = "Dieses Smartphone",
                action = viewModel::onProceedOnSameDevice
            ),
            secondaryButton = BundButtonConfig(
                title = "Anderes Gerät",
                action = viewModel::onSwitchDevice
            ),
            modifier = Modifier.padding(top = topPadding)
        )
    }
}

interface IdentificationDeviceSelectionViewModelInterface {
    fun onCancel()
    fun onProceedOnSameDevice()
    fun onSwitchDevice()
}

@HiltViewModel
class IdentificationDeviceSelectionViewModel @Inject constructor(
    private val coordinator: IdentificationCoordinator
): ViewModel(), IdentificationDeviceSelectionViewModelInterface {
    override fun onCancel() {
        coordinator.cancelIdentification()
    }

    override fun onProceedOnSameDevice() {
        coordinator.redirectOnSameDevice()
    }

    override fun onSwitchDevice() {
        coordinator.redirectOnDifferentDevice()
    }
}
