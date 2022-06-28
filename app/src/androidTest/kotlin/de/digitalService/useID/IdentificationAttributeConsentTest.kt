package de.digitalService.useID

import androidx.activity.compose.setContent
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import de.digitalService.useID.ui.composables.screens.identification.IdentificationAttributeConsent
import de.digitalService.useID.ui.composables.screens.identification.IdentificationAttributeConsentViewModel
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class IdentificationAttributeConsentTest {

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @Test
    fun correctUsage() {
        val viewModel: IdentificationAttributeConsentViewModel = mockk(relaxUnitFun = true)
        val testIdentificationProviderString = "testIdentificationProviderString"
        val testRequiredReadAttributes = listOf(
            R.string.idCardAttribute_DG01,
            R.string.idCardAttribute_DG02,
            R.string.idCardAttribute_DG03,
            R.string.idCardAttribute_DG04,
            R.string.idCardAttribute_DG05,
        )

        every { viewModel.identificationProvider } returns testIdentificationProviderString
        every { viewModel.requiredReadAttributes } returns testRequiredReadAttributes

        composeTestRule.activity.setContent {
            IdentificationAttributeConsent(viewModel = viewModel)
        }

        testRequiredReadAttributes.forEach { testId ->
            val attributeText = composeTestRule.activity.getString(testId)
            composeTestRule.onNodeWithText(attributeText, substring = true).assertIsDisplayed()
        }

        val buttonText = composeTestRule.activity.getString(R.string.identification_attributeConsent_pinButton)
        composeTestRule.onNodeWithText(buttonText).performClick()

        verify(exactly = 1) { viewModel.onPINButtonTapped() }
    }
}
