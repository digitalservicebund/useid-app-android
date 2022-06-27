package de.digitalService.useID.ui.composables.screens

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
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import com.ramcosta.composedestinations.annotation.Destination
import dagger.hilt.android.lifecycle.HiltViewModel
import de.digitalService.useID.R
import de.digitalService.useID.SecureStorageManagerInterface
import de.digitalService.useID.ui.composables.PINEntryField
import de.digitalService.useID.ui.coordinators.SetupCoordinator
import de.digitalService.useID.ui.theme.UseIDTheme
import javax.inject.Inject

@Destination
@Composable
fun SetupPersonalPIN(modifier: Modifier = Modifier, viewModel: SetupPersonalPINViewModelInterface = hiltViewModel<SetupPersonalPINViewModel>()) {
    val focusRequesterPIN1 = remember { FocusRequester() }
    val focusRequesterPIN2 = remember { FocusRequester() }

    val pin1EntryFieldDescription = stringResource(
        id = R.string.firstTimeUser_personalPIN_PIN1TextFieldDescription,
        viewModel.pin1.map { "$it " }
    )
    val pin2EntryFieldDescription = stringResource(
        id = R.string.firstTimeUser_personalPIN_PIN2TextFieldDescription,
        viewModel.pin2.map { "$it " }
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(horizontal = 20.dp)
    ) {
        Text(
            stringResource(id = R.string.firstTimeUser_personalPIN_title),
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(modifier = Modifier.weight(1f))
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            PINEntryField(
                value = viewModel.pin1,
                digitCount = 6,
                spacerPosition = 3,
                onValueChanged = viewModel::userInputPIN1,
                contentDescription = pin1EntryFieldDescription,
                focusRequester = focusRequesterPIN1,
                modifier = Modifier
                    .width(240.dp)
                    .height(56.dp)
            )
            AnimatedVisibility(viewModel.shouldShowPIN2EntryField) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        stringResource(id = R.string.firstTimeUser_personalPIN_confirmation),
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 10.dp)
                    )
                    PINEntryField(
                        value = viewModel.pin2,
                        digitCount = 6,
                        spacerPosition = 3,
                        onValueChanged = viewModel::userInputPIN2,
                        contentDescription = pin2EntryFieldDescription,
                        focusRequester = focusRequesterPIN2,
                        modifier = Modifier
                            .width(240.dp)
                            .height(56.dp)
                    )
                }
            }
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
        Spacer(modifier = Modifier.weight(1f))
    }

    LaunchedEffect(viewModel.focus) {
        when (viewModel.focus) {
            SetupPersonalPINViewModelInterface.PINEntryFieldFocus.PIN_1 -> focusRequesterPIN1.requestFocus()
            SetupPersonalPINViewModelInterface.PINEntryFieldFocus.PIN_2 -> focusRequesterPIN2.requestFocus()
        }
    }
}

interface SetupPersonalPINViewModelInterface {
    enum class PINEntryFieldFocus {
        PIN_1, PIN_2
    }

    val pin1: String
    val pin2: String

    val focus: PINEntryFieldFocus

    val shouldShowPIN2EntryField: Boolean
    val shouldShowError: Boolean

    fun userInputPIN1(value: String)
    fun userInputPIN2(value: String)
}

@HiltViewModel
class SetupPersonalPINViewModel @Inject constructor(private val coordinator: SetupCoordinator, private val secureStorageManager: SecureStorageManagerInterface) : ViewModel(), SetupPersonalPINViewModelInterface {
    override var pin1 by mutableStateOf("")
        private set

    override var pin2 by mutableStateOf("")
        private set

    override var focus: SetupPersonalPINViewModelInterface.PINEntryFieldFocus by mutableStateOf(
        SetupPersonalPINViewModelInterface.PINEntryFieldFocus.PIN_1
    )
        private set

    override var shouldShowPIN2EntryField by mutableStateOf(false)
        private set

    override var shouldShowError by mutableStateOf(false)
        private set

    override fun userInputPIN1(value: String) {
        pin1 = value
        shouldShowError = false

        val pinComplete = pin1.length >= 6

        shouldShowPIN2EntryField = pinComplete || pin2.isNotEmpty()

        if (pinComplete) {
            focus = SetupPersonalPINViewModelInterface.PINEntryFieldFocus.PIN_2
        }
    }

    override fun userInputPIN2(value: String) {
        pin2 = value
        if (pin2.length > 5) {
            handlePINInput()
        }
    }

    private fun handlePINInput() {
        if (pin1 == pin2) {
            secureStorageManager.setPersonalPIN(pin1)
            coordinator.onPersonalPINEntered()
        } else {
            pin1 = ""
            pin2 = ""
            shouldShowError = true
            focus = SetupPersonalPINViewModelInterface.PINEntryFieldFocus.PIN_1
        }
    }
}

//region Preview
private class PreviewSetupPersonalPINViewModel(
    override val pin1: String,
    override val pin2: String,
    override val focus: SetupPersonalPINViewModelInterface.PINEntryFieldFocus,
    override val shouldShowPIN2EntryField: Boolean,
    override val shouldShowError: Boolean
) : SetupPersonalPINViewModelInterface {
    override fun userInputPIN1(value: String) {}
    override fun userInputPIN2(value: String) {}
}

@Preview(device = Devices.PIXEL_3A)
@Composable
fun PreviewSetupPersonalPIN() {
    UseIDTheme {
        SetupPersonalPIN(
            viewModel = PreviewSetupPersonalPINViewModel(
                "12",
                "",
                SetupPersonalPINViewModelInterface.PINEntryFieldFocus.PIN_1,
                false,
                false
            )
        )
    }
}
//endregion
