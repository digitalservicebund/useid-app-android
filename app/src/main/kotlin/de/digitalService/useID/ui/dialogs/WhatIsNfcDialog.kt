package de.digitalService.useID.ui.dialogs

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import de.digitalService.useID.R
import de.digitalService.useID.ui.theme.UseIDTheme

@Composable
fun WhatIsNfcDialog(onButtonTap: () -> Unit) {
    StandardDialog(
        title = { Text(stringResource(id = R.string.helpNFC_title), style = MaterialTheme.typography.titleMedium) },
        text = { Text(stringResource(id = R.string.helpNFC_body), style = MaterialTheme.typography.bodySmall) },
        onButtonTap = onButtonTap
    )
}

@Preview
@Composable
private fun Preview() {
    UseIDTheme {
        WhatIsNfcDialog(onButtonTap = { })
    }
}
