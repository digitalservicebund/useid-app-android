package de.digitalService.useID.userFlowTests

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import android.provider.Settings
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.espresso.Espresso
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.intent.rule.IntentsTestRule
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import de.digitalService.useID.MainActivity
import de.digitalService.useID.StorageManager
import de.digitalService.useID.analytics.TrackerManagerType
import de.digitalService.useID.hilt.CoroutineContextProviderModule
import de.digitalService.useID.hilt.NfcInterfaceMangerModule
import de.digitalService.useID.hilt.SingletonModule
import de.digitalService.useID.idCardInterface.IdCardManager
import de.digitalService.useID.models.NfcAvailability
import de.digitalService.useID.ui.UseIDApp
import de.digitalService.useID.ui.components.NavigationIcon
import de.digitalService.useID.ui.navigation.Navigator
import de.digitalService.useID.userFlowTests.setupFlows.TestScreen
import de.digitalService.useID.util.*
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.hamcrest.Matchers
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@HiltAndroidTest
class NfcTest {

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val intentsTestRule = IntentsTestRule(MainActivity::class.java)

    @get:Rule(order = 2)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Inject
    lateinit var navigator: Navigator

    @Inject
    lateinit var trackerManager: TrackerManagerType

    @BindValue
    val mockStorageManager: StorageManager = mockk(relaxed = true) {
        every { firstTimeUser } returns false
    }

    @Before
    fun before() {
        hiltRule.inject()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun testNoNfc() = runTest {
        composeTestRule.activity.setContentUsingUseIdTheme {
            UseIDApp(
                nfcAvailability = NfcAvailability.NoNfc,
                navigator = navigator,
                trackerManager = trackerManager
            )
        }

        val noNfcScreen = TestScreen.NoNfc(composeTestRule)

        noNfcScreen.assertIsDisplayed()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun testNfcDeactivated() = runTest {

        composeTestRule.activity.setContentUsingUseIdTheme {
            UseIDApp(
                nfcAvailability = NfcAvailability.Deactivated,
                navigator = navigator,
                trackerManager = trackerManager
            )
        }

        val nfcDeactivatedScreen = TestScreen.NfcDeactivated(composeTestRule)

        nfcDeactivatedScreen.assertIsDisplayed()

        Intents.intending(
            Matchers.isOneOf(
                Intent(Settings.Panel.ACTION_NFC),
                Intent(Settings.ACTION_SETTINGS)
            )
        ).respondWith(
            Instrumentation.ActivityResult(
                Activity.RESULT_OK,
                null
            )
        )

        nfcDeactivatedScreen.adaptSettingsBtn.click()
    }
}
