package de.digitalService.useID.ui.composables

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import de.digitalService.useID.R
import de.digitalService.useID.ui.ScanError
import de.digitalService.useID.ui.composables.screens.identification.IdentificationScanViewModelInterface

@Composable
fun ScanErrorAlertDialog(error: ScanError, onButtonTap: () -> Unit) {
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
                Text(stringResource(id = R.string.idScan_error_button_close))
            }
        },
        shape = RoundedCornerShape(10.dp),
        title = { Text(stringResource(id = error.titleResID), style = MaterialTheme.typography.titleMedium) },
        text = { Text(stringResource(id = error.textResID), style = MaterialTheme.typography.bodySmall) },
        containerColor = MaterialTheme.colorScheme.background,
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    )
}
