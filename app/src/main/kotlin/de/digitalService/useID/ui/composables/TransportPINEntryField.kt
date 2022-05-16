package de.digitalService.useID.ui.composables

import android.util.Log
import androidx.compose.foundation.Image
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.digitalService.useID.R
import de.digitalService.useID.ui.theme.UseIDTheme

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun TransportPINEntryField(
    onDone: (String) -> Unit,
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier
) {
    var pinInput by remember { mutableStateOf("") }

    val keyboardController = LocalSoftwareKeyboardController.current
    val pinEntryDescription = stringResource(id = R.string.firstTimeUser_transportPIN_PINTextFieldDescription, pinInput.map { "$it " })

    Box(modifier = modifier) {
        Image(
            painter = painterResource(id = R.drawable.transport_pin),
            contentDescription = null,
            alignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
        )
        Box(
            modifier = Modifier
                .width(240.dp)
                .height(56.dp)
                .align(Alignment.Center)
                .focusable(false)
        ) {
            TextField(
                value = pinInput,
                onValueChange = {
                    if (it.length < 6) {
                        pinInput = it
                    }
                },
                shape = MaterialTheme.shapes.small,
                colors = TextFieldDefaults.textFieldColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                ),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.NumberPassword,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        if (pinInput.length != 5) {
                            Log.d("DEBUG", "PIN input too short.")
                        } else {
                            onDone(pinInput)
                        }
                    }
                ),
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
            )
            Surface(
                shape = MaterialTheme.shapes.small,
                contentColor = MaterialTheme.colorScheme.background,
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
                        stateDescription = pinEntryDescription
                    }
            ) {

            }
            PINDigitRow(
                input = pinInput, modifier = Modifier
                    .align(Alignment.Center)
                    .width(240.dp)
            )
        }
    }
}

@Preview
@Composable
fun PreviewTransportPINEntryField() {
    UseIDTheme {
        TransportPINEntryField(onDone = { }, focusRequester = FocusRequester())
    }
}