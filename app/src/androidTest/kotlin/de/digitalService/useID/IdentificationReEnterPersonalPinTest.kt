package de.digitalService.useID

import androidx.activity.compose.setContent
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import de.digitalService.useID.ui.screens.identification.IdentificationReEnterPersonalPIN
import de.digitalService.useID.ui.screens.identification.IdentificationReEnterPersonalPINViewModel
import de.digitalService.useID.util.MockNfcAdapterUtil
import de.digitalService.useID.util.NfcAdapterUtil
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class IdentificationReEnterPersonalPinTest {

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @BindValue
    val mockNfcAdapterUtil: NfcAdapterUtil = MockNfcAdapterUtil()

    @Test
    fun correctUsage() {
        val testPinState = mutableStateOf("")

        val testPinInput1 = "12345"
        val testPinInput2 = "123456"

        val mockViewModel: IdentificationReEnterPersonalPINViewModel = mockk(relaxed = true)

        every { mockViewModel.pin } answers { testPinState.value }

        composeTestRule.activity.setContent {
            IdentificationReEnterPersonalPIN(viewModel = mockViewModel)
        }

        val pinEntryTextFieldTag = "PINEntryField"
        composeTestRule.onNodeWithTag(pinEntryTextFieldTag).assertIsFocused()

        composeTestRule.onNodeWithTag(pinEntryTextFieldTag).performTextInput(testPinInput1)
        composeTestRule.onNodeWithTag(pinEntryTextFieldTag).performTextInput(testPinInput2)

        val pinEntryTag = "PinEntry"
        testPinState.value = testPinInput1
        composeTestRule.onAllNodesWithTag(pinEntryTag).assertCountEquals(5)

        testPinState.value = testPinInput2
        composeTestRule.onAllNodesWithTag(pinEntryTag).assertCountEquals(6)

        verify(exactly = 1) { mockViewModel.userInputPIN(testPinInput1) }
        verify(exactly = 1) { mockViewModel.userInputPIN(testPinInput2) }
    }
}
