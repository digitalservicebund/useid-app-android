package de.digitalService.useID.ui.composables.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import de.digitalService.useID.R
import de.digitalService.useID.ui.theme.UseIDTheme

@Composable
fun FirstTimeUserCheckScreen(firstTimeUserHandler: () -> Unit, experiencedUserHandler: () -> Unit) {
    OnboardingScreen(
        title = "Haben Sie Ihren Online-Ausweis bereits benutzt?",
        body = "Folgende Dokumente bieten die Funktion an:\nDeutscher Personalausweis, Elektronischer Aufenthaltstitel, eID-Karte für Unionsbürger",
        imageID = R.drawable.eids,
        imageScaling = ContentScale.Inside,
        primaryButtonAction = experiencedUserHandler,
        primaryButtonLabel = "Ja, ich habe es bereits genutzt",
        secondaryButtonAction = firstTimeUserHandler,
        secondaryButtonLabel = "Nein, jetzt Online-Ausweis einrichten"
    )
}

@Composable
@Preview
fun PreviewFirstTimeUserCheckScreen() {
    UseIDTheme {
        FirstTimeUserCheckScreen(firstTimeUserHandler = { }, experiencedUserHandler = { })
    }
}