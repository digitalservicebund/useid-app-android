package de.digitalService.useID.ui.dialogs

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import de.digitalService.useID.ui.theme.UseIdTheme

@Composable
fun StandardDialog(
    title: @Composable () -> Unit,
    text: @Composable () -> Unit,
    confirmButtonText: String,
    dismissButtonText: String? = null,
    onDismissButtonClick: (() -> Unit)? = null,
    onConfirmButtonClick: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissButtonClick ?: { },
        dismissButton = {
            if (dismissButtonText != null && onDismissButtonClick != null) {
                TextButton(onClick = onDismissButtonClick) {
                    Text(
                        text = dismissButtonText,
                        style = UseIdTheme.typography.bodyLBold,
                        color = UseIdTheme.colors.blue800
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirmButtonClick) {
                Text(
                    text = confirmButtonText,
                    style = UseIdTheme.typography.bodyLBold,
                    color = UseIdTheme.colors.blue800
                )
            }
        },
        containerColor = UseIdTheme.colors.white,
        shape = UseIdTheme.shapes.roundedMedium,
        title = title,
        text = text
    )
}

@Preview
@Composable
private fun Preview() {
    UseIdTheme {
        StandardDialog(
            title = { Text(text = "Test Dialog") },
            text = { Text(text = "test Dialog text") },
            confirmButtonText = "Confirm"
        ) {
        }
    }
}

@Preview
@Composable
private fun PreviewWithDismissButton() {
    UseIdTheme {
        StandardDialog(
            title = { Text(text = "Test Dialog") },
            text = { Text(text = "test Dialog text") },
            confirmButtonText = "Confirm",
            dismissButtonText = "Dismiss",
            onDismissButtonClick = {}
        ) {
        }
    }
}
