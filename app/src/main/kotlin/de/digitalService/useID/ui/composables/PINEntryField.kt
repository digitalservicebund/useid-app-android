package de.digitalService.useID.ui.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import de.digitalService.useID.ui.theme.UseIDTheme

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun PINEntryField(
    value: String,
    onValueChanged: (String) -> Unit,
    digitCount: Int,
    obfuscation: Boolean,
    spacerPosition: Int?,
    contentDescription: String,
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier,
    onDone: () -> Unit = { }
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    Box(
        modifier = modifier
            .focusable(false)
    ) {
        TextField(
            value = value,
            onValueChange = {
                if (it.length <= digitCount) {
                    onValueChanged(it)
                }
            },
            shape = MaterialTheme.shapes.small,
            colors = TextFieldDefaults.textFieldColors(
                containerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent
            ),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.NumberPassword,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { onDone() }
            ),
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .focusRequester(focusRequester)
        )
        Surface(
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.background,
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxSize()
                .clickable(
                    enabled = true,
                    onClick = {
                        focusRequester.requestFocus()
                        keyboardController?.show()
                    }
                )
                .semantics(mergeDescendants = true) {
                    this.contentDescription = contentDescription
                    stateDescription = value.replace(".".toRegex(), "$0 ")
                }
        ) {
        }
        PINDigitRow(
            input = value,
            digitCount = digitCount,
            obfuscation = obfuscation,
            placeholder = false,
            spacerPosition = spacerPosition,
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
        )
    }
}

@Preview
@Composable
fun PreviewPINEntryField() {
    UseIDTheme {
        PINEntryField(value = "22", onValueChanged = { }, digitCount = 6, obfuscation = true, spacerPosition = 3, contentDescription = "", focusRequester = FocusRequester())
    }
}
