package de.digitalService.useID.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.digitalService.useID.R

@Composable
fun UseIdTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val useIdColors = UseIdColors(
        blue100 = Color(0xFFF2F6F8),
        blue200 = Color(0xFFE0F1FB),
        blue300 = Color(0xFFDCE8EF),
        blue400 = Color(0xFFCCDBE4),
        blue500 = Color(0xFFB3C9D6),
        blue600 = Color(0xFF6693AD),
        blue700 = Color(0xFF336F91),
        blue800 = Color(0xFF004B76),
        blue900 = Color(0xFF003350),

        green100 = Color(0xFFE8F7F0),
        green800 = Color(0xFF006538),

        yellow200 = Color(0xFFDDD9D2),
        yellow600 = Color(0xFFF2DC5D),
        yellow900 = Color(0xFFA28C0D),

        orange400 = Color(0xFFCD7610),

        red200 = Color(0xFFF9E5EC),
        red900 = Color(0xFF8E001B),

        white = Color(0xFFFFFFFF),
        neutrals100 = Color(0xFFF6F7F8),
        neutrals300 = Color(0xFFEDEEF0),
        neutrals400 = Color(0xFFDFE1E5),
        neutrals600 = Color(0xFFB8BDC3),
        neutrals900 = Color(0xFF4E596A),
        black = Color(0xFF0B0C0C)
    )

    val bundesSans = FontFamily(
        Font(R.font.bundes_sans_dtp_regular, FontWeight.Normal, FontStyle.Normal),
        Font(R.font.bundes_sans_dtp_regular_italic, FontWeight.Normal, FontStyle.Italic),
        Font(R.font.bundes_sans_dtp_medium, FontWeight.Medium, FontStyle.Normal),
        Font(R.font.bundes_sans_dtp_medium_italic, FontWeight.Medium, FontStyle.Italic),
        Font(R.font.bundes_sans_dtp_light, FontWeight.Light, FontStyle.Normal),
        Font(R.font.bundes_sans_dtp_light_italic, FontWeight.Light, FontStyle.Italic),
        Font(R.font.bundes_sans_dtp_bold, FontWeight.Bold, FontStyle.Normal),
        Font(R.font.bundes_sans_dtp_bold_italic, FontWeight.Bold, FontStyle.Italic),
        Font(R.font.bundes_sans_dtp_black, FontWeight.Black, FontStyle.Normal),
        Font(R.font.bundes_sans_dtp_black_italic, FontWeight.Black, FontStyle.Italic)
    )

    val useIdTypography = UseIdTypography(
        headingXl = TextStyle(fontFamily = bundesSans, fontWeight = FontWeight.Bold, fontSize = 30.sp),
        headingL = TextStyle(fontFamily = bundesSans, fontWeight = FontWeight.Bold, fontSize = 26.sp),
        headingMBold = TextStyle(fontFamily = bundesSans, fontWeight = FontWeight.Bold, fontSize = 20.sp),
        headingMRegular = TextStyle(fontFamily = bundesSans, fontWeight = FontWeight.Normal, fontSize = 20.sp),

        bodyLBold = TextStyle(fontFamily = bundesSans, fontWeight = FontWeight.Bold, fontSize = 18.sp),
        bodyLRegular = TextStyle(fontFamily = bundesSans, fontWeight = FontWeight.Normal, fontSize = 18.sp),
        bodyMBold = TextStyle(fontFamily = bundesSans, fontWeight = FontWeight.Bold, fontSize = 16.sp),
        bodyMRegular = TextStyle(fontFamily = bundesSans, fontWeight = FontWeight.Normal, fontSize = 16.sp),

        captionL = TextStyle(fontFamily = bundesSans, fontWeight = FontWeight.Normal, fontSize = 14.sp),
        captionM = TextStyle(fontFamily = bundesSans, fontWeight = FontWeight.Normal, fontSize = 12.sp)
    )

    val useIdShapes = UseIdShapes(
        roundedSmall = RoundedCornerShape(8.dp),
        roundedMedium = RoundedCornerShape(10.dp),
        roundedLarge = RoundedCornerShape(16.dp)
    )

    val useIdElevations = UseIdElevations(
        default = 3.dp
    )

    val useIdSpaces = UseIdSpaces(
        xxs = 4.dp,
        xs = 8.dp,
        s = 16.dp,
        m = 24.dp,
        l = 32.dp,
        xl = 48.dp,
        xxl = 72.dp,
        xxxl = 96.dp
    )

    CompositionLocalProvider(
        LocalUseIdColors provides useIdColors,
        LocalUseIdTypography provides useIdTypography,
        LocalUseIdShapes provides useIdShapes,
        LocalUseIdElevations provides useIdElevations,
        LocalUseIdSpaces provides useIdSpaces,
        content = content
    )
}

object UseIdTheme {
    val colors: UseIdColors
        @Composable
        get() = LocalUseIdColors.current
    val typography: UseIdTypography
        @Composable
        get() = LocalUseIdTypography.current
    val shapes: UseIdShapes
        @Composable
        get() = LocalUseIdShapes.current
    val elevations: UseIdElevations
        @Composable
        get() = LocalUseIdElevations.current
    val spaces: UseIdSpaces
        @Composable
        get() = LocalUseIdSpaces.current
}
