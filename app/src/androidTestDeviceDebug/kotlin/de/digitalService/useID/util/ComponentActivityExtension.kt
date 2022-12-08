package de.digitalService.useID.util

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import de.digitalService.useID.ui.theme.UseIdTheme

fun ComponentActivity.setContentUsingUseIdTheme(
    content: @Composable () -> Unit
) {
    setContent {
        UseIdTheme {
            content()
        }
    }
}

