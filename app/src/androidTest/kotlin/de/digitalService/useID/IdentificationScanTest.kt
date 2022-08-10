package de.digitalService.useID

import androidx.activity.compose.setContent
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import de.digitalService.useID.ui.ScanError
import de.digitalService.useID.ui.composables.screens.identification.IdentificationScan
import de.digitalService.useID.ui.composables.screens.identification.IdentificationScanViewModel
import de.digitalService.useID.util.MockNfcAdapterUtil
import de.digitalService.useID.util.NfcAdapterUtil
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class IdentificationScanTest {

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @BindValue
    val mockNfcAdapterUtil: NfcAdapterUtil = MockNfcAdapterUtil()

    @Test
    fun openErrorDialogAndConfirmWithButton_ScanErrorOther() {
        val testErrorState = ScanError.Other(null)

        val mockViewModel: IdentificationScanViewModel = mockk(relaxed = true)
        every { mockViewModel.errorState } returns testErrorState

        composeTestRule.activity.setContent {
            IdentificationScan(viewModel = mockViewModel)
        }

        val errorDialogTitleText = composeTestRule.activity.getString(testErrorState.titleResID)
        composeTestRule.onNodeWithText(errorDialogTitleText).assertIsDisplayed()

        val errorDialogDescriptionText = composeTestRule.activity.getString(testErrorState.titleResID)
        composeTestRule.onNodeWithText(errorDialogDescriptionText).assertIsDisplayed()

        val buttonText = composeTestRule.activity.getString(R.string.idScan_error_button_close)
        composeTestRule.onNodeWithText(buttonText).performClick()

        verify(exactly = 1) { mockViewModel.onCancelIdentification() }
    }

    @Test
    fun openErrorDialogAndConfirmWithButton_ScanErrorCardDeactivated() {
        val testErrorState = ScanError.CardDeactivated

        val mockViewModel: IdentificationScanViewModel = mockk(relaxed = true)
        every { mockViewModel.errorState } returns testErrorState

        composeTestRule.activity.setContent {
            IdentificationScan(viewModel = mockViewModel)
        }

        val errorDialogTitleText = composeTestRule.activity.getString(testErrorState.titleResID)
        composeTestRule.onNodeWithText(errorDialogTitleText).assertIsDisplayed()

        val errorDialogDescriptionText = composeTestRule.activity.getString(testErrorState.titleResID)
        composeTestRule.onNodeWithText(errorDialogDescriptionText).assertIsDisplayed()

        val buttonText = composeTestRule.activity.getString(R.string.idScan_error_button_close)
        composeTestRule.onNodeWithText(buttonText).performClick()

        verify(exactly = 1) { mockViewModel.onCancelIdentification() }
    }

    @Test
    fun openErrorDialogAndConfirmWithButton_ScanErrorPINBlocked() {
        val testErrorState = ScanError.PINBlocked

        val mockViewModel: IdentificationScanViewModel = mockk(relaxed = true)
        every { mockViewModel.errorState } returns testErrorState

        composeTestRule.activity.setContent {
            IdentificationScan(viewModel = mockViewModel)
        }

        val errorDialogTitleText = composeTestRule.activity.getString(testErrorState.titleResID)
        composeTestRule.onNodeWithText(errorDialogTitleText).assertIsDisplayed()

        val errorDialogDescriptionText = composeTestRule.activity.getString(testErrorState.titleResID)
        composeTestRule.onNodeWithText(errorDialogDescriptionText).assertIsDisplayed()

        val buttonText = composeTestRule.activity.getString(R.string.idScan_error_button_close)
        composeTestRule.onNodeWithText(buttonText).performClick()

        verify(exactly = 1) { mockViewModel.onCancelIdentification() }
    }

    @Test
    fun openErrorDialogAndConfirmWithButton_ScanErrorPINSuspended() {
        val testErrorState = ScanError.PINSuspended

        val mockViewModel: IdentificationScanViewModel = mockk(relaxed = true)
        every { mockViewModel.errorState } returns testErrorState

        composeTestRule.activity.setContent {
            IdentificationScan(viewModel = mockViewModel)
        }

        val errorDialogTitleText = composeTestRule.activity.getString(testErrorState.titleResID)
        composeTestRule.onNodeWithText(errorDialogTitleText).assertIsDisplayed()

        val errorDialogDescriptionText = composeTestRule.activity.getString(testErrorState.titleResID)
        composeTestRule.onNodeWithText(errorDialogDescriptionText).assertIsDisplayed()

        val buttonText = composeTestRule.activity.getString(R.string.idScan_error_button_close)
        composeTestRule.onNodeWithText(buttonText).performClick()

        verify(exactly = 1) { mockViewModel.onCancelIdentification() }
    }

    @Test
    fun enterPinDialogOpens() {
        val mockViewModel: IdentificationScanViewModel = mockk(relaxed = true)
        every { mockViewModel.errorState } returns ScanError.IncorrectPIN(2)

        composeTestRule.activity.setContent {
            IdentificationScan(viewModel = mockViewModel)
        }

        val pinEntryTag = "PINEntryField"
        composeTestRule.onNodeWithTag(pinEntryTag).assertIsDisplayed()
    }

    @Test
    fun noDialogIsOpen() {
        val mockViewModel: IdentificationScanViewModel = mockk(relaxed = true)
        every { mockViewModel.errorState } returns null

        composeTestRule.activity.setContent {
            IdentificationScan(viewModel = mockViewModel)
        }

        val pinEntryTag = "PINEntryField"
        composeTestRule.onNodeWithText(pinEntryTag).assertDoesNotExist()

        val errorDialogTitleText = composeTestRule.activity.getString(R.string.firstTimeUser_scan_error_title_pin_suspended)
        composeTestRule.onNodeWithText(errorDialogTitleText).assertDoesNotExist()
    }

    @Test
    fun noDialogIsOpen_ShowProgress() {
        val mockViewModel: IdentificationScanViewModel = mockk(relaxed = true)

        every { mockViewModel.errorState } returns null
        every { mockViewModel.shouldShowProgress } returns true

        composeTestRule.activity.setContent {
            IdentificationScan(viewModel = mockViewModel)
        }

        val progressIndicatorTag = "ProgressIndicator"
        composeTestRule.onNodeWithTag(progressIndicatorTag).assertIsDisplayed()
    }
}
