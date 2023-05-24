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
import de.digitalService.useID.ui.theme.UseIdTheme

@Composable
fun PukDigitRow(
    input: String,
    digitCount: Int,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = modifier
            .background(color = Color.Transparent)
    ) {
        for (position in 0 until digitCount) {
            val char = input.toCharArray().getOrNull(position)

            PukDigitField(
                input = char,
                modifier = Modifier
                    .padding(horizontal = 2.dp)
                    .align(Alignment.Bottom)
            )
        }
    }
}

@Preview
@Composable
fun PreviewPukDigitRow() {
    UseIdTheme {
        PukDigitRow(
            input = "12345",
            digitCount = 10,
            modifier = Modifier
                .width(300.dp)
                .background(Color.White)
        )
    }
}
