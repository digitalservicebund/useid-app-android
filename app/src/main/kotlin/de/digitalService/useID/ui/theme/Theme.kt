package de.digitalService.useID.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.digitalService.useID.R

private val UseIDLightColorPalette = lightColors(
    primary = Blue800,
    primaryVariant = Blue900,
    secondary = Blue200,
    secondaryVariant = Blue400,
    background = Color.White,
    surface = Blue100,
    error = Red900,
    onPrimary = Color.White,
    onSecondary = Blue700,
    onBackground = Black,
    onSurface = Black,
    onError = Color.White
)

private val BundesSans = FontFamily(
    Font(R.font.bundes_sans_dtp_regular, FontWeight.Normal, FontStyle.Normal),
    Font(R.font.bundes_sans_dtp_regular_italic, FontWeight.Normal, FontStyle.Italic),
    Font(R.font.bundes_sans_dtp_medium, FontWeight.Medium, FontStyle.Normal),
    Font(R.font.bundes_sans_dtp_medium_italic, FontWeight.Medium, FontStyle.Italic),
    Font(R.font.bundes_sans_dtp_light, FontWeight.Light, FontStyle.Normal),
    Font(R.font.bundes_sans_dtp_light_italic, FontWeight.Light, FontStyle.Italic),
    Font(R.font.bundes_sans_dtp_bold, FontWeight.Bold, FontStyle.Normal),
    Font(R.font.bundes_sans_dtp_bold_italic, FontWeight.Bold, FontStyle.Italic),
    Font(R.font.bundes_sans_dtp_black, FontWeight.Black, FontStyle.Normal),
    Font(R.font.bundes_sans_dtp_black_italic, FontWeight.Black, FontStyle.Italic),
)

private val largeTitle = TextStyle(fontFamily = BundesSans, fontWeight = FontWeight.Bold, fontSize = 30.sp)
private val title = TextStyle(fontFamily = BundesSans, fontWeight = FontWeight.Bold, fontSize = 26.sp)
private val header = TextStyle(fontFamily = BundesSans, fontWeight = FontWeight.Bold, fontSize = 20.sp)
private val bodyBold = TextStyle(fontFamily = BundesSans, fontWeight = FontWeight.Bold, fontSize = 18.sp)
private val body = TextStyle(fontFamily = BundesSans, fontWeight = FontWeight.Normal, fontSize = 18.sp)
private val subtextBold = TextStyle(fontFamily = BundesSans, fontWeight = FontWeight.Bold, fontSize = 16.sp)
private val subtext = TextStyle(fontFamily = BundesSans, fontWeight = FontWeight.Normal, fontSize = 16.sp)
private val caption1 = TextStyle(fontFamily = BundesSans, fontWeight = FontWeight.Normal, fontSize = 14.sp)
private val caption2 = TextStyle(fontFamily = BundesSans, fontWeight = FontWeight.Normal, fontSize = 12.sp)

val UseIDTypography = androidx.compose.material.Typography(
    defaultFontFamily = BundesSans,
    h4 = largeTitle,
    h5 = title,
    h6 = header,
    body1 = body,
    body2 = bodyBold,
    subtitle1 = subtext,
    subtitle2 = subtextBold,
    caption = caption1,
    overline = caption2
)

val UseIDShapes = androidx.compose.material.Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(30.dp)
)

@Composable
fun UseIDTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val colors = if (darkTheme) {
        // TODO: Replace by dark color palette
        UseIDLightColorPalette
    } else {
        UseIDLightColorPalette
    }

    MaterialTheme(
        colors = colors,
        typography = UseIDTypography,
        shapes = UseIDShapes,
        content = content
    )
}