package de.digitalService.useID

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import de.digitalService.useID.ui.components.NavigationIcon
import de.digitalService.useID.ui.screens.setup.SetupIntro
import de.digitalService.useID.ui.screens.setup.SetupIntroViewModelInterface
import de.digitalService.useID.util.setContentUsingUseIdTheme
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class SetupIntroTest {

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun shouldShowCancelDialog() {
        val viewModel: SetupIntroViewModelInterface = mockk(relaxUnitFun = true)

        every { viewModel.confirmCancellation } returns true
        every { viewModel.showVariant } returns false

        composeTestRule.activity.setContentUsingUseIdTheme {
            SetupIntro(viewModel = viewModel)
        }

        composeTestRule.onNodeWithTag(NavigationIcon.Cancel.name).performClick()
        verify(exactly = 0) { viewModel.onCancelSetup() }

        val cancelButton = composeTestRule.activity.getString(R.string.firstTimeUser_confirmEnd_confirm)
        composeTestRule.onNodeWithText(cancelButton).performClick()

        verify(exactly = 1) { viewModel.onCancelSetup() }
    }

    @Test
    fun doNotShowCancelDialog() {
        val viewModel: SetupIntroViewModelInterface = mockk(relaxUnitFun = true)

        every { viewModel.confirmCancellation } returns false
        every { viewModel.showVariant } returns false

        composeTestRule.activity.setContentUsingUseIdTheme {
            SetupIntro(viewModel = viewModel)
        }

        composeTestRule.onNodeWithTag(NavigationIcon.Cancel.name).performClick()
        verify(exactly = 1) { viewModel.onCancelSetup() }

        val confirmButton1 = composeTestRule.activity.getString(R.string.identification_confirmEnd_confirm)
        composeTestRule.onNodeWithText(confirmButton1).assertDoesNotExist()

        val confirmButton2 = composeTestRule.activity.getString(R.string.firstTimeUser_confirmEnd_confirm)
        composeTestRule.onNodeWithText(confirmButton2).assertDoesNotExist()
    }

    @Test
    fun primaryButton() {
        val viewModel: SetupIntroViewModelInterface = mockk(relaxUnitFun = true)

        every { viewModel.confirmCancellation } returns false
        every { viewModel.showVariant } returns false

        composeTestRule.activity.setContentUsingUseIdTheme {
            SetupIntro(viewModel = viewModel)
        }

        val primaryButton = composeTestRule.activity.getString(R.string.firstTimeUser_intro_startSetup)
        composeTestRule.onNodeWithText(primaryButton).performClick()

        verify { viewModel.onFirstTimeUsage() }
    }

    @Test
    fun secondaryButton() {
        val viewModel: SetupIntroViewModelInterface = mockk(relaxUnitFun = true)

        every { viewModel.confirmCancellation } returns false
        every { viewModel.showVariant } returns false

        composeTestRule.activity.setContentUsingUseIdTheme {
            SetupIntro(viewModel = viewModel)
        }

        val secondaryButton = composeTestRule.activity.getString(R.string.firstTimeUser_intro_skipSetup)
        composeTestRule.onNodeWithText(secondaryButton).performClick()

        verify { viewModel.onNonFirstTimeUsage() }
    }

    @Test
    fun showsOriginal() {
        val viewModel: SetupIntroViewModelInterface = mockk(relaxUnitFun = true) {
            every { confirmCancellation } returns false
            every { showVariant } returns false
        }

        composeTestRule.activity.setContentUsingUseIdTheme {
            SetupIntro(viewModel = viewModel)
        }

        val title = composeTestRule.activity.getString(R.string.firstTimeUser_intro_title)

        composeTestRule.onNodeWithText(title).assertIsDisplayed()
    }

    @Test
    fun showsVariant() {
        val viewModel: SetupIntroViewModelInterface = mockk(relaxUnitFun = true) {
            every { confirmCancellation } returns false
            every { showVariant } returns true
        }

        composeTestRule.activity.setContentUsingUseIdTheme {
            SetupIntro(viewModel = viewModel)
        }

        val title = composeTestRule.activity.getString(R.string.firstTimeUser_intro_title_variant)

        composeTestRule.onNodeWithText(title).assertIsDisplayed()
    }
}
