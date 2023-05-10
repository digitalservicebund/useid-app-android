package de.digitalService.useID.ui.screens.pincheck

import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import com.ramcosta.composedestinations.annotation.Destination
import dagger.hilt.android.lifecycle.HiltViewModel
import de.digitalService.useID.R
import de.digitalService.useID.ui.components.Flow
import de.digitalService.useID.ui.components.NavigationButton
import de.digitalService.useID.ui.components.NavigationIcon
import de.digitalService.useID.ui.components.pin.InputType
import de.digitalService.useID.ui.components.pin.StandardNumberEntryScreen
import de.digitalService.useID.ui.coordinators.CheckPinCoordinator
import de.digitalService.useID.ui.theme.UseIdTheme
import javax.inject.Inject

@Destination
@Composable
fun CheckPersonalPin(
    viewModel: CheckPersonalPinViewModelInterface = hiltViewModel<CheckPersonalPinViewModel>()
) {
    StandardNumberEntryScreen(
        title = stringResource(id = R.string.identification_personalPIN_title),
        entryFieldDescription = stringResource(id = R.string.identification_personalPIN_PINTextFieldDescription),
        navigationButton = NavigationButton(
            icon = NavigationIcon.Back,
            onClick = viewModel::onNavigationButtonClicked,
            confirmation = null
        ),
        inputType = InputType.Pin,
        onDone = viewModel::onDone
    )
}

interface CheckPersonalPinViewModelInterface {

    fun onDone(pin: String)
    fun onNavigationButtonClicked()
}

@HiltViewModel
class CheckPersonalPinViewModel @Inject constructor(
    private val checkPinCoordinator: CheckPinCoordinator,
) : ViewModel(), CheckPersonalPinViewModelInterface {

    override fun onDone(pin: String) {
        checkPinCoordinator.onPinEntered(pin)
    }

    override fun onNavigationButtonClicked() {
        checkPinCoordinator.onBack()
    }
}

class PreviewCheckPersonalPinViewModel : CheckPersonalPinViewModelInterface {
    override fun onDone(pin: String) {}
    override fun onNavigationButtonClicked() {}
}

@Preview
@Composable
fun PreviewCheckPersonalPin() {
    UseIdTheme {
        CheckPersonalPin(viewModel = PreviewCheckPersonalPinViewModel())
    }
}
