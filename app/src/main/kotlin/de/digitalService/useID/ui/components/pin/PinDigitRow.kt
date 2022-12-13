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
        horizontalArrangement = Arrangement.Center,
        modifier = modifier
            .fillMaxWidth()
            .background(color = Color.Transparent)
    ) {
        for (position in 0 until digitCount) {
            val char = input.toCharArray().getOrNull(position)
            spacerPosition?.let {
                if (spacerPosition == position) {
                    Spacer(
                        modifier = Modifier
                            .width(UseIdTheme.spaces.s)
                            .testTag("PINDigitRowSpacer")
                    )
                }
            }

            PinDigitField(
                input = char,
                obfuscation = obfuscation,
                placeholder = placeholder,
                modifier = Modifier.padding(horizontal = UseIdTheme.spaces.xxs)
            )
        }
    }
}

@Preview
@Composable
fun PreviewPinDigitRowTransportPin() {
    UseIdTheme {
        PinDigitRow(
            input = "12",
            digitCount = 5,
            obfuscation = false,
            placeholder = false,
            spacerPosition = null,
            modifier = Modifier
                .width(300.dp)
                .background(Color.White)
        )
    }
}

@Preview
@Composable
fun PreviewPinDigitRowPersonalPin() {
    UseIdTheme {
        PinDigitRow(
            input = "12",
            digitCount = 6,
            obfuscation = true,
            placeholder = false,
            spacerPosition = 3,
            modifier = Modifier
                .width(300.dp)
                .background(Color.White)
        )
    }
}
