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
fun ImpressumScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        StyledSiteTitle(text = "Impressum")

        StyledMediumTitle(text = "Angabe gemäß §5 TMG")

        StyledTextBlock(text = "Digital Service GmbH des Bundes\nPrinzessinenstraße 8-14\n10969 Belrin\nDeutschland")

        StyledSmallTitle(text = "Vertretung durch die Geschäftsführung:")
        StyledTextBlock(text = "Frau Christina Lang, Herr Philipp Moser")

        StyledSmallTitle(text = "Alleingesellschafterin:")
        StyledTextBlock(text = "Bundesrepublik Deutschland, vertreten durch das Bundeskanzleramt")

        StyledSmallTitle(text = "Handelsregister-Nummer:")
        StyledTextBlock(text = "HRB 212879 B")

        StyledSmallTitle(text = "Registergericht:")
        StyledTextBlock(text = "Berlin Charlottenburg")

        StyledSmallTitle(text = "Umsatzsteueridentifikationsnummer:")
        StyledTextBlock(text = "DE327075535")

        StyledMediumTitle(text = "Kontakt")

        StyledSmallTitle(text = "E-Mail:")
        StyledTextBlock(text = "adfldakflkdfkla@inter.net")

        StyledMediumTitle(text = "Lorem ipsum")
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
        ImpressumScreen()
    }
}
