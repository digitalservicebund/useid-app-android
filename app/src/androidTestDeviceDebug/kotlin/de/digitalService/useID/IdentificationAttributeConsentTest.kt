package de.digitalService.useID

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import de.digitalService.useID.ui.components.NavigationIcon
import de.digitalService.useID.ui.screens.identification.IdentificationAttributeConsent
import de.digitalService.useID.ui.screens.identification.IdentificationAttributeConsentViewModelInterface
import de.digitalService.useID.ui.screens.identification.ProviderInfoDialogContent
import de.digitalService.useID.util.setContentUsingUseIdTheme
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class IdentificationAttributeConsentTest {

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun correctUsage() {
        val viewModel: IdentificationAttributeConsentViewModelInterface = mockk(relaxUnitFun = true)
        val testIdentificationProviderString = "testIdentificationProviderString"
        val testRequiredReadAttributes = listOf(
            R.string.cardAttribute_dg01,
            R.string.cardAttribute_dg02,
            R.string.cardAttribute_dg03,
            R.string.cardAttribute_dg04,
            R.string.cardAttribute_dg05
        )

        every { viewModel.identificationProvider } returns testIdentificationProviderString
        every { viewModel.requiredReadAttributes } returns testRequiredReadAttributes
        every { viewModel.shouldShowInfoDialog } returns false
        every { viewModel.backAllowed } returns true

        composeTestRule.activity.setContentUsingUseIdTheme {
            IdentificationAttributeConsent(viewModel = viewModel)
        }

        testRequiredReadAttributes.forEach { testId ->
            val attributeText = composeTestRule.activity.getString(testId)
            composeTestRule.onNodeWithText(attributeText, substring = true).assertIsDisplayed()
        }

        val buttonText = composeTestRule.activity.getString(R.string.identification_attributeConsent_continue)
        composeTestRule.onNodeWithText(buttonText).performClick()

        verify(exactly = 1) { viewModel.onPinButtonClicked() }
    }

    @Test
    fun dialogTest() {
        val viewModel: IdentificationAttributeConsentViewModelInterface = mockk(relaxUnitFun = true)
        val testIdentificationProviderString = "testIdentificationProviderString"
        val testRequiredReadAttributes = listOf(
            R.string.cardAttribute_dg01,
            R.string.cardAttribute_dg02,
            R.string.cardAttribute_dg03,
            R.string.cardAttribute_dg04,
            R.string.cardAttribute_dg05
        )

        val testIssue = "ISSUE"
        val testIssueUrl = "ISSUE_URL"
        val testSubject = "SUBJECT"
        val testSubjectUrl = "SUBJECT_URL"
        val testTerms = "TERMS"

        every { viewModel.identificationProvider } returns testIdentificationProviderString
        every { viewModel.requiredReadAttributes } returns testRequiredReadAttributes

        every { viewModel.shouldShowInfoDialog } returns true
        every { viewModel.infoDialogContent } returns ProviderInfoDialogContent(
            testIssue,
            testIssueUrl,
            testSubject,
            testSubjectUrl,
            testTerms
        )
        every { viewModel.backAllowed } returns false

        composeTestRule.activity.setContentUsingUseIdTheme {
            IdentificationAttributeConsent(viewModel = viewModel)
        }

        testRequiredReadAttributes.forEach { testId ->
            val attributeText = composeTestRule.activity.getString(testId)
            composeTestRule.onNodeWithText(attributeText, substring = true).assertIsDisplayed()
        }

        composeTestRule.onNodeWithText(testIssue).assertIsDisplayed()
        composeTestRule.onNodeWithText(testIssueUrl).assertIsDisplayed()
        composeTestRule.onAllNodesWithText(testSubject).assertCountEquals(2)
        composeTestRule.onNodeWithText(testSubjectUrl).assertIsDisplayed()
        composeTestRule.onNodeWithText(testTerms).assertIsDisplayed()

        val cancelButtonTag = "infoDialogCancel"
        composeTestRule.onNodeWithTag(cancelButtonTag).performClick()

        verify(exactly = 1) { viewModel.onInfoDialogDismissalRequest() }
    }

    @Test
    fun hasCancelButton() {
        val viewModel: IdentificationAttributeConsentViewModelInterface = mockk(relaxUnitFun = true)
        val testIdentificationProviderString = "testIdentificationProviderString"
        val testRequiredReadAttributes = listOf(
            R.string.cardAttribute_dg01,
            R.string.cardAttribute_dg02,
            R.string.cardAttribute_dg03,
            R.string.cardAttribute_dg04,
            R.string.cardAttribute_dg05
        )

        val testIssue = "ISSUE"
        val testIssueUrl = "ISSUE_URL"
        val testSubject = "SUBJECT"
        val testSubjectUrl = "SUBJECT_URL"
        val testTerms = "TERMS"

        every { viewModel.identificationProvider } returns testIdentificationProviderString
        every { viewModel.requiredReadAttributes } returns testRequiredReadAttributes

        every { viewModel.shouldShowInfoDialog } returns false
        every { viewModel.infoDialogContent } returns ProviderInfoDialogContent(
            testIssue,
            testIssueUrl,
            testSubject,
            testSubjectUrl,
            testTerms
        )
        every { viewModel.backAllowed } returns false

        composeTestRule.activity.setContentUsingUseIdTheme {
            IdentificationAttributeConsent(viewModel = viewModel)
        }

        val navigationButtonTag = NavigationIcon.Cancel.name
        composeTestRule.onNodeWithTag(navigationButtonTag).assertIsDisplayed()
    }

    @Test
    fun hasBacksButton() {
        val viewModel: IdentificationAttributeConsentViewModelInterface = mockk(relaxUnitFun = true)
        val testIdentificationProviderString = "testIdentificationProviderString"
        val testRequiredReadAttributes = listOf(
            R.string.cardAttribute_dg01,
            R.string.cardAttribute_dg02,
            R.string.cardAttribute_dg03,
            R.string.cardAttribute_dg04,
            R.string.cardAttribute_dg05
        )

        val testIssue = "ISSUE"
        val testIssueUrl = "ISSUE_URL"
        val testSubject = "SUBJECT"
        val testSubjectUrl = "SUBJECT_URL"
        val testTerms = "TERMS"

        every { viewModel.identificationProvider } returns testIdentificationProviderString
        every { viewModel.requiredReadAttributes } returns testRequiredReadAttributes

        every { viewModel.shouldShowInfoDialog } returns false
        every { viewModel.infoDialogContent } returns ProviderInfoDialogContent(
            testIssue,
            testIssueUrl,
            testSubject,
            testSubjectUrl,
            testTerms
        )
        every { viewModel.backAllowed } returns false

        composeTestRule.activity.setContentUsingUseIdTheme {
            IdentificationAttributeConsent(viewModel = viewModel)
        }

        val navigationButtonTag = NavigationIcon.Cancel.name
        composeTestRule.onNodeWithTag(navigationButtonTag).assertIsDisplayed()
    }
}
