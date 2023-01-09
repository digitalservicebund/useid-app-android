package de.digitalService.useID.ui.components.pin

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
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
import de.digitalService.useID.ui.components.NavigationButton
import de.digitalService.useID.ui.components.NavigationIcon
import de.digitalService.useID.ui.components.ScreenWithTopBar
import de.digitalService.useID.ui.coordinators.PinManagementCoordinator
import de.digitalService.useID.ui.theme.UseIdTheme
import kotlinx.coroutines.delay
import javax.inject.Inject

@Composable
fun TransportPinEntryScreen(
    title: String,
    attempts: Int?,
    navigationButton: NavigationButton,
    onDone: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val resources = LocalContext.current.resources

    val attemptString = attempts?.let {
        resources.getQuantityString(
            R.plurals.firstTimeUser_transportPIN_remainingAttempts,
            attempts,
            attempts
        )
    }

    ScreenWithTopBar(
        navigationButton = navigationButton
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
                text = title,
                style = UseIdTheme.typography.headingXl
            )

            Spacer(modifier = Modifier.height(UseIdTheme.spaces.m))

            Text(
                text = stringResource(id = R.string.firstTimeUser_transportPIN_body),
                style = UseIdTheme.typography.bodyLRegular
            )

            Spacer(modifier = Modifier.height(UseIdTheme.spaces.m))

            TransportPinEntryField(
                onDone = onDone,
                focusRequester = focusRequester
            )

            Spacer(modifier = Modifier.height(UseIdTheme.spaces.m))

            attemptString?.let {
                Text(
                    it,
                    style = UseIdTheme.typography.bodyLRegular,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}

@Preview(widthDp = 300, showBackground = true)
@Composable
private fun PreviewSetupTransportPinWithoutAttemptsNarrowDevice() {
    UseIdTheme {
        TransportPinEntryScreen("Title", null, NavigationButton(NavigationIcon.Cancel, { }, null, null), { })
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewSetupTransportPinWithoutAttempts() {
    UseIdTheme {
        TransportPinEntryScreen("Title", null, NavigationButton(NavigationIcon.Cancel, { }, null, null), { })
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewSetupTransportPinRetry() {
    UseIdTheme {
        TransportPinEntryScreen("Title", 2, NavigationButton(NavigationIcon.Cancel, { }, null, null), { })
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewSetupTransportPinCan() {
    UseIdTheme {
        TransportPinEntryScreen("Title", 1, NavigationButton(NavigationIcon.Cancel, { }, null, null), { })
    }
}
