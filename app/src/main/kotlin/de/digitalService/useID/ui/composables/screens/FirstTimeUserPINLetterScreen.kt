package de.digitalService.useID.ui.composables.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import de.digitalService.useID.R
import de.digitalService.useID.ui.theme.UseIDTheme

@Composable
fun FirstTimeUserPINLetterScreen() {
    OnboardingScreen(
        title = "Haben Sie noch Ihren PIN-Brief?",
        body = "Der PIN-Brief wurde Ihnen nach der Beantragung des Ausweises zugesandt.",
        imageID = R.drawable.pin_brief,
        imageScaling = ContentScale.FillWidth,
        primaryButtonAction = { },
        primaryButtonLabel = "Ja, PIN-Brief vorhanden",
        secondaryButtonAction = { },
        secondaryButtonLabel = "Nein, neuen PIN-Brief bestellen"
    )
}

@Composable
@Preview
fun PreviewFirstTimeUserPINLetterScreen() {
    UseIDTheme {
        FirstTimeUserPINLetterScreen()
    }
}