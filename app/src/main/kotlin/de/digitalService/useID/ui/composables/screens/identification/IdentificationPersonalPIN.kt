package de.digitalService.useID.ui.composables.screens.identification

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.ramcosta.composedestinations.annotation.Destination
import dagger.hilt.android.lifecycle.HiltViewModel
import de.digitalService.useID.R
import de.digitalService.useID.SecureStorageManagerInterface
import de.digitalService.useID.ui.composables.PINEntryField
import de.digitalService.useID.ui.composables.screens.destinations.IdentificationPersonalPINDestination
import de.digitalService.useID.ui.coordinators.IdentificationCoordinator
import de.digitalService.useID.ui.theme.UseIDTheme
import javax.inject.Inject

@Destination(
    navArgsDelegate = IdentificationPersonalPINNavArgs::class
)
@Composable
fun IdentificationPersonalPIN(
    modifier: Modifier = Modifier,
    viewModel: IdentificationPersonalPINViewModelInterface = hiltViewModel<IdentificationPersonalPINViewModel>()
) {
    val pinEntryFieldDescription = stringResource(
        id = R.string.identification_personalPIN_PINTextFieldDescription,
        viewModel.pin.map { "$it " }
    )

    val focusRequester = remember { FocusRequester() }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(horizontal = 20.dp)
    ) {
        Text(
            stringResource(id = R.string.identification_personalPIN_title),
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(modifier = Modifier.weight(1f))
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            PINEntryField(
                value = viewModel.pin,
                digitCount = 6,
                obfuscation = false,
                spacerPosition = 3,
                onValueChanged = viewModel::userInputPIN,
                contentDescription = pinEntryFieldDescription,
                focusRequester = focusRequester,
                onDone = viewModel::onDone,
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
                        stringResource(id = R.string.identification_personalPIN_error_incorrectPIN),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 10.dp)
                    )
                    Text(
                        stringResource(id = R.string.identification_personalPIN_error_tryAgain),
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 10.dp)
                    )
                }
            }
            viewModel.attempts?.let { attempts ->
                Text(
                    LocalContext.current.resources.getQuantityString(
                        R.plurals.identification_personalPIN_remainingAttempts,
                        attempts,
                        attempts
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 10.dp)
                )
            }
        }
        Spacer(modifier = Modifier.weight(1f))
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}

interface IdentificationPersonalPINViewModelInterface {
    val pin: String

    val attempts: Int?
    val shouldShowError: Boolean

    fun userInputPIN(value: String)
    fun onDone()
}

data class IdentificationPersonalPINNavArgs(
    val attempts: Int?,
    val shouldShowError: Boolean
)

@HiltViewModel
class IdentificationPersonalPINViewModel @Inject constructor(
    private val coordinator: IdentificationCoordinator,
    private val secureStorageManager: SecureStorageManagerInterface,
    savedStateHandle: SavedStateHandle
): ViewModel(), IdentificationPersonalPINViewModelInterface {
    override var pin by mutableStateOf("")
        private set

    override val attempts: Int?
    override var shouldShowError: Boolean = false
        private set

    init {
        val args = IdentificationPersonalPINDestination.argsFrom(savedStateHandle)
        attempts = args.attempts
        shouldShowError = args.shouldShowError
    }

    override fun userInputPIN(value: String) {
        pin = value
        shouldShowError = false
    }

    override fun onDone() {
        secureStorageManager.setPersonalPIN(pin)
        coordinator.onPINEntered()
    }
}

class PreviewIdentificationPersonalPINViewModel(
    override val pin: String, override val attempts: Int?, override val shouldShowError: Boolean
) : IdentificationPersonalPINViewModelInterface {
    override fun userInputPIN(value: String) { }
    override fun onDone() { }
}

@Preview
@Composable
fun PreviewIdentificationPersonalPIN() {
    UseIDTheme {
        IdentificationPersonalPIN(viewModel = PreviewIdentificationPersonalPINViewModel("123", null, false))
    }
}

@Preview
@Composable
fun PreviewIdentificationPersonalPINWithAttempts() {
    UseIDTheme {
        IdentificationPersonalPIN(viewModel = PreviewIdentificationPersonalPINViewModel("123", 2, false))
    }
}

@Preview
@Composable
fun PreviewIdentificationPersonalPINError() {
    UseIDTheme {
        IdentificationPersonalPIN(viewModel = PreviewIdentificationPersonalPINViewModel("123", 2, true))
    }
}
