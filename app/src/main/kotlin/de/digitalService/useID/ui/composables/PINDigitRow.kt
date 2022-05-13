package de.digitalService.useID.ui.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun PINDigitRow(input: String, modifier: Modifier = Modifier) {
    Row(horizontalArrangement = Arrangement.SpaceAround, modifier = modifier) {
        for (position in 1..5) {
            PINDigitField(input = input.length >= position)
        }
    }
}

@Preview
@Composable
fun PreviewPINDigitRow() {
    PINDigitRow(input = "12", modifier = Modifier.width(300.dp))
}