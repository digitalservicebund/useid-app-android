package de.digitalService.useID.userFlowTests.setupAndIdentFlows.successful

import android.net.Uri
import androidx.compose.ui.test.junit4.createAndroidComposeRule
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
import de.digitalService.useID.idCardInterface.EidInteractionEvent
import de.digitalService.useID.idCardInterface.EidInteractionManager
import de.digitalService.useID.models.NfcAvailability
import de.digitalService.useID.ui.UseIDApp
import de.digitalService.useID.ui.coordinators.AppCoordinatorType
import de.digitalService.useID.ui.navigation.Navigator
import de.digitalService.useID.userFlowTests.utils.TestScreen
import de.digitalService.useID.userFlowTests.utils.flowParts.ident.*
import de.digitalService.useID.userFlowTests.utils.flowParts.setup.runSetupSuccessfulAfterTransportPinWrongOnceAndThenCorrect
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
class SetupSuccessfulAfterTransportPinWrongOnceAndDifferentIdentFlowsTest {

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
        every { firstTimeUser } returns true
    }

    @BindValue
    val mockCoroutineContextProvider: CoroutineContextProviderType = mockk {
        every { Main } returns Dispatchers.Main
    }

    @BindValue
    val mockNfcInterfaceManager: NfcInterfaceManagerType = mockk(relaxed = true){
        every { nfcAvailability } returns MutableStateFlow(NfcAvailability.Available)
    }

    private val deepLink = Uri.parse("bundesident://127.0.0.1:24727/eID-Client?tcTokenURL=https%3A%2F%2Feid.digitalservicebund.de%2Fapi%2Fv1%2Fidentification%2Fsessions%2F30d20d97-cf31-4f01-ab27-35dea918bb83%2Ftc-token")

    @Before
    fun before() {
        hiltRule.inject()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun testSuccessfulSetupAndIdentAfterTransportPinWrongOnce() = runTest {
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

        val setupFinish = TestScreen.SetupFinish(composeTestRule)

        composeTestRule.waitForIdle()

        appCoordinator.handleDeepLink(deepLink)
        advanceUntilIdle()

        runSetupSuccessfulAfterTransportPinWrongOnceAndThenCorrect(
            testRule = composeTestRule,
            eidFlow = eidFlow,
            testScope = this
        )

        setupFinish.setIdentPending(true).assertIsDisplayed()
        setupFinish.identifyNowBtn.click()

        runIdentSuccessful(
            testRule = composeTestRule,
            eidFlow = eidFlow,
            testScope = this
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun testSuccessfulSetupAndIdentAfterTransportPinWrongOnceAndPersonalPinWrongOnce() = runTest {
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

        val setupFinish = TestScreen.SetupFinish(composeTestRule)

        composeTestRule.waitForIdle()

        appCoordinator.handleDeepLink(deepLink)
        advanceUntilIdle()

        runSetupSuccessfulAfterTransportPinWrongOnceAndThenCorrect(
            testRule = composeTestRule,
            eidFlow = eidFlow,
            testScope = this
        )

        setupFinish.setIdentPending(true).assertIsDisplayed()
        setupFinish.identifyNowBtn.click()

        runIdentSuccessfulAfterPersonalPinIncorrectAndThenCorrect(
            testRule = composeTestRule,
            eidFlow = eidFlow,
            testScope = this
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun testSuccessfulSetupAndIdentAfterTransportPinWrongOnceAndPersonalPinIncorrectTwice() = runTest {
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

        val setupFinish = TestScreen.SetupFinish(composeTestRule)

        composeTestRule.waitForIdle()

        appCoordinator.handleDeepLink(deepLink)
        advanceUntilIdle()

        runSetupSuccessfulAfterTransportPinWrongOnceAndThenCorrect(
            testRule = composeTestRule,
            eidFlow = eidFlow,
            testScope = this
        )

        setupFinish.setIdentPending(true).assertIsDisplayed()
        setupFinish.identifyNowBtn.click()

        runIdentSuccessfulCan(
            testRule = composeTestRule,
            eidFlow = eidFlow,
            testScope = this
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun testSuccessfulSetupAndIdentAfterTransportPinWrongOnceAndCanIncorrect() = runTest {
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

        val setupFinish = TestScreen.SetupFinish(composeTestRule)

        composeTestRule.waitForIdle()

        appCoordinator.handleDeepLink(deepLink)
        advanceUntilIdle()

        runSetupSuccessfulAfterTransportPinWrongOnceAndThenCorrect(
            testRule = composeTestRule,
            eidFlow = eidFlow,
            testScope = this
        )

        setupFinish.setIdentPending(true).assertIsDisplayed()
        setupFinish.identifyNowBtn.click()

        runIdentSuccessfulAfterCanIncorrectOnceAndThenCorrect(
            testRule = composeTestRule,
            eidFlow = eidFlow,
            testScope = this
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun testSuccessfulSetupAndIdentAfterTransportPinWrongOnceAndCanIncorrectMultipleTimes() = runTest {
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

        val setupFinish = TestScreen.SetupFinish(composeTestRule)

        composeTestRule.waitForIdle()

        appCoordinator.handleDeepLink(deepLink)
        advanceUntilIdle()

        runSetupSuccessfulAfterTransportPinWrongOnceAndThenCorrect(
            testRule = composeTestRule,
            eidFlow = eidFlow,
            testScope = this
        )

        setupFinish.setIdentPending(true).assertIsDisplayed()
        setupFinish.identifyNowBtn.click()

        runIdentSuccessfulAfterCanIncorrectMultipleTimesAndThenCorrect(
            testRule = composeTestRule,
            eidFlow = eidFlow,
            testScope = this
        )
    }
}
