package de.digitalService.useID

import androidx.activity.compose.setContent
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import de.digitalService.useID.ui.components.NavigationIcon
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
    fun hasBackNavigation() {
        val viewModel: IdentificationFetchMetadataViewModelInterface = mockk(relaxUnitFun = true)

        every { viewModel.didSetup } returns true

        composeTestRule.activity.setContent {
            IdentificationFetchMetadata(viewModel = viewModel)
        }

        composeTestRule.onNodeWithTag("ProgressIndicator").assertIsDisplayed()

        verify(exactly = 1) { viewModel.startIdentificationProcess() }

        composeTestRule.onNodeWithTag(NavigationIcon.Back.name).performClick()

        val cancelButton = composeTestRule.activity.getString(R.string.navigation_cancel)
        composeTestRule.onNodeWithText(cancelButton).assertDoesNotExist()

        verify(exactly = 1) { viewModel.onCancelButtonTapped() }
    }

    @Test
    fun hasCancelNavigation() {
        val viewModel: IdentificationFetchMetadataViewModelInterface = mockk(relaxUnitFun = true)

        every { viewModel.didSetup } returns false

        composeTestRule.activity.setContent {
            IdentificationFetchMetadata(viewModel = viewModel)
        }

        composeTestRule.onNodeWithTag("ProgressIndicator").assertIsDisplayed()

        verify(exactly = 1) { viewModel.startIdentificationProcess() }

        composeTestRule.onNodeWithTag(NavigationIcon.Cancel.name).performClick()

        val cancelButton = composeTestRule.activity.getString(R.string.navigation_cancel)
        composeTestRule.onNodeWithText(cancelButton).performClick()

        verify(exactly = 1) { viewModel.onCancelButtonTapped() }
    }
}
