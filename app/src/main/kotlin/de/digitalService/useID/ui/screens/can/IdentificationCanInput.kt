package de.digitalService.useID.ui.screens.can

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.text.isDigitsOnly
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import com.ramcosta.composedestinations.annotation.Destination
import dagger.hilt.android.lifecycle.HiltViewModel
import de.digitalService.useID.R
import de.digitalService.useID.getLogger
import de.digitalService.useID.ui.components.*
import de.digitalService.useID.ui.components.pin.StandardPinScreen
import de.digitalService.useID.ui.coordinators.CanCoordinator
import de.digitalService.useID.ui.theme.UseIdTheme
import javax.inject.Inject

@Destination
@Composable
fun IdentificationCanInput(viewModel: IdentificationCanInputViewModelInterface = hiltViewModel<IdentificationCanInputViewModel>()) {
    StandardPinScreen(
        header = stringResource(id = R.string.identification_can_input_title),
        description = stringResource(id = R.string.identification_can_input_body),
        pinEntryDescription = stringResource(id = R.string.identification_can_input_canInputLabel),
        pin = viewModel.can,
        onNavigationButtonBackClick = viewModel::onBack,
        onInitialize = viewModel::onInitialize,
        onValueChanged = viewModel::userInputCan,
        obfuscation = false,
        onDone = viewModel::onContinue)
}

interface IdentificationCanInputViewModelInterface {
    val can: String

    fun onInitialize()
    fun userInputCan(value: String)
    fun onBack()
    fun onContinue()
}

@HiltViewModel
class IdentificationCanInputViewModel @Inject constructor(
    val coordinator: CanCoordinator
): ViewModel(), IdentificationCanInputViewModelInterface {
    private val logger by getLogger()

    override var can: String by mutableStateOf("")
        private set

    override fun onInitialize() {
        can = ""
    }

    override fun userInputCan(value: String) {
        if (!value.isDigitsOnly()) {
            return
        }

        can = value
    }

    override fun onBack() {
        coordinator.onBack()
    }

    override fun onContinue() {
        logger.debug("ON:CONTINUE!")
        if (can.length == 6) {
            logger.debug("CALLBACK TO COORDINATOR!")
            coordinator.onCanEntered(can)
        }
    }
}

private class PreviewIdentificationCanInputViewModel: IdentificationCanInputViewModelInterface {
    override val can: String = "123"
    override fun onInitialize() {}
    override fun userInputCan(value: String) {}
    override fun onBack() {}
    override fun onContinue() {}
}

@Preview
@Composable
private fun Preview() {
    UseIdTheme {
        IdentificationCanInput(PreviewIdentificationCanInputViewModel())
    }
}
