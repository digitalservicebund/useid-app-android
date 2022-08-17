package de.digitalService.useID.ui.screens.information

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ramcosta.composedestinations.annotation.Destination
import de.digitalService.useID.R
import de.digitalService.useID.ui.components.BodyText
import de.digitalService.useID.ui.components.LargeTitleText
import de.digitalService.useID.ui.components.ScreenTitleText
import de.digitalService.useID.ui.theme.UseIDTheme

@Destination
@Composable
fun PrivacyScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        ScreenTitleText(text = stringResource(R.string.privacy_screen_title))

        BodyText(text = stringResource(R.string.privacy_updated_date))

        LargeTitleText(text = "Lorem ipsum")
        BodyText(text = "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et iusto duo dolores e+ rum")

        LargeTitleText(text = "Dolor sit amet")
        BodyText(text = "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et iusto duo dolores e+ rum")
    }
}

@Preview(showBackground = true)
@Composable
private fun Preview() {
    UseIDTheme {
        PrivacyScreen()
    }
}
