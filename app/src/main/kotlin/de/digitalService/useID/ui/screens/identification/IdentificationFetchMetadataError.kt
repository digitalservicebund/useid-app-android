package de.digitalService.useID.ui.screens.identification

import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.digitalService.useID.R
import de.digitalService.useID.ui.composables.BundButtonConfig
import de.digitalService.useID.ui.composables.StandardButtonScreen
import de.digitalService.useID.ui.theme.UseIDTheme

@Composable
fun IdentificationFetchMetadataError(
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    StandardButtonScreen(
        primaryButton = BundButtonConfig(
            stringResource(id = R.string.identification_fetchMetadataError_button),
            onRetry
        )
    ) {
        Column(modifier = modifier) {
            Text(stringResource(id = R.string.identification_fetchMetadataError_title), style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(20.dp))
            Text(stringResource(id = R.string.identification_fetchMetadataError_body), style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Preview
@Composable
fun PreviewIdentificationFetchMetadataError() {
    UseIDTheme {
        IdentificationFetchMetadataError({ })
    }
}
