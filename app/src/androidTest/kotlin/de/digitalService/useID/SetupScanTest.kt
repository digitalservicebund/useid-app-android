package de.digitalService.useID

import androidx.activity.compose.setContent
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import de.digitalService.useID.ui.ScanError
import de.digitalService.useID.ui.composables.screens.SetupScan
import de.digitalService.useID.ui.composables.screens.SetupScanViewModelInterface
import de.digitalService.useID.util.MockNfcAdapterUtil
import de.digitalService.useID.util.NfcAdapterUtil
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class SetupScanTest {

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @BindValue
    val mockNfcAdapterUtil: NfcAdapterUtil = MockNfcAdapterUtil()

    @Test
    fun openErrorDialogAndConfirmWithButton() {
        val testErrorState = ScanError.Other(null)

        val mockViewModel: SetupScanViewModelInterface = mockk(relaxed = true)
        every { mockViewModel.errorState } returns testErrorState

        composeTestRule.activity.setContent {
            SetupScan(viewModel = mockViewModel)
        }

        val errorDialogTitleText = composeTestRule.activity.getString(testErrorState.titleResID)
        composeTestRule.onNodeWithText(errorDialogTitleText).assertIsDisplayed()

        val errorDialogDescriptionText = composeTestRule.activity.getString(testErrorState.titleResID)
        composeTestRule.onNodeWithText(errorDialogDescriptionText).assertIsDisplayed()

        val buttonText = composeTestRule.activity.getString(R.string.idScan_error_button_close)
        composeTestRule.onNodeWithText(buttonText).performClick()

        verify(exactly = 1) { mockViewModel.onCancel() }
        verify(exactly = 1) { mockViewModel.startSettingPIN(any()) }
    }

    @Test
    fun enterTransportPinDialogOpens() {
        val mockViewModel: SetupScanViewModelInterface = mockk(relaxed = true)
        every { mockViewModel.errorState } returns ScanError.IncorrectPIN(2)

        composeTestRule.activity.setContent {
            SetupScan(viewModel = mockViewModel)
        }

        val transportPinDialogTitleText = composeTestRule.activity.getString(R.string.firstTimeUser_transportPIN_title)
        composeTestRule.onNodeWithText(transportPinDialogTitleText).assertIsDisplayed()

        verify(exactly = 1) { mockViewModel.startSettingPIN(any()) }
    }

    @Test
    fun noDialogIsOpen() {
        val mockViewModel: SetupScanViewModelInterface = mockk(relaxed = true)
        every { mockViewModel.errorState } returns null

        composeTestRule.activity.setContent {
            SetupScan(viewModel = mockViewModel)
        }

        val transportPinDialogTitleText = composeTestRule.activity.getString(R.string.firstTimeUser_transportPIN_title)
        composeTestRule.onNodeWithText(transportPinDialogTitleText).assertDoesNotExist()

        val errorDialogTitleText = composeTestRule.activity.getString(R.string.firstTimeUser_scan_error_title_pin_suspended)
        composeTestRule.onNodeWithText(errorDialogTitleText).assertDoesNotExist()

        verify(exactly = 1) { mockViewModel.startSettingPIN(any()) }
    }
}
