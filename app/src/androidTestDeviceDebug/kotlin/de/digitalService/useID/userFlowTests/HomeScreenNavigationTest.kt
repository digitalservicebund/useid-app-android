package de.digitalService.useID.userFlowTests

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import de.digitalService.useID.MainActivity
import de.digitalService.useID.StorageManager
import de.digitalService.useID.analytics.TrackerManagerType
import de.digitalService.useID.hilt.SingletonModule
import de.digitalService.useID.idCardInterface.EidInteractionManager
import de.digitalService.useID.models.NfcAvailability
import de.digitalService.useID.ui.UseIDApp
import de.digitalService.useID.ui.components.NavigationIcon
import de.digitalService.useID.ui.navigation.Navigator
import de.digitalService.useID.userFlowTests.utils.TestScreen
import de.digitalService.useID.util.*
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@UninstallModules(SingletonModule::class)
@HiltAndroidTest
class HomeScreenNavigationTest {

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Inject
    lateinit var navigator: Navigator

    @Inject
    lateinit var trackerManager: TrackerManagerType

    @BindValue
    val mockEidInteractionManager: EidInteractionManager = mockk(relaxed = true)

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
    fun testImprintScreenNavigation() = runTest {

        composeTestRule.activity.setContentUsingUseIdTheme {
            UseIDApp(
                nfcAvailability = NfcAvailability.Available,
                navigator = navigator,
                trackerManager = trackerManager
            )
        }

        val home = TestScreen.Home(composeTestRule)

        home.assertIsDisplayed()
        home.imprintBtn.scrollToAndClick()
        composeTestRule.onNodeWithTag(NavigationIcon.Back.name).performClick()
        home.assertIsDisplayed()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun testAccessibilityScreenNavigation() = runTest {

        composeTestRule.activity.setContentUsingUseIdTheme {
            UseIDApp(
                nfcAvailability = NfcAvailability.Available,
                navigator = navigator,
                trackerManager = trackerManager
            )
        }

        val home = TestScreen.Home(composeTestRule)

        home.assertIsDisplayed()
        home.accessibilityBtn.scrollToAndClick()
        composeTestRule.onNodeWithTag(NavigationIcon.Back.name).performClick()
        home.assertIsDisplayed()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun testPrivacyScreenNavigation() = runTest {

        composeTestRule.activity.setContentUsingUseIdTheme {
            UseIDApp(
                nfcAvailability = NfcAvailability.Available,
                navigator = navigator,
                trackerManager = trackerManager
            )
        }

        val home = TestScreen.Home(composeTestRule)

        home.assertIsDisplayed()
        home.privacyBtn.scrollToAndClick()
        composeTestRule.onNodeWithTag(NavigationIcon.Back.name).performClick()
        home.assertIsDisplayed()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun testTermsOfUseScreenNavigation() = runTest {

        composeTestRule.activity.setContentUsingUseIdTheme {
            UseIDApp(
                nfcAvailability = NfcAvailability.Available,
                navigator = navigator,
                trackerManager = trackerManager
            )
        }

        val home = TestScreen.Home(composeTestRule)

        home.assertIsDisplayed()
        home.termsAndConditionsBtn.scrollToAndClick()
        composeTestRule.onNodeWithTag(NavigationIcon.Back.name).performClick()
        home.assertIsDisplayed()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun testDependenciesScreenNavigation() = runTest {

        composeTestRule.activity.setContentUsingUseIdTheme {
            UseIDApp(
                nfcAvailability = NfcAvailability.Available,
                navigator = navigator,
                trackerManager = trackerManager
            )
        }

        val home = TestScreen.Home(composeTestRule)

        home.assertIsDisplayed()
        home.licensesBtn.scrollToAndClick()
        composeTestRule.onNodeWithTag(NavigationIcon.Back.name).performClick()
        home.assertIsDisplayed()
    }
}
