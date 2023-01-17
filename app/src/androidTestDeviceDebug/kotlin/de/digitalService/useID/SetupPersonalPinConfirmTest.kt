package de.digitalService.useID

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import de.digitalService.useID.ui.screens.setup.SetupPersonalPinConfirm
import de.digitalService.useID.ui.screens.setup.SetupPersonalPinConfirmViewModel
import de.digitalService.useID.ui.screens.setup.SetupPersonalPinConfirmViewModelInterface
import de.digitalService.useID.util.setContentUsingUseIdTheme
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class SetupPersonalPinConfirmTest {

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    private val pinEntryFieldTestTag = "PINEntryField"

    @Test
    fun correctPinEntryShown() {
        val mockViewModel: SetupPersonalPinConfirmViewModelInterface = mockk(relaxed = true)
        every { mockViewModel.shouldShowError } returns false

        composeTestRule.activity.setContentUsingUseIdTheme {
            SetupPersonalPinConfirm(viewModel = mockViewModel)
        }

        val personalPinConfirmationTitleText = composeTestRule.activity.getString(R.string.firstTimeUser_personalPIN_confirmation_title)
        composeTestRule.onNodeWithText(personalPinConfirmationTitleText).assertIsDisplayed()

        val pinEntryTestTag = "Obfuscation"
        composeTestRule.onAllNodesWithTag(pinEntryTestTag).assertCountEquals(0)

        composeTestRule.onNodeWithTag(pinEntryFieldTestTag).performTextInput("1")
        composeTestRule.onAllNodesWithTag(pinEntryTestTag).assertCountEquals(1)

        composeTestRule.onNodeWithTag(pinEntryFieldTestTag).performTextInput("2")
        composeTestRule.onAllNodesWithTag(pinEntryTestTag).assertCountEquals(2)

        composeTestRule.onNodeWithTag(pinEntryFieldTestTag).performTextInput("3")
        composeTestRule.onAllNodesWithTag(pinEntryTestTag).assertCountEquals(3)

        composeTestRule.onNodeWithTag(pinEntryFieldTestTag).performTextInput("4")
        composeTestRule.onAllNodesWithTag(pinEntryTestTag).assertCountEquals(4)

        composeTestRule.onNodeWithTag(pinEntryFieldTestTag).performTextInput("5")
        composeTestRule.onAllNodesWithTag(pinEntryTestTag).assertCountEquals(5)

        composeTestRule.onNodeWithTag(pinEntryFieldTestTag).performTextInput("6")
        composeTestRule.onAllNodesWithTag(pinEntryTestTag).assertCountEquals(6)

        composeTestRule.onNodeWithTag(pinEntryFieldTestTag).performTextInput("7")
        composeTestRule.onAllNodesWithTag(pinEntryTestTag).assertCountEquals(6)
    }

    @Test
    fun errorDialogShown() {
        val mockViewModel: SetupPersonalPinConfirmViewModel = mockk(relaxed = true)

        every { mockViewModel.shouldShowError } returns true

        composeTestRule.activity.setContentUsingUseIdTheme {
            SetupPersonalPinConfirm(viewModel = mockViewModel)
        }

        val errorDialogTitleText = composeTestRule.activity.getString(R.string.firstTimeUser_personalPIN_error_mismatch_title)
        composeTestRule.onNodeWithText(errorDialogTitleText).assertIsDisplayed()

        val errorDialogButtonText = composeTestRule.activity.getString(R.string.identification_fetchMetadataError_retry)
        composeTestRule.onNodeWithText(errorDialogButtonText).performClick()

        verify(exactly = 1) { mockViewModel.onErrorDialogButtonClicked() }
    }
}
