package de.digitalService.useID.ui.components.pin

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.digitalService.useID.ui.theme.UseIdTheme

@Composable
fun NumberEntryField(
    obfuscation: Boolean,
    onDone: (String) -> Unit,
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier
) {
    PinEntryField(
        digitCount = 6,
        obfuscation = obfuscation,
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

@Preview
@Composable
fun PreviewPersonalPinEntryField() {
    UseIdTheme {
        NumberEntryField(
            obfuscation = false,
            onDone = { },
            focusRequester = FocusRequester()
        )
    }
}

@Preview
@Composable
fun PreviewPersonalPinEntryFieldObfuscated() {
    UseIdTheme {
        NumberEntryField(
            obfuscation = true,
            onDone = { },
            focusRequester = FocusRequester()
        )
    }
}
