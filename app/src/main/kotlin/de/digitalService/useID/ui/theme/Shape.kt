package de.digitalService.useID.ui.theme

import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp

@Immutable
data class UseIdShapes(
    val roundedSmall: CornerBasedShape,
    val roundedMedium: CornerBasedShape,
    val roundedLarge: CornerBasedShape,
)

val LocalUseIdShapes = staticCompositionLocalOf {
    UseIdShapes(
        roundedSmall = ShapeDefaults.Small,
        roundedMedium = ShapeDefaults.Medium,
        roundedLarge = ShapeDefaults.Large
    )
}
