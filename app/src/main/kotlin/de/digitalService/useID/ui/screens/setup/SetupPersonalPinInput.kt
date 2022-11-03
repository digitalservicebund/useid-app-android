package de.digitalService.useID.ui.screens.setup

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.text.isDigitsOnly
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.ramcosta.composedestinations.annotation.Destination
import dagger.hilt.android.lifecycle.HiltViewModel
import de.digitalService.useID.R
import de.digitalService.useID.ui.components.NavigationButton
import de.digitalService.useID.ui.components.NavigationIcon
import de.digitalService.useID.ui.components.ScreenWithTopBar
import de.digitalService.useID.ui.components.pin.PINEntryField
import de.digitalService.useID.ui.coordinators.SetupCoordinator
import de.digitalService.useID.ui.screens.destinations.SetupPersonalPinInputDestination
import de.digitalService.useID.ui.theme.Gray300
import de.digitalService.useID.ui.theme.UseIDTheme
import javax.inject.Inject

@Destination(navArgsDelegate = SetupPersonalPinInputNavArgs::class)
@Composable
fun SetupPersonalPinInput(viewModel: SetupPersonalPinInputViewModelInterface = hiltViewModel<SetupPersonalPinInputViewModel>()) {
    ScreenWithTopBar(
        navigationButton = NavigationButton(NavigationIcon.Back, viewModel::onNavigationButtonTapped)
    ) {
        val focusRequesterPIN = remember { FocusRequester() }

        LaunchedEffect(Unit) {
            focusRequesterPIN.requestFocus()
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = it, start = 16.dp, end = 16.dp)
        ) {

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

            AnimatedVisibility(viewModel.shouldShowError) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.semantics(mergeDescendants = true) { }
                ) {
                    Text(
                        stringResource(id = R.string.firstTimeUser_personalPIN_error_mismatch_title),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 10.dp)
                    )
                    Text(
                        stringResource(id = R.string.firstTimeUser_personalPIN_error_mismatch_body),
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 10.dp)
                    )
                }
            }
        }
    }
}

interface SetupPersonalPinInputViewModelInterface {
    val pin: String
    val shouldShowError: Boolean

    fun userInputPIN(value: String)
    fun onNavigationButtonTapped()
}

@HiltViewModel
class SetupPersonalPinInputViewModel @Inject constructor(
    private val setupCoordinator: SetupCoordinator,
    savedStateHandle: SavedStateHandle
) : ViewModel(), SetupPersonalPinInputViewModelInterface {
    override var pin: String by mutableStateOf("")

    override var shouldShowError by mutableStateOf(false)
        private set

    init {
        shouldShowError = SetupPersonalPinInputDestination.argsFrom(savedStateHandle).shouldShowError
    }

    override fun userInputPIN(value: String) {
        if (!value.isDigitsOnly()) {
            return
        }

        pin = value
        shouldShowError = false

        if (pin.length == 6) {
            setupCoordinator.onPersonalPinInput(pin)
        }
    }

    override fun onNavigationButtonTapped() = setupCoordinator.onBackTapped()
}

private class PreviewSetupPersonalPinInputViewModel : SetupPersonalPinInputViewModelInterface {
    override val pin: String = ""
    override val shouldShowError: Boolean = false
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

data class SetupPersonalPinInputNavArgs(val shouldShowError: Boolean)
