package de.digitalService.useID

import androidx.activity.compose.setContent
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import de.digitalService.useID.ui.AppCoordinator
import de.digitalService.useID.ui.screens.identification.IdentificationSuccess
import de.digitalService.useID.ui.screens.identification.IdentificationSuccessViewModel
import de.digitalService.useID.util.MockNfcAdapterUtil
import de.digitalService.useID.util.NfcAdapterUtil
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@HiltAndroidTest
class IdentificationSuccessTest {
    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @BindValue
    val mockNfcAdapterUtil: NfcAdapterUtil = MockNfcAdapterUtil()

    @Inject
    lateinit var appCoordinator: AppCoordinator

    @Before
    fun before() {
        hiltRule.inject()
    }

    @Test
    fun test() {
        val testProvider = "testProvider"

        val mockIdentificationSuccessViewModel: IdentificationSuccessViewModel = mockk(relaxUnitFun = true)

        every { mockIdentificationSuccessViewModel.provider } returns testProvider

        composeTestRule.activity.setContent {
            IdentificationSuccess(viewModel = mockIdentificationSuccessViewModel)
        }

        val button = composeTestRule.activity.getString(R.string.identification_success_button)
        composeTestRule.onNodeWithText(button).performClick()

        composeTestRule.onNodeWithText(text = testProvider, substring = true).assertIsDisplayed()

        verify(exactly = 1) { mockIdentificationSuccessViewModel.onButtonTapped(any()) }
    }
}
