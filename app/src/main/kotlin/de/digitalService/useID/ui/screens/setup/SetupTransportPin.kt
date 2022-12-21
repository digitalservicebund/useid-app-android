package de.digitalService.useID.ui.screens.setup

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.ramcosta.composedestinations.annotation.Destination
import dagger.hilt.android.lifecycle.HiltViewModel
import de.digitalService.useID.R
import de.digitalService.useID.getLogger
import de.digitalService.useID.ui.components.Flow
import de.digitalService.useID.ui.components.NavigationButton
import de.digitalService.useID.ui.components.NavigationIcon
import de.digitalService.useID.ui.components.ScreenWithTopBar
import de.digitalService.useID.ui.components.pin.TransportPinEntryField
import de.digitalService.useID.ui.coordinators.SetupCoordinator
import de.digitalService.useID.ui.screens.destinations.SetupTransportPinDestination
import de.digitalService.useID.ui.theme.UseIdTheme
import kotlinx.coroutines.delay
import javax.inject.Inject

@Destination(navArgsDelegate = SetupTransportPinNavArgs::class)
@Composable
fun SetupTransportPin(
    modifier: Modifier = Modifier,
    viewModel: SetupTransportPinViewModelInterface = hiltViewModel<SetupTransportPinViewModel>()
) {
    val resources = LocalContext.current.resources

    val icon = if (viewModel.attempts == null) {
        NavigationIcon.Back
    } else {
        NavigationIcon.Cancel
    }

    val titleString = if (viewModel.attempts == null) {
        stringResource(id = R.string.firstTimeUser_transportPIN_title)
    } else {
        stringResource(id = R.string.firstTimeUser_incorrectTransportPIN_title)
    }

    val attemptString = viewModel.attempts?.let { attempts ->
        resources.getQuantityString(
            R.plurals.firstTimeUser_transportPIN_remainingAttempts,
            attempts,
            attempts
        )
    }

    ScreenWithTopBar(
        navigationButton = NavigationButton(
            icon = icon,
            onClick = if (viewModel.attempts == null) viewModel::onBackButtonClicked else viewModel::onCancelClicked,
            confirmation = Flow.Setup.takeIf { viewModel.attempts != null },
            contentDescription = "$titleString $attemptString"
        )
    ) { topPadding ->
        val focusRequester = remember { FocusRequester() }

        LaunchedEffect(Unit) {
            delay(100)
            focusRequester.requestFocus()
        }

        Column(
            modifier = modifier
                .padding(horizontal = UseIdTheme.spaces.m)
                .padding(top = topPadding)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = titleString,
                style = UseIdTheme.typography.headingXl
            )

            Spacer(modifier = Modifier.height(UseIdTheme.spaces.m))

            Text(
                text = stringResource(id = R.string.firstTimeUser_transportPIN_body),
                style = UseIdTheme.typography.bodyLRegular
            )

            Spacer(modifier = Modifier.height(UseIdTheme.spaces.m))

            TransportPinEntryField(
                onDone = viewModel::onDoneClicked,
                focusRequester = focusRequester
            )

            Spacer(modifier = Modifier.height(UseIdTheme.spaces.m))

            attemptString?.let {
                Text(
                    it,
                    style = UseIdTheme.typography.bodyLRegular,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.align(androidx.compose.ui.Alignment.CenterHorizontally)
                )
            }
        }
    }
}

data class SetupTransportPinNavArgs(
    val attempts: Int?
)

interface SetupTransportPinViewModelInterface {
    val attempts: Int?

    fun onDoneClicked(pin: String)
    fun onCancelClicked()
    fun onBackButtonClicked()
}

@HiltViewModel
class SetupTransportPinViewModel @Inject constructor(
    private val coordinator: SetupCoordinator,
    savedStateHandle: SavedStateHandle
) :
    ViewModel(), SetupTransportPinViewModelInterface {
    override val attempts: Int?

    init {
        attempts = SetupTransportPinDestination.argsFrom(savedStateHandle).attempts
    }

    override fun onDoneClicked(transportPin: String) {
        coordinator.onTransportPinEntered(transportPin)
    }

    override fun onCancelClicked() {
        coordinator.cancelSetup()
    }

    override fun onBackButtonClicked() {
        coordinator.onBackClicked()
    }
}

//region Preview
private class PreviewSetupTransportPinViewModel(
//    override val transportPin: String,
    override val attempts: Int?
) : SetupTransportPinViewModelInterface {
    override fun onDoneClicked(pin: String) {}
    override fun onCancelClicked() {}
    override fun onBackButtonClicked() {}
}

@Preview(widthDp = 300, showBackground = true)
@Composable
fun PreviewSetupTransportPinWithoutAttemptsNarrowDevice() {
    UseIdTheme {
        SetupTransportPin(viewModel = PreviewSetupTransportPinViewModel(null))
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewSetupTransportPinWithoutAttempts() {
    UseIdTheme {
        SetupTransportPin(viewModel = PreviewSetupTransportPinViewModel(null))
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewSetupTransportPinOneAttempt() {
    UseIdTheme {
        SetupTransportPin(viewModel = PreviewSetupTransportPinViewModel(attempts = 1))
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewSetupTransportPinTwoAttempts() {
    UseIdTheme {
        SetupTransportPin(viewModel = PreviewSetupTransportPinViewModel(attempts = 2))
    }
}
//endregion
