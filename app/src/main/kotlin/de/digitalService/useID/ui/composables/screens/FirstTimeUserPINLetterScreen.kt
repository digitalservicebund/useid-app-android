package de.digitalService.useID.ui.composables.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import de.digitalService.useID.R
import de.digitalService.useID.ui.theme.UseIDTheme

@Composable
fun FirstTimeUserPINLetterScreen(transportPINAvailableHandler: () -> Unit, noPINAvailable: () -> Unit) {
    OnboardingScreen(
        title = "Haben Sie noch Ihren PIN-Brief?",
        body = "Der PIN-Brief wurde Ihnen nach der Beantragung des Ausweises zugesandt.",
        imageID = R.drawable.pin_brief,
        imageScaling = ContentScale.FillWidth,
        primaryButton = BundButtonConfig(title = "Nein, neuen PIN-Brief bestellen", action = noPINAvailable),
        secondaryButton = BundButtonConfig(title = "Ja, PIN-Brief vorhanden", action = transportPINAvailableHandler)
    )
}

@Composable
@Preview
fun PreviewFirstTimeUserPINLetterScreen() {
    UseIDTheme {
        FirstTimeUserPINLetterScreen({ }, { })
    }
}