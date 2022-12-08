package de.digitalService.useID.ui.screens.information

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.navigation.EmptyDestinationsNavigator
import de.digitalService.useID.R
import de.digitalService.useID.ui.components.StandardInformationScreen
import de.digitalService.useID.ui.theme.UseIdTheme
import de.digitalService.useID.util.markDownResource

@Destination
@Composable
fun PrivacyScreen(
    navigator: DestinationsNavigator
) {
    StandardInformationScreen(
        navigator = navigator,
        markdown = markDownResource(id = R.string.privacy_text)
    )
}

@Preview(showBackground = true)
@Composable
private fun Preview() {
    UseIdTheme {
        PrivacyScreen(EmptyDestinationsNavigator)
    }
}
