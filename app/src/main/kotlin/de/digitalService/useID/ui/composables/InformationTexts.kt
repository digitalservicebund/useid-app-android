package de.digitalService.useID.ui.composables

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ScreenTitleText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge,

        modifier = Modifier.padding(bottom = 32.dp)
    )
}

@Composable
fun LargeTitleText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontSize = 24.sp,

        modifier = Modifier.padding(bottom = 16.dp)
    )
}

@Composable
fun SmallTitleText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontSize = 19.sp
    )
}

@Composable
fun BodyText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyLarge,
        fontSize = 17.sp,

        modifier = Modifier.padding(bottom = 16.dp)
    )
}
