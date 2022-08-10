package de.digitalService.useID

import androidx.activity.compose.setContent
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import de.digitalService.useID.ui.composables.screens.SetupReEnterTransportPIN
import de.digitalService.useID.ui.composables.screens.SetupReEnterTransportPINViewModelInterface
import de.digitalService.useID.util.MockNfcAdapterUtil
import de.digitalService.useID.util.NfcAdapterUtil
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class SetupReEnterTransportPINTest {

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @BindValue
    val mockNfcAdapterUtil: NfcAdapterUtil = MockNfcAdapterUtil()

    @Test
    fun inputReceived() {
        val testAttempts = 3

        val mockViewModel: SetupReEnterTransportPINViewModelInterface = mockk(relaxed = true)
        every { mockViewModel.attempts } returns testAttempts

        composeTestRule.activity.setContent {
            SetupReEnterTransportPIN(viewModel = mockViewModel)
        }

        val transportPinDialogTitleText = composeTestRule.activity.getString(R.string.firstTimeUser_transportPIN_title)
        composeTestRule.onNodeWithText(transportPinDialogTitleText).assertIsDisplayed()

        val pinEntryTextFieldTag = "PINEntryField"
        composeTestRule.onNodeWithTag(pinEntryTextFieldTag).assertIsFocused()

        composeTestRule.onNodeWithTag(pinEntryTextFieldTag).performTextInput("1")
        composeTestRule.onNodeWithTag(pinEntryTextFieldTag).performTextInput("12")
        composeTestRule.onNodeWithTag(pinEntryTextFieldTag).performTextInput("123")
        composeTestRule.onNodeWithTag(pinEntryTextFieldTag).performTextInput("1234")
        composeTestRule.onNodeWithTag(pinEntryTextFieldTag).performTextInput("12345")

        composeTestRule.onNodeWithTag(pinEntryTextFieldTag).performTextInput("123456")

        verify(exactly = 1) { mockViewModel.onInputChanged("1") }
        verify(exactly = 1) { mockViewModel.onInputChanged("12") }
        verify(exactly = 1) { mockViewModel.onInputChanged("123") }
        verify(exactly = 1) { mockViewModel.onInputChanged("1234") }
        verify(exactly = 1) { mockViewModel.onInputChanged("12345") }

        verify(exactly = 0) { mockViewModel.onInputChanged("123456") }
    }

    @Test
    fun correctPinEntryShown() {
        val testAttempts = 3
        val testTransportPin = mutableStateOf("")

        val mockViewModel: SetupReEnterTransportPINViewModelInterface = mockk(relaxed = true)
        every { mockViewModel.attempts } returns testAttempts
        every { mockViewModel.transportPIN } answers { testTransportPin.value }

        composeTestRule.activity.setContent {
            SetupReEnterTransportPIN(viewModel = mockViewModel)
        }

        val transportPinDialogTitleText = composeTestRule.activity.getString(R.string.firstTimeUser_transportPIN_title)
        composeTestRule.onNodeWithText(transportPinDialogTitleText).assertIsDisplayed()

        composeTestRule.onAllNodesWithTag("PinEntry").assertCountEquals(0)

        testTransportPin.value = "1"
        composeTestRule.onAllNodesWithTag("PinEntry").assertCountEquals(1)

        testTransportPin.value = "12"
        composeTestRule.onAllNodesWithTag("PinEntry").assertCountEquals(2)

        testTransportPin.value = "123"
        composeTestRule.onAllNodesWithTag("PinEntry").assertCountEquals(3)

        testTransportPin.value = "1234"
        composeTestRule.onAllNodesWithTag("PinEntry").assertCountEquals(4)

        testTransportPin.value = "12345"
        composeTestRule.onAllNodesWithTag("PinEntry").assertCountEquals(5)

        testTransportPin.value = "1"
        composeTestRule.onAllNodesWithTag("PinEntry").assertCountEquals(1)

        testTransportPin.value = "12345"
        composeTestRule.onAllNodesWithTag("PinEntry").assertCountEquals(5)

        testTransportPin.value = "123"
        composeTestRule.onAllNodesWithTag("PinEntry").assertCountEquals(3)

        testTransportPin.value = ""
        composeTestRule.onAllNodesWithTag("PinEntry").assertCountEquals(0)

        testTransportPin.value = "1234567890"
        composeTestRule.onAllNodesWithTag("PinEntry").assertCountEquals(5)

        testTransportPin.value = "1234"
        composeTestRule.onAllNodesWithTag("PinEntry").assertCountEquals(4)
    }
}
