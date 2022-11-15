package de.digitalService.useID.ui.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.digitalService.useID.R
import de.digitalService.useID.models.ScanError
import de.digitalService.useID.ui.theme.Red200
import de.digitalService.useID.ui.theme.Red900
import de.digitalService.useID.ui.theme.UseIDTheme
import de.digitalService.useID.util.markDownResource
import dev.jeziellago.compose.markdowntext.MarkdownText

@Composable
fun ScanErrorAlertDialog(error: ScanError, onButtonTap: () -> Unit) {
    val buttonTextStringId = if (error is ScanError.CardErrorWithRedirect) {
        R.string.scanError_redirect
    } else {
        R.string.scanError_close
    }

    StandardDialog(
        title = { Text(stringResource(id = error.titleResID), style = MaterialTheme.typography.titleMedium) },
        text = {
            Column {
                if (error is ScanError.CardErrorWithRedirect || error is ScanError.CardErrorWithoutRedirect) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Red200),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(20.dp)
                        ) {
                            Row {
                                Icon(
                                    imageVector = Icons.Outlined.ErrorOutline,
                                    contentDescription = "",
                                    tint = Red900,
                                    modifier = Modifier.padding(end = 6.dp)
                                )

                                Text(
                                    text = stringResource(R.string.scanError_box_title),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                stringResource(id = R.string.scanError_box_body),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                val packageName = LocalContext.current.packageName
                val imagePath = "android.resource://$packageName/${R.drawable.nfc_positions}"

                MarkdownText(
                    markdown = markDownResource(id = error.textResID, imagePath),
                    fontResource = R.font.bundes_sans_dtp_regular,
                    modifier = Modifier.testTag("${error.textResID}")
                )
            }
        },
        confirmButtonText = stringResource(id = buttonTextStringId),
        onConfirmButtonTap = onButtonTap
    )
}

@Preview
@Composable
private fun PreviewPINBlocked() {
    UseIDTheme {
        ScanErrorAlertDialog(error = ScanError.PINBlocked, onButtonTap = { })
    }
}

@Preview
@Composable
private fun PreviewCardErrorWithoutRedirect() {
    UseIDTheme {
        ScanErrorAlertDialog(error = ScanError.CardErrorWithoutRedirect, onButtonTap = { })
    }
}

@Preview
@Composable
private fun PreviewCardErrorWithRedirect() {
    UseIDTheme {
        ScanErrorAlertDialog(error = ScanError.CardErrorWithRedirect(""), onButtonTap = { })
    }
}
