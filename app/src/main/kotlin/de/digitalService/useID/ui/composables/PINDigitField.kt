package de.digitalService.useID.ui.composables

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun PINDigitField(input: Boolean, placeholder: Boolean) {
    Box(
        modifier = Modifier
            .width(30.dp)
            .height(30.dp)
            .drawBehind {
                drawLine(
                    color = Color.Black,
                    start = Offset(0f, size.height),
                    end = Offset(size.width, size.height),
                    strokeWidth = 2f
                )
            }
    ) {
        if (input) {
            Icon(
                imageVector = Icons.Filled.Circle,
                contentDescription = "",
                modifier = Modifier.align(
                    Alignment.Center
                ).size(10.dp)
            )
        } else if (placeholder) {
            Icon(
                imageVector = Icons.Outlined.Circle,
                contentDescription = "",
                modifier = Modifier.align(
                    Alignment.Center
                ).size(10.dp)
            )
        }
    }
}

@Preview
@Composable
fun PreviewPINDigitFieldWithInput() {
    PINDigitField(input = true, placeholder = false)
}

@Preview
@Composable
fun PreviewPINDigitFieldWithouInput() {
    PINDigitField(input = false, placeholder = false)
}

@Preview
@Composable
fun PreviewPINDigitFieldWithouInputAndPlaceholder() {
    PINDigitField(input = false, placeholder = true)
}
