package de.digitalService.useID.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player.REPEAT_MODE_ONE
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.RawResourceDataSource
import de.digitalService.useID.R
import de.digitalService.useID.ui.components.BundInformationButton
import de.digitalService.useID.ui.dialogs.StandardDialog
import de.digitalService.useID.ui.dialogs.WhatIsNfcDialog
import de.digitalService.useID.ui.theme.UseIdTheme
import dev.jeziellago.compose.markdowntext.MarkdownText

@Composable
fun ScanScreen(
    title: String,
    body: String,
    showProgress: Boolean,
    modifier: Modifier = Modifier,
    onHelpDialogShown: () -> Unit = { },
    onNfcDialogShown: () -> Unit = { }
) {
    var helpDialogShown by remember { mutableStateOf(false) }
    var whatIsNfcDialogShown by remember { mutableStateOf(false) }

    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        val exoPlayer = remember {
            ExoPlayer.Builder(context).build().apply {
                val mediaItem = MediaItem.fromUri(RawResourceDataSource.buildRawResourceUri(R.raw.animation_id_scan_video))
                setMediaItem(mediaItem)
                prepare()
                repeatMode = REPEAT_MODE_ONE
                playWhenReady = true
            }
        }

        val animationDescription = stringResource(id = R.string.scan_animationAccessibilityLabel)
        Box(
            modifier = Modifier
                .semantics { contentDescription = animationDescription }
                .fillMaxWidth()
                .aspectRatio(1.47f)
        ) {
            DisposableEffect(
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = {
                        PlayerView(context).apply {
                            player = exoPlayer
                            useController = false
                        }
                    }
                )
            ) {
                onDispose {
                    exoPlayer.release()
                }
            }
        }

        Spacer(modifier = Modifier.padding(UseIdTheme.spaces.m))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = UseIdTheme.spaces.m)
        ) {
            Text(
                title,
                style = UseIdTheme.typography.headingXl,
                modifier = Modifier.semantics { heading() }
            )

            Spacer(modifier = Modifier.height(UseIdTheme.spaces.m))

            Text(
                body,
                style = UseIdTheme.typography.bodyLRegular
            )

            Spacer(modifier = Modifier.height(UseIdTheme.spaces.m))

            BundInformationButton(
                onClick = {
                    onNfcDialogShown()
                    whatIsNfcDialogShown = true
                },
                label = stringResource(id = R.string.scan_helpNFC)
            )

            Spacer(modifier = Modifier.height(UseIdTheme.spaces.s))

            BundInformationButton(
                onClick = {
                    onHelpDialogShown()
                    helpDialogShown = true
                },
                label = stringResource(id = R.string.scan_helpScanning)
            )

            Spacer(modifier = Modifier.height(UseIdTheme.spaces.s))
        }
    }

    if (showProgress) {
        Dialog(onDismissRequest = {}) {
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .background(
                        color = UseIdTheme.colors.white,
                        shape = UseIdTheme.shapes.roundedMedium
                    )
            ) {
                CircularProgressIndicator(
                    color = UseIdTheme.colors.blue800,
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
        WhatIsNfcDialog(onClose = { whatIsNfcDialogShown = false })
    }

    AnimatedVisibility(visible = helpDialogShown) {
        StandardDialog(
            title = {
                Text(
                    stringResource(id = R.string.scanError_cardUnreadable_title),
                    style = UseIdTheme.typography.headingL
                )
            },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    val packageName = LocalContext.current.packageName
                    val imagePath = "android.resource://$packageName/${R.drawable.nfc_positions}"

                    MarkdownText(
                        markdown = stringResource(
                            id = R.string.scanError_cardUnreadable_body,
                            imagePath
                        ),
                        fontResource = R.font.bundes_sans_dtp_regular,
                        fontSize = UseIdTheme.typography.bodyLRegular.fontSize
                    )
                }
            },
            confirmButtonText = stringResource(id = R.string.scanError_close),
            onDismissButtonClick = { helpDialogShown = false },
            onConfirmButtonClick = { helpDialogShown = false }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewWithoutProgress() {
    UseIdTheme {
        ScanScreen(
            title = "Title",
            body = "Body",
            showProgress = false
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewWithProgress() {
    UseIdTheme {
        ScanScreen(
            title = "Title",
            body = "Body",
            showProgress = true
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewWithError() {
    UseIdTheme {
        ScanScreen(
            title = "Title",
            body = "Body",
            showProgress = false
        )
    }
}
