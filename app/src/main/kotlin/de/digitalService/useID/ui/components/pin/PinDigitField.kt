package de.digitalService.useID.ui.components.pin

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.digitalService.useID.ui.theme.UseIdTheme

@Composable
fun PinDigitField(input: Char?, obfuscation: Boolean, placeholder: Boolean) {
    Box(
        modifier = Modifier
            .size(30.dp)
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
                        .clearAndSetSemantics { testTag = "Obfuscation" }
                )
            } else {
                Text(
                    text = input.toString(),
                    style = UseIdTheme.typography.headingXl,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .clearAndSetSemantics { testTag = "PINEntry" }
                )
            }
        } else if (placeholder) {
            Icon(
                imageVector = Icons.Outlined.Circle,
                contentDescription = "",
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(10.dp)
                    .clearAndSetSemantics { testTag = "Placeholder" }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewPinDigitFieldWithInputNotObfuscated() {
    UseIdTheme {
        PinDigitField(input = '2', obfuscation = false, placeholder = false)
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewPinDigitFieldWithInputObfuscated() {
    UseIdTheme {
        PinDigitField(input = '2', obfuscation = true, placeholder = false)
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewPinDigitFieldWithoutInput() {
    UseIdTheme {
        PinDigitField(input = null, obfuscation = false, placeholder = false)
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewPinDigitFieldWithoutInputAndPlaceholder() {
    UseIdTheme {
        PinDigitField(input = null, obfuscation = false, placeholder = true)
    }
}
