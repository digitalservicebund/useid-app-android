package de.digitalService.useID.nfcDialogTests

import android.nfc.NfcAdapter
import androidx.activity.compose.setContent
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
import de.digitalService.useID.ui.coordinators.AppCoordinator
import de.digitalService.useID.ui.navigation.Navigator
import de.digitalService.useID.util.NfcAdapterUtil
import de.digitalService.useID.util.setContentUsingUseIdTheme
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@HiltAndroidTest
class NfcDeactivatedTest {

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    private val mockNfcAdapter: NfcAdapter = mockk(relaxUnitFun = true) {
        every { isEnabled } returns false
    }

    @BindValue
    val mockNfcAdapterUtil: NfcAdapterUtil = mockk(relaxUnitFun = true) {
        every { getNfcAdapter() } returns mockNfcAdapter
    }

    @BindValue
    val mockTrackerManager: TrackerManager = mockk(relaxed = true)

    @Inject
    lateinit var mockNavigator: Navigator

    @Inject
    lateinit var appCoordinator: AppCoordinator

    @Before
    fun before() {
        hiltRule.inject()
    }

    @Test
    fun nfcDeactivatedTest() {
        composeTestRule.activity.setContentUsingUseIdTheme {
            UseIDApp(appCoordinator, mockNavigator, mockTrackerManager)
        }

        val nfcDialogTitle = composeTestRule.activity.getString(R.string.nfcDeactivated_info_title)
        composeTestRule.onNodeWithText(nfcDialogTitle).assertIsDisplayed()

        Assert.assertEquals(NfcAvailability.Deactivated, appCoordinator.nfcAvailability.value)
    }
}
