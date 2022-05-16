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
import de.digitalService.useID.ui.composables.TransportPINEntryField
import de.digitalService.useID.ui.theme.UseIDTheme

@Composable
fun TransportPINScreen() {
    val focusRequester = remember { FocusRequester() }

    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        Text(
            text = stringResource(id = R.string.firstTimeUser_transportPIN_title),
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(modifier = Modifier.height(40.dp))
        TransportPINEntryField(onDone = { }, focusRequester = focusRequester, modifier = Modifier.padding(horizontal = 20.dp))
    }

    LaunchedEffect(Unit) {
        // A bug in Material 3 (1.0.0-alpha11) prevents this from showing the keyboard automatically sometimes.
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