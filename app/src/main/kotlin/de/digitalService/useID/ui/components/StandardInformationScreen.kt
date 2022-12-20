package de.digitalService.useID.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.navigation.EmptyDestinationsNavigator
import de.digitalService.useID.R
import de.digitalService.useID.ui.theme.UseIdTheme
import de.digitalService.useID.util.markDownResource
import dev.jeziellago.compose.markdowntext.MarkdownText

@Composable
fun StandardInformationScreen(
    navigator: DestinationsNavigator,
    markdown: String
) {
    ScreenWithTopBar(
        navigationButton = NavigationButton(icon = NavigationIcon.Back, onClick = { navigator.navigateUp() })
    ) { topPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = UseIdTheme.spaces.s)
                .padding(top = topPadding)
                .verticalScroll(rememberScrollState())
        ) {
            MarkdownText(
                markdown = markdown,
                fontResource = R.font.bundes_sans_dtp_regular,
                fontSize = UseIdTheme.typography.bodyLRegular.fontSize,
                modifier = Modifier.padding(bottom = UseIdTheme.spaces.m)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun Preview() {
    UseIdTheme {
        StandardInformationScreen(
            navigator = EmptyDestinationsNavigator,
            markdown = markDownResource(id = R.string.accessibility_text)
        )
    }
}
