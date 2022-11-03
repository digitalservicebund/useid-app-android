package de.digitalService.useID.ui.screens.setup

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.text.isDigitsOnly
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import com.ramcosta.composedestinations.annotation.Destination
import dagger.hilt.android.lifecycle.HiltViewModel
import de.digitalService.useID.ui.components.NavigationButton
import de.digitalService.useID.ui.components.NavigationIcon
import de.digitalService.useID.ui.components.ScreenWithTopBar
import de.digitalService.useID.ui.components.pin.PINEntryField
import de.digitalService.useID.ui.coordinators.SetupCoordinator
import de.digitalService.useID.ui.theme.Gray300
import de.digitalService.useID.ui.theme.UseIDTheme
import javax.inject.Inject

@Destination
@Composable
fun SetupPersonalPinConfirm(viewModel: SetupPersonalPinConfirmViewModelInterface = hiltViewModel<SetupPersonalPinConfirmViewModel>()) {
    ScreenWithTopBar(
        navigationButton = NavigationButton(NavigationIcon.Back, viewModel::onNavigationButtonTapped)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = it, start = 16.dp, end = 16.dp)
        ) {
            val focusRequesterPIN = remember { FocusRequester() }

            LaunchedEffect(Unit) {
                focusRequesterPIN.requestFocus()
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Bestätigen Sie Ihre persönliche Ausweis-PIN.",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Nutzen Sie nicht Ihr Geburtsdatum als PIN. Vergeben Sie eine PIN, die nicht leicht zu erraten ist.",
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(32.dp))

            PINEntryField(
                value = viewModel.pin,
                digitCount = 6,
                obfuscation = true,
                spacerPosition = 3,
                onValueChanged = viewModel::userInputPIN,
                contentDescription = "pin2EntryFieldDescription",
                focusRequester = focusRequesterPIN,
                backgroundColor = Gray300,
                modifier = Modifier
                    .width(240.dp)
                    .height(56.dp)
            )
        }
    }
}

interface SetupPersonalPinConfirmViewModelInterface {
    val pin: String

    fun userInputPIN(value: String)
    fun onNavigationButtonTapped()
}

@HiltViewModel
class SetupPersonalPinConfirmViewModel @Inject constructor(
    private val setupCoordinator: SetupCoordinator
) : ViewModel(), SetupPersonalPinConfirmViewModelInterface {
    override var pin: String by mutableStateOf("")

    override fun userInputPIN(value: String) {
        if (!value.isDigitsOnly()) {
            return
        }

        pin = value

        if (pin.length == 6) {
            setupCoordinator.onPersonalPinConfirm(pin)
        }
    }

    override fun onNavigationButtonTapped() = setupCoordinator.onBackTapped()
}

private class PreviewSetupPersonalPinConfirmViewModel : SetupPersonalPinConfirmViewModelInterface {
    override val pin: String = ""
    override fun userInputPIN(value: String) {}
    override fun onNavigationButtonTapped() {}
}

@Preview(showBackground = true)
@Composable
private fun PreviewSetupPersonalPinConfirm() {
    UseIDTheme {
        SetupPersonalPinConfirm(PreviewSetupPersonalPinConfirmViewModel())
    }
}
