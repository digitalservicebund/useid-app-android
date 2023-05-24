package de.digitalService.useID.ui.components.pin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.size
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
fun PukDigitField(
    input: Char?,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom,
        modifier = modifier
            .defaultMinSize(minWidth = 24.dp, minHeight = 24.dp)
    ) {
        if (input != null) {
            Text(
                text = input.toString(),
                style = UseIdTheme.typography.headingL,
                modifier = Modifier
                    .clearAndSetSemantics { testTag = "PUKEntry" }
            )
        }

        Box(
            modifier = Modifier
                .size(width = 24.dp, height = 1.dp)
                .drawBehind {
                    drawLine(
                        color = Color.Black,
                        start = Offset(size.width * 0.1f, size.height),
                        end = Offset(size.width * 0.9f, size.height),
                        strokeWidth = 2f
                    )
                }
                .testTag("PUKDigitField")
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewPukDigitFieldWithInput() {
    UseIdTheme {
        PukDigitField(input = '2')
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewPukDigitFieldWithoutInput() {
    UseIdTheme {
        PukDigitField(input = null)
    }
}
