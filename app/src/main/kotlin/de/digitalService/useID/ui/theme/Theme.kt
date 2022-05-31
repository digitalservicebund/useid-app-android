package de.digitalService.useID.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable

@Composable
fun UseIDTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val colorScheme = if (darkTheme) {
        // TODO: Replace by dark color palette
        UseIDLightColorPalette
    } else {
        UseIDLightColorPalette
    }

    MaterialTheme(
        typography = UseIDTypography,
        shapes = UseIDShapes,
        content = content,
        colorScheme = colorScheme
    )
}
