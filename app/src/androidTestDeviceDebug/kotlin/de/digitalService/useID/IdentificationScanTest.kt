package de.digitalService.useID

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import de.digitalService.useID.ui.screens.identification.IdentificationScan
import de.digitalService.useID.ui.screens.identification.IdentificationScanViewModel
import de.digitalService.useID.ui.screens.identification.IdentificationScanViewModelInterface
import de.digitalService.useID.util.MockNfcAdapterUtil
import de.digitalService.useID.util.NfcAdapterUtil
import de.digitalService.useID.util.setContentUsingUseIdTheme
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

    @Test
    fun testNoDialogIsOpenAfterCancellation() {
        val mockViewModel: IdentificationScanViewModelInterface = mockk(relaxed = true)

        composeTestRule.activity.setContentUsingUseIdTheme {
            IdentificationScan(viewModel = mockViewModel)
        }

        val pinEntryTag = "PINEntryField"
        composeTestRule.onNodeWithText(pinEntryTag).assertDoesNotExist()

        val errorDialogTitleText = composeTestRule.activity.getString(R.string.firstTimeUser_scan_error_title_pin_suspended)
        composeTestRule.onNodeWithText(errorDialogTitleText).assertDoesNotExist()

        val cancelDialogTitle = composeTestRule.activity.getString(R.string.identification_confirmEnd_title)
        composeTestRule.onNodeWithText(cancelDialogTitle).assertDoesNotExist()

        val cancelButtonTag = "Cancel"
        composeTestRule.onNodeWithTag(cancelButtonTag).performClick()

        composeTestRule.onNodeWithText(cancelDialogTitle).assertIsDisplayed()

        val confirmButton = composeTestRule.activity.getString(R.string.identification_confirmEnd_confirm)
        composeTestRule.onNodeWithText(confirmButton).performClick()

        verify(exactly = 1) { mockViewModel.onCancelIdentification() }

        composeTestRule.onNodeWithText(cancelDialogTitle).assertDoesNotExist()
    }

    @Test
    fun testShowProgress() {
        val mockViewModel: IdentificationScanViewModelInterface = mockk(relaxed = true)

        every { mockViewModel.shouldShowProgress } returns true

        composeTestRule.activity.setContentUsingUseIdTheme {
            IdentificationScan(viewModel = mockViewModel)
        }

        val progressIndicatorTag = "ProgressIndicator"
        composeTestRule.onNodeWithTag(progressIndicatorTag).assertIsDisplayed()
    }

    @Test
    fun testWhatIsNfcDialogOpen() {
        val mockViewModel: IdentificationScanViewModelInterface = mockk(relaxed = true)

        composeTestRule.activity.setContentUsingUseIdTheme {
            IdentificationScan(viewModel = mockViewModel)
        }

        val whatIsNfcButton = composeTestRule.activity.getString(R.string.scan_helpNFC)
        composeTestRule.onNodeWithText(whatIsNfcButton).performClick()

        verify(exactly = 1) { mockViewModel.onNfcButtonClicked() }

        val whatIsNfcDialogTitle = composeTestRule.activity.getString(R.string.helpNFC_body)
        composeTestRule.onNodeWithText(whatIsNfcDialogTitle).assertIsDisplayed()

        val dialogCloseButton = composeTestRule.activity.getString(R.string.scanError_close)
        composeTestRule.onNodeWithText(dialogCloseButton).performClick()

        composeTestRule.onNodeWithText(whatIsNfcDialogTitle).assertDoesNotExist()
    }

    @Test
    fun testHelpDialogOpen() {
        val mockViewModel: IdentificationScanViewModelInterface = mockk(relaxed = true)

        composeTestRule.activity.setContentUsingUseIdTheme {
            IdentificationScan(viewModel = mockViewModel)
        }

        val helpButton = composeTestRule.activity.getString(R.string.scan_helpScanning)
        composeTestRule.onNodeWithText(helpButton).performClick()

        verify(exactly = 1) { mockViewModel.onHelpButtonClicked() }

        val helpDialogTitle = composeTestRule.activity.getString(R.string.scanError_cardUnreadable_title)
        composeTestRule.onNodeWithText(helpDialogTitle).assertIsDisplayed()

        val dialogCloseButton = composeTestRule.activity.getString(R.string.scanError_close)
        composeTestRule.onNodeWithText(dialogCloseButton).performClick()

        composeTestRule.onNodeWithText(helpDialogTitle).assertDoesNotExist()
    }
}
