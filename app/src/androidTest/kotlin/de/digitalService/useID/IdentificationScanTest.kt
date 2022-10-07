package de.digitalService.useID

import androidx.activity.compose.setContent
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import de.digitalService.useID.models.ScanError
import de.digitalService.useID.ui.screens.identification.IdentificationScan
import de.digitalService.useID.ui.screens.identification.IdentificationScanViewModel
import de.digitalService.useID.ui.screens.setup.SetupScan
import de.digitalService.useID.ui.screens.setup.SetupScanViewModelInterface
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

        composeTestRule.onNodeWithTag("${testErrorState.textResID}").assertIsDisplayed()

        val buttonText = composeTestRule.activity.getString(R.string.scanError_close)
        composeTestRule.onNodeWithText(buttonText).performClick()

        verify(exactly = 1) { mockViewModel.onErrorDialogButtonTapped(any()) }
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

        composeTestRule.onNodeWithTag("${testErrorState.textResID}").assertIsDisplayed()

        val buttonText = composeTestRule.activity.getString(R.string.scanError_close)
        composeTestRule.onNodeWithText(buttonText).performClick()

        verify(exactly = 1) { mockViewModel.onErrorDialogButtonTapped(any()) }
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

        composeTestRule.onNodeWithTag("${testErrorState.textResID}").assertIsDisplayed()

        val buttonText = composeTestRule.activity.getString(R.string.scanError_close)
        composeTestRule.onNodeWithText(buttonText).performClick()

        verify(exactly = 1) { mockViewModel.onErrorDialogButtonTapped(any()) }
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

        composeTestRule.onNodeWithTag("${testErrorState.textResID}").assertIsDisplayed()

        val buttonText = composeTestRule.activity.getString(R.string.scanError_close)
        composeTestRule.onNodeWithText(buttonText).performClick()

        verify(exactly = 1) { mockViewModel.onErrorDialogButtonTapped(any()) }
    }

    @Test
    fun openErrorDialogAndConfirmWithButton_ScanErrorCardErrorWithRedirect() {
        val testErrorState = ScanError.CardErrorWithRedirect("")

        val mockViewModel: IdentificationScanViewModel = mockk(relaxed = true)
        every { mockViewModel.errorState } returns testErrorState

        composeTestRule.activity.setContent {
            IdentificationScan(viewModel = mockViewModel)
        }

        val boxTitleText = composeTestRule.activity.getString(R.string.scanError_box_title)
        composeTestRule.onNodeWithText(boxTitleText).assertIsDisplayed()

        val boxBodyText = composeTestRule.activity.getString(R.string.scanError_box_body)
        composeTestRule.onNodeWithText(boxBodyText).assertIsDisplayed()

        val errorDialogTitleText = composeTestRule.activity.getString(testErrorState.titleResID)
        composeTestRule.onNodeWithText(errorDialogTitleText).assertIsDisplayed()

        composeTestRule.onNodeWithTag("${testErrorState.textResID}").assertIsDisplayed()

        val buttonText = composeTestRule.activity.getString(R.string.scanError_redirect)
        composeTestRule.onNodeWithText(buttonText).performClick()

        verify(exactly = 1) { mockViewModel.onErrorDialogButtonTapped(any()) }
    }

    @Test
    fun openErrorDialogAndConfirmWithButton_ScanErrorCardErrorWithoutRedirect() {
        val testErrorState = ScanError.CardErrorWithoutRedirect

        val mockViewModel: IdentificationScanViewModel = mockk(relaxed = true)
        every { mockViewModel.errorState } returns testErrorState

        composeTestRule.activity.setContent {
            IdentificationScan(viewModel = mockViewModel)
        }

        val boxTitleText = composeTestRule.activity.getString(R.string.scanError_box_title)
        composeTestRule.onNodeWithText(boxTitleText).assertIsDisplayed()

        val boxBodyText = composeTestRule.activity.getString(R.string.scanError_box_body)
        composeTestRule.onNodeWithText(boxBodyText).assertIsDisplayed()

        val errorDialogTitleText = composeTestRule.activity.getString(testErrorState.titleResID)
        composeTestRule.onNodeWithText(errorDialogTitleText).assertIsDisplayed()

        composeTestRule.onNodeWithTag("${testErrorState.textResID}").assertIsDisplayed()

        val buttonText = composeTestRule.activity.getString(R.string.scanError_close)
        composeTestRule.onNodeWithText(buttonText).performClick()

        verify(exactly = 1) { mockViewModel.onErrorDialogButtonTapped(any()) }
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

        val cancelDialogTitle = composeTestRule.activity.getString(R.string.identification_scan_cancelDialog_title)
        composeTestRule.onNodeWithText(cancelDialogTitle).assertDoesNotExist()

        val cancelButtonTag = "Cancel"
        composeTestRule.onAllNodesWithTag(cancelButtonTag)[1].performClick()

        composeTestRule.onNodeWithText(cancelDialogTitle).assertIsDisplayed()

        val confirmButton = composeTestRule.activity.getString(R.string.identification_scan_cancelDialog_confirm)
        composeTestRule.onNodeWithText(confirmButton).performClick()

        verify(exactly = 1) { mockViewModel.onCancelIdentification() }

        val dismissButton = composeTestRule.activity.getString(R.string.identification_scan_cancelDialog_dismiss)
        composeTestRule.onNodeWithText(dismissButton).performClick()

        composeTestRule.onNodeWithText(cancelDialogTitle).assertDoesNotExist()
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

        val cancelDialogTitle = composeTestRule.activity.getString(R.string.identification_scan_cancelDialog_title)
        composeTestRule.onNodeWithText(cancelDialogTitle).assertDoesNotExist()

        val cancelButtonTag = "Cancel"
        composeTestRule.onNodeWithTag(cancelButtonTag).performClick()

        composeTestRule.onNodeWithText(cancelDialogTitle).assertIsDisplayed()

        val confirmButton = composeTestRule.activity.getString(R.string.identification_scan_cancelDialog_confirm)
        composeTestRule.onNodeWithText(confirmButton).performClick()

        verify(exactly = 1) { mockViewModel.onCancelIdentification() }

        val dismissButton = composeTestRule.activity.getString(R.string.identification_scan_cancelDialog_dismiss)
        composeTestRule.onNodeWithText(dismissButton).performClick()

        composeTestRule.onNodeWithText(cancelDialogTitle).assertDoesNotExist()
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

    @Test
    fun whatIsNfcDialogOpen() {
        val mockViewModel: SetupScanViewModelInterface = mockk(relaxed = true)
        every { mockViewModel.errorState } returns null

        composeTestRule.activity.setContent {
            SetupScan(viewModel = mockViewModel)
        }

        val whatIsNfcButton = composeTestRule.activity.getString(R.string.scan_helpNFC)
        composeTestRule.onNodeWithText(whatIsNfcButton).performClick()

        val whatIsNfcDialogTitle = composeTestRule.activity.getString(R.string.helpNFC_body)
        composeTestRule.onNodeWithText(whatIsNfcDialogTitle).assertIsDisplayed()

        val dialogCloseButton = composeTestRule.activity.getString(R.string.scanError_close)
        composeTestRule.onNodeWithText(dialogCloseButton).performClick()

        composeTestRule.onNodeWithText(whatIsNfcDialogTitle).assertDoesNotExist()

        verify(exactly = 1) { mockViewModel.startSettingPIN(any()) }
    }
}
