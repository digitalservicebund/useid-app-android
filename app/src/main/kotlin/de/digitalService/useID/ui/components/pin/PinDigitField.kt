package de.digitalService.useID.ui.components.pin

import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.digitalService.useID.ui.theme.UseIdTheme

@Composable
fun PinDigitField(
    input: Char?,
    obfuscation: Boolean,
    placeholder: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom,
        modifier = modifier
            // To prevent the box from resizing after a first digit is entered, calulcate a suitable height
            // with respect to the current system font scaling
            .defaultMinSize(minWidth = 30.dp, minHeight = if (!obfuscation && !placeholder) (15 + (LocalDensity.current.fontScale * 30)).dp else 30.dp)
    ) {
        if (input != null) {
            if (obfuscation) {
                Icon(
                    imageVector = Icons.Filled.Circle,
                    contentDescription = "",
                    modifier = Modifier
                        .size(16.dp)
                        .clearAndSetSemantics { testTag = "Obfuscation" }
                )
                Spacer(modifier = Modifier.height(UseIdTheme.spaces.xs))
            } else {
                Text(
                    text = input.toString(),
                    style = UseIdTheme.typography.headingXl,
                    modifier = Modifier
                        .clearAndSetSemantics { testTag = "PINEntry" }
                )
            }
        } else if (placeholder) {
            Icon(
                imageVector = Icons.Outlined.Circle,
                contentDescription = "",
                modifier = Modifier
                    .size(10.dp)
                    .clearAndSetSemantics { testTag = "Placeholder" }
            )
            Spacer(modifier = Modifier.height(UseIdTheme.spaces.xs))
        }

        Box(
            modifier = modifier
                .size(width = 30.dp, height = 1.dp)
                .drawBehind {
                    drawLine(
                        color = Color.Black,
                        start = Offset(size.width * 0.1f, size.height),
                        end = Offset(size.width * 0.9f, size.height),
                        strokeWidth = 2f
                    )
                }
                .testTag("PINDigitField")
        )
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
