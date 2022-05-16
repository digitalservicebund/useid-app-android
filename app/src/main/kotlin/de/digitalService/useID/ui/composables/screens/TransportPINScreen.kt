package de.digitalService.useID.ui.composables.screens

import android.accessibilityservice.AccessibilityService
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.digitalService.useID.R
import de.digitalService.useID.ui.composables.PINDigitField
import de.digitalService.useID.ui.composables.PINDigitRow
import de.digitalService.useID.ui.theme.UseIDTheme

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TransportPINScreen() {
    var pinInput by remember { mutableStateOf("") }

    val focusRequester = remember { FocusRequester() }
    val keyboardManager = LocalSoftwareKeyboardController.current

    val pinEntryDescription = stringResource(id = R.string.firstTimeUser_transportPIN_PINTextFieldDescription, pinInput.map { "$it " })

    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        Text(
            text = stringResource(id = R.string.firstTimeUser_transportPIN_title),
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(modifier = Modifier.height(40.dp))
        Box {
            Image(
                painter = painterResource(id = R.drawable.transport_pin),
                contentDescription = null,
                alignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            )
            Box(modifier = Modifier
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
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.NumberPassword, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (pinInput.length != 5) {
                                Log.d("DEBUG", "PIN input too short.")
                            } else {
                                Log.d("DEBUG", "Proceed to next screen.")
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
                                keyboardManager?.show()
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

    LaunchedEffect(Unit) {
        // A bug in Material 3 (1.0.0-alpha11) prevents this from showing the keyboard automatically.
        focusRequester.requestFocus()
    }
}

@Preview
@Composable
fun PreviewTransportPINScreen() {
    UseIDTheme {
        TransportPINScreen()
    }
}