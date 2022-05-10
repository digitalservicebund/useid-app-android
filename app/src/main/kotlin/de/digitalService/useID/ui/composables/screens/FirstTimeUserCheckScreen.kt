package de.digitalService.useID.ui.composables.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import de.digitalService.useID.R
import de.digitalService.useID.ui.theme.UseIDTheme

@Composable
fun FirstTimeUserCheckScreen(firstTimeUserHandler: () -> Unit, experiencedUserHandler: () -> Unit) {
    OnboardingScreen(
        title = stringResource(id = R.string.firstTimeUser_intro_title),
        body = stringResource(id = R.string.firstTimeUser_intro_body),
        imageID = R.drawable.eids,
        imageScaling = ContentScale.Inside,
        primaryButton = BundButtonConfig(title = stringResource(id = R.string.firstTimeUser_intro_no), action = firstTimeUserHandler),
        secondaryButton = BundButtonConfig(title = stringResource(id = R.string.firstTimeUser_intro_yes), action = experiencedUserHandler)
    )
}

@Composable
@Preview
fun PreviewFirstTimeUserCheckScreen() {
    UseIDTheme {
        FirstTimeUserCheckScreen(firstTimeUserHandler = { }, experiencedUserHandler = { })
    }
}