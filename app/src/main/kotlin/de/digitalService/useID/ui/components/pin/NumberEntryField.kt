package de.digitalService.useID.ui.components.pin

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.invisibleToUser
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.digitalService.useID.R
import de.digitalService.useID.ui.theme.UseIdTheme

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun NumberEntryField(
    inputType: InputType,
    onDone: (String) -> Unit,
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier
) {
    when (inputType) {
        InputType.TransportPin ->
            Box(
                modifier = Modifier
                    .aspectRatio(2f)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.transport_pin),
                    contentDescription = null,
                    alignment = Alignment.Center,
                    contentScale = ContentScale.FillWidth,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxWidth()
                        .semantics { invisibleToUser() }
                )
                NumberEntryTextField(
                    digitCount = 5,
                    obfuscation = false,
                    spacerPosition = null,
                    focusRequester = focusRequester,
                    onDone = onDone,
                    modifier = modifier
                        .align(Alignment.Center)
                        .padding(horizontal = 25.dp)
                        .clip(UseIdTheme.shapes.roundedMedium)
                )
            }

        InputType.Pin, InputType.Can ->
            NumberEntryTextField(
                digitCount = 6,
                obfuscation = inputType == InputType.Pin,
                spacerPosition = 3,
                onDone = onDone,
                focusRequester = focusRequester,
                backgroundColor = UseIdTheme.colors.neutrals100,
                modifier = modifier
                    .defaultMinSize(minHeight = 56.dp)
                    .clip(UseIdTheme.shapes.roundedMedium)
                    .fillMaxWidth()
            )
    }
}

@Preview
@Composable
private fun PreviewTransportPinEntryField() {
    UseIdTheme {
        NumberEntryField(
            inputType = InputType.TransportPin,
            onDone = { },
            focusRequester = FocusRequester()
        )
    }
}

@Preview
@Composable
private fun PreviewPinEntryField() {
    UseIdTheme {
        NumberEntryField(
            inputType = InputType.Pin,
            onDone = { },
            focusRequester = FocusRequester()
        )
    }
}

@Preview
@Composable
private fun PreviewCanEntryField() {
    UseIdTheme {
        NumberEntryField(
            inputType = InputType.Can,
            onDone = { },
            focusRequester = FocusRequester()
        )
    }
}
