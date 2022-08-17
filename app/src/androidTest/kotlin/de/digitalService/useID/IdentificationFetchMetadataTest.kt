package de.digitalService.useID

import androidx.activity.compose.setContent
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import de.digitalService.useID.ui.screens.identification.IdentificationFetchMetadata
import de.digitalService.useID.ui.screens.identification.IdentificationFetchMetadataViewModelInterface
import de.digitalService.useID.util.MockNfcAdapterUtil
import de.digitalService.useID.util.NfcAdapterUtil
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class IdentificationFetchMetadataTest {

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @BindValue
    val mockNfcAdapterUtil: NfcAdapterUtil = MockNfcAdapterUtil()

    @Test
    fun errorNotShown() {
        val viewModel: IdentificationFetchMetadataViewModelInterface = mockk(relaxUnitFun = true)

        every { viewModel.shouldShowProgressIndicator } returns true
        every { viewModel.shouldShowError } returns false

        composeTestRule.activity.setContent {
            IdentificationFetchMetadata(viewModel = viewModel)
        }

        composeTestRule.waitForIdle()

        verify(exactly = 1) { viewModel.fetchMetadata() }

        val errorDialogTitle = composeTestRule.activity.getString(R.string.identification_fetchMetadataError_title)
        composeTestRule.onNodeWithText(errorDialogTitle).assertDoesNotExist()
    }

    @Test
    fun error() {
        val viewModel: IdentificationFetchMetadataViewModelInterface = mockk(relaxUnitFun = true)

        every { viewModel.shouldShowProgressIndicator } returns true
        every { viewModel.shouldShowError } returns true

        composeTestRule.activity.setContent {
            IdentificationFetchMetadata(viewModel = viewModel)
        }

        composeTestRule.waitForIdle()

        verify(exactly = 1) { viewModel.fetchMetadata() }

        val errorDialogTitle = composeTestRule.activity.getString(R.string.identification_fetchMetadataError_title)
        composeTestRule.onNodeWithText(errorDialogTitle).assertIsDisplayed()

        val errorRetryButtonTitle = composeTestRule.activity.getString(R.string.identification_fetchMetadataError_button)
        composeTestRule.onNodeWithText(errorRetryButtonTitle).performClick()

        verify { viewModel.onErrorRetry() }

        val errorCancelButtonTitle = composeTestRule.activity.getString(R.string.navigation_cancel)
        composeTestRule.onNodeWithContentDescription(errorCancelButtonTitle).performClick()

        verify { viewModel.onErrorCancel() }

    }
}
