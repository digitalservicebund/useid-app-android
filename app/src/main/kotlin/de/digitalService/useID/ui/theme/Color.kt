package de.digitalService.useID.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class UseIdColors(
    val blue100: Color,
    val blue200: Color,
    val blue300: Color,
    val blue400: Color,
    val blue500: Color,
    val blue600: Color,
    val blue700: Color,
    val blue800: Color,
    val blue900: Color,

    val green100: Color,
    val green800: Color,

    val yellow200: Color,
    val yellow600: Color,
    val yellow900: Color,

    val orange400: Color,

    val red200: Color,
    val red900: Color,

    val white: Color,
    val neutrals100: Color,
    val neutrals300: Color,
    val neutrals400: Color,
    val neutrals600: Color,
    val neutrals900: Color,
    val black: Color
)

val LocalUseIdColors = staticCompositionLocalOf {
    UseIdColors(
        blue100 = Color.Unspecified,
        blue200 = Color.Unspecified,
        blue300 = Color.Unspecified,
        blue400 = Color.Unspecified,
        blue500 = Color.Unspecified,
        blue600 = Color.Unspecified,
        blue700 = Color.Unspecified,
        blue800 = Color.Unspecified,
        blue900 = Color.Unspecified,

        green100 = Color.Unspecified,
        green800 = Color.Unspecified,

        yellow200 = Color.Unspecified,
        yellow600 = Color.Unspecified,
        yellow900 = Color.Unspecified,

        orange400 = Color.Unspecified,

        red200 = Color.Unspecified,
        red900 = Color.Unspecified,

        white = Color.Unspecified,
        neutrals100 = Color.Unspecified,
        neutrals300 = Color.Unspecified,
        neutrals400 = Color.Unspecified,
        neutrals600 = Color.Unspecified,
        neutrals900 = Color.Unspecified,
        black = Color.Unspecified
    )
}
