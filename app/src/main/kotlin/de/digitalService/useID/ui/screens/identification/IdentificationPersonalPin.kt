package de.digitalService.useID.ui.screens.identification

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import de.digitalService.useID.ui.components.pin.PinEntryField
import de.digitalService.useID.ui.coordinators.IdentificationCoordinator
import de.digitalService.useID.ui.screens.destinations.IdentificationPersonalPinDestination
import de.digitalService.useID.ui.theme.UseIdTheme
import kotlinx.coroutines.delay
import javax.inject.Inject

@Destination(navArgsDelegate = IdentificationPersonalPinNavArgs::class)
@Composable
fun IdentificationPersonalPin(
    modifier: Modifier = Modifier,
    viewModel: IdentificationPersonalPinViewModelInterface = hiltViewModel<IdentificationPersonalPinViewModel>()
) {
    val resources = LocalContext.current.resources

    val pinEntryFieldDescription = stringResource(
        id = R.string.identification_personalPIN_PINTextFieldDescription,
        viewModel.pin.map { "$it " }
    )

    ScreenWithTopBar(
        navigationButton = NavigationButton(
            icon = if (viewModel.attempts == null) NavigationIcon.Back else NavigationIcon.Cancel,
            onClick = viewModel::onNavigationButtonClicked,
            shouldShowConfirmDialog = viewModel.attempts != null,
            isIdentification = true
        )
    ) { topPadding ->
        val focusRequester = remember { FocusRequester() }
        LaunchedEffect(Unit) {
            delay(100)
            focusRequester.requestFocus()
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier
                .padding(horizontal = UseIdTheme.spaces.m)
                .padding(top = topPadding)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                stringResource(id = R.string.identification_personalPIN_title),
                style = UseIdTheme.typography.headingXl
            )

            Spacer(modifier = Modifier.weight(1f))

            PinEntryField(
                value = viewModel.pin,
                digitCount = 6,
                obfuscation = true,
                spacerPosition = 3,
                onValueChanged = viewModel::userInputPin,
                contentDescription = pinEntryFieldDescription,
                focusRequester = focusRequester,
                onDone = viewModel::onDone,
                backgroundColor = UseIdTheme.colors.neutrals100,
                modifier = Modifier
                    .padding(top = 50.dp)
                    .fillMaxWidth()
                    .height(56.dp)
            )

            viewModel.attempts?.let { attempts ->
                Spacer(modifier = Modifier.height(40.dp))

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        stringResource(id = R.string.identification_personalPIN_error_incorrectPIN),
                        color = UseIdTheme.colors.red900,
                        style = UseIdTheme.typography.bodyLBold
                    )
                    Spacer(modifier = Modifier.height(UseIdTheme.spaces.xs))
                    Text(
                        stringResource(id = R.string.identification_personalPIN_error_tryAgain),
                        style = UseIdTheme.typography.bodyLRegular
                    )
                    Spacer(modifier = Modifier.height(UseIdTheme.spaces.xs))
                    val attemptString = resources.getQuantityString(
                        R.plurals.identification_personalPIN_remainingAttempts,
                        attempts,
                        attempts
                    )
                    Text(
                        attemptString,
                        style = UseIdTheme.typography.bodyLRegular,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

data class IdentificationPersonalPinNavArgs(
    val attempts: Int?
)

interface IdentificationPersonalPinViewModelInterface {
    val pin: String
    val attempts: Int?

    fun userInputPin(value: String)
    fun onDone()
    fun onNavigationButtonClicked()
}

@HiltViewModel
class IdentificationPersonalPinViewModel @Inject constructor(
    private val coordinator: IdentificationCoordinator,
    savedStateHandle: SavedStateHandle
) : ViewModel(), IdentificationPersonalPinViewModelInterface {
    override var pin by mutableStateOf("")
        private set

    override val attempts: Int?

    init {
        attempts = IdentificationPersonalPinDestination.argsFrom(savedStateHandle).attempts
    }

    override fun userInputPin(value: String) {
        if (!checkPinString(value)) return
        pin = value
    }

    override fun onDone() {
        if (pin.length == 6) {
            coordinator.onPinEntered(pin)
        }
    }

    override fun onNavigationButtonClicked() {
        if (attempts != null) {
            coordinator.cancelIdentification()
        } else {
            coordinator.pop()
        }
    }

    private fun checkPinString(value: String): Boolean = value.length < 7 && value.isDigitsOnly()
}

class PreviewIdentificationPersonalPinViewModel(
    override val pin: String,
    override val attempts: Int?
) : IdentificationPersonalPinViewModelInterface {
    override fun userInputPin(value: String) {}
    override fun onDone() {}
    override fun onNavigationButtonClicked() {}
}

@Preview
@Composable
fun PreviewIdentificationPersonalPin() {
    UseIdTheme {
        IdentificationPersonalPin(viewModel = PreviewIdentificationPersonalPinViewModel("123", null))
    }
}

@Preview
@Composable
fun PreviewIdentificationPersonalPinTwoAttempts() {
    UseIdTheme {
        IdentificationPersonalPin(viewModel = PreviewIdentificationPersonalPinViewModel("123", 2))
    }
}
