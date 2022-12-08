package de.digitalService.useID.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.digitalService.useID.ui.theme.UseIdTheme

enum class ButtonType {
    PRIMARY, SECONDARY
}

@Composable
fun BundButton(type: ButtonType, onClick: () -> Unit, label: String, modifier: Modifier = Modifier) {
    val containerColor: Color
    val contentColor: Color

    when (type) {
        ButtonType.PRIMARY -> {
            containerColor = UseIdTheme.colors.blue800
            contentColor = UseIdTheme.colors.white
        }
        ButtonType.SECONDARY -> {
            containerColor = UseIdTheme.colors.blue200
            contentColor = UseIdTheme.colors.blue800
        }
    }

    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        shape = UseIdTheme.shapes.roundedMedium,
        modifier = modifier.fillMaxWidth().height(56.dp)
    ) {
        Text(
            text = label,
            style = UseIdTheme.typography.bodyLBold
        )
    }
}

@Preview
@Composable
private fun PreviewPrimacy() {
    UseIdTheme {
        BundButton(type = ButtonType.PRIMARY, onClick = { }, label = "PRIMARY")
    }
}

@Preview
@Composable
private fun PreviewSecondary() {
    UseIdTheme {
        BundButton(type = ButtonType.SECONDARY, onClick = { }, label = "SECONDARY")
    }
}
