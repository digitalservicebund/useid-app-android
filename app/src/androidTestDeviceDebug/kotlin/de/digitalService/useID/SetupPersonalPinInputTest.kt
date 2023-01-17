package de.digitalService.useID

import android.view.KeyEvent
import androidx.compose.ui.input.key.NativeKeyEvent
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import de.digitalService.useID.ui.components.NavigationIcon
import de.digitalService.useID.ui.screens.setup.SetupPersonalPinInput
import de.digitalService.useID.ui.screens.setup.SetupPersonalPinInputViewModelInterface
import de.digitalService.useID.util.setContentUsingUseIdTheme
import io.mockk.mockk
import io.mockk.verify
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class SetupPersonalPinInputTest {

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    private val pinEntryFieldTestTag = "PINEntryField"

    @Test
    fun inputReceived() {
        val mockViewModel: SetupPersonalPinInputViewModelInterface = mockk(relaxed = true)

        composeTestRule.activity.setContentUsingUseIdTheme {
            SetupPersonalPinInput(viewModel = mockViewModel)
        }

        val transportPinDialogTitleText = composeTestRule.activity.getString(R.string.firstTimeUser_personalPIN_title)
        composeTestRule.onNodeWithText(transportPinDialogTitleText).assertIsDisplayed()

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

        composeTestRule.onNodeWithTag(pinEntryFieldTestTag).performImeAction()
        verify(exactly = 1) { mockViewModel.onDoneClicked("123456") }
    }

    @Test
    fun onNavigatingBack() {
        val mockViewModel: SetupPersonalPinInputViewModelInterface = mockk(relaxed = true)

        composeTestRule.activity.setContentUsingUseIdTheme {
            SetupPersonalPinInput(viewModel = mockViewModel)
        }

        val transportPinDialogTitleText = composeTestRule.activity.getString(R.string.firstTimeUser_personalPIN_title)
        composeTestRule.onNodeWithText(transportPinDialogTitleText).assertIsDisplayed()

        composeTestRule.onNodeWithTag(NavigationIcon.Back.name).performClick()
        verify(exactly = 1) { mockViewModel.onBack() }
    }
}
