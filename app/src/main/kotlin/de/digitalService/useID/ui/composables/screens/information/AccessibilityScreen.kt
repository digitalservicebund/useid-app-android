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
fun AccessibilityScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        StyledSiteTitle(text = "Erklärung zur Barrierefreiheit")

        StyledTextBlock(text = "Stand: DD.MM.JJJJ")
        StyledTextBlock(text = "Die Applikation \"AppName\" ist bemüht, im Einklang mit den nationalen Rechtsvorschriften zur Umsetzung der Richtlinie (EU) 2016/2102 des Europäischen Parlaments und des Rates barrierefrei zugänglich zu machen. Diese Erklärung zur Barrierefreiheit gilt für die aktuell im Apple App Store beziehungsweise Google Playstore erreichbare Version xxx. Sie wurde am xx.xx.2022 erstellt.")

        StyledMediumTitle(text = "Wie barrierefrei ist das Angebot?")
        StyledTextBlock(text = "Diese Applikation wurde neu entwickelt. Das Team hat ein Accessibility Training absolviert, indem die meisten Bereiche der Applikation auf Barrierefreiheit überprüft wurden. Die Applikation ist größtenteils barrierefrei. An den unten aufgeführten Mängeln wird gearbeitet.")
        StyledTextBlock(text = "Die Applikation \"AppName\" wird außerdem in den kommenden Wochen kontinuierlich auf Basis der Testergebnisse und der Rückmeldung der Nutzer:innen weiterentwickelt")

        StyledMediumTitle(text = "Welche Bereiche sind nicht barrierefrei?")
        StyledTextBlock(text = "• Text Vergrößerung möglich?\n• Tastatur navigierbar?\n• Bildschirmlesegerät/Screenreader bedienbar?")

        StyledMediumTitle(text = "Kontakt und Feedback-Möglichkeit")
        StyledTextBlock(text = "Sind Ihnen Mängel beim barrierefreien Zugang zu Inhalten von aufgefallen? Dann können Sie sich gerne bei uns melden: hilfe@inter.net")

        StyledMediumTitle(text = "Schlichtungsverfahren")
        StyledTextBlock(text = "Beim Beauftragten der Bundesregierung für die Belange von Menschen mit Behinderungen gibtes eine Schlichtungsstelle gemäß \$ 16 BGG. Die Schlichtungsstelle hat die Aufgabe, Konfliktezwischen Menschen mit Behinderungen und öffentlichen Stellen des Bundes zu lösen. Sie können die Schlichtungsstelle einschalten, wenn Sie mit den Antworten aus der oben genannten Kontaktmöglichkeit nicht zufrieden sind. Dabei geht es nicht darum, Gewinner oder Verlierer zu finden. Vielmehr ist es das Ziel, mit Hilfe der Schlichtungsstelle gemeinsam und außergerichtlich eine Lösung für ein Problem zufinden. Das Schlichtungsverfahren ist kostenlos. Sie brauchen auch keinen Rechtsbeistand. Auf der Internetseite der Schlichtungsstelle finden Sie alle Informationen zum Schlichtungsverfahren. Dort können Sie nachlesen, wie ein Schlichtungsverfahren abläuft und wie Sie den Antrag auf Schlichtung stellen. Sie können den Antrag auch in Leichter Spracheoder in Deutscher Gebärdensprache stellen.")
        StyledTextBlock(text = "Sie erreichen die Schlichtungsstelle unter folgender Adresse:")
        StyledTextBlock(text = "Schlichtungsstelle nach dem Behindertengleichstellungsgesetz bei dem Beauftragten der Bundesregierung für die Belange von Menschen mit Behinderungen\nMauerstraße53\n10117 Berlin\nTelefon: 030 18 527 2805\nE-Mail: info@schlichtungsstelle-bgg.de\nInternet: www.schlichtungsstelle-bgg.de")
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
        AccessibilityScreen()
    }
}
