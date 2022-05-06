package de.digitalService.useID.ui.theme

import androidx.compose.material.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import de.digitalService.useID.R

private val inter = FontFamily(
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_medium, FontWeight.Medium, FontStyle.Normal),
    Font(R.font.inter_light, FontWeight.Light),
    Font(R.font.inter_extra_light, FontWeight.ExtraLight, FontStyle.Normal),
    Font(R.font.inter_bold, FontWeight.Bold, FontStyle.Normal),
    Font(R.font.inter_extra_bold, FontWeight.ExtraBold, FontStyle.Normal),
    Font(R.font.inter_black, FontWeight.Black, FontStyle.Normal),
    Font(R.font.inter_semi_bold, FontWeight.SemiBold, FontStyle.Normal),
)

val typography = Typography(
    defaultFontFamily = inter,
    h1 = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 25.sp
    ),
    h5 = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp
    ),
    button = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 17.sp
    ),
    subtitle1 = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp
    ),
)