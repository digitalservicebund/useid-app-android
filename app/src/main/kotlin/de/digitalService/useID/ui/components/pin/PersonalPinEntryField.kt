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
fun PersonalPinEntryField(
    value: String,
    onValueChanged: (String) -> Unit,
    onDone: () -> Unit,
    focusRequester: FocusRequester,
    entryDescription: String,
    modifier: Modifier = Modifier
) {
    PinEntryField(
        value = value,
        digitCount = 6,
        obfuscation = true,
        spacerPosition = 3,
        onValueChanged = onValueChanged,
        onDone = onDone,
        contentDescription = entryDescription,
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
        PersonalPinEntryField(
            value = "12",
            onValueChanged = { },
            onDone = { },
            focusRequester = FocusRequester(),
            entryDescription = "Description"
        )
    }
}
