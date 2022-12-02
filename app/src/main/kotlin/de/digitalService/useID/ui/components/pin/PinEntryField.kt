package de.digitalService.useID.ui.components.pin

import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.digitalService.useID.R
import de.digitalService.useID.ui.theme.UseIdTheme

@Composable
fun PinEntryField(
    value: String,
    onValueChanged: (String) -> Unit,
    digitCount: Int,
    obfuscation: Boolean = false,
    spacerPosition: Int?,
    contentDescription: String,
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.background,
    onDone: () -> Unit = { }
) {
    var textFieldValueState by remember(value) { mutableStateOf(TextFieldValue(value, TextRange(value.length))) }

    val cursorPositionDescription = stringResource(
        id = R.string.pinEntryField_accessibility_cursorPosition,
        textFieldValueState.selection.end + 1
    )

    Box(
        modifier = modifier
            .background(backgroundColor)
            .semantics(mergeDescendants = false) {
                this.contentDescription = contentDescription
                stateDescription = value.replace(".".toRegex(), "$0 ") + cursorPositionDescription
            }
            .focusable(false)
    ) {
        BasicTextField(
            value = textFieldValueState,
            onValueChange = { newValue ->
                if (newValue.text.length <= digitCount) {
                    if (newValue.selection.length > 0) {
                        textFieldValueState = newValue.copy(selection = textFieldValueState.selection)
                    } else {
                        onValueChanged(newValue.text)
                    }
                }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.NumberPassword,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { onDone() }
            ),
            textStyle = TextStyle(color = Color.Transparent),
            cursorBrush = SolidColor(Color.Transparent),
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .focusRequester(focusRequester)
                .clipToBounds()
                .testTag("PINEntryField")
                .focusable(false)
        )

        PinDigitRow(
            input = value,
            digitCount = digitCount,
            obfuscation = obfuscation,
            placeholder = false,
            spacerPosition = spacerPosition,
            modifier = Modifier
                .align(Alignment.Center)
                .focusable(false)
        )
    }
}

@Preview
@Composable
fun PreviewPinEntryField() {
    UseIdTheme {
        Column(modifier = Modifier.fillMaxSize()) {
            var text by remember { mutableStateOf("") }
            val focusRequester = remember {
                FocusRequester()
            }

            PinEntryField(
                text,
                onValueChanged = { text = it },
                digitCount = 6,
                obfuscation = false,
                spacerPosition = 3,
                contentDescription = "",
                focusRequester = focusRequester,
                modifier = Modifier.padding(64.dp)
            )
        }
    }
}
