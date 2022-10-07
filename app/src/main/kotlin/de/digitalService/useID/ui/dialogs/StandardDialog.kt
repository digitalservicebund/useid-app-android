package de.digitalService.useID.ui.dialogs

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties

@Composable
fun StandardDialog(title: @Composable () -> Unit, text: @Composable () -> Unit, buttonText: String, onButtonTap: () -> Unit) {
    AlertDialog(
        onDismissRequest = { },
        confirmButton = {
            Button(
                onClick = onButtonTap,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(buttonText)
            }
        },
        shape = RoundedCornerShape(10.dp),
        title = title,
        text = text,
        containerColor = MaterialTheme.colorScheme.background,
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    )
}

@Preview
@Composable
private fun Preview() {
    StandardDialog(title = { Text(text = "Test Dialog") }, text = { Text(text = "test Dialog text") }, buttonText = "Button text") {
    }
}
