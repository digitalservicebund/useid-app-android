package de.digitalService.useID.ui.screens.information

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ramcosta.composedestinations.annotation.Destination
import de.digitalService.useID.R
import de.digitalService.useID.ui.composables.BodyText
import de.digitalService.useID.ui.composables.LargeTitleText
import de.digitalService.useID.ui.composables.ScreenTitleText
import de.digitalService.useID.ui.composables.SmallTitleText
import de.digitalService.useID.ui.theme.UseIDTheme
import dev.jeziellago.compose.markdowntext.MarkdownText

@Destination
@Composable
fun ImprintScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        ScreenTitleText(text = stringResource(R.string.imprint_screen_title))

        LargeTitleText(text = stringResource(R.string.imprint_duty_info))

        BodyText(text = stringResource(R.string.imprint_address_body))

        SmallTitleText(text = stringResource(R.string.imprint_managementRepresentation_title))
        BodyText(text = stringResource(R.string.imprint_managementRepresentation_body))

        SmallTitleText(text = stringResource(R.string.imprint_soloShareholder_title))
        BodyText(text = stringResource(R.string.imprint_soloShareholder_body))

        SmallTitleText(text = stringResource(R.string.imprint_companyRegistrationNumber_title))
        BodyText(text = stringResource(R.string.imprint_companyRegistrationNumber_body))

        SmallTitleText(text = stringResource(R.string.imprint_registryCourt_title))
        BodyText(text = stringResource(R.string.imprint_registryCourt_body))

        SmallTitleText(text = stringResource(R.string.imprint_vATNumber_title))
        BodyText(text = stringResource(R.string.imprint_vATNumber_body))

        LargeTitleText(text = stringResource(R.string.imprint_contact_title))

        SmallTitleText(text = stringResource(R.string.imprint_mail_title))
        MarkdownText(
            markdown = stringResource(id = R.string.imprint_email_body),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LargeTitleText(text = "Lorem ipsum")
        BodyText(text = "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et iusto duo dolores e+ rum")
    }
}

@Preview(showBackground = true)
@Composable
private fun Preview() {
    UseIDTheme {
        ImprintScreen()
    }
}
