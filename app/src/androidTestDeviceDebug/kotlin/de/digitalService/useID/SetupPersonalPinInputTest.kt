package de.digitalService.useID

import androidx.activity.compose.setContent
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import de.digitalService.useID.ui.screens.setup.SetupPersonalPinInput
import de.digitalService.useID.ui.screens.setup.SetupPersonalPinInputViewModel
import de.digitalService.useID.util.MockNfcAdapterUtil
import de.digitalService.useID.util.NfcAdapterUtil
import io.mockk.every
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

    @BindValue
    val mockNfcAdapterUtil: NfcAdapterUtil = MockNfcAdapterUtil()

    @Test
    fun inputReceived() {
        val mockViewModel: SetupPersonalPinInputViewModel = mockk(relaxed = true)

        composeTestRule.activity.setContent {
            SetupPersonalPinInput(viewModel = mockViewModel)
        }

        val transportPinDialogTitleText = composeTestRule.activity.getString(R.string.firstTimeUser_personalPIN_title)
        composeTestRule.onNodeWithText(transportPinDialogTitleText).assertIsDisplayed()

        val pinEntryTextFieldTag = "PINEntryField"
        composeTestRule.onNodeWithTag(pinEntryTextFieldTag).performTextInput("1")
        composeTestRule.onNodeWithTag(pinEntryTextFieldTag).performTextInput("12")
        composeTestRule.onNodeWithTag(pinEntryTextFieldTag).performTextInput("123")
        composeTestRule.onNodeWithTag(pinEntryTextFieldTag).performTextInput("1234")
        composeTestRule.onNodeWithTag(pinEntryTextFieldTag).performTextInput("12345")

        composeTestRule.onNodeWithTag(pinEntryTextFieldTag).performTextInput("123456")
        composeTestRule.onNodeWithTag(pinEntryTextFieldTag).performImeAction()

        verify(exactly = 1) { mockViewModel.userInputPIN("1") }
        verify(exactly = 1) { mockViewModel.userInputPIN("12") }
        verify(exactly = 1) { mockViewModel.userInputPIN("123") }
        verify(exactly = 1) { mockViewModel.userInputPIN("1234") }
        verify(exactly = 1) { mockViewModel.userInputPIN("12345") }

        verify(exactly = 1) { mockViewModel.userInputPIN("123456") }
        verify(exactly = 1) { mockViewModel.onDonePressed() }
    }

    @Test
    fun correctPinEntryShown() {
        val testPin = mutableStateOf("")

        val mockViewModel: SetupPersonalPinInputViewModel = mockk(relaxed = true)
        every { mockViewModel.pin } answers { testPin.value }

        composeTestRule.activity.setContent {
            SetupPersonalPinInput(viewModel = mockViewModel)
        }

        val pinTitleText = composeTestRule.activity.getString(R.string.firstTimeUser_personalPIN_title)
        composeTestRule.onNodeWithText(pinTitleText).assertIsDisplayed()

        val pinEntryTestTag = "Obfuscation"
        composeTestRule.onAllNodesWithTag(pinEntryTestTag).assertCountEquals(0)

        testPin.value = "1"
        composeTestRule.onAllNodesWithTag(pinEntryTestTag).assertCountEquals(1)

        testPin.value = "12"
        composeTestRule.onAllNodesWithTag(pinEntryTestTag).assertCountEquals(2)

        testPin.value = "123"
        composeTestRule.onAllNodesWithTag(pinEntryTestTag).assertCountEquals(3)

        testPin.value = "1234"
        composeTestRule.onAllNodesWithTag(pinEntryTestTag).assertCountEquals(4)

        testPin.value = "12345"
        composeTestRule.onAllNodesWithTag(pinEntryTestTag).assertCountEquals(5)

        testPin.value = "1"
        composeTestRule.onAllNodesWithTag(pinEntryTestTag).assertCountEquals(1)

        testPin.value = "12345"
        composeTestRule.onAllNodesWithTag(pinEntryTestTag).assertCountEquals(5)

        testPin.value = "123"
        composeTestRule.onAllNodesWithTag(pinEntryTestTag).assertCountEquals(3)

        testPin.value = ""
        composeTestRule.onAllNodesWithTag(pinEntryTestTag).assertCountEquals(0)

        testPin.value = "1234567890"
        composeTestRule.onAllNodesWithTag(pinEntryTestTag).assertCountEquals(6)

        testPin.value = "1234"
        composeTestRule.onAllNodesWithTag(pinEntryTestTag).assertCountEquals(4)
    }
}
