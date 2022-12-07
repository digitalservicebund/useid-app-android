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
import de.digitalService.useID.ui.theme.UseIdTheme
import de.digitalService.useID.util.markDownResource
import dev.jeziellago.compose.markdowntext.MarkdownText

@Composable
fun ScanErrorAlertDialog(error: ScanError, onButtonClick: () -> Unit) {
    val buttonTextStringId = if (error is ScanError.CardErrorWithRedirect) {
        R.string.scanError_redirect
    } else {
        R.string.scanError_close
    }

    StandardDialog(
        title = { Text(stringResource(id = error.titleResID), style = UseIdTheme.typography.headingXl) },
        text = {
            Column {
                if (error is ScanError.CardErrorWithRedirect || error is ScanError.CardErrorWithoutRedirect) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = UseIdTheme.colors.red200),
                        shape = UseIdTheme.shapes.roundedLarge
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(20.dp)
                        ) {
                            Row {
                                Icon(
                                    imageVector = Icons.Outlined.ErrorOutline,
                                    contentDescription = "",
                                    tint = UseIdTheme.colors.red900,
                                    modifier = Modifier.padding(end = 6.dp)
                                )

                                Text(
                                    text = stringResource(R.string.scanError_box_title),
                                    style = UseIdTheme.typography.bodyMBold,
                                )
                            }

                            Spacer(modifier = Modifier.height(UseIdTheme.spaces.xs))

                            Text(
                                stringResource(id = R.string.scanError_box_body),
                                style = UseIdTheme.typography.bodyMRegular
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(UseIdTheme.spaces.m))
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
        onConfirmButtonClick = onButtonClick
    )
}

@Preview
@Composable
private fun PreviewPinBlocked() {
    UseIdTheme {
        ScanErrorAlertDialog(error = ScanError.PinBlocked, onButtonClick = { })
    }
}

@Preview
@Composable
private fun PreviewCardErrorWithoutRedirect() {
    UseIdTheme {
        ScanErrorAlertDialog(error = ScanError.CardErrorWithoutRedirect, onButtonClick = { })
    }
}

@Preview
@Composable
private fun PreviewCardErrorWithRedirect() {
    UseIdTheme {
        ScanErrorAlertDialog(error = ScanError.CardErrorWithRedirect(""), onButtonClick = { })
    }
}
