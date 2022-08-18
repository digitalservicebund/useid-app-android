package de.digitalService.useID.ui.dialogs

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import de.digitalService.useID.R
import de.digitalService.useID.models.ScanError
import de.digitalService.useID.ui.theme.UseIDTheme
import dev.jeziellago.compose.markdowntext.MarkdownText

@Composable
fun ScanErrorAlertDialog(error: ScanError, onButtonTap: () -> Unit) {
    StandardDialog(
        title = { Text(stringResource(id = error.titleResID), style = MaterialTheme.typography.titleMedium) },
        text = { MarkdownText(markdown = stringResource(id = error.textResID), fontResource = R.font.bundes_sans_dtp_regular) },
        onButtonTap = onButtonTap
    )
}

@Preview
@Composable
private fun Preview() {
    UseIDTheme {
        ScanErrorAlertDialog(error = ScanError.PINBlocked, onButtonTap = { })
    }
}
