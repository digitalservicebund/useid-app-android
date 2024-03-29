package de.digitalService.useID.ui.screens.setup

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import de.digitalService.useID.R
import de.digitalService.useID.ui.components.NavigationButton
import de.digitalService.useID.ui.components.NavigationIcon
import de.digitalService.useID.ui.components.ScreenWithTopBar
import de.digitalService.useID.ui.components.StandardStaticComposition
import de.digitalService.useID.ui.theme.UseIdTheme

@Composable
fun ResetPersonalPin(onBack: () -> Unit) {
    ScreenWithTopBar(
        navigationButton = NavigationButton(icon = NavigationIcon.Back, onClick = onBack, confirmation = null)
    ) { topPadding ->
        StandardStaticComposition(
            title = stringResource(R.string.firstTimeUser_missingPINLetter_title),
            body = stringResource(R.string.firstTimeUser_missingPINLetter_body),
            imageId = R.drawable.ic_illustration_pin_letter,
            imageScaling = ContentScale.FillWidth,
            modifier = Modifier.padding(top = topPadding)
        )
    }
}

@Composable
@Preview
fun PinReSetupPersonalPin() {
    UseIdTheme {
        ResetPersonalPin {}
    }
}
