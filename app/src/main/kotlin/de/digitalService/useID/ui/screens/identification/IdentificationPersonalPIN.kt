package de.digitalService.useID.ui.screens.identification

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
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
import de.digitalService.useID.ui.coordinators.IdentificationCoordinator
import de.digitalService.useID.ui.screens.destinations.SetupTransportPINDestination
import de.digitalService.useID.ui.theme.Gray300
import de.digitalService.useID.ui.theme.UseIDTheme
import kotlinx.coroutines.delay
import javax.inject.Inject

@Destination(navArgsDelegate = IdentificationPersonalPINNavArgs::class)
@Composable
fun IdentificationPersonalPIN(
    modifier: Modifier = Modifier,
    viewModel: IdentificationPersonalPINViewModelInterface = hiltViewModel<IdentificationPersonalPINViewModel>()
) {
    val resources = LocalContext.current.resources

    val pinEntryFieldDescription = stringResource(
        id = R.string.identification_personalPIN_PINTextFieldDescription,
        viewModel.pin.map { "$it " }
    )

    ScreenWithTopBar(
        navigationButton = NavigationButton(
            icon = NavigationIcon.Cancel,
            onClick = viewModel::onCancelButtonTapped
        )
    ) { topPadding ->
        val focusRequester = remember { FocusRequester() }
        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier
                .padding(horizontal = 20.dp)
                .padding(top = topPadding)
        ) {
            Text(
                stringResource(id = R.string.identification_personalPIN_title),
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.weight(1f))

            PINEntryField(
                value = viewModel.pin,
                digitCount = 6,
                obfuscation = true,
                spacerPosition = 3,
                onValueChanged = viewModel::userInputPIN,
                contentDescription = pinEntryFieldDescription,
                focusRequester = focusRequester,
                onDone = viewModel::onDone,
                backgroundColor = Gray300,
                modifier = Modifier
                    .padding(top = 50.dp)
                    .width(240.dp)
                    .height(56.dp)
            )

            viewModel.attempts?.let { attempts ->
                Spacer(modifier = Modifier.height(40.dp))

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {

                    Text(
                        stringResource(id = R.string.firstTimeUser_incorrectTransportPIN_title),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        stringResource(id = R.string.identification_personalPIN_error_tryAgain),
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    val attemptString = if (attempts > 0) {
                        resources.getQuantityString(
                            R.plurals.firstTimeUser_transportPIN_remainingAttempts,
                            attempts,
                            attempts
                        )
                    } else {
                        stringResource(id = R.string.firstTimeUser_incorrectTransportPIN_noAttemptLeft)
                    }
                    Text(
                        attemptString,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

data class IdentificationPersonalPINNavArgs(
    val attempts: Int?
)

interface IdentificationPersonalPINViewModelInterface {
    val pin: String
    val attempts: Int?

    fun userInputPIN(value: String)
    fun onDone()
    fun onCancelButtonTapped()
}

@HiltViewModel
class IdentificationPersonalPINViewModel @Inject constructor(
    private val coordinator: IdentificationCoordinator,
    savedStateHandle: SavedStateHandle
) : ViewModel(), IdentificationPersonalPINViewModelInterface {
    override var pin by mutableStateOf("")
        private set

    override val attempts: Int?

    init {
        attempts = SetupTransportPINDestination.argsFrom(savedStateHandle).attempts
    }

    override fun userInputPIN(value: String) {
        if (!checkPINString(value)) return
        pin = value
    }

    override fun onDone() {
        if (pin.length == 6) {
            coordinator.onPINEntered(pin)
        }
    }

    override fun onCancelButtonTapped() = coordinator.cancelIdentification()

    private fun checkPINString(value: String): Boolean = value.length < 7 && value.isDigitsOnly()
}

class PreviewIdentificationPersonalPINViewModel(
    override val pin: String,
    override val attempts: Int?
) : IdentificationPersonalPINViewModelInterface {
    override fun userInputPIN(value: String) {}
    override fun onDone() {}
    override fun onCancelButtonTapped() {}
}

@Preview
@Composable
fun PreviewIdentificationPersonalPIN() {
    UseIDTheme {
        IdentificationPersonalPIN(viewModel = PreviewIdentificationPersonalPINViewModel("123", null))
    }
}

@Preview
@Composable
fun PreviewIdentificationPersonalPINTwoAttempts() {
    UseIDTheme {
        IdentificationPersonalPIN(viewModel = PreviewIdentificationPersonalPINViewModel("123", 2))
    }
}
