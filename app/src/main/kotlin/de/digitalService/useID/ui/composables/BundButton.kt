package de.digitalService.useID.ui.composables

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

enum class ButtonType {
    PRIMARY, SECONDARY
}

@Composable
fun BundButton(type: ButtonType, onClick: () -> Unit, label: String, modifier: Modifier = Modifier) {
    val containerColor: Color
    val contentColor: Color

    when (type) {
        ButtonType.PRIMARY -> {
            containerColor = MaterialTheme.colorScheme.primary
            contentColor = MaterialTheme.colorScheme.onPrimary
        }
        ButtonType.SECONDARY -> {
            containerColor = MaterialTheme.colorScheme.secondary
            contentColor = MaterialTheme.colorScheme.onSecondary
        }
    }

    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        shape = MaterialTheme.shapes.small,
        modifier = modifier
    ) {
        Text(label)
    }
}

@Composable
fun RegularBundButton(type: ButtonType, onClick: () -> Unit, label: String, modifier: Modifier = Modifier) {
    BundButton(type = type, onClick = onClick, label = label, modifier = modifier.fillMaxWidth().height(50.dp))
}
