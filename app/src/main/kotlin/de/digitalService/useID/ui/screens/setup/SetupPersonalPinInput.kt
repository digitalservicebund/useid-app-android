package de.digitalService.useID.ui.screens.setup

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.text.isDigitsOnly
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import com.ramcosta.composedestinations.annotation.Destination
import dagger.hilt.android.lifecycle.HiltViewModel
import de.digitalService.useID.R
import de.digitalService.useID.ui.components.NavigationButton
import de.digitalService.useID.ui.components.NavigationIcon
import de.digitalService.useID.ui.components.ScreenWithTopBar
import de.digitalService.useID.ui.components.pin.PinEntryField
import de.digitalService.useID.ui.coordinators.SetupCoordinator
import de.digitalService.useID.ui.theme.UseIdTheme
import kotlinx.coroutines.delay
import javax.inject.Inject

@Destination
@Composable
fun SetupPersonalPinInput(viewModel: SetupPersonalPinInputViewModelInterface = hiltViewModel<SetupPersonalPinInputViewModel>()) {
    ScreenWithTopBar(
        navigationButton = NavigationButton(
            icon = NavigationIcon.Back,
            onClick = viewModel::onNavigationButtonClicked
        )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(top = it)
                .padding(horizontal = UseIdTheme.spaces.s)
                .verticalScroll(rememberScrollState())

        ) {
            val focusRequesterPin = remember { FocusRequester() }

            LaunchedEffect(Unit) {
                viewModel.onInitialize()
                delay(400)
                focusRequesterPin.requestFocus()
            }

            Spacer(modifier = Modifier.height(UseIdTheme.spaces.s))

            Text(
                text = stringResource(id = R.string.firstTimeUser_personalPIN_title),
                style = UseIdTheme.typography.headingXl,
            )

            Spacer(modifier = Modifier.height(UseIdTheme.spaces.m))

            Text(
                text = stringResource(id = R.string.firstTimeUser_personalPIN_body),
                style = UseIdTheme.typography.bodyLRegular
            )

            Spacer(modifier = Modifier.height(UseIdTheme.spaces.m))

            val pinEntryFieldDescription = stringResource(
                id = R.string.firstTimeUser_personalPIN_textFieldLabel_first,
                viewModel.pin.map { "$it " }
            )

            PinEntryField(
                value = viewModel.pin,
                digitCount = 6,
                obfuscation = true,
                spacerPosition = 3,
                onValueChanged = viewModel::userInputPin,
                onDone = viewModel::onDoneClicked,
                contentDescription = pinEntryFieldDescription,
                focusRequester = focusRequesterPin,
                backgroundColor = UseIdTheme.colors.neutrals100,
                modifier = Modifier
                    .height(56.dp)
                    .fillMaxWidth()
            )
        }
    }
}

interface SetupPersonalPinInputViewModelInterface {
    val pin: String

    fun onInitialize()
    fun onDoneClicked()
    fun userInputPin(value: String)
    fun onNavigationButtonClicked()
}

@HiltViewModel
class SetupPersonalPinInputViewModel @Inject constructor(
    private val setupCoordinator: SetupCoordinator
) : ViewModel(), SetupPersonalPinInputViewModelInterface {
    override var pin: String by mutableStateOf("")

    override fun onInitialize() {
        pin = ""
    }

    override fun userInputPin(value: String) {
        if (!value.isDigitsOnly()) {
            return
        }

        pin = value
    }

    override fun onDoneClicked() {
        if (pin.length == 6) {
            setupCoordinator.onPersonalPinInput(pin)
        }
    }

    override fun onNavigationButtonClicked() {
        setupCoordinator.onBackClicked()
    }
}

private class PreviewSetupPersonalPinInputViewModel : SetupPersonalPinInputViewModelInterface {
    override val pin: String = ""
    override fun onInitialize() {}
    override fun onDoneClicked() {}
    override fun userInputPin(value: String) {}
    override fun onNavigationButtonClicked() {}
}

@Preview(showBackground = true)
@Composable
private fun PreviewSetupPersonalPinInput() {
    UseIdTheme {
        SetupPersonalPinInput(PreviewSetupPersonalPinInputViewModel())
    }
}
