package de.digitalService.useID.ui.components.pin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun PinDigitRow(
    input: String,
    digitCount: Int,
    obfuscation: Boolean,
    placeholder: Boolean,
    spacerPosition: Int?,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceAround,
        modifier = modifier.fillMaxWidth().background(color = Color.Transparent)
    ) {
        for (position in 0 until digitCount) {
            val char = input.toCharArray().getOrNull(position)
            spacerPosition?.let {
                if (spacerPosition == position) {
                    Spacer(
                        modifier = Modifier
                            .width(8.dp)
                            .testTag("PINDigitRowSpacer")
                    )
                }
            }

            PinDigitField(input = char, obfuscation = obfuscation, placeholder = placeholder)
        }
    }
}

@Preview
@Composable
fun PreviewPinDigitRowTransportPin() {
    PinDigitRow(input = "12", digitCount = 5, obfuscation = false, placeholder = false, spacerPosition = null, modifier = Modifier.width(300.dp))
}

@Preview
@Composable
fun PreviewPinDigitRowPersonalPin() {
    PinDigitRow(input = "12", digitCount = 6, obfuscation = true, placeholder = false, spacerPosition = 3, modifier = Modifier.width(300.dp))
}
