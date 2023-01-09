package de.digitalService.useID.ui.components.pin

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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.digitalService.useID.R
import de.digitalService.useID.ui.components.NavigationButton
import de.digitalService.useID.ui.components.ScreenWithTopBar
import de.digitalService.useID.ui.theme.UseIdTheme
import kotlinx.coroutines.delay

@Composable
// TODO: Generalize to NumberEntryScreen and add Transport-PIN mode
fun StandardNumberEntryScreen(
    title: String,
    body: String? = null,
    entryFieldDescription: String,
    errorMessage: String? = null,
    attempts: Int? = null,
    navigationButton: NavigationButton? = null,
    obfuscation: Boolean,
    onDone: (String) -> Unit,
    delayFocusRequest: Boolean = true
) {
    val resources = LocalContext.current.resources

    ScreenWithTopBar(
        navigationButton = navigationButton
    ) { topPadding ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(top = topPadding)
                .padding(horizontal = UseIdTheme.spaces.m)
                .verticalScroll(rememberScrollState())

        ) {
            val focusRequesterPin = remember { FocusRequester() }

            LaunchedEffect(Unit) {
                if (delayFocusRequest) {
                    delay(400)
                }

                focusRequesterPin.requestFocus()
            }

            Text(
                text = title,
                style = UseIdTheme.typography.headingXl
            )

            Spacer(modifier = Modifier.height(UseIdTheme.spaces.m))

            if (body != null) {
                Text(
                    text = body,
                    style = UseIdTheme.typography.bodyLRegular
                )
            } else {
                Spacer(modifier = Modifier.height(UseIdTheme.spaces.m))
            }

            Spacer(modifier = Modifier.height(UseIdTheme.spaces.m))

            NumberEntryField(
                obfuscation = obfuscation,
                onDone = onDone,
                focusRequester = focusRequesterPin,
                modifier = Modifier.semantics {
                    contentDescription = entryFieldDescription
                }
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Spacer(modifier = Modifier.height(40.dp))

                if (errorMessage != null) {
                    Text(
                        errorMessage,
                        color = UseIdTheme.colors.red900,
                        style = UseIdTheme.typography.bodyLBold
                    )

                    Text(
                        stringResource(id = R.string.identification_personalPIN_error_tryAgain),
                        style = UseIdTheme.typography.bodyLRegular
                    )
                }

                if (attempts != null) {
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

@Composable
@Preview
private fun PreviewWithObfuscation() {
    UseIdTheme {
        StandardNumberEntryScreen(
            title = "This is a test header for this pin entry page",
            body = "This a description. This is a description. This is a description.",
            entryFieldDescription = "This description is specific for the pin entry field.",
            navigationButton = null,
            obfuscation = true,
            onDone = {}
        )
    }
}

@Composable
@Preview
private fun PreviewWithBody() {
    UseIdTheme {
        StandardNumberEntryScreen(
            title = "This is a test header for this pin entry page",
            body = null,
            entryFieldDescription = "This description is specific for the pin entry field.",
            navigationButton = null,
            obfuscation = true,
            onDone = {}
        )
    }
}

@Composable
@Preview
private fun PreviewWithErrorMessage() {
    UseIdTheme {
        StandardNumberEntryScreen(
            title = "This is a test header for this pin entry page",
            body = null,
            errorMessage = "Error Message",
            entryFieldDescription = "This description is specific for the pin entry field.",
            navigationButton = null,
            obfuscation = true,
            onDone = {}
        )
    }
}

@Composable
@Preview
private fun PreviewWithAttempts() {
    UseIdTheme {
        StandardNumberEntryScreen(
            title = "This is a test header for this pin entry page",
            body = null,
            attempts = 2,
            entryFieldDescription = "This description is specific for the pin entry field.",
            navigationButton = null,
            obfuscation = true,
            onDone = {}
        )
    }
}

@Composable
@Preview
private fun PreviewWithErrorMessageAndAttempts() {
    UseIdTheme {
        StandardNumberEntryScreen(
            title = "This is a test header for this pin entry page",
            body = null,
            errorMessage = "Error message",
            attempts = 2,
            entryFieldDescription = "This description is specific for the pin entry field.",
            navigationButton = null,
            obfuscation = true,
            onDone = {}
        )
    }
}
