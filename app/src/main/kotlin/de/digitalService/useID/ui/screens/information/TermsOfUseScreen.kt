package de.digitalService.useID.ui.screens.information

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.navigation.EmptyDestinationsNavigator
import de.digitalService.useID.R
import de.digitalService.useID.ui.components.NavigationButton
import de.digitalService.useID.ui.components.NavigationIcon
import de.digitalService.useID.ui.components.ScreenWithTopBar
import de.digitalService.useID.ui.components.StandardInformationScreen
import de.digitalService.useID.ui.theme.UseIdTheme
import de.digitalService.useID.util.markDownResource
import dev.jeziellago.compose.markdowntext.MarkdownText

@Destination
@Composable
fun TermsOfUseScreen(
    navigator: DestinationsNavigator
) {
    StandardInformationScreen(
        navigator = navigator,
        markdown = markDownResource(id = R.string.termsOfUse_text)
    )
}

@Preview(showBackground = true)
@Composable
private fun Preview() {
    UseIdTheme {
        TermsOfUseScreen(EmptyDestinationsNavigator)
    }
}
