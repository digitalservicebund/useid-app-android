package de.digitalService.useID.ui.screens.identification

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.ramcosta.composedestinations.annotation.Destination
import dagger.hilt.android.lifecycle.HiltViewModel
import de.digitalService.useID.ui.components.*
import de.digitalService.useID.ui.coordinators.IdentificationCoordinator
import javax.inject.Inject

@Destination
@Composable
fun IdentificationSuccess(viewModel: IdentificationSuccessViewModelInterface = hiltViewModel<IdentificationSuccessViewModel>()) {
    ScreenWithTopBar(
        navigationButton = NavigationButton(
            icon = NavigationIcon.Back,
            confirmation = null,
            onClick = viewModel::onBack
        )
    ) { topPadding ->
        StandardStaticComposition(
            title = "Identifizierung abgeschlossen",
            body = "Sie haben sich erfolgreich ausgewiesen.",
            primaryButton = BundButtonConfig(
                title = "Schließen",
                action = viewModel::onFinish
            ),
            modifier = Modifier.padding(top = topPadding)
        )
    }
}

interface IdentificationSuccessViewModelInterface {
    fun onBack()
    fun onFinish()
}

@HiltViewModel
class IdentificationSuccessViewModel @Inject constructor(
    private val coordinator: IdentificationCoordinator
): ViewModel(), IdentificationSuccessViewModelInterface {
    override fun onBack() {
        coordinator.onBack()
    }

    override fun onFinish() {
        coordinator.confirmFinish()
    }
}
