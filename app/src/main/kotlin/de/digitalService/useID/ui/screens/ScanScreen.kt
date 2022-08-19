package de.digitalService.useID.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import de.digitalService.useID.R
import de.digitalService.useID.models.ScanError
import de.digitalService.useID.ui.dialogs.ScanErrorAlertDialog
import de.digitalService.useID.ui.dialogs.StandardDialog
import de.digitalService.useID.ui.dialogs.WhatIsNfcDialog
import de.digitalService.useID.ui.theme.UseIDTheme
import dev.jeziellago.compose.markdowntext.MarkdownText

@Composable
fun ScanScreen(
    title: String,
    body: String,
    errorState: ScanError?,
    onIncorrectPIN: @Composable (Int) -> Unit,
    onCancel: () -> Unit,
    showProgress: Boolean,
    modifier: Modifier = Modifier
) {
    var helpDialogShown by remember { mutableStateOf(false) }
    var whatIsNfcDialogShown by remember { mutableStateOf(false) }

    errorState?.let { error ->
        when (error) {
            is ScanError.IncorrectPIN -> onIncorrectPIN(error.attempts)
            else -> ScanErrorAlertDialog(error = error, onButtonTap = onCancel)
        }
    }

    Column(
        verticalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
            .fillMaxSize()
            .padding(vertical = 40.dp, horizontal = 20.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.eid_3),
            contentScale = ContentScale.Fit,
            contentDescription = ""
        )
        Text(
            title,
            style = MaterialTheme.typography.titleLarge
        )
        Text(
            body,
            style = MaterialTheme.typography.bodySmall
        )
        Column {
            Button(
                onClick = { whatIsNfcDialogShown = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                ),
                shape = MaterialTheme.shapes.small,
                modifier = Modifier
                    .height(40.dp)
            ) {
                Text(stringResource(id = R.string.firstTimeUser_scan_whatIsNfc_button))
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { helpDialogShown = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                ),
                shape = MaterialTheme.shapes.small,
                modifier = Modifier
                    .height(40.dp)
            ) {
                Text(stringResource(id = R.string.firstTimeUser_scan_help_button))
            }
        }
    }

    AnimatedVisibility(visible = showProgress) {
        Dialog(onDismissRequest = {}) {
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .background(
                        color = MaterialTheme.colorScheme.background,
                        shape = RoundedCornerShape(10.dp)
                    )
            ) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 10.dp,
                    modifier = Modifier
                        .size(70.dp)
                        .align(Alignment.Center)
                        .testTag("ProgressIndicator")
                )
            }
        }
    }

    AnimatedVisibility(visible = whatIsNfcDialogShown) {
        WhatIsNfcDialog(onButtonTap = { whatIsNfcDialogShown = false })
    }

    AnimatedVisibility(visible = helpDialogShown) {
        StandardDialog(
            title = {
                Text(
                    stringResource(id = R.string.idScan_help_title),
                    style = MaterialTheme.typography.titleMedium
                )
            },
            text = {
                MarkdownText(
                    markdown = stringResource(id = R.string.idScan_help_body),
                    fontResource = R.font.bundes_sans_dtp_regular
                )
            },
            onButtonTap = { helpDialogShown = false }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewWithoutProgress() {
    UseIDTheme {
        ScanScreen(
            title = "Title",
            body = "Body",
            errorState = null,
            onIncorrectPIN = { },
            onCancel = { },
            showProgress = false
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewWithProgress() {
    UseIDTheme {
        ScanScreen(
            title = "Title",
            body = "Body",
            errorState = null,
            onIncorrectPIN = { },
            onCancel = { },
            showProgress = true
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewWithError() {
    UseIDTheme {
        ScanScreen(
            title = "Title",
            body = "Body",
            errorState = ScanError.PINSuspended,
            onIncorrectPIN = { },
            onCancel = { },
            showProgress = false
        )
    }
}
