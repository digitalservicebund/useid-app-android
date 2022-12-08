package de.digitalService.useID.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp

@Immutable
data class UseIdSpaces(
    val xxs: Dp,
    val xs: Dp,
    val s: Dp,
    val m: Dp,
    val l: Dp,
    val xl: Dp,
    val xxl: Dp,
    val xxxl: Dp
)

val LocalUseIdSpaces = staticCompositionLocalOf {
    UseIdSpaces(
        xxs = Dp.Unspecified,
        xs = Dp.Unspecified,
        s = Dp.Unspecified,
        m = Dp.Unspecified,
        l = Dp.Unspecified,
        xl = Dp.Unspecified,
        xxl = Dp.Unspecified,
        xxxl = Dp.Unspecified
    )
}
