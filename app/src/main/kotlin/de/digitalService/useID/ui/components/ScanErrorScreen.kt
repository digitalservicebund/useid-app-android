package de.digitalService.useID.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
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
    onButtonClicked: () -> Unit,
    isIdentification: Boolean = false
) {
    ScreenWithTopBar(
        navigationButton = NavigationButton(
            icon = NavigationIcon.Cancel,
            confirmation = Flow.Identification.takeIf { confirmNavigationButtonDialog },
            onClick = onNavigationButtonClicked
        )
    ) { topPadding ->
        Column(
            modifier = Modifier
                .padding(top = topPadding)
                .padding(horizontal = UseIdTheme.spaces.m)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = stringResource(id = titleResId),
                    style = UseIdTheme.typography.headingXl,
                    modifier = Modifier.semantics { heading() }
                )

                Spacer(modifier = Modifier.height(UseIdTheme.spaces.m))

                if (showErrorCard) {
                    BundCard(
                        type = BundCardType.ERROR,
                        title = stringResource(id = R.string.scanError_box_title),
                        body = stringResource(id = R.string.scanError_box_body)
                    )
                    Spacer(modifier = Modifier.height(UseIdTheme.spaces.m))
                }

                val packageName = LocalContext.current.packageName
                val imagePath = "android.resource://$packageName/${R.drawable.nfc_positions}"

                MarkdownText(
                    markdown = markDownResource(id = bodyResId, imagePath),
                    fontResource = R.font.bundes_sans_dtp_regular,
                    fontSize = UseIdTheme.typography.bodyLRegular.fontSize
                )

                Spacer(modifier = Modifier.height(UseIdTheme.spaces.m))
            }

            BundButton(
                type = ButtonType.PRIMARY,
                onClick = onButtonClicked,
                label = stringResource(id = buttonTitleResId),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = UseIdTheme.spaces.m, top = 12.dp)
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
