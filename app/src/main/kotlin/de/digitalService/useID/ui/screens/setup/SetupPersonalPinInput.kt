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
import de.digitalService.useID.ui.components.pin.PINEntryField
import de.digitalService.useID.ui.coordinators.SetupCoordinator
import de.digitalService.useID.ui.theme.Gray300
import de.digitalService.useID.ui.theme.UseIDTheme
import kotlinx.coroutines.delay
import javax.inject.Inject

@Destination
@Composable
fun SetupPersonalPinInput(viewModel: SetupPersonalPinInputViewModelInterface = hiltViewModel<SetupPersonalPinInputViewModel>()) {
    ScreenWithTopBar(
        navigationButton = NavigationButton(
            icon = NavigationIcon.Back,
            onClick = viewModel::onNavigationButtonTapped
        )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(top = it, start = 16.dp, end = 16.dp)
                .verticalScroll(rememberScrollState())

        ) {
            val focusRequesterPIN = remember { FocusRequester() }

            LaunchedEffect(Unit) {
                viewModel.onInitialize()
                delay(400)
                focusRequesterPIN.requestFocus()
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(id = R.string.firstTimeUser_personalPIN_title),
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = stringResource(id = R.string.firstTimeUser_personalPIN_body),
                style = MaterialTheme.typography.bodySmall
            )

            Spacer(modifier = Modifier.height(32.dp))

            val pinEntryFieldDescription = stringResource(
                id = R.string.firstTimeUser_personalPIN_textFieldLabel_first,
                viewModel.pin.map { "$it " }
            )

            PINEntryField(
                value = viewModel.pin,
                digitCount = 6,
                obfuscation = true,
                spacerPosition = 3,
                onValueChanged = viewModel::userInputPIN,
                onDone = viewModel::onDonePressed,
                contentDescription = pinEntryFieldDescription,
                focusRequester = focusRequesterPIN,
                backgroundColor = Gray300,
                modifier = Modifier
                    .width(240.dp)
                    .height(56.dp)
            )
        }
    }
}

interface SetupPersonalPinInputViewModelInterface {
    val pin: String

    fun onInitialize()
    fun onDonePressed()
    fun userInputPIN(value: String)
    fun onNavigationButtonTapped()
}

@HiltViewModel
class SetupPersonalPinInputViewModel @Inject constructor(
    private val setupCoordinator: SetupCoordinator
) : ViewModel(), SetupPersonalPinInputViewModelInterface {
    override var pin: String by mutableStateOf("")

    override fun onInitialize() {
        pin = ""
    }

    override fun userInputPIN(value: String) {
        if (!value.isDigitsOnly()) {
            return
        }

        pin = value
    }

    override fun onDonePressed() {
        if (pin.length == 6) {
            setupCoordinator.onPersonalPinInput(pin)
        }
    }

    override fun onNavigationButtonTapped() {
        setupCoordinator.onBackTapped()
    }
}

private class PreviewSetupPersonalPinInputViewModel : SetupPersonalPinInputViewModelInterface {
    override val pin: String = ""
    override fun onInitialize() {}
    override fun onDonePressed() {}
    override fun userInputPIN(value: String) {}
    override fun onNavigationButtonTapped() {}
}

@Preview(showBackground = true)
@Composable
private fun PreviewSetupPersonalPinInput() {
    UseIDTheme {
        SetupPersonalPinInput(PreviewSetupPersonalPinInputViewModel())
    }
}
