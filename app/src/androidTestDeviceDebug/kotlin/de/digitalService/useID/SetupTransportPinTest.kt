package de.digitalService.useID

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import de.digitalService.useID.ui.components.NavigationIcon
import de.digitalService.useID.ui.screens.setup.SetupTransportPin
import de.digitalService.useID.ui.screens.setup.SetupTransportPinViewModelInterface
import de.digitalService.useID.util.setContentUsingUseIdTheme
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class SetupTransportPinTest {

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun testPinInputAndVisualisationNullAttempts() {

        val mockViewModel: SetupTransportPinViewModelInterface = mockk(relaxed = true)
        every { mockViewModel.retry } returns false

        composeTestRule.activity.setContentUsingUseIdTheme {
            SetupTransportPin(viewModel = mockViewModel)
        }

        val quantityAttemptsString = composeTestRule.activity.resources.getQuantityString(
            R.plurals.firstTimeUser_transportPIN_remainingAttempts,
            2,
            2
        )
        composeTestRule.onNodeWithText(quantityAttemptsString).assertDoesNotExist()

        val pinEntryTestTag = "PINDigitField"
        composeTestRule.onAllNodesWithTag(pinEntryTestTag).assertCountEquals(5)

        val pinEntryFieldTestTag = "PINEntryField"
        composeTestRule.onNodeWithTag(pinEntryFieldTestTag).performTextInput("1234")

        val obfuscationTestTag = "PINEntry"
        composeTestRule.onAllNodesWithTag(obfuscationTestTag).assertCountEquals(4)

        composeTestRule.onNodeWithTag(pinEntryFieldTestTag).performTextInput("5")
        composeTestRule.onAllNodesWithTag(obfuscationTestTag).assertCountEquals(5)

        composeTestRule.onNodeWithTag(pinEntryFieldTestTag).performImeAction()

        verify(exactly = 1) { mockViewModel.onDoneClicked("12345") }
    }

    @Test
    fun testPinInputAndVisualisationTwoAttempts() {
        val mockViewModel: SetupTransportPinViewModelInterface = mockk(relaxed = true)
        every { mockViewModel.retry } returns true

        composeTestRule.activity.setContentUsingUseIdTheme {
            SetupTransportPin(viewModel = mockViewModel)
        }

        val quantityAttemptsString = composeTestRule.activity.resources.getQuantityString(
            R.plurals.firstTimeUser_transportPIN_remainingAttempts,
            2,
            2
        )
        composeTestRule.onNodeWithText(quantityAttemptsString).assertIsDisplayed()

        val errorMessage = composeTestRule.activity.getString(R.string.firstTimeUser_incorrectTransportPIN_title)
        composeTestRule.onNodeWithText(errorMessage).assertIsDisplayed()

        val pinEntryTestTag = "PINDigitField"
        composeTestRule.onAllNodesWithTag(pinEntryTestTag).assertCountEquals(5)

        val pinEntryFieldTestTag = "PINEntryField"
        composeTestRule.onNodeWithTag(pinEntryFieldTestTag).performTextInput("1234")

        val obfuscationTestTag = "PINEntry"
        composeTestRule.onAllNodesWithTag(obfuscationTestTag).assertCountEquals(4)

        composeTestRule.onNodeWithTag(pinEntryFieldTestTag).performTextInput("5")
        composeTestRule.onAllNodesWithTag(obfuscationTestTag).assertCountEquals(5)

        composeTestRule.onNodeWithTag(pinEntryFieldTestTag).performImeAction()

        verify(exactly = 1) { mockViewModel.onDoneClicked("12345") }
    }

    @Test
    fun testOnNavigationNullAttempts() {

        val mockViewModel: SetupTransportPinViewModelInterface = mockk(relaxed = true)
        every { mockViewModel.retry } returns false

        composeTestRule.activity.setContentUsingUseIdTheme {
            SetupTransportPin(viewModel = mockViewModel)
        }

        composeTestRule.onNodeWithTag(NavigationIcon.Cancel.name).assertDoesNotExist()
        composeTestRule.onNodeWithTag(NavigationIcon.Back.name).performClick()

        verify(exactly = 1) { mockViewModel.onNavigationButtonClicked()}
    }

    @Test
    fun testOnNavigationTwoAttempts() {

        val mockViewModel: SetupTransportPinViewModelInterface = mockk(relaxed = true)
        every { mockViewModel.retry } returns true

        composeTestRule.activity.setContentUsingUseIdTheme {
            SetupTransportPin(viewModel = mockViewModel)
        }

        composeTestRule.onNodeWithTag(NavigationIcon.Back.name).assertDoesNotExist()
        composeTestRule.onNodeWithTag(NavigationIcon.Cancel.name).performClick()

        val confirmAlertTitle = composeTestRule.activity.resources.getString(R.string.firstTimeUser_confirmEnd_title)
        composeTestRule.onNodeWithText(confirmAlertTitle).assertIsDisplayed()

        val confirmDismissButton = composeTestRule.activity.resources.getString(R.string.firstTimeUser_confirmEnd_confirm)
        composeTestRule.onNodeWithText(confirmDismissButton).performClick()

        verify(exactly = 1) { mockViewModel.onNavigationButtonClicked()}
    }
}
