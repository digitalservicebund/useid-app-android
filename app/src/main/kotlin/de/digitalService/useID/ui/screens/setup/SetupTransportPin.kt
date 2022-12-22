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
import de.digitalService.useID.ui.components.Flow
import de.digitalService.useID.ui.components.NavigationButton
import de.digitalService.useID.ui.components.NavigationIcon
import de.digitalService.useID.ui.components.ScreenWithTopBar
import de.digitalService.useID.ui.components.pin.TransportPinEntryField
import de.digitalService.useID.ui.coordinators.PinManagementCoordinator
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

    val icon = if (viewModel.retry) {
        NavigationIcon.Cancel
    } else {
        NavigationIcon.Back
    }

    val titleString = if (viewModel.retry) {
        stringResource(id = R.string.firstTimeUser_incorrectTransportPIN_title)
    } else {
        stringResource(id = R.string.firstTimeUser_transportPIN_title)
    }

    val attemptString =
        resources.getQuantityString(
            R.plurals.firstTimeUser_transportPIN_remainingAttempts,
            2,
            2
        ).takeIf { viewModel.retry }

    ScreenWithTopBar(
        navigationButton = NavigationButton(
            icon = icon,
            onClick = viewModel::onNavigationButtonClicked,
            confirmation = Flow.Setup.takeIf { viewModel.retry },
            contentDescription = titleString
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
    val retry: Boolean
)

interface SetupTransportPinViewModelInterface {
    val retry: Boolean

    fun onDoneClicked(pin: String)
    fun onNavigationButtonClicked()
}

@HiltViewModel
class SetupTransportPinViewModel @Inject constructor(
    private val coordinator: PinManagementCoordinator,
    savedStateHandle: SavedStateHandle
) :
    ViewModel(), SetupTransportPinViewModelInterface {
    override val retry: Boolean

    init {
        retry = SetupTransportPinDestination.argsFrom(savedStateHandle).retry
    }

    override fun onDoneClicked(pin: String) {
        coordinator.setOldPin(pin)
    }

    override fun onNavigationButtonClicked() {
        if (retry) {
            coordinator.cancelPinManagement()
        } else {
            coordinator.onBack()
        }
    }
}

//region Preview
private class PreviewSetupTransportPinViewModel(
    override val retry: Boolean
) : SetupTransportPinViewModelInterface {
    override fun onDoneClicked(pin: String) {}
    override fun onNavigationButtonClicked() {}
}

@Preview(widthDp = 300, showBackground = true)
@Composable
fun PreviewSetupTransportPinWithoutAttemptsNarrowDevice() {
    UseIdTheme {
        SetupTransportPin(viewModel = PreviewSetupTransportPinViewModel(false))
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewSetupTransportPinWithoutAttempts() {
    UseIdTheme {
        SetupTransportPin(viewModel = PreviewSetupTransportPinViewModel(false))
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewSetupTransportPinRetry() {
    UseIdTheme {
        SetupTransportPin(viewModel = PreviewSetupTransportPinViewModel(true))
    }
}
//endregion
