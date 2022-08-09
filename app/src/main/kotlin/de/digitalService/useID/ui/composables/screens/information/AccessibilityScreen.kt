package de.digitalService.useID.ui.composables.screens.information

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ramcosta.composedestinations.annotation.Destination
import de.digitalService.useID.R
import de.digitalService.useID.ui.composables.BodyText
import de.digitalService.useID.ui.composables.LargeTitleText
import de.digitalService.useID.ui.composables.ScreenTitleText
import de.digitalService.useID.ui.theme.UseIDTheme
import dev.jeziellago.compose.markdowntext.MarkdownText

@Destination
@Composable
fun AccessibilityScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        ScreenTitleText(text = stringResource(R.string.accessibility_screen_title))

        BodyText(text = stringResource(R.string.accessibility_updated_date))
        BodyText(text = stringResource(R.string.accessibility_intro_text))

        LargeTitleText(text = stringResource(R.string.accessibility_question_howAccessible_title))
        BodyText(text = stringResource(R.string.accessibility_question_howAccessible_body1))
        BodyText(text = stringResource(R.string.accessibility_question_howAccessible_body2))

        LargeTitleText(text = stringResource(R.string.accessibility_question_whatIsNotAccessible_title))
        BodyText(text = stringResource(R.string.accessibility_question_whatIsNotAccessible_body))

        LargeTitleText(text = stringResource(R.string.accessibility_feedbackPossibilities_title))
        MarkdownText(
            markdown = stringResource(id = R.string.accessibility_feedbackPossibilities_body),
            style = MaterialTheme.typography.bodyLarge,
            fontSize = 17.sp,

            modifier = Modifier.padding(bottom = 16.dp)
        )

        LargeTitleText(text = stringResource(R.string.accessibility_arbitrationProcedure_title))
        BodyText(text = stringResource(R.string.accessibility_arbitrationProcedure_body1))
        BodyText(text = stringResource(R.string.accessibility_arbitrationProcedure_contact_title))
        BodyText(text = stringResource(R.string.accessibility_arbitrationProcedure_contact_info))
    }
}

@Preview(showBackground = true)
@Composable
private fun Preview() {
    UseIDTheme {
        AccessibilityScreen()
    }
}
