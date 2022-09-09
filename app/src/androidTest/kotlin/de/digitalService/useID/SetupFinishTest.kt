package de.digitalService.useID

import androidx.activity.compose.setContent
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
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
class SetupFinishTest {

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @BindValue
    val mockNfcAdapterUtil: NfcAdapterUtil = MockNfcAdapterUtil()

    @Test
    fun setupFinish_identificationPendingFalse() {
        val mockViewModel: SetupFinishViewModelInterface = mockk(relaxed = true)
        every { mockViewModel.identificationPending() } returns false

        composeTestRule.activity.setContent {
            SetupFinish(viewModel = mockViewModel)
        }

        val finishButtonText = composeTestRule.activity.getString(R.string.firstTimeUser_done_close)
        composeTestRule.onNodeWithText(finishButtonText).performClick()

        verify(exactly = 1) { mockViewModel.onButtonTapped() }
    }


    @Test
    fun setupFinish_identificationPendingTrue() {
        val mockViewModel: SetupFinishViewModelInterface = mockk(relaxed = true)
        every { mockViewModel.identificationPending() } returns true

        composeTestRule.activity.setContent {
            SetupFinish(viewModel = mockViewModel)
        }

        val finishButtonText = composeTestRule.activity.getString(R.string.firstTimeUser_done_identify)
        composeTestRule.onNodeWithText(finishButtonText).performClick()

        verify(exactly = 1) { mockViewModel.onButtonTapped() }
    }
}
