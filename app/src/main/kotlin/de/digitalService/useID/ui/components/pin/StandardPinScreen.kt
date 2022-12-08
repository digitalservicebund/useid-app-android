package de.digitalService.useID.ui.components.pin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.digitalService.useID.ui.components.NavigationButton
import de.digitalService.useID.ui.components.NavigationIcon
import de.digitalService.useID.ui.components.ScreenWithTopBar
import de.digitalService.useID.ui.theme.UseIdTheme
import kotlinx.coroutines.delay

@Composable
fun StandardPinScreen(
    header: String,
    description: String,
    pinEntryDescription: String,
    pin: String,
    onNavigationButtonBackClick: () -> Unit,
    onInitialize: () -> Unit,
    onValueChanged: (String) -> Unit,
    onDone: () -> Unit
) {
    ScreenWithTopBar(
        navigationButton = NavigationButton(
            icon = NavigationIcon.Back,
            onClick = onNavigationButtonBackClick
        )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(top = it)
                .padding(horizontal = UseIdTheme.spaces.s)
                .verticalScroll(rememberScrollState())

        ) {
            val focusRequesterPin = remember { FocusRequester() }

            LaunchedEffect(Unit) {
                onInitialize()
                delay(400)
                focusRequesterPin.requestFocus()
            }

            Spacer(modifier = Modifier.height(UseIdTheme.spaces.s))

            Text(
                text = header,
                style = UseIdTheme.typography.headingXl
            )

            Spacer(modifier = Modifier.height(UseIdTheme.spaces.m))

            Text(
                text = description,
                style = UseIdTheme.typography.bodyLRegular
            )

            Spacer(modifier = Modifier.height(UseIdTheme.spaces.m))

            PinEntryField(
                value = pin,
                digitCount = 6,
                obfuscation = true,
                spacerPosition = 3,
                onValueChanged = onValueChanged,
                onDone = onDone,
                contentDescription = pinEntryDescription,
                focusRequester = focusRequesterPin,
                backgroundColor = UseIdTheme.colors.neutrals100,
                modifier = Modifier
                    .height(56.dp)
                    .fillMaxWidth()
            )
        }
    }
}

@Composable
@Preview
fun PreviewStandardPinScreen() {
    UseIdTheme {
        StandardPinScreen(
            header = "This is a test header for this pin entry page",
            description = "This a description. This is a description. This is a description.",
            pinEntryDescription = "This description is specific for the pin entry field.",
            pin = "123",
            onNavigationButtonBackClick = {},
            onInitialize = {},
            onValueChanged = {},
            onDone = {}
        )
    }
}
