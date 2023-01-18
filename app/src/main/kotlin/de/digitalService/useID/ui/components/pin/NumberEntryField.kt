package de.digitalService.useID.ui.components.pin

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.digitalService.useID.R
import de.digitalService.useID.ui.theme.UseIdTheme

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
                NumberEntryTextField(
                    digitCount = 5,
                    obfuscation = false,
                    spacerPosition = null,
                    focusRequester = focusRequester,
                    onDone = onDone,
                    modifier = modifier
                        .align(Alignment.Center)
                        .aspectRatio(5f)
                        .padding(horizontal = 25.dp)
                        .clip(UseIdTheme.shapes.roundedMedium)
                        .fillMaxSize()
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
                    .height(56.dp)
                    .clip(UseIdTheme.shapes.roundedMedium)
                    .fillMaxWidth(),
                digitsModifier = Modifier.padding(horizontal = 30.dp)
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
