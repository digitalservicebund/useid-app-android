package de.digitalService.useID

import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import de.digitalService.useID.ui.components.pin.PinEntryField
import de.digitalService.useID.util.setContentUsingUseIdTheme
import io.mockk.mockk
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class PinEntryFieldTest {
    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    private val obfuscationTestTag = "Obfuscation"
    private val pinEntryTestTag = "PINEntry"
    private val pinEntryFieldTestTag = "PINEntryField"
    private val pinDigitFieldTestTag = "PINDigitField"
    private val spacerTag = "PINDigitRowSpacer"

    @Test
    fun testObfuscated() {
        val mockOnDone = mockk<(String) -> Unit>()
        val focusRequester = FocusRequester()
        val digitCount = 6
        val testSixDigitPin = "123456"

        composeTestRule.activity.setContentUsingUseIdTheme {
            PinEntryField(
                digitCount = digitCount,
                obfuscation = true,
                spacerPosition = null,
                focusRequester = focusRequester,
                onDone = mockOnDone
            )

            focusRequester.requestFocus()
        }

        composeTestRule.onAllNodesWithTag(pinDigitFieldTestTag).assertCountEquals(6)

        composeTestRule.onNodeWithTag(pinEntryFieldTestTag).performTextInput(testSixDigitPin)

        composeTestRule.onAllNodesWithTag(obfuscationTestTag).assertCountEquals(6)
        composeTestRule.onAllNodesWithTag(pinEntryTestTag).assertCountEquals(0)
    }

    @Test
    fun testNotObfuscated() {
        val mockOnDone = mockk<(String) -> Unit>()
        val focusRequester = FocusRequester()
        val digitCount = 6
        val testSixDigitPin = "123456"

        composeTestRule.activity.setContentUsingUseIdTheme {
            PinEntryField(
                digitCount = digitCount,
                obfuscation = false,
                spacerPosition = null,
                focusRequester = focusRequester,
                onDone = mockOnDone
            )

            focusRequester.requestFocus()
        }

        composeTestRule.onAllNodesWithTag(pinDigitFieldTestTag).assertCountEquals(6)

        composeTestRule.onNodeWithTag(pinEntryFieldTestTag).performTextInput(testSixDigitPin)

        composeTestRule.onAllNodesWithTag(obfuscationTestTag).assertCountEquals(0)
        composeTestRule.onAllNodesWithTag(pinEntryTestTag).assertCountEquals(6)
    }

    @Test
    fun testWithSpacer() {
        val mockOnDone = mockk<(String) -> Unit>()
        val focusRequester = FocusRequester()
        val digitCount = 6

        composeTestRule.activity.setContentUsingUseIdTheme {
            PinEntryField(
                digitCount = digitCount,
                obfuscation = true,
                spacerPosition = 2,
                focusRequester = focusRequester,
                onDone = mockOnDone
            )

            focusRequester.requestFocus()
        }

        composeTestRule.onNodeWithTag(spacerTag).assertExists()
    }

    @Test
    fun testWithoutSpacer() {
        val mockOnDone = mockk<(String) -> Unit>()
        val focusRequester = FocusRequester()
        val digitCount = 6

        composeTestRule.activity.setContentUsingUseIdTheme {
            PinEntryField(
                digitCount = digitCount,
                obfuscation = true,
                spacerPosition = null,
                focusRequester = focusRequester,
                onDone = mockOnDone
            )

            focusRequester.requestFocus()
        }

        composeTestRule.onNodeWithTag(spacerTag).assertDoesNotExist()
    }

    @Test
    fun testValidInput() {
        val mockOnDone = mockk<(String) -> Unit>()
        val focusRequester = FocusRequester()
        val digitCount = 6

        composeTestRule.activity.setContentUsingUseIdTheme {
            PinEntryField(
                digitCount = digitCount,
                obfuscation = false,
                spacerPosition = null,
                focusRequester = focusRequester,
                onDone = mockOnDone
            )

            focusRequester.requestFocus()
        }

        composeTestRule.onAllNodesWithTag(pinDigitFieldTestTag).assertCountEquals(6)

        composeTestRule.onNodeWithTag(pinEntryFieldTestTag).performTextInput("12")
        composeTestRule.onAllNodesWithTag(pinEntryTestTag).assertCountEquals(2)

        // FORBIDDEN INPUT
        composeTestRule.onNodeWithTag(pinEntryFieldTestTag).performTextInput("3456")
        composeTestRule.onAllNodesWithTag(pinEntryTestTag).assertCountEquals(6)
    }

    @Test
    fun testInvalidInputs() {
        val mockOnDone = mockk<(String) -> Unit>()
        val focusRequester = FocusRequester()
        val digitCount = 6

        composeTestRule.activity.setContentUsingUseIdTheme {
            PinEntryField(
                digitCount = digitCount,
                obfuscation = false,
                spacerPosition = null,
                focusRequester = focusRequester,
                onDone = mockOnDone
            )

            focusRequester.requestFocus()
        }

        composeTestRule.onAllNodesWithTag(pinDigitFieldTestTag).assertCountEquals(6)

        val emoji = String(intArrayOf(0x274C), 0, 1)
        val invalidTestValues = arrayOf(
            "abcdef",
            "12345A",
            "123456.",
            "--12--",
            "      ",
            "\n\n\n\n\n\n",
            "12345\n",
            "123 56",
            " 123456 ",
            "$emoji$emoji$emoji$emoji$emoji$emoji"
        )

        invalidTestValues.onEach {
            composeTestRule.onNodeWithTag(pinEntryFieldTestTag).performTextInput(it)
            composeTestRule.onAllNodesWithTag(pinEntryTestTag).assertCountEquals(0)
        }
    }
}
