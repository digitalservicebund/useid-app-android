package de.digitalService.useID

import androidx.activity.compose.setContent
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import de.digitalService.useID.ui.screens.identification.IdentificationPersonalPIN
import de.digitalService.useID.ui.screens.identification.IdentificationPersonalPINViewModel
import de.digitalService.useID.util.MockNfcAdapterUtil
import de.digitalService.useID.util.NfcAdapterUtil
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class IdentificationPersonalPinTest {

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @BindValue
    val mockNfcAdapterUtil: NfcAdapterUtil = MockNfcAdapterUtil()

    @Test
    fun testPinInputAndVisualisation_NullAttempts() {
        val testAttempts: Int? = null
        val testPinState = mutableStateOf("")

        val testPinInput1 = "12345"
        val testPinInput2 = "123456"

        val mockViewModel: IdentificationPersonalPINViewModel = mockk(relaxed = true)

        every { mockViewModel.pin } answers { testPinState.value }
        every { mockViewModel.attempts } answers { testAttempts }

        composeTestRule.activity.setContent {
            IdentificationPersonalPIN(viewModel = mockViewModel)
        }

        val quantityAttemptsString = composeTestRule.activity.resources.getQuantityString(
            R.plurals.firstTimeUser_transportPIN_remainingAttempts,
            2,
            2
        )
        composeTestRule.onNodeWithText(quantityAttemptsString).assertDoesNotExist()

        val pinEntryTestTag = "PINDigitField"
        composeTestRule.onAllNodesWithTag(pinEntryTestTag).assertCountEquals(6)

        composeTestRule.waitForIdle()

        val pinEntryFieldTestTag = "PINEntryField"
        composeTestRule.onNodeWithTag(pinEntryFieldTestTag).performTextInput(testPinInput1)
        composeTestRule.onNodeWithTag(pinEntryFieldTestTag).performTextInput(testPinInput2)

        val obfuscationTestTag = "Obfuscation"
        testPinState.value = testPinInput1
        composeTestRule.onAllNodesWithTag(obfuscationTestTag).assertCountEquals(5)

        testPinState.value = testPinInput2
        composeTestRule.onAllNodesWithTag(obfuscationTestTag).assertCountEquals(6)

        verify(exactly = 1) { mockViewModel.userInputPIN(testPinInput1) }
        verify(exactly = 1) { mockViewModel.userInputPIN(testPinInput2) }
    }

    @Test
    fun testPinInputAndVisualisation_2Attempts() {
        val testAttempts = 2
        val testPinState = mutableStateOf("")

        val testPinInput1 = "12345"
        val testPinInput2 = "123456"

        val mockViewModel: IdentificationPersonalPINViewModel = mockk(relaxed = true)

        every { mockViewModel.pin } answers { testPinState.value }
        every { mockViewModel.attempts } answers { 2 }

        composeTestRule.activity.setContent {
            IdentificationPersonalPIN(viewModel = mockViewModel)
        }

        val quantityAttemptsString = composeTestRule.activity.resources.getQuantityString(
            R.plurals.identification_personalPIN_remainingAttempts,
            testAttempts,
            testAttempts
        )
        composeTestRule.onNodeWithText(quantityAttemptsString).assertIsDisplayed()

        val errorMessage = composeTestRule.activity.getString(R.string.identification_personalPIN_error_incorrectPIN)
        composeTestRule.onNodeWithText(errorMessage).assertIsDisplayed()

        val pinEntryTestTag = "PINDigitField"
        composeTestRule.onAllNodesWithTag(pinEntryTestTag).assertCountEquals(6)

        composeTestRule.waitForIdle()

        val pinEntryFieldTestTag = "PINEntryField"
        composeTestRule.onNodeWithTag(pinEntryFieldTestTag).performTextInput(testPinInput1)
        composeTestRule.onNodeWithTag(pinEntryFieldTestTag).performTextInput(testPinInput2)

        val obfuscationTestTag = "Obfuscation"
        testPinState.value = testPinInput1
        composeTestRule.onAllNodesWithTag(obfuscationTestTag).assertCountEquals(5)

        testPinState.value = testPinInput2
        composeTestRule.onAllNodesWithTag(obfuscationTestTag).assertCountEquals(6)

        verify(exactly = 1) { mockViewModel.userInputPIN(testPinInput1) }
        verify(exactly = 1) { mockViewModel.userInputPIN(testPinInput2) }
    }
}
