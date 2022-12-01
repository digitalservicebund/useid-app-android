package de.digitalService.useID.ui.theme

import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

private val Blue100 = Color(0xFFF2F6F8)
private val Blue200 = Color(0xFFE0F1FB)
private val Blue300 = Color(0xFFDCE8EF)
private val Blue400 = Color(0xFFCCDBE4)
private val Blue500 = Color(0xFFB3C9D6)
private val Blue600 = Color(0xFF6693AD)
val Blue700 = Color(0xFF336F91)
private val Blue800 = Color(0xFF004B76)
private val Blue900 = Color(0xFF003350)

private val NeutralsWhite = Color(0xFFFFFFFF)
private val Neutrals100 = Color(0xFFF6F7F8)
private val Neutrals300 = Color(0xFFEDEEF0)
private val Neutrals400 = Color(0xFFDFE1E5)
private val Neutrals600 = Color(0xFFB8BDC3)
private val Neutrals900 = Color(0xFF4E596A)
private val NeutralsBlack = Color(0xFF0B0C0C)

private val Green100 = Color(0xFFE8F7F0)
private val Green800 = Color(0xFF006538)

private val Yellow200 = Color(0xFFDDD9D2)
private val Yellow600 = Color(0xFFF2DC5D)
private val Yellow900 = Color(0xFFA28C0D)

private val Orange = Color(0xFFCD7610)

private val Red200 = Color(0xFFF9E5EC)
private val Red900 = Color(0xFF8E001B)

val UseIDLightColorPalette = lightColorScheme(
    primary = Blue800,
    onPrimary = NeutralsWhite,
    inversePrimary = Blue900,
    secondary = Blue200,
    onSecondary = Blue800,
    tertiary = Neutrals300,
    onTertiary = Neutrals900,
    background = NeutralsWhite,
    onBackground = NeutralsBlack,
    surface = Blue300,
    onSurface = NeutralsBlack,
    error = Red900,
    errorContainer = Red200,
    onErrorContainer = NeutralsBlack,
)
