package de.digitalService.useID

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import de.digitalService.useID.ui.composables.screens.SetupPersonalPIN
import de.digitalService.useID.ui.composables.screens.SetupPersonalPINViewModelInterface
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Rule
import org.junit.Test

class SetupPersonalPINTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun correctUsage() {
        val testShouldShowErrorState = mutableStateOf(false)
        val testShouldShowPIN2EntryFieldState = mutableStateOf(false)
        val testPin1State = mutableStateOf("")
        val testPin2State = mutableStateOf("")
        val testFocusPinState = mutableStateOf(SetupPersonalPINViewModelInterface.PINEntryFieldFocus.PIN_1)

        val testPinInput = "123456"
        val testPinInput2 = "1234"

        val mockViewModel: SetupPersonalPINViewModelInterface = mockk(relaxed = true)

        every { mockViewModel.pin1 } returns ""
        every { mockViewModel.pin2 } returns ""
        every { mockViewModel.shouldShowError } answers { testShouldShowErrorState.value }
        every { mockViewModel.shouldShowPIN2EntryField } answers { testShouldShowPIN2EntryFieldState.value }
        every { mockViewModel.pin1 } answers { testPin1State.value }
        every { mockViewModel.pin2 } answers { testPin2State.value }
        every { mockViewModel.focus } answers { testFocusPinState.value }

        composeTestRule.setContent {
            SetupPersonalPIN(viewModel = mockViewModel)
        }

        val pinEntryTextFieldTag = "PINEntryField"
        composeTestRule.onNodeWithTag(pinEntryTextFieldTag).assertIsFocused()

        composeTestRule.onNodeWithTag(pinEntryTextFieldTag).performTextInput(testPinInput)
        testPin1State.value = testPinInput
        composeTestRule.onAllNodesWithTag("PinEntry").assertCountEquals(6)

        composeTestRule.onAllNodesWithTag("PINEntryField").assertCountEquals(1)

        testShouldShowPIN2EntryFieldState.value = true
        testFocusPinState.value = SetupPersonalPINViewModelInterface.PINEntryFieldFocus.PIN_2

        composeTestRule.onAllNodesWithTag("PINEntryField").assertCountEquals(2)

        composeTestRule.onAllNodesWithTag("PINEntryField")[1].assertIsFocused()

        composeTestRule.onAllNodesWithTag("PINEntryField")[1].performTextInput(testPinInput2)
        testPin2State.value = testPinInput2

        composeTestRule.onAllNodesWithTag("PinEntry").assertCountEquals(10)

        val transportPinDialogTitleText = composeTestRule.activity.getString(R.string.firstTimeUser_personalPIN_error_mismatch_title)
        composeTestRule.onNodeWithText(transportPinDialogTitleText).assertDoesNotExist()

        testShouldShowErrorState.value = true

        composeTestRule.onNodeWithText(transportPinDialogTitleText).assertIsDisplayed()

        verify(exactly = 1) { mockViewModel.userInputPIN1(testPinInput) }
        verify(exactly = 1) { mockViewModel.userInputPIN2(testPinInput2) }
    }
}
