package de.digitalService.useID

import androidx.activity.compose.setContent
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import de.digitalService.useID.ui.composables.screens.SetupScan
import de.digitalService.useID.ui.composables.screens.SetupScanViewModelInterface
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class SetupScanTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun openErrorDialogAndConfirmWithButton() {
        val testErrorState = SetupScanViewModelInterface.Error.PINSuspended
        val testAttempts = 3

        val mockViewModel: SetupScanViewModelInterface = mockk(relaxed = true)
        every { mockViewModel.errorState } returns testErrorState
        every { mockViewModel.attempts } returns testAttempts

        composeTestRule.activity.setContent {
            SetupScan(viewModel = mockViewModel)
        }

        val errorDialogTitleText = composeTestRule.activity.getString(testErrorState.titleResID)
        composeTestRule.onNodeWithText(errorDialogTitleText).assertIsDisplayed()

        val errorDialogDescriptionText = composeTestRule.activity.getString(testErrorState.titleResID)
        composeTestRule.onNodeWithText(errorDialogDescriptionText).assertIsDisplayed()

        val buttonText = composeTestRule.activity.getString(R.string.firstTimeUser_scan_error_button)
        composeTestRule.onNodeWithText(buttonText).performClick()

        verify(exactly = 1) { mockViewModel.onErrorDialogButtonTap() }
        verify(exactly = 1) { mockViewModel.startSettingPIN(any()) }
    }

    @Test
    fun enterTransportPinDialogOpens() {
        val testAttempts = 0

        val mockViewModel: SetupScanViewModelInterface = mockk(relaxed = true)
        every { mockViewModel.errorState } returns null
        every { mockViewModel.attempts } returns testAttempts

        composeTestRule.activity.setContent {
            SetupScan(viewModel = mockViewModel)
        }

        val transportPinDialogTitleText = composeTestRule.activity.getString(R.string.firstTimeUser_transportPIN_title)
        composeTestRule.onNodeWithText(transportPinDialogTitleText).assertIsDisplayed()

        verify(exactly = 1) { mockViewModel.startSettingPIN(any()) }
    }

    @Test
    fun noDialogIsOpen() {
        val testAttempts = 3

        val mockViewModel: SetupScanViewModelInterface = mockk(relaxed = true)
        every { mockViewModel.errorState } returns null
        every { mockViewModel.attempts } returns testAttempts

        composeTestRule.activity.setContent {
            SetupScan(viewModel = mockViewModel)
        }

        val transportPinDialogTitleText = composeTestRule.activity.getString(R.string.firstTimeUser_transportPIN_title)
        composeTestRule.onNodeWithText(transportPinDialogTitleText).assertDoesNotExist()

        val errorDialogTitleText = composeTestRule.activity.getString(R.string.firstTimeUser_scan_error_title_pin_suspended)
        composeTestRule.onNodeWithText(errorDialogTitleText).assertDoesNotExist()

        verify(exactly = 1) { mockViewModel.startSettingPIN(any()) }
    }

    @Test
    fun openErrorDialogAndTransportPinDialog() {
        val testErrorState = SetupScanViewModelInterface.Error.PINSuspended
        val testAttempts = 2

        val mockViewModel: SetupScanViewModelInterface = mockk(relaxed = true)
        every { mockViewModel.errorState } returns testErrorState
        every { mockViewModel.attempts } returns testAttempts

        composeTestRule.activity.setContent {
            SetupScan(viewModel = mockViewModel)
        }

        val errorDialogTitleText = composeTestRule.activity.getString(testErrorState.titleResID)
        composeTestRule.onNodeWithText(errorDialogTitleText).assertIsDisplayed()

        val transportPinDialogTitleText = composeTestRule.activity.getString(R.string.firstTimeUser_transportPIN_title)
        composeTestRule.onNodeWithText(transportPinDialogTitleText).assertIsNotDisplayed()

        val buttonText = composeTestRule.activity.getString(R.string.firstTimeUser_scan_error_button)
        composeTestRule.onNodeWithText(buttonText).performClick()

        verify(exactly = 1) { mockViewModel.onErrorDialogButtonTap() }
        verify(exactly = 1) { mockViewModel.startSettingPIN(any()) }
    }
}
