package de.digitalService.useID.ui.composables.screens.information

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ramcosta.composedestinations.annotation.Destination
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
        StyledSiteTitle(text = "Nutzungsbedingungen")

        StyledTextBlock(text = "Stand: DD.MM.JJJJ")

        StyledMediumTitle(text = "Lorem ipsum")
        StyledTextBlock(text = "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et iusto duo dolores e+ rum")

        StyledMediumTitle(text = "Dolor sit amet")
        StyledTextBlock(text = "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et iusto duo dolores e+ rum")
    }
}

@Composable
private fun StyledSiteTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge
    )

    Spacer(modifier = Modifier.height(32.dp))
}

@Composable
private fun StyledMediumTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontSize = 24.sp
    )

    Spacer(modifier = Modifier.height(16.dp))
}

@Composable
private fun StyledSmallTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontSize = 19.sp
    )
}

@Composable
private fun StyledTextBlock(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyLarge,
        fontSize = 17.sp
    )

    Spacer(modifier = Modifier.height(16.dp))
}

@Preview(showBackground = true)
@Composable
private fun Preview() {
    UseIDTheme {
        PrivacyScreen()
    }
}
