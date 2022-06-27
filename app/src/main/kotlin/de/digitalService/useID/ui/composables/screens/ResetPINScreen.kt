package de.digitalService.useID.ui.composables.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import com.ramcosta.composedestinations.annotation.Destination
import de.digitalService.useID.R
import de.digitalService.useID.ui.theme.UseIDTheme

@Destination
@Composable
fun SetupResetPersonalPIN() {
    StandardScreen(
        title = "Neuen PIN-Brief bestellen",
        body = "Wir arbeiten noch an dieser Funktion.\n\nWeitere Informationen über den PIN-Rücksetzbrief finden Sie unter www.pin-ruecksetzbrief-bestellen.de",
        imageID = R.drawable.pin_brief,
        imageScaling = ContentScale.FillWidth
    )
}

@Composable
@Preview
fun PINReSetupPersonalPIN() {
    UseIDTheme {
        SetupResetPersonalPIN()
    }
}
