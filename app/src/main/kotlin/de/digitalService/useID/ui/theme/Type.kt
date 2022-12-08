package de.digitalService.useID.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle

@Immutable
data class UseIdTypography(
    val headingXl: TextStyle,
    val headingL: TextStyle,
    val headingMBold: TextStyle,
    val headingMRegular: TextStyle,

    val bodyLBold: TextStyle,
    val bodyLRegular: TextStyle,
    val bodyMBold: TextStyle,
    val bodyMRegular: TextStyle,

    val captionL: TextStyle,
    val captionM: TextStyle
)

val LocalUseIdTypography = staticCompositionLocalOf {
    UseIdTypography(
        headingXl = TextStyle.Default,
        headingL = TextStyle.Default,
        headingMBold = TextStyle.Default,
        headingMRegular = TextStyle.Default,
        bodyLBold = TextStyle.Default,
        bodyLRegular = TextStyle.Default,
        bodyMBold = TextStyle.Default,
        bodyMRegular = TextStyle.Default,
        captionL = TextStyle.Default,
        captionM = TextStyle.Default
    )
}
