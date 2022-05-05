package de.digitalService.useID.ui.theme

import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

val Blue100 = Color(0xFFF2F6F8)
val Blue200 = Color(0xFFE0F1FB)
val Blue300 = Color(0xFFDCE8EF)
val Blue400 = Color(0xFFCCDBE4)
val Blue500 = Color(0xFFB3C9D6)
val Blue600 = Color(0xFF6693AD)
val Blue700 = Color(0xFF336F91)
val Blue800 = Color(0xFF004B76)
val Blue900 = Color(0xFF003350)

val Gray100 = Color(0xFFF6F7F8)
val Gray300 = Color(0xFFEDEEF0)
val Gray600 = Color(0xFFB8BDC3)
val Gray900 = Color(0xFF4E596A)
val Black = Color(0xFF0B0C0C)

val Green100 = Color(0xFFE8F7F0)
val Green800 = Color(0xFF006538)

val Yellow300 = Color(0xFFF9EC9E)
val Yellow600 = Color(0xFFF2DC5D)

val Red200 = Color(0xFFF9E5EC)
val Red900 = Color(0xFF8E001B)

val UseIDLightColorPalette = lightColorScheme(
    primary = Blue800,
    onPrimary = Color.White,
    inversePrimary = Blue900,
    secondary = Blue200,
    onSecondary = Blue900,
    tertiary = Blue400,
    background = Color.White,
    surface = Blue100,
    error = Red900,
    onBackground = Black,
    onSurface = Black,
    onError = Color.White
)