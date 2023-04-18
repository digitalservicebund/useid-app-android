package de.digitalService.useID.userFlowTests.identFlows.can.successful

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.espresso.intent.Intents.intending
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
import de.digitalService.useID.userFlowTests.setupFlows.TestScreen
import de.digitalService.useID.userFlowTests.utils.flowParts.ident.helper.runIdentUpToCan
import de.digitalService.useID.util.*
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.hamcrest.Matchers.allOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject


@UninstallModules(SingletonModule::class, CoroutineContextProviderModule::class, NfcInterfaceMangerModule::class)
@HiltAndroidTest
class IdentCanSuccessfulAfterCanIncorrectTwoTimesTest {

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
    val mockIdCardManager: IdCardManager = mockk(relaxed = true)

    @BindValue
    val mockStorageManager: StorageManager = mockk(relaxed = true) {
        every { firstTimeUser } returns false
    }

    @BindValue
    val mockCoroutineContextProvider: CoroutineContextProviderType = mockk {
        every { Main } returns Dispatchers.Main
    }

    @BindValue
    val mockNfcInterfaceManager: NfcInterfaceManagerType = mockk(relaxed = true){
        every { nfcAvailability } returns MutableStateFlow(NfcAvailability.Available)
    }

    @Before
    fun before() {
        hiltRule.inject()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun testIdentCanSuccessfulAfterCanIncorrectTwoTimes() = runTest {
        every { mockCoroutineContextProvider.IO } returns StandardTestDispatcher(testScheduler)
        every { mockCoroutineContextProvider.Default } returns StandardTestDispatcher(testScheduler)

        val eidFlow = MutableStateFlow<EidInteractionEvent>(EidInteractionEvent.Idle)
        every { mockIdCardManager.eidFlow } returns eidFlow

        composeTestRule.activity.setContentUsingUseIdTheme {
            UseIDApp(
                nfcAvailability = NfcAvailability.Available,
                navigator = navigator,
                trackerManager = trackerManager
            )
        }

        val deepLink = Uri.parse("bundesident://127.0.0.1:24727/eID-Client?tcTokenURL=https%3A%2F%2Feid.digitalservicebund.de%2Fapi%2Fv1%2Fidentification%2Fsessions%2F30d20d97-cf31-4f01-ab27-35dea918bb83%2Ftc-token")
        val redirectUrl = "test.url.com"
        val personalPin = "123456"
        val can = "123456"
        val wrongCan = "222222"

        // Define screens to be tested
        val identificationPersonalPin = TestScreen.IdentificationPersonalPin(composeTestRule)
        val identificationScan = TestScreen.Scan(composeTestRule)
        val identificationCanPinForgotten = TestScreen.IdentificationCanPinForgotten(composeTestRule)
        val identificationCanIntro = TestScreen.CanIntro(composeTestRule)
        val identificationCanInput = TestScreen.CanInput(composeTestRule)

        composeTestRule.waitForIdle()

        appCoordinator.handleDeepLink(deepLink)
        advanceUntilIdle()

        runIdentUpToCan(
            testRule = composeTestRule,
            eidFlow = eidFlow,
            testScope = this
        )

        identificationCanPinForgotten.assertIsDisplayed()
        identificationCanPinForgotten.tryAgainBtn.click()

        advanceUntilIdle()

        identificationCanIntro.setBackAllowed(true).setIdentPending(true).assertIsDisplayed()
        identificationCanIntro.enterCanNowBtn.click()

        advanceUntilIdle()

        // ENTER WRONG CAN
        identificationCanInput.assertIsDisplayed()
        identificationCanInput.canEntryField.assertLength(0)
        composeTestRule.performPinInput(wrongCan)
        identificationCanInput.canEntryField.assertLength(wrongCan.length)
        composeTestRule.pressReturn()

        advanceUntilIdle()

        // ENTER CORRECT PIN 3RD TIME
        identificationPersonalPin.setAttemptsLeft(1).assertIsDisplayed()
        identificationPersonalPin.personalPinField.assertLength(0)
        composeTestRule.performPinInput(personalPin)
        identificationPersonalPin.personalPinField.assertLength(personalPin.length)
        composeTestRule.pressReturn()

        eidFlow.value = EidInteractionEvent.CardInsertionRequested
        advanceUntilIdle()

        identificationScan
            .setIdentPending(true)
            .setBackAllowed(false)
            .setProgress(false)
            .assertIsDisplayed()

        eidFlow.value = EidInteractionEvent.CardRecognized
        advanceUntilIdle()

        identificationScan.setProgress(true).assertIsDisplayed()

        eidFlow.value = EidInteractionEvent.CanRequested
        advanceUntilIdle()

        eidFlow.value = EidInteractionEvent.CardRemoved
        advanceUntilIdle()

        // ENTER WRONG CAN 2ND TIME
        identificationCanInput.setRetry(true).assertIsDisplayed()
        identificationCanInput.canEntryField.assertLength(0)
        composeTestRule.performPinInput(wrongCan)
        identificationCanInput.canEntryField.assertLength(wrongCan.length)
        composeTestRule.pressReturn()

        eidFlow.value = EidInteractionEvent.CardInsertionRequested
        advanceUntilIdle()

        identificationScan
            .setIdentPending(true)
            .setBackAllowed(false)
            .setProgress(false)
            .assertIsDisplayed()

        eidFlow.value = EidInteractionEvent.CardRecognized
        advanceUntilIdle()

        identificationScan.setProgress(true).assertIsDisplayed()

        eidFlow.value = EidInteractionEvent.CanRequested
        advanceUntilIdle()

        eidFlow.value = EidInteractionEvent.CardRemoved
        advanceUntilIdle()

        // ENTER CORRECT CAN
        identificationCanInput.setRetry(true).assertIsDisplayed()
        identificationCanInput.canEntryField.assertLength(0)
        composeTestRule.performPinInput(can)
        identificationCanInput.canEntryField.assertLength(can.length)
        composeTestRule.pressReturn()

        eidFlow.value = EidInteractionEvent.CardInsertionRequested
        advanceUntilIdle()

        identificationScan
            .setIdentPending(true)
            .setBackAllowed(false)
            .setProgress(false)
            .assertIsDisplayed()

        eidFlow.value = EidInteractionEvent.CardRecognized
        advanceUntilIdle()

        identificationScan.setProgress(true).assertIsDisplayed()

        eidFlow.value = EidInteractionEvent.PinRequested(1)
        advanceUntilIdle()

        intending(allOf(
            hasAction(Intent.ACTION_VIEW),
            hasData(redirectUrl),
            hasFlag(Intent.FLAG_ACTIVITY_NEW_TASK)
        )).respondWith(
            Instrumentation.ActivityResult(
                Activity.RESULT_OK,
                null
            )
        )

        eidFlow.value = EidInteractionEvent.AuthenticationSucceededWithRedirect(redirectUrl)
        advanceUntilIdle()
    }
}
