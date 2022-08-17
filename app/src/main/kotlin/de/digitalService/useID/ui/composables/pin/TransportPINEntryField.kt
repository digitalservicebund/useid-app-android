package de.digitalService.useID.ui.composables

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.digitalService.useID.R
import de.digitalService.useID.ui.theme.UseIDTheme

@Composable
fun TransportPINEntryField(
    value: String,
    onValueChanged: (String) -> Unit,
    onDone: () -> Unit,
    focusRequester: FocusRequester
) {
    val description = stringResource(
        id = R.string.firstTimeUser_transportPIN_PINTextFieldDescription,
        value.map { "$it " }
    )

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
        PINEntryField(
            value = value,
            onValueChanged = onValueChanged,
            digitCount = 5,
            obfuscation = false,
            spacerPosition = null,
            contentDescription = description,
            focusRequester = focusRequester,
            onDone = onDone,
            modifier = Modifier
                .align(Alignment.Center)
                .aspectRatio(5f)
                .padding(horizontal = 25.dp)
                .fillMaxSize()
        )
    }
}

@Preview
@Composable
fun PreviewTransportPINEntryField() {
    UseIDTheme {
        TransportPINEntryField(
            value = "12",
            onValueChanged = { },
            onDone = { },
            focusRequester = FocusRequester()
        )
    }
}
