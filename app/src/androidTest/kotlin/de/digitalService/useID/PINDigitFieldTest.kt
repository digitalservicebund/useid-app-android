package de.digitalService.useID

import androidx.activity.compose.setContent
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import de.digitalService.useID.ui.components.pin.PINEntryField
import io.mockk.mockk
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class PINDigitFieldTest {
    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    val obfuscationTestTag = "Obfuscation"
    val pinEntryTestTag = "PINEntry"
    val pinEntryFieldTestTag = "PINEntryField"
    val pinDigitFieldTestTag = "PINDigitField"

    val testPinInput1 = "12345"
    val testPinInput2 = "123456"
    val spacerTag = "PINDigitRowSpacer"

    @Test
    fun obfuscated() {
        val mockCallback = mockk<(String) -> Unit>(relaxed = true)
        val testPinState = mutableStateOf("")
        val focusRequester = FocusRequester()
        composeTestRule.activity.setContent {
            PINEntryField(
                value = testPinState.value,
                onValueChanged = mockCallback,
                digitCount = 6,
                obfuscation = true,
                spacerPosition = null,
                contentDescription = "",
                focusRequester = focusRequester
            )

            focusRequester.requestFocus()
        }

        composeTestRule.onAllNodesWithTag(pinDigitFieldTestTag).assertCountEquals(6)

        composeTestRule.onNodeWithTag(pinEntryFieldTestTag).performTextInput(testPinInput1)
        composeTestRule.onNodeWithTag(pinEntryFieldTestTag).performTextInput(testPinInput2)

        testPinState.value = testPinInput1
        composeTestRule.onAllNodesWithTag(obfuscationTestTag).assertCountEquals(5)
        composeTestRule.onAllNodesWithTag(pinEntryTestTag).assertCountEquals(0)

        testPinState.value = testPinInput2
        composeTestRule.onAllNodesWithTag(obfuscationTestTag).assertCountEquals(6)
        composeTestRule.onAllNodesWithTag(pinEntryTestTag).assertCountEquals(0)

        composeTestRule.onNodeWithTag(spacerTag).assertDoesNotExist()
    }

    @Test
    fun notObfuscated() {
        val mockCallback = mockk<(String) -> Unit>(relaxed = true)
        val testPinState = mutableStateOf("")
        val focusRequester = FocusRequester()

        composeTestRule.activity.setContent {
            PINEntryField(
                value = testPinState.value,
                onValueChanged = mockCallback,
                digitCount = 6,
                obfuscation = false,
                spacerPosition = null,
                contentDescription = "",
                focusRequester = focusRequester
            )

            focusRequester.requestFocus()
        }

        composeTestRule.onAllNodesWithTag(pinDigitFieldTestTag).assertCountEquals(6)

        composeTestRule.onNodeWithTag(pinEntryFieldTestTag).performTextInput(testPinInput1)
        composeTestRule.onNodeWithTag(pinEntryFieldTestTag).performTextInput(testPinInput2)

        testPinState.value = testPinInput1
        composeTestRule.onAllNodesWithTag(pinEntryTestTag).assertCountEquals(5)
        composeTestRule.onAllNodesWithTag(obfuscationTestTag).assertCountEquals(0)

        testPinState.value = testPinInput2
        composeTestRule.onAllNodesWithTag(pinEntryTestTag).assertCountEquals(6)
        composeTestRule.onAllNodesWithTag(obfuscationTestTag).assertCountEquals(0)

        composeTestRule.onNodeWithTag(spacerTag).assertDoesNotExist()
    }

    @Test
    fun withSpacer() {
        val mockCallback = mockk<(String) -> Unit>(relaxed = true)
        val testPinState = mutableStateOf("")
        val focusRequester = FocusRequester()

        composeTestRule.activity.setContent {
            PINEntryField(
                value = testPinState.value,
                onValueChanged = mockCallback,
                digitCount = 6,
                obfuscation = false,
                spacerPosition = 2,
                contentDescription = "",
                focusRequester = focusRequester
            )

            focusRequester.requestFocus()
        }

        composeTestRule.onAllNodesWithTag(pinDigitFieldTestTag).assertCountEquals(6)

        composeTestRule.onNodeWithTag(pinEntryFieldTestTag).performTextInput(testPinInput1)
        composeTestRule.onNodeWithTag(pinEntryFieldTestTag).performTextInput(testPinInput2)

        testPinState.value = testPinInput1
        composeTestRule.onAllNodesWithTag(pinEntryTestTag).assertCountEquals(5)
        composeTestRule.onAllNodesWithTag(obfuscationTestTag).assertCountEquals(0)

        testPinState.value = testPinInput2
        composeTestRule.onAllNodesWithTag(pinEntryTestTag).assertCountEquals(6)
        composeTestRule.onAllNodesWithTag(obfuscationTestTag).assertCountEquals(0)

        composeTestRule.onNodeWithTag(spacerTag).assertExists()
    }
}
