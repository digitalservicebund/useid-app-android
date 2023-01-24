package de.digitalService.useID.nfcDialogTests

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import de.digitalService.useID.MainActivity
import de.digitalService.useID.R
import de.digitalService.useID.analytics.TrackerManager
import de.digitalService.useID.models.NfcAvailability
import de.digitalService.useID.ui.UseIDApp
import de.digitalService.useID.ui.navigation.Navigator
import de.digitalService.useID.util.setContentUsingUseIdTheme
import io.mockk.mockk
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@HiltAndroidTest
class NfcAvailabilityTest {

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @BindValue
    val mockTrackerManager: TrackerManager = mockk(relaxed = true)

    @Inject
    lateinit var mockNavigator: Navigator

    @Before
    fun before() {
        hiltRule.inject()
    }

    @Test
    fun nfcAvailable() {
        composeTestRule.activity.setContentUsingUseIdTheme {
            UseIDApp(NfcAvailability.Available, mockNavigator, mockTrackerManager)
        }

        val nfcDialogTitle1 = composeTestRule.activity.getString(R.string.noNfc_info_title)
        composeTestRule.onNodeWithText(nfcDialogTitle1).assertDoesNotExist()

        val nfcDialogTitle2 = composeTestRule.activity.getString(R.string.nfcDeactivated_info_title)
        composeTestRule.onNodeWithText(nfcDialogTitle2).assertDoesNotExist()
    }

    @Test
    fun nfcNotAvailable() {
        composeTestRule.activity.setContentUsingUseIdTheme {
            UseIDApp(NfcAvailability.NoNfc, mockNavigator, mockTrackerManager)
        }

        val nfcDialogTitle = composeTestRule.activity.getString(R.string.noNfc_info_title)
        composeTestRule.onNodeWithText(nfcDialogTitle).assertIsDisplayed()
    }

    @Test
    fun nfcDeactivated() {
        composeTestRule.activity.setContentUsingUseIdTheme {
            UseIDApp(NfcAvailability.Deactivated, mockNavigator, mockTrackerManager)
        }

        val nfcDialogTitle = composeTestRule.activity.getString(R.string.nfcDeactivated_info_title)
        composeTestRule.onNodeWithText(nfcDialogTitle).assertIsDisplayed()
    }
}
