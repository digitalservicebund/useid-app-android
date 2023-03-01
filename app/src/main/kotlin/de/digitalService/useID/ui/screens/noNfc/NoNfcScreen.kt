package de.digitalService.useID.ui.screens.noNfc

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.digitalService.useID.R
import de.digitalService.useID.ui.theme.UseIdTheme

@Composable
fun NoNfcScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(),
            color = UseIdTheme.colors.blue200
        ) {
            Image(
                painter = painterResource(id = R.drawable.illustration_no_nfc),
                contentDescription = "",
                modifier = Modifier
                    .padding(vertical = 79.dp)
                    .semantics { testTag = "NoNfcImage" }
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = UseIdTheme.spaces.xl)
        ) {
            Text(
                text = stringResource(R.string.noNfc_info_title),
                style = UseIdTheme.typography.headingL,
                color = UseIdTheme.colors.black,
                modifier = Modifier
                    .padding(top = UseIdTheme.spaces.m)
                    .semantics { heading() }
            )

            Spacer(modifier = Modifier.height(UseIdTheme.spaces.s))

            Text(
                text = stringResource(R.string.noNfc_info_body),
                style = UseIdTheme.typography.bodyLRegular,
                color = UseIdTheme.colors.black
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun Preview() {
    UseIdTheme {
        NoNfcScreen()
    }
}
