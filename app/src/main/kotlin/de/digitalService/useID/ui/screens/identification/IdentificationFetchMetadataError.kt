package de.digitalService.useID.ui.screens.identification

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import de.digitalService.useID.R
import de.digitalService.useID.ui.components.BundButtonConfig
import de.digitalService.useID.ui.components.StandardButtonScreen
import de.digitalService.useID.ui.theme.UseIdTheme

@Composable
fun IdentificationFetchMetadataError(
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    StandardButtonScreen(
        primaryButton = BundButtonConfig(
            stringResource(id = R.string.identification_fetchMetadataError_retry),
            onRetry
        )
    ) {
        Column(
            modifier = modifier
                .padding(horizontal = UseIdTheme.spaces.m)
                .padding(top = UseIdTheme.spaces.m)
        ) {
            Text(stringResource(id = R.string.identification_fetchMetadataError_title), style = UseIdTheme.typography.headingXl)
            Spacer(modifier = Modifier.height(UseIdTheme.spaces.m))
            Text(stringResource(id = R.string.identification_fetchMetadataError_body), style = UseIdTheme.typography.bodyLRegular)
        }
    }
}

@Preview
@Composable
fun PreviewIdentificationFetchMetadataError() {
    UseIdTheme {
        IdentificationFetchMetadataError({ })
    }
}
