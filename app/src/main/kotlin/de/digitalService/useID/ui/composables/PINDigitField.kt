package de.digitalService.useID.ui.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun PINDigitField(input: Boolean) {
    Box(modifier = Modifier
        .width(30.dp)
        .height(30.dp)
        .drawBehind {
            drawLine(
                color = Color.Black,
                start = Offset(0f, size.height),
                end = Offset(size.width, size.height),
                strokeWidth = 2f
            )
        }) {
        if (input) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(color = Color.Black)
            )
        }
    }
}

@Preview
@Composable
fun PreviewPINDigitFieldWithInput() {
    PINDigitField(input = true)
}

@Preview
@Composable
fun PreviewPINDigitFieldWithouInput() {
    PINDigitField(input = false)
}