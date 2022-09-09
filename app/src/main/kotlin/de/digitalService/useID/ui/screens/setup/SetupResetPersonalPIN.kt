package de.digitalService.useID.ui.screens.setup

import androidx.compose.runtime.Composable
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.ramcosta.composedestinations.annotation.Destination
import de.digitalService.useID.R
import de.digitalService.useID.ui.components.StandardStaticComposition
import de.digitalService.useID.ui.theme.UseIDTheme

@Destination
@Composable
fun SetupResetPersonalPIN() {
    StandardStaticComposition(
        title = stringResource(R.string.firstTimeUser_missingPINLetter_title),
        body = stringResource(R.string.firstTimeUser_missingPINLetter_body),
        imageID = R.drawable.ic_illustration_pin_letter,
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
