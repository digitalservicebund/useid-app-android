package de.digitalService.useID.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp

@Immutable
data class UseIdElevations(
    val default: Dp
)

val LocalUseIdElevations = staticCompositionLocalOf {
    UseIdElevations(
        default = Dp.Unspecified
    )
}
