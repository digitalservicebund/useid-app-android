package de.digitalService.useID.ui.composables.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import de.digitalService.useID.R
import de.digitalService.useID.ui.theme.UseIDTheme

@Composable
fun FirstTimeUserPINLetterScreen(transportPINAvailableHandler: () -> Unit, noPINAvailable: () -> Unit) {
    OnboardingScreen(
        title = stringResource(id = R.string.firstTimeUser_pinLetter_title),
        body = stringResource(id = R.string.firstTimeUser_pinLetter_body),
        imageID = R.drawable.pin_brief,
        imageScaling = ContentScale.FillWidth,
        primaryButton = BundButtonConfig(title = stringResource(id = R.string.firstTimeUser_pinLetter_no), action = noPINAvailable),
        secondaryButton = BundButtonConfig(title = stringResource(id = R.string.firstTimeUser_pinLetter_yes), action = transportPINAvailableHandler)
    )
}

@Composable
@Preview
fun PreviewFirstTimeUserPINLetterScreen() {
    UseIDTheme {
        FirstTimeUserPINLetterScreen({ }, { })
    }
}