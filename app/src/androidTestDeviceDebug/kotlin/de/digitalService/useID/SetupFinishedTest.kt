package de.digitalService.useID

import androidx.activity.compose.setContent
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.espresso.Espresso
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import de.digitalService.useID.ui.components.NavigationIcon
import de.digitalService.useID.ui.screens.setup.SetupFinish
import de.digitalService.useID.ui.screens.setup.SetupFinishViewModelInterface
import de.digitalService.useID.util.MockNfcAdapterUtil
import de.digitalService.useID.util.NfcAdapterUtil
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class SetupFinishedTest {

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @BindValue
    val mockNfcAdapterUtil: NfcAdapterUtil = MockNfcAdapterUtil()

    @Test
    fun identificationPending() {
        val viewModel: SetupFinishViewModelInterface = mockk(relaxUnitFun = true)

        every { viewModel.identificationPending() } returns true

        composeTestRule.activity.setContent {
            SetupFinish(viewModel = viewModel)
        }

        val wantedButtonLabel = composeTestRule.activity.getString(R.string.firstTimeUser_done_identify)
        composeTestRule.onNodeWithText(wantedButtonLabel).assertIsDisplayed()

        val notWantedButtonLabel = composeTestRule.activity.getString(R.string.firstTimeUser_done_close)
        composeTestRule.onNodeWithText(notWantedButtonLabel).assertDoesNotExist()

        composeTestRule.onNodeWithTag(NavigationIcon.Back.name).assertDoesNotExist()
        composeTestRule.onNodeWithTag(NavigationIcon.Cancel.name).assertDoesNotExist()

        composeTestRule.onNodeWithText(wantedButtonLabel).performClick()
        verify(exactly = 1) { viewModel.onButtonClicked() }
    }

    @Test
    fun noIdentificationPending() {
        val viewModel: SetupFinishViewModelInterface = mockk(relaxUnitFun = true)

        every { viewModel.identificationPending() } returns false

        composeTestRule.activity.setContent {
            SetupFinish(viewModel = viewModel)
        }

        val wantedButtonLabel = composeTestRule.activity.getString(R.string.firstTimeUser_done_close)
        composeTestRule.onNodeWithText(wantedButtonLabel).assertIsDisplayed()

        val notWantedButtonLabel = composeTestRule.activity.getString(R.string.firstTimeUser_done_identify)
        composeTestRule.onNodeWithText(notWantedButtonLabel).assertDoesNotExist()

        composeTestRule.onNodeWithTag(NavigationIcon.Back.name).assertDoesNotExist()
        composeTestRule.onNodeWithTag(NavigationIcon.Cancel.name).performClick()
        verify(exactly = 1) { viewModel.onButtonClicked() }

        composeTestRule.onNodeWithText(wantedButtonLabel).performClick()
        verify(exactly = 2) { viewModel.onButtonClicked() }

        composeTestRule.onNodeWithText(wantedButtonLabel).performClick()
        verify(exactly = 3) { viewModel.onButtonClicked() }

        Espresso.pressBack()

        verify(exactly = 4) { viewModel.onButtonClicked() }
    }
}
