package de.digitalService.useID

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import de.digitalService.useID.ui.screens.setup.SetupScan
import de.digitalService.useID.ui.screens.setup.SetupScanViewModelInterface
import de.digitalService.useID.util.setContentUsingUseIdTheme
import io.mockk.mockk
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class SetupScanTest {

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun whatIsNfcDialogOpen() {
        val mockViewModel: SetupScanViewModelInterface = mockk(relaxed = true)

        composeTestRule.activity.setContentUsingUseIdTheme {
            SetupScan(viewModel = mockViewModel)
        }

        val whatIsNfcButton = composeTestRule.activity.getString(R.string.scan_helpNFC)
        composeTestRule.onNodeWithText(whatIsNfcButton).performClick()

        val whatIsNfcDialogTitle = composeTestRule.activity.getString(R.string.helpNFC_body)
        composeTestRule.onNodeWithText(whatIsNfcDialogTitle).assertIsDisplayed()

        val dialogCloseButton = composeTestRule.activity.getString(R.string.scanError_close)
        composeTestRule.onNodeWithText(dialogCloseButton).performClick()

        composeTestRule.onNodeWithText(whatIsNfcDialogTitle).assertDoesNotExist()
    }
}
