package de.digitalService.useID.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import de.digitalService.useID.ui.theme.Blue800

@Composable
fun StandardDialog(
    title: @Composable () -> Unit,
    text: @Composable () -> Unit,
    confirmButtonText: String,
    dismissButtonText: String? = null,
    onDismissButtonClick: (() -> Unit)? = null,
    onConfirmButtonClick: () -> Unit
) {
    Dialog(onDismissRequest = onDismissButtonClick ?: { }) {
        Column(
            modifier = Modifier
                .background(Color.White, shape = RoundedCornerShape(10.dp))
                .padding(horizontal = 16.dp)
                .padding(top = 16.dp)
        ) {
            title()

            Spacer(modifier = Modifier.height(16.dp))

            text()

            Spacer(modifier = Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                if (dismissButtonText != null && onDismissButtonClick != null) {
                    TextButton(onClick = onDismissButtonClick) {
                        Text(
                            text = dismissButtonText,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = Blue800
                        )
                    }
                }

                TextButton(onClick = onConfirmButtonClick) {
                    Text(
                        text = confirmButtonText,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = Blue800
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun Preview() {
    StandardDialog(title = { Text(text = "Test Dialog") }, text = { Text(text = "test Dialog text") }, confirmButtonText = "Button text") {
    }
}
