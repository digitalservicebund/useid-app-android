package de.digitalService.useID.ui.composables.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

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
    }
}