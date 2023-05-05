package de.digitalService.useID.userFlowTests.setupFlows.success

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.espresso.intent.matcher.IntentMatchers.*
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
import de.digitalService.useID.idCardInterface.*
import de.digitalService.useID.models.NfcAvailability
import de.digitalService.useID.ui.UseIDApp
import de.digitalService.useID.ui.coordinators.AppCoordinatorType
import de.digitalService.useID.ui.navigation.Navigator
import de.digitalService.useID.userFlowTests.utils.TestScreen
import de.digitalService.useID.userFlowTests.utils.flowParts.setup.helper.runSetupUpToCan
import de.digitalService.useID.userFlowTests.utils.flowParts.setup.runSetupSuccessful
import de.digitalService.useID.util.*
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject


@UninstallModules(SingletonModule::class, CoroutineContextProviderModule::class, NfcInterfaceMangerModule::class)
@HiltAndroidTest
class SetupSuccessfulAfterBlockingCardTest {

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

    @Inject
    lateinit var appCoordinator: AppCoordinatorType

    @BindValue
    val mockEidInteractionManager: EidInteractionManager = mockk(relaxed = true)

    @BindValue
    val mockStorageManager: StorageManager = mockk(relaxed = true) {
        every { firstTimeUser } returns false
    }

    @BindValue
    val mockCoroutineContextProvider: CoroutineContextProviderType = mockk {
        every { Main } returns Dispatchers.Main
    }

    @BindValue
    val mockNfcInterfaceManager: NfcInterfaceManagerType = mockk(relaxed = true) {
        every { nfcAvailability } returns MutableStateFlow(NfcAvailability.Available)
    }

    @Before
    fun before() {
        hiltRule.inject()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun testSetupSuccessfulAfterCardBlockedError() = runTest {
        every { mockCoroutineContextProvider.IO } returns StandardTestDispatcher(testScheduler)
        every { mockCoroutineContextProvider.Default } returns StandardTestDispatcher(testScheduler)

        val eidFlow = MutableStateFlow<EidInteractionEvent>(EidInteractionEvent.Idle)
        every { mockEidInteractionManager.eidFlow } returns eidFlow

        composeTestRule.activity.setContentUsingUseIdTheme {
            UseIDApp(
                nfcAvailability = NfcAvailability.Available,
                navigator = navigator,
                trackerManager = trackerManager
            )
        }

        val wrongTransportPin = "11111"
        val can = "654321"

        // Define screens to be tested
        val setupTransportPin = TestScreen.SetupTransportPin(composeTestRule)
        val setupScan = TestScreen.Scan(composeTestRule)
        val setupCanConfirmTransportPin = TestScreen.SetupCanConfirmTransportPin(composeTestRule)
        val setupCanAlreadySetup = TestScreen.SetupCanAlreadySetup(composeTestRule)
        val setupCanIntro = TestScreen.CanIntro(composeTestRule)
        val setupCanInput = TestScreen.CanInput(composeTestRule)
        val setupErrorCardBlocked = TestScreen.ErrorCardBlocked(composeTestRule)
        val home = TestScreen.Home(composeTestRule)

        home.assertIsDisplayed()
        home.setupButton.click()
        advanceUntilIdle()

        runSetupUpToCan(
            testRule = composeTestRule,
            eidFlow = eidFlow,
            testScope = this
        )

        setupCanConfirmTransportPin.setTransportPin(wrongTransportPin).assertIsDisplayed()
        setupCanConfirmTransportPin.inputCorrectBtn.click()
        advanceUntilIdle()

        setupCanAlreadySetup.assertIsDisplayed()
        setupCanAlreadySetup.back.click()
        advanceUntilIdle()

        setupCanConfirmTransportPin.setTransportPin(wrongTransportPin).assertIsDisplayed()
        setupCanConfirmTransportPin.retryInputBtn.click()
        advanceUntilIdle()

        setupCanIntro.setBackAllowed(true).assertIsDisplayed()
        setupCanIntro.enterCanNowBtn.click()
        advanceUntilIdle()

        // ENTER CORRECT CAN
        setupCanInput.assertIsDisplayed()
        setupCanInput.canEntryField.assertLength(0)
        composeTestRule.performPinInput(can)
        setupCanInput.canEntryField.assertLength(can.length)
        composeTestRule.pressReturn()
        advanceUntilIdle()

        // ENTER WRONG TRANSPORT PIN THIRD TIME
        setupTransportPin.setAttemptsLeft(1).assertIsDisplayed()
        setupTransportPin.transportPinField.assertLength(0)
        composeTestRule.performPinInput(wrongTransportPin)
        setupTransportPin.transportPinField.assertLength(wrongTransportPin.length)
        composeTestRule.pressReturn()

        advanceUntilIdle()

        eidFlow.value = EidInteractionEvent.CardInsertionRequested
        advanceUntilIdle()

        setupScan.setBackAllowed(false).setProgress(false).assertIsDisplayed()

        eidFlow.value = EidInteractionEvent.CardRecognized
        advanceUntilIdle()

        setupScan.setProgress(true).assertIsDisplayed()

        eidFlow.value = EidInteractionEvent.PukRequested
        advanceUntilIdle()

        setupErrorCardBlocked.assertIsDisplayed()
        setupErrorCardBlocked.closeBtn.click()
        advanceUntilIdle()

        home.assertIsDisplayed()
        home.setupButton.click()
        advanceUntilIdle()

        runSetupSuccessful(
            testRule = composeTestRule,
            eidFlow = eidFlow,
            testScope = this
        )
    }
}
