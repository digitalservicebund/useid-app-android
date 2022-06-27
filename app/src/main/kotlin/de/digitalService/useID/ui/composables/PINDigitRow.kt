package de.digitalService.useID.ui.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun PINDigitRow(input: String, digitCount: Int, obfuscation: Boolean, placeholder: Boolean, spacerPosition: Int?, modifier: Modifier = Modifier) {
    Row(horizontalArrangement = Arrangement.SpaceAround, modifier = modifier) {
        for (position in 1..digitCount) {
            val char = input.toCharArray().getOrNull(position - 1)
            PINDigitField(input = char, obfuscation = obfuscation,  placeholder = placeholder)
            spacerPosition?.let {
                if (spacerPosition == position) {
                    Spacer(modifier = Modifier.width(8.dp))
                }
            }
        }
    }
}

@Preview
@Composable
fun PreviewPINDigitRowTransportPIN() {
    PINDigitRow(input = "12", digitCount = 5, obfuscation = false, placeholder = false, spacerPosition = null, modifier = Modifier.width(300.dp))
}

@Preview
@Composable
fun PreviewPINDigitRowPersonalPIN() {
    PINDigitRow(input = "12", digitCount = 6, obfuscation = true, placeholder = false, spacerPosition = 3, modifier = Modifier.width(300.dp))
}
