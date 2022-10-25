package de.digitalService.useID.ui.screens.information

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import de.digitalService.useID.ui.theme.UseIDTheme
import de.digitalService.useID.util.markDownResource
import dev.jeziellago.compose.markdowntext.MarkdownText

@Destination
@Composable
fun ImprintScreen(
    navigator: DestinationsNavigator
) {
    ScreenWithTopBar(
        navigationButton = NavigationButton(icon = NavigationIcon.Back, onClick = { navigator.navigateUp() })
    ) { topPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .padding(top = topPadding)
                .verticalScroll(rememberScrollState())
        ) {
            MarkdownText(
                markdown = markDownResource(id = R.string.imprint_text),
                fontResource = R.font.bundes_sans_dtp_regular,
                modifier = Modifier.padding(bottom = 24.dp)
            )
        }
    }
}

@Preview(showBackground = true, heightDp = 9000)
@Composable
private fun Preview() {
    UseIDTheme {
        ImprintScreen(EmptyDestinationsNavigator)
    }
}
