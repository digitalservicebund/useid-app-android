package de.digitalService.useID.ui.components.pin

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.digitalService.useID.R
import de.digitalService.useID.ui.theme.UseIdTheme

@Composable
fun TransportPinEntryField(
    onDone: (String) -> Unit,
    focusRequester: FocusRequester,
) {
    val contentDescription = stringResource(id = R.string.firstTimeUser_transportPIN_textFieldLabel)

    Box(
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .aspectRatio(2f)
    ) {
        Image(
            painter = painterResource(id = R.drawable.transport_pin),
            contentDescription = null,
            alignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
        )
        PinEntryField(
            digitCount = 5,
            obfuscation = false,
            spacerPosition = null,
            focusRequester = focusRequester,
            onDone = onDone,
            modifier = Modifier
                .align(Alignment.Center)
                .aspectRatio(5f)
                .padding(horizontal = 25.dp)
                .clip(UseIdTheme.shapes.roundedMedium)
                .fillMaxSize()
                .semantics {
                    this.contentDescription = contentDescription
                }
        )
    }
}

@Preview
@Composable
fun PreviewTransportPinEntryField() {
    UseIdTheme {
        TransportPinEntryField(
            onDone = { },
            focusRequester = FocusRequester()
        )
    }
}
