package de.digitalService.useID

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import de.digitalService.useID.ui.screens.identification.IdentificationPersonalPin
import de.digitalService.useID.ui.screens.identification.IdentificationPersonalPinViewModel
import de.digitalService.useID.util.MockNfcAdapterUtil
import de.digitalService.useID.util.NfcAdapterUtil
import de.digitalService.useID.util.setContentUsingUseIdTheme
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
    fun testPinInputAndVisualisationNullAttempts() {
        val testSixDigitPin = "123456"

        val mockViewModel: IdentificationPersonalPinViewModel = mockk(relaxed = true)

        every { mockViewModel.retry } returns false

        composeTestRule.activity.setContentUsingUseIdTheme {
            IdentificationPersonalPin(viewModel = mockViewModel)
        }

        val quantityAttemptsString = composeTestRule.activity.resources.getQuantityString(
            R.plurals.firstTimeUser_transportPIN_remainingAttempts,
            2,
            2
        )
        composeTestRule.onNodeWithText(quantityAttemptsString).assertDoesNotExist()

        val errorMessage = composeTestRule.activity.resources.getString(
            R.string.identification_personalPIN_error_incorrectPIN
        )
        composeTestRule.onNodeWithText(errorMessage).assertDoesNotExist()

        val pinEntryTestTag = "PINDigitField"
        composeTestRule.onAllNodesWithTag(pinEntryTestTag).assertCountEquals(6)

        composeTestRule.waitForIdle()

        val pinEntryFieldTestTag = "PINEntryField"
        val obfuscationTestTag = "Obfuscation"

        composeTestRule.onNodeWithTag(pinEntryFieldTestTag).performTextInput(testSixDigitPin)
        composeTestRule.onAllNodesWithTag(obfuscationTestTag).assertCountEquals(6)
    }

    @Test
    fun testPinInputAndVisualisation2Attempts() {
        val testSixDigitPin = "123456"

        val mockViewModel: IdentificationPersonalPinViewModel = mockk(relaxed = true)

        every { mockViewModel.retry } returns true

        composeTestRule.activity.setContentUsingUseIdTheme {
            IdentificationPersonalPin(viewModel = mockViewModel)
        }

        val quantityAttemptsString = composeTestRule.activity.resources.getQuantityString(
            R.plurals.identification_personalPIN_remainingAttempts,
            2,
            2
        )
        composeTestRule.onNodeWithText(quantityAttemptsString).assertIsDisplayed()

        val errorMessage = composeTestRule.activity.getString(
            R.string.identification_personalPIN_error_incorrectPIN
        )
        composeTestRule.onNodeWithText(errorMessage).assertIsDisplayed()

        val pinEntryTestTag = "PINDigitField"
        composeTestRule.onAllNodesWithTag(pinEntryTestTag).assertCountEquals(6)

        composeTestRule.waitForIdle()

        val pinEntryFieldTestTag = "PINEntryField"
        val obfuscationTestTag = "Obfuscation"

        composeTestRule.onNodeWithTag(pinEntryFieldTestTag).performTextInput(testSixDigitPin)
        composeTestRule.onAllNodesWithTag(obfuscationTestTag).assertCountEquals(6)
    }

    @Test
    fun testPinInputOnDoneClicked() {
        val testSixDigitPin = "123456"

        val mockViewModel: IdentificationPersonalPinViewModel = mockk(relaxed = true)

        every { mockViewModel.retry } answers { false }

        composeTestRule.activity.setContentUsingUseIdTheme {
            IdentificationPersonalPin(viewModel = mockViewModel)
        }

        val pinEntryTestTag = "PINDigitField"
        composeTestRule.onAllNodesWithTag(pinEntryTestTag).assertCountEquals(6)

        composeTestRule.waitForIdle()

        val pinEntryFieldTestTag = "PINEntryField"
        composeTestRule.onNodeWithTag(pinEntryFieldTestTag).performTextInput(testSixDigitPin)

        val obfuscationTestTag = "Obfuscation"
        composeTestRule.onAllNodesWithTag(obfuscationTestTag).assertCountEquals(6)

        composeTestRule.onNodeWithTag(pinEntryFieldTestTag).performImeAction()

        verify(exactly = 1) { mockViewModel.onDone(testSixDigitPin)  }
    }
}
