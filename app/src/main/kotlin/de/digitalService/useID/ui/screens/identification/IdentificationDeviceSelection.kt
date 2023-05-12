package de.digitalService.useID.ui.screens.identification

import android.app.PendingIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ramcosta.composedestinations.annotation.Destination
import dagger.hilt.android.lifecycle.HiltViewModel
import de.digitalService.useID.getLogger
import de.digitalService.useID.ui.components.*
import de.digitalService.useID.ui.coordinators.IdentificationCoordinator
import kotlinx.coroutines.launch
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
//        val launcher = rememberLauncherForActivityResult(contract = ActivityResultContracts.StartIntentSenderForResult()) {
//            result ->
//            viewModel.onCredentialResult(result)
//        }
//
//        viewModel.registrationIntent?.let {
//            launcher.launch(it.intentSender)
//        }

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
//    val registrationIntent: PendingIntent?

    fun onCancel()
    fun onProceedOnSameDevice()
    fun onSwitchDevice()

    fun onCredentialResult(result: ActivityResult)
}

@HiltViewModel
class IdentificationDeviceSelectionViewModel @Inject constructor(
    private val coordinator: IdentificationCoordinator
): ViewModel(), IdentificationDeviceSelectionViewModelInterface {
    private val logger by getLogger()

//    var registrationIntent: PendingIntent? = null

//    init {
//        viewModelScope.launch {
//            coordinator.credentialRequestFlow.collect {
//                registrationIntent = it
//            }
//        }
//    }

    override fun onCancel() {
        coordinator.cancelIdentification()
    }

    override fun onProceedOnSameDevice() {
        coordinator.redirectOnSameDevice()
    }

    override fun onSwitchDevice() {
        coordinator.redirectOnDifferentDevice()
    }

    override fun onCredentialResult(result: ActivityResult) {
        logger.debug("Activity result: $result")
    }
}
