package de.digitalService.useID.ui.composables.screens.information

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ramcosta.composedestinations.annotation.Destination
import de.digitalService.useID.R
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
        StyledSiteTitle(text = stringResource(R.string.accessibility_screen_title))

        StyledTextBlock(text = stringResource(R.string.accessibility_updated_date))
        StyledTextBlock(text = stringResource(R.string.accessibility_intro_text))

        StyledMediumTitle(text = stringResource(R.string.accessibility_question_howAccessible_title))
        StyledTextBlock(text = stringResource(R.string.accessibility_question_howAccessible_body1))
        StyledTextBlock(text = stringResource(R.string.accessibility_question_howAccessible_body2))

        StyledMediumTitle(text = stringResource(R.string.accessibility_question_whatIsNotAccessible_title))
        StyledTextBlock(text = stringResource(R.string.accessibility_question_whatIsNotAccessible_body))

        StyledMediumTitle(text = stringResource(R.string.accessibility_feedbackPossibilities_title))
        StyledTextBlock(text = stringResource(R.string.accessibility_feedbackPossibilities_body))

        StyledMediumTitle(text = stringResource(R.string.accessibility_arbitrationProcedure_title))
        StyledTextBlock(text = stringResource(R.string.accessibility_arbitrationProcedure_body1))
        StyledTextBlock(text = stringResource(R.string.accessibility_arbitrationProcedure_contact_title))
        StyledTextBlock(text = stringResource(R.string.accessibility_arbitrationProcedure_contact_info))
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
