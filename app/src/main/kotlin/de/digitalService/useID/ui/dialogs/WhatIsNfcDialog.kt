package de.digitalService.useID.ui.dialogs

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import de.digitalService.useID.R
import de.digitalService.useID.ui.theme.UseIdTheme

@Composable
fun WhatIsNfcDialog(onClose: () -> Unit) {
    StandardDialog(
        title = {
            Text(
                text = stringResource(id = R.string.helpNFC_title),
                style = UseIdTheme.typography.headingL,
                modifier = Modifier.semantics { heading() }
            )
        },
        text = {
            Text(
                stringResource(id = R.string.helpNFC_body),
                style = UseIdTheme.typography.bodyLRegular,
                modifier = Modifier.verticalScroll(rememberScrollState())
            )
        },
        confirmButtonText = stringResource(id = R.string.scanError_close),
        onConfirmButtonClick = onClose,
        onDismissButtonClick = onClose
    )
}

@Preview
@Composable
private fun Preview() {
    UseIdTheme {
        WhatIsNfcDialog(onClose = {})
    }
}
