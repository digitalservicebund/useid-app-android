package de.digitalService.useID.ui.components.pin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.text.isDigitsOnly
import de.digitalService.useID.ui.theme.UseIdTheme

@Composable
fun NumberEntryTextField(
    digitCount: Int,
    obfuscation: Boolean = false,
    spacerPosition: Int?,
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier,
    backgroundColor: Color = UseIdTheme.colors.neutrals100,
    onDone: (String) -> Unit
) {
    var number: String by remember { mutableStateOf("") }

    Box(
        modifier = modifier
            .background(backgroundColor)
            .focusable(false)
    ) {
        BasicTextField(
            value = number,
            onValueChange = { newNumber ->
                if (newNumber.length <= digitCount && newNumber.isDigitsOnly()) {
                    number = newNumber
                }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.NumberPassword,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    if (number.length == digitCount) {
                        onDone(number)
                    }
                }
            ),
            textStyle = TextStyle(color = Color.Transparent),
            cursorBrush = SolidColor(Color.Transparent),
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier
                .clip(UseIdTheme.shapes.roundedSmall)
                .focusRequester(focusRequester)
                .clipToBounds()
                .testTag("PINEntryField")
                .focusable(false)
                .align(Alignment.Center)
                .defaultMinSize(minWidth = (digitCount * 40).dp, minHeight = 50.dp)
        )

        PinDigitRow(
            input = number,
            digitCount = digitCount,
            obfuscation = obfuscation,
            placeholder = false,
            spacerPosition = spacerPosition,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .focusable(false)
                .padding(vertical = 10.dp, horizontal = 10.dp)
        )
    }
}

@Preview
@Composable
fun PreviewPinEntryTextField() {
    UseIdTheme {
        Column(modifier = Modifier.fillMaxSize()) {
            val focusRequester = remember {
                FocusRequester()
            }

            NumberEntryTextField(
                digitCount = 6,
                obfuscation = false,
                spacerPosition = 3,
                focusRequester = focusRequester,
                modifier = Modifier.padding(64.dp),
                onDone = { }
            )
        }
    }
}

@Preview
@Composable
fun PreviewPinEntryTextFieldWide() {
    UseIdTheme {
        Column(modifier = Modifier.fillMaxSize()) {
            val focusRequester = remember {
                FocusRequester()
            }

            NumberEntryTextField(
                digitCount = 6,
                obfuscation = false,
                spacerPosition = 3,
                focusRequester = focusRequester,
                modifier = Modifier.fillMaxWidth(),
                onDone = { }
            )
        }
    }
}
