package de.digitalService.useID.ui.screens.setup

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.navigation.EmptyDestinationsNavigator
import de.digitalService.useID.R
import de.digitalService.useID.ui.components.NavigationButton
import de.digitalService.useID.ui.components.NavigationIcon
import de.digitalService.useID.ui.components.ScreenWithTopBar
import de.digitalService.useID.ui.components.StandardStaticComposition
import de.digitalService.useID.ui.theme.UseIDTheme

@Destination
@Composable
fun SetupResetPersonalPin(
    navigator: DestinationsNavigator
) {
    ScreenWithTopBar(
        navigationButton = NavigationButton(icon = NavigationIcon.Back, onClick = { navigator.navigateUp() })
    ) { topPadding ->
        StandardStaticComposition(
            title = stringResource(R.string.firstTimeUser_missingPINLetter_title),
            body = stringResource(R.string.firstTimeUser_missingPINLetter_body),
            imageID = R.drawable.ic_illustration_pin_letter,
            imageScaling = ContentScale.FillWidth,
            modifier = Modifier.padding(top = topPadding)
        )
    }
}

@Composable
@Preview
fun PinReSetupPersonalPin() {
    UseIDTheme {
        SetupResetPersonalPin(EmptyDestinationsNavigator)
    }
}
