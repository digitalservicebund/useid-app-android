package de.digitalService.useID.nfcDialogTests

import androidx.activity.compose.setContent
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import de.digitalService.useID.MainActivity
import de.digitalService.useID.R
import de.digitalService.useID.models.NfcAvailability
import de.digitalService.useID.ui.AppCoordinator
import de.digitalService.useID.ui.composables.UseIDApp
import de.digitalService.useID.util.NfcAdapterUtil
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@HiltAndroidTest
class NoNfcTest {

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @BindValue
    val mockNfcAdapterUtil: NfcAdapterUtil = mockk(relaxUnitFun = true) {
        every { getNfcAdapter() } returns null
    }

    @Inject
    lateinit var appCoordinator: AppCoordinator

    @Before
    fun before() {
        hiltRule.inject()
    }

    @Test
    fun noNfcTest() {
        composeTestRule.activity.setContent {
            UseIDApp(appCoordinator)
        }

        val nfcDialogTitle = composeTestRule.activity.getString(R.string.noNfc_info_title)
        composeTestRule.onNodeWithText(nfcDialogTitle).assertIsDisplayed()

        Assert.assertEquals(NfcAvailability.NoNfc, appCoordinator.nfcAvailability.value)
    }
}
