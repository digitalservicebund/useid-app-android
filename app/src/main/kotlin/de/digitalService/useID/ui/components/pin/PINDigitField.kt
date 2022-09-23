package de.digitalService.useID.ui.components.pin

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun PINDigitField(input: Char?, obfuscation: Boolean, placeholder: Boolean) {
    Box(
        modifier = Modifier
            .width(30.dp)
            .height(30.dp)
            .drawBehind {
                drawLine(
                    color = Color.Black,
                    start = Offset(size.width * 0.1f, size.height),
                    end = Offset(size.width * 0.9f, size.height),
                    strokeWidth = 2f
                )
            }
            .testTag("PINDigitField")
    ) {
        if (input != null) {
            if (obfuscation) {
                Icon(
                    imageVector = Icons.Filled.Circle,
                    contentDescription = "",
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(10.dp)
                        .testTag("Obfuscation")
                )
            } else {
                Text(
                    text = input.toString(),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .testTag("PINEntry")
                )
            }
        } else if (placeholder) {
            Icon(
                imageVector = Icons.Outlined.Circle,
                contentDescription = "",
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(10.dp)
                    .testTag("Placeholder")
            )
        }
    }
}

@Preview
@Composable
fun PreviewPINDigitFieldWithInputNotObfuscated() {
    PINDigitField(input = '2', obfuscation = false, placeholder = false)
}

@Preview
@Composable
fun PreviewPINDigitFieldWithInputObfuscated() {
    PINDigitField(input = '2', obfuscation = true, placeholder = false)
}

@Preview
@Composable
fun PreviewPINDigitFieldWithoutInput() {
    PINDigitField(input = null, obfuscation = false, placeholder = false)
}

@Preview
@Composable
fun PreviewPINDigitFieldWithouInputAndPlaceholder() {
    PINDigitField(input = null, obfuscation = false, placeholder = true)
}
