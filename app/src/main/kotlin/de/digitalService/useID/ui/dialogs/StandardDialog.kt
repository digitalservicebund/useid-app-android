package de.digitalService.useID.ui.dialogs

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties

@Composable
fun StandardDialog(
    title: @Composable () -> Unit,
    text: @Composable () -> Unit,
    confirmButtonText: String,
    dismissButtonText: String? = null,
    onDismissButtonTap: (() -> Unit)? = null,
    onConfirmButtonTap: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { },
        confirmButton = {
            Button(
                onClick = onConfirmButtonTap,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(confirmButtonText)
            }
        },
        dismissButton = {
            if (dismissButtonText == null || onDismissButtonTap == null) {
                return@AlertDialog
            }

            Button(
                onClick = onDismissButtonTap,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(dismissButtonText)
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
    StandardDialog(title = { Text(text = "Test Dialog") }, text = { Text(text = "test Dialog text") }, confirmButtonText = "Button text") {
    }
}
