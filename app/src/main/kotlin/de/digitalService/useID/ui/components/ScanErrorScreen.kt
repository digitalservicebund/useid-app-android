package de.digitalService.useID.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.digitalService.useID.R
import de.digitalService.useID.ui.theme.UseIdTheme
import de.digitalService.useID.util.markDownResource
import dev.jeziellago.compose.markdowntext.MarkdownText

@Composable
fun ScanErrorScreen(
    @StringRes titleResId: Int,
    @StringRes bodyResId: Int,
    @StringRes buttonTitleResId: Int,
    showErrorCard: Boolean = false,
    confirmNavigationButtonDialog: Boolean = false,
    onNavigationButtonClicked: () -> Unit,
    onButtonClicked: () -> Unit
) {
    ScreenWithTopBar(
        navigationButton = NavigationButton(
            icon = NavigationIcon.Cancel,
            shouldShowConfirmDialog = confirmNavigationButtonDialog,
            onClick = onNavigationButtonClicked
        )
    ) { topPadding ->
        Column(
            modifier = Modifier
                .padding(top = topPadding)
                .padding(horizontal = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = stringResource(id = titleResId),
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(24.dp))

                if (showErrorCard) {
                    ErrorCard()
                    Spacer(modifier = Modifier.height(24.dp))
                }

                val packageName = LocalContext.current.packageName
                val imagePath = "android.resource://$packageName/${R.drawable.nfc_positions}"

                MarkdownText(
                    markdown = markDownResource(id = bodyResId, imagePath),
                    fontResource = R.font.bundes_sans_dtp_regular
                )

                Spacer(modifier = Modifier.height(24.dp))
            }

            BundButton(
                type = ButtonType.PRIMARY,
                onClick = onButtonClicked,
                label = stringResource(id = buttonTitleResId),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp, top = 12.dp)
                    .height(50.dp)
            )
        }
    }
}

@Composable
private fun ErrorCard() {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
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
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(end = 6.dp)
                )

                Text(
                    text = stringResource(R.string.scanError_box_title),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                stringResource(id = R.string.scanError_box_body),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewCardDeactivated() {
    UseIdTheme {
        ScanErrorScreen(
            titleResId = R.string.scanError_cardUnreadable_title,
            bodyResId = R.string.scanError_cardUnreadable_body,
            buttonTitleResId = R.string.scanError_close,
            showErrorCard = true,
            onNavigationButtonClicked = {},
            onButtonClicked = {}
        )
    }
}
