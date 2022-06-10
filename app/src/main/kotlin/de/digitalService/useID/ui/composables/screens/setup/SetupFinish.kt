package de.digitalService.useID.ui.composables.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.digitalService.useID.ui.composables.BundButton
import de.digitalService.useID.ui.composables.ButtonType
import de.digitalService.useID.ui.theme.UseIDTheme

@Composable
fun SetupFinish() {
    Column(
        verticalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 40.dp, horizontal = 20.dp)
    ) {
        Text(
            "success_title",
            style = MaterialTheme.typography.titleLarge
        )
        Text(
            "success_body",
            style = MaterialTheme.typography.bodySmall
        )
        BundButton(type = ButtonType.PRIMARY, onClick = { }, label = "Close")
    }
}

@Preview
@Composable
fun PreviewSetupFinish() {
    UseIDTheme {
        SetupFinish()
    }
}
