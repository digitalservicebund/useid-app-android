package de.digitalService.useID

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.intent.rule.IntentsTestRule
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import de.digitalService.useID.analytics.IssueTrackerManagerType
import de.digitalService.useID.analytics.TrackerManagerType
import de.digitalService.useID.hilt.CoroutineContextProviderModule
import de.digitalService.useID.hilt.NfcInterfaceMangerModule
import de.digitalService.useID.hilt.SingletonModule
import de.digitalService.useID.hilt.TrackerManagerModule
import de.digitalService.useID.idCardInterface.*
import de.digitalService.useID.models.NfcAvailability
import de.digitalService.useID.ui.UseIDApp
import de.digitalService.useID.ui.components.NavigationIcon
import de.digitalService.useID.ui.coordinators.AppCoordinatorType
import de.digitalService.useID.ui.navigation.Navigator
import de.digitalService.useID.userFlowTests.setupFlows.TestScreen
import de.digitalService.useID.userFlowTests.utils.flowParts.ident.helper.runIdentUpToCan
import de.digitalService.useID.userFlowTests.utils.flowParts.setup.helper.runSetupUpToCan
import de.digitalService.useID.util.*
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.hamcrest.Matchers
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.openecard.mobile.activation.ActivationResultCode
import javax.inject.Inject

@UninstallModules(SingletonModule::class, CoroutineContextProviderModule::class, NfcInterfaceMangerModule::class, TrackerManagerModule::class)
@HiltAndroidTest
@ExperimentalCoroutinesApi
class UserTrackingTest {

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val intentsTestRule = IntentsTestRule(MainActivity::class.java)

    @get:Rule(order = 2)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Inject
    lateinit var navigator: Navigator

    @Inject
    lateinit var appCoordinator: AppCoordinatorType

    private val trackingRouteSlot = slot<String>()
    private val trackingEventCategorySlot = slot<String>()
    private val trackingEventActionSlot = slot<String>()
    private val trackingEventNameSlot = slot<String>()
    private val trackingButtonPressedCategorySlot = slot<String>()
    private val trackingButtonPressedNameSlot = slot<String>()

    @BindValue
    val trackerManager: TrackerManagerType = mockk(relaxed = true) {
        every { trackScreen(capture(trackingRouteSlot)) } returns Unit
        every { trackEvent(capture(trackingEventCategorySlot), capture(trackingEventActionSlot), capture(trackingEventNameSlot)) } returns Unit
        every { trackButtonPressed(capture(trackingButtonPressedCategorySlot), capture(trackingButtonPressedNameSlot)) } returns Unit
    }

    @BindValue
    val issueTrackerManager: IssueTrackerManagerType = mockk(relaxUnitFun = true)

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
    val mockNfcInterfaceManager: NfcInterfaceManagerType = mockk(relaxed = true) {
        every { nfcAvailability } returns MutableStateFlow(NfcAvailability.Available)
    }

    @Before
    fun before() {
        hiltRule.inject()
    }

    @Test
    fun metadataScreens() = runTest {
        composeTestRule.activity.setContentUsingUseIdTheme {
            UseIDApp(
                nfcAvailability = NfcAvailability.Available,
                navigator = navigator,
                trackerManager = trackerManager
            )
        }

        val home = TestScreen.Home(composeTestRule)

        composeTestRule.waitForIdle()

        Assert.assertEquals(home.trackingIdentifier, trackingRouteSlot.captured)
        home.imprintBtn.scrollToAndClick()

        composeTestRule.waitForIdle()
        Assert.assertEquals("imprint", trackingRouteSlot.captured)

        composeTestRule.onNodeWithTag(NavigationIcon.Back.name).performClick()
        composeTestRule.waitForIdle()

        home.accessibilityBtn.scrollToAndClick()

        composeTestRule.waitForIdle()
        Assert.assertEquals("accessibility", trackingRouteSlot.captured)

        composeTestRule.onNodeWithTag(NavigationIcon.Back.name).performClick()
        composeTestRule.waitForIdle()

        home.licensesBtn.scrollToAndClick()

        composeTestRule.waitForIdle()
        Assert.assertEquals("thirdPartyDependencies", trackingRouteSlot.captured)

        composeTestRule.onNodeWithTag(NavigationIcon.Back.name).performClick()
        composeTestRule.waitForIdle()

        home.privacyBtn.scrollToAndClick()

        composeTestRule.waitForIdle()
        Assert.assertEquals("privacy", trackingRouteSlot.captured)

        composeTestRule.onNodeWithTag(NavigationIcon.Back.name).performClick()
        composeTestRule.waitForIdle()
    }

    @Test
    fun setup() = runTest {
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

        every { mockStorageManager.firstTimeUser } returns false

        val transportPin = "12345"
        val personalPin = "123456"

        // Define screens to be tested
        val setupIntro = TestScreen.SetupIntro(composeTestRule)
        val setupPinLetter = TestScreen.SetupPinLetter(composeTestRule)
        val resetPin = TestScreen.ResetPersonalPin(composeTestRule)
        val setupTransportPin = TestScreen.SetupTransportPin(composeTestRule)
        val setupPersonalPinIntro = TestScreen.SetupPersonalPinIntro(composeTestRule)
        val setupPersonalPinInput = TestScreen.SetupPersonalPinInput(composeTestRule)
        val setupPersonalPinConfirm = TestScreen.SetupPersonalPinConfirm(composeTestRule)
        val setupScan = TestScreen.Scan(composeTestRule)
        val setupFinish = TestScreen.SetupFinish(composeTestRule)
        val home = TestScreen.Home(composeTestRule)

        composeTestRule.waitForIdle()

        Assert.assertEquals(home.trackingIdentifier, trackingRouteSlot.captured)
        home.setupButton.scrollToAndClick()
        advanceUntilIdle()
        composeTestRule.waitForIdle()

        Assert.assertEquals("firstTimeUser", trackingButtonPressedCategorySlot.captured)
        Assert.assertEquals("start", trackingButtonPressedNameSlot.captured)

        advanceUntilIdle()
        composeTestRule.waitForIdle()

        Assert.assertEquals("firstTimeUser", trackingEventCategorySlot.captured)
        Assert.assertEquals("setupIntroOpened", trackingEventActionSlot.captured)
        Assert.assertEquals("home", trackingEventNameSlot.captured)

        Assert.assertEquals(setupIntro.trackingIdentifier, trackingRouteSlot.captured)
        setupIntro.setupIdBtn.click()

        advanceUntilIdle()
        composeTestRule.waitForIdle()

        Assert.assertEquals("firstTimeUser", trackingButtonPressedCategorySlot.captured)
        Assert.assertEquals("startSetup", trackingButtonPressedNameSlot.captured)

        Assert.assertEquals(setupPinLetter.trackingIdentifier, trackingRouteSlot.captured)
        setupPinLetter.noLetterBtn.click()

        advanceUntilIdle()
        composeTestRule.waitForIdle()

        Assert.assertEquals(resetPin.trackingIdentifier, trackingRouteSlot.captured)
        composeTestRule.onNodeWithTag(resetPin.back.tag).performClick()

        advanceUntilIdle()
        composeTestRule.waitForIdle()

        setupPinLetter.letterPresentBtn.click()

        advanceUntilIdle()
        composeTestRule.waitForIdle()

        Assert.assertEquals(setupTransportPin.trackingIdentifier, trackingRouteSlot.captured)
        composeTestRule.performPinInput(transportPin)
        composeTestRule.pressReturn()

        advanceUntilIdle()
        composeTestRule.waitForIdle()

        Assert.assertEquals(setupPersonalPinIntro.trackingIdentifier, trackingRouteSlot.captured)
        setupPersonalPinIntro.continueBtn.click()

        advanceUntilIdle()
        composeTestRule.waitForIdle()

        Assert.assertEquals(setupPersonalPinInput.trackingIdentifier, trackingRouteSlot.captured)
        composeTestRule.performPinInput(personalPin)
        composeTestRule.pressReturn()

        advanceUntilIdle()
        composeTestRule.waitForIdle()

        Assert.assertEquals(setupPersonalPinConfirm.trackingIdentifier, trackingRouteSlot.captured)
        composeTestRule.performPinInput(personalPin)
        composeTestRule.pressReturn()

        advanceUntilIdle()

        eidFlow.value = EidInteractionEvent.RequestCardInsertion
        advanceUntilIdle()
        composeTestRule.waitForIdle()

        Assert.assertEquals(setupScan.trackingIdentifier, trackingRouteSlot.captured)

        setupScan.nfcHelpBtn.click()
        composeTestRule.waitForIdle()

        Assert.assertEquals("firstTimeUser", trackingEventCategorySlot.captured)
        Assert.assertEquals("alertShown", trackingEventActionSlot.captured)
        Assert.assertEquals("NFCInfo", trackingEventNameSlot.captured)

        setupScan.nfcDialog.dismiss()
        composeTestRule.waitForIdle()

        eidFlow.value = EidInteractionEvent.CardRecognized
        advanceUntilIdle()

        eidFlow.value = EidInteractionEvent.ProcessCompletedSuccessfullyWithoutResult
        advanceUntilIdle()
        composeTestRule.waitForIdle()

        Assert.assertEquals(setupFinish.trackingIdentifier, trackingRouteSlot.captured)
        setupFinish.finishSetupBtn.click()

        advanceUntilIdle()
        composeTestRule.waitForIdle()

        home.assertIsDisplayed()
        Assert.assertEquals(home.trackingIdentifier, trackingRouteSlot.captured)
    }

    @Test
    fun setupCan() = runTest {
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

        val transportPin = "12345"
        val wrongTransportPin = "11111"
        val can = "123456"
        val wrongCan = "111222"

        // Define screens to be tested
        val setupTransportPin = TestScreen.SetupTransportPin(composeTestRule).setAttemptsLeft(1)
        val setupScan = TestScreen.Scan(composeTestRule)
        val setupCanConfirmTransportPin = TestScreen.SetupCanConfirmTransportPin(composeTestRule)
        val alreadySetup = TestScreen.SetupCanAlreadySetup(composeTestRule)
        val setupCanIntro = TestScreen.CanIntro(composeTestRule)
        val setupCanInput = TestScreen.CanInput(composeTestRule)
        val setupFinish = TestScreen.SetupFinish(composeTestRule)
        val home = TestScreen.Home(composeTestRule)

        home.assertIsDisplayed()
        home.setupButton.click()

        advanceUntilIdle()

        runSetupUpToCan(
            testRule = composeTestRule,
            eidFlow = eidFlow,
            testScope = this
        )

        composeTestRule.waitForIdle()
        Assert.assertEquals(setupCanConfirmTransportPin.trackingIdentifier, trackingRouteSlot.captured)
        setupCanConfirmTransportPin.setTransportPin(wrongTransportPin).assertIsDisplayed()
        setupCanConfirmTransportPin.inputCorrectBtn.click()

        advanceUntilIdle()
        composeTestRule.waitForIdle()
        Assert.assertEquals(alreadySetup.trackingIdentifier, trackingRouteSlot.captured)
        composeTestRule.onNodeWithTag(alreadySetup.back.tag).performClick()

        advanceUntilIdle()
        composeTestRule.waitForIdle()

        setupCanConfirmTransportPin.retryInputBtn.click()

        advanceUntilIdle()
        composeTestRule.waitForIdle()

        Assert.assertEquals(setupCanIntro.trackingIdentifier, trackingRouteSlot.captured)
        setupCanIntro.enterCanNowBtn.click()

        advanceUntilIdle()
        composeTestRule.waitForIdle()

        // ENTER WRONG CAN
        Assert.assertEquals(setupCanInput.trackingIdentifier, trackingRouteSlot.captured)
        composeTestRule.performPinInput(wrongCan)
        composeTestRule.pressReturn()

        advanceUntilIdle()
        composeTestRule.waitForIdle()

        // ENTER CORRECT TRANSPORT PIN
        Assert.assertEquals(setupTransportPin.trackingIdentifier, trackingRouteSlot.captured)
        composeTestRule.performPinInput(transportPin)
        composeTestRule.pressReturn()

        eidFlow.value = EidInteractionEvent.RequestCardInsertion
        advanceUntilIdle()

        eidFlow.value = EidInteractionEvent.CardRecognized
        advanceUntilIdle()

        eidFlow.value = EidInteractionEvent.RequestCanAndChangedPin { _, _, _ -> }
        advanceUntilIdle()

        eidFlow.value = EidInteractionEvent.CardRemoved
        advanceUntilIdle()
        composeTestRule.waitForIdle()

        // ENTER CORRECT
        Assert.assertEquals(setupCanInput.trackingIdentifier, trackingRouteSlot.captured)
        composeTestRule.performPinInput(can)
        composeTestRule.pressReturn()

        eidFlow.value = EidInteractionEvent.RequestCardInsertion
        advanceUntilIdle()

        eidFlow.value = EidInteractionEvent.CardRecognized
        advanceUntilIdle()

        eidFlow.value = EidInteractionEvent.ProcessCompletedSuccessfullyWithoutResult
        advanceUntilIdle()
        composeTestRule.waitForIdle()

        Assert.assertEquals(setupFinish.trackingIdentifier, trackingRouteSlot.captured)
        setupFinish.finishSetupBtn.click()

        advanceUntilIdle()

        home.assertIsDisplayed()
    }

    @Test
    fun setupCardDeactivated() = runTest {
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

        every { mockStorageManager.firstTimeUser } returns false

        val transportPin = "12345"
        val personalPin = "123456"

        // Define screens to be tested
        val setupIntro = TestScreen.SetupIntro(composeTestRule)
        val setupPinLetter = TestScreen.SetupPinLetter(composeTestRule)
        val setupPersonalPinIntro = TestScreen.SetupPersonalPinIntro(composeTestRule)
        val cardDeactivated = TestScreen.ErrorCardDeactivated(composeTestRule)
        val home = TestScreen.Home(composeTestRule)

        composeTestRule.waitForIdle()

        home.setupButton.scrollToAndClick()
        advanceUntilIdle()
        setupIntro.setupIdBtn.click()
        advanceUntilIdle()
        setupPinLetter.letterPresentBtn.click()
        advanceUntilIdle()
        composeTestRule.performPinInput(transportPin)
        composeTestRule.pressReturn()
        advanceUntilIdle()
        setupPersonalPinIntro.continueBtn.click()
        advanceUntilIdle()
        composeTestRule.performPinInput(personalPin)
        composeTestRule.pressReturn()
        advanceUntilIdle()
        composeTestRule.performPinInput(personalPin)
        composeTestRule.pressReturn()
        advanceUntilIdle()
        eidFlow.value = EidInteractionEvent.RequestCardInsertion
        advanceUntilIdle()
        eidFlow.value = EidInteractionEvent.CardRecognized
        advanceUntilIdle()

        eidFlow.value = EidInteractionEvent.Error(IdCardInteractionException.CardDeactivated)
        advanceUntilIdle()
        composeTestRule.waitForIdle()

        Assert.assertEquals(cardDeactivated.trackingIdentifier, trackingRouteSlot.captured)
    }

    @Test
    fun setupCardBlocked() = runTest {
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

        every { mockStorageManager.firstTimeUser } returns false

        val transportPin = "12345"
        val personalPin = "123456"

        // Define screens to be tested
        val setupIntro = TestScreen.SetupIntro(composeTestRule)
        val setupPinLetter = TestScreen.SetupPinLetter(composeTestRule)
        val setupPersonalPinIntro = TestScreen.SetupPersonalPinIntro(composeTestRule)
        val cardBlocked = TestScreen.ErrorCardBlocked(composeTestRule)
        val home = TestScreen.Home(composeTestRule)

        composeTestRule.waitForIdle()

        home.setupButton.scrollToAndClick()
        advanceUntilIdle()
        setupIntro.setupIdBtn.click()
        advanceUntilIdle()
        setupPinLetter.letterPresentBtn.click()
        advanceUntilIdle()
        composeTestRule.performPinInput(transportPin)
        composeTestRule.pressReturn()
        advanceUntilIdle()
        setupPersonalPinIntro.continueBtn.click()
        advanceUntilIdle()
        composeTestRule.performPinInput(personalPin)
        composeTestRule.pressReturn()
        advanceUntilIdle()
        composeTestRule.performPinInput(personalPin)
        composeTestRule.pressReturn()
        advanceUntilIdle()
        eidFlow.value = EidInteractionEvent.RequestCardInsertion
        advanceUntilIdle()
        eidFlow.value = EidInteractionEvent.CardRecognized
        advanceUntilIdle()

        eidFlow.value = EidInteractionEvent.Error(IdCardInteractionException.CardBlocked)
        advanceUntilIdle()
        composeTestRule.waitForIdle()

        Assert.assertEquals(cardBlocked.trackingIdentifier, trackingRouteSlot.captured)
    }

    @Test
    fun setupCardUnreadable() = runTest {
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

        every { mockStorageManager.firstTimeUser } returns false

        val transportPin = "12345"
        val personalPin = "123456"

        // Define screens to be tested
        val setupIntro = TestScreen.SetupIntro(composeTestRule)
        val setupPinLetter = TestScreen.SetupPinLetter(composeTestRule)
        val setupPersonalPinIntro = TestScreen.SetupPersonalPinIntro(composeTestRule)
        val cardUnreadable = TestScreen.ErrorCardUnreadable(composeTestRule)
        val home = TestScreen.Home(composeTestRule)

        composeTestRule.waitForIdle()

        home.setupButton.scrollToAndClick()
        advanceUntilIdle()
        setupIntro.setupIdBtn.click()
        advanceUntilIdle()
        setupPinLetter.letterPresentBtn.click()
        advanceUntilIdle()
        composeTestRule.performPinInput(transportPin)
        composeTestRule.pressReturn()
        advanceUntilIdle()
        setupPersonalPinIntro.continueBtn.click()
        advanceUntilIdle()
        composeTestRule.performPinInput(personalPin)
        composeTestRule.pressReturn()
        advanceUntilIdle()
        composeTestRule.performPinInput(personalPin)
        composeTestRule.pressReturn()
        advanceUntilIdle()
        eidFlow.value = EidInteractionEvent.RequestCardInsertion
        advanceUntilIdle()
        eidFlow.value = EidInteractionEvent.CardRecognized
        advanceUntilIdle()

        eidFlow.value = EidInteractionEvent.Error(IdCardInteractionException.ProcessFailed(ActivationResultCode.INTERNAL_ERROR, null, null))
        advanceUntilIdle()
        composeTestRule.waitForIdle()

        Assert.assertEquals(cardUnreadable.trackingIdentifier, trackingRouteSlot.captured)
    }

    @Test
    fun skipSetup() = runTest {
        every { mockCoroutineContextProvider.IO } returns StandardTestDispatcher(testScheduler)
        every { mockCoroutineContextProvider.Default } returns StandardTestDispatcher(testScheduler)

        composeTestRule.activity.setContentUsingUseIdTheme {
            UseIDApp(
                nfcAvailability = NfcAvailability.Available,
                navigator = navigator,
                trackerManager = trackerManager
            )
        }

        // Define screens to be tested
        val setupIntro = TestScreen.SetupIntro(composeTestRule)
        val alreadySetupConfirmation = TestScreen.AlreadySetupConfirmation(composeTestRule)
        val home = TestScreen.Home(composeTestRule)

        composeTestRule.waitForIdle()

        Assert.assertEquals(home.trackingIdentifier, trackingRouteSlot.captured)
        home.setupButton.scrollToAndClick()
        advanceUntilIdle()
        composeTestRule.waitForIdle()

        Assert.assertEquals("firstTimeUser", trackingButtonPressedCategorySlot.captured)
        Assert.assertEquals("start", trackingButtonPressedNameSlot.captured)

        advanceUntilIdle()
        composeTestRule.waitForIdle()

        Assert.assertEquals("firstTimeUser", trackingEventCategorySlot.captured)
        Assert.assertEquals("setupIntroOpened", trackingEventActionSlot.captured)
        Assert.assertEquals("home", trackingEventNameSlot.captured)

        Assert.assertEquals(setupIntro.trackingIdentifier, trackingRouteSlot.captured)
        setupIntro.alreadySetupBtn.click()

        advanceUntilIdle()
        composeTestRule.waitForIdle()

        Assert.assertEquals("firstTimeUser", trackingButtonPressedCategorySlot.captured)
        Assert.assertEquals("alreadySetup", trackingButtonPressedNameSlot.captured)

        advanceUntilIdle()
        composeTestRule.waitForIdle()

        alreadySetupConfirmation.assertIsDisplayed()
        Assert.assertEquals(alreadySetupConfirmation.trackingIdentifier, trackingRouteSlot.captured)
        alreadySetupConfirmation.confirmationButton.click()

        advanceUntilIdle()
        composeTestRule.waitForIdle()

        home.assertIsDisplayed()
        Assert.assertEquals(home.trackingIdentifier, trackingRouteSlot.captured)
    }

    @Test
    fun setupFromWidget() = runTest {
        every { mockCoroutineContextProvider.IO } returns StandardTestDispatcher(testScheduler)
        every { mockCoroutineContextProvider.Default } returns StandardTestDispatcher(testScheduler)

        composeTestRule.activity.setContentUsingUseIdTheme {
            UseIDApp(
                nfcAvailability = NfcAvailability.Available,
                navigator = navigator,
                trackerManager = trackerManager
            )
        }

        every { mockStorageManager.firstTimeUser } returns true

        val deepLink = Uri.parse("bundesident://127.0.0.1:24727/eID-Client?tcTokenURL=https%3A%2F%2Feid.digitalservicebund.de%2Fapi%2Fv1%2Fidentification%2Fsessions%2F30d20d97-cf31-4f01-ab27-35dea918bb83%2Ftc-token")

        // Define screens to be tested
        val setupIntro = TestScreen.SetupIntro(composeTestRule)

        composeTestRule.waitForIdle()

        appCoordinator.handleDeepLink(deepLink)
        advanceUntilIdle()
        composeTestRule.waitForIdle()

        Assert.assertEquals(setupIntro.trackingIdentifier, trackingRouteSlot.captured)
        Assert.assertEquals("firstTimeUser", trackingEventCategorySlot.captured)
        Assert.assertEquals("setupIntroOpened", trackingEventActionSlot.captured)
        Assert.assertEquals("widget", trackingEventNameSlot.captured)
    }

    @Test
    fun ident() = runTest {
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

        val deepLink =
            Uri.parse("bundesident://127.0.0.1:24727/eID-Client?tcTokenURL=https%3A%2F%2Feid.digitalservicebund.de%2Fapi%2Fv1%2Fidentification%2Fsessions%2F30d20d97-cf31-4f01-ab27-35dea918bb83%2Ftc-token")
        val redirectUrl = "test.url.com"
        val personalPin = "123456"

        // Define screens to be tested
        val identificationFetchMetaData = TestScreen.IdentificationFetchMetaData(composeTestRule)
        val identificationAttributeConsent = TestScreen.IdentificationAttributeConsent(composeTestRule)
        val identificationPersonalPin = TestScreen.IdentificationPersonalPin(composeTestRule)
        val identificationScan = TestScreen.Scan(composeTestRule).setIdentPending(true)

        composeTestRule.waitForIdle()

        appCoordinator.handleDeepLink(deepLink)
        advanceUntilIdle()

        eidFlow.value = EidInteractionEvent.AuthenticationStarted
        advanceUntilIdle()

        identificationFetchMetaData.assertIsDisplayed()
        Assert.assertEquals(identificationFetchMetaData.trackingIdentifier, trackingRouteSlot.captured)

        eidFlow.value = EidInteractionEvent.RequestAuthenticationRequestConfirmation(
            EidAuthenticationRequest(
                TestScreen.IdentificationAttributeConsent.RequestData.issuer,
                TestScreen.IdentificationAttributeConsent.RequestData.issuerURL,
                TestScreen.IdentificationAttributeConsent.RequestData.subject,
                TestScreen.IdentificationAttributeConsent.RequestData.subjectURL,
                TestScreen.IdentificationAttributeConsent.RequestData.validity,
                AuthenticationTerms.Text(TestScreen.IdentificationAttributeConsent.RequestData.authenticationTerms),
                TestScreen.IdentificationAttributeConsent.RequestData.transactionInfo,
                TestScreen.IdentificationAttributeConsent.RequestData.readAttributes
            )
        ) {
            eidFlow.value = EidInteractionEvent.RequestPin(attempts = null, pinCallback = {
                eidFlow.value = EidInteractionEvent.RequestCardInsertion
            })
        }

        advanceUntilIdle()
        composeTestRule.waitForIdle()

        Assert.assertEquals(identificationAttributeConsent.trackingIdentifier, trackingRouteSlot.captured)
        identificationAttributeConsent.continueBtn.click()

        advanceUntilIdle()
        composeTestRule.waitForIdle()

        Assert.assertEquals(identificationPersonalPin.trackingIdentifier, trackingRouteSlot.captured)
        composeTestRule.performPinInput(personalPin)
        composeTestRule.pressReturn()

        advanceUntilIdle()
        composeTestRule.waitForIdle()

        Assert.assertEquals(identificationScan.trackingIdentifier, trackingRouteSlot.captured)

        identificationScan.nfcHelpBtn.click()
        composeTestRule.waitForIdle()

        Assert.assertEquals("identification", trackingEventCategorySlot.captured)
        Assert.assertEquals("alertShown", trackingEventActionSlot.captured)
        Assert.assertEquals("NFCInfo", trackingEventNameSlot.captured)

        identificationScan.nfcDialog.dismiss()
        composeTestRule.waitForIdle()

        eidFlow.value = EidInteractionEvent.CardRecognized
        advanceUntilIdle()
        composeTestRule.waitForIdle()

        Intents.intending(
            Matchers.allOf(
                IntentMatchers.hasAction(Intent.ACTION_VIEW),
                IntentMatchers.hasData(redirectUrl),
                IntentMatchers.hasFlag(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        ).respondWith(
            Instrumentation.ActivityResult(
                Activity.RESULT_OK,
                null
            )
        )

        eidFlow.value = EidInteractionEvent.ProcessCompletedSuccessfullyWithRedirect(redirectUrl)
        advanceUntilIdle()
        composeTestRule.waitForIdle()

        Assert.assertEquals("identification", trackingEventCategorySlot.captured)
        Assert.assertEquals("success", trackingEventActionSlot.captured)
        Assert.assertEquals("", trackingEventNameSlot.captured)
    }

    @Test
    fun identCan() = runTest {
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
        val identificationScan = TestScreen.Scan(composeTestRule).setIdentPending(true)
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

        advanceUntilIdle()
        composeTestRule.waitForIdle()

        Assert.assertEquals(identificationCanPinForgotten.trackingIdentifier, trackingRouteSlot.captured)
        identificationCanPinForgotten.tryAgainBtn.click()

        advanceUntilIdle()
        composeTestRule.waitForIdle()

        Assert.assertEquals(identificationCanIntro.trackingIdentifier, trackingRouteSlot.captured)
        identificationCanIntro.enterCanNowBtn.click()

        advanceUntilIdle()
        composeTestRule.waitForIdle()

        // ENTER WRONG CAN
        Assert.assertEquals(identificationCanInput.trackingIdentifier, trackingRouteSlot.captured)
        composeTestRule.performPinInput(wrongCan)
        composeTestRule.pressReturn()

        advanceUntilIdle()
        composeTestRule.waitForIdle()

        // ENTER CORRECT PIN 3RD TIME
        Assert.assertEquals(identificationPersonalPin.trackingIdentifier, trackingRouteSlot.captured)
        composeTestRule.performPinInput(personalPin)
        composeTestRule.pressReturn()

        eidFlow.value = EidInteractionEvent.RequestCardInsertion
        advanceUntilIdle()
        composeTestRule.waitForIdle()

        eidFlow.value = EidInteractionEvent.CardRecognized
        advanceUntilIdle()

        eidFlow.value = EidInteractionEvent.RequestPinAndCan { _, _ -> }
        advanceUntilIdle()

        eidFlow.value = EidInteractionEvent.CardRemoved
        advanceUntilIdle()
        composeTestRule.waitForIdle()

        // ENTER CORRECT CAN
        Assert.assertEquals(identificationCanInput.trackingIdentifier, trackingRouteSlot.captured)
        composeTestRule.performPinInput(can)
        composeTestRule.pressReturn()

        eidFlow.value = EidInteractionEvent.RequestCardInsertion
        advanceUntilIdle()
        composeTestRule.waitForIdle()

        Assert.assertEquals(identificationScan.trackingIdentifier, trackingRouteSlot.captured)

        eidFlow.value = EidInteractionEvent.CardRecognized
        advanceUntilIdle()

        Intents.intending(
            Matchers.allOf(
                IntentMatchers.hasAction(Intent.ACTION_VIEW),
                IntentMatchers.hasData(redirectUrl),
                IntentMatchers.hasFlag(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        ).respondWith(
            Instrumentation.ActivityResult(
                Activity.RESULT_OK,
                null
            )
        )

        eidFlow.value = EidInteractionEvent.ProcessCompletedSuccessfullyWithRedirect(redirectUrl)
        advanceUntilIdle()
        composeTestRule.waitForIdle()

        Assert.assertEquals("identification", trackingEventCategorySlot.captured)
        Assert.assertEquals("success", trackingEventActionSlot.captured)
        Assert.assertEquals("", trackingEventNameSlot.captured)
    }

    @Test
    fun identCardDeactivated() = runTest {
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
        val personalPin = "123456"

        // Define screens to be tested
        val identificationFetchMetaData = TestScreen.IdentificationFetchMetaData(composeTestRule)
        val identificationAttributeConsent = TestScreen.IdentificationAttributeConsent(composeTestRule)
        val cardDeactivated = TestScreen.ErrorCardDeactivated(composeTestRule).setIdent(true)

        composeTestRule.waitForIdle()

        appCoordinator.handleDeepLink(deepLink)
        advanceUntilIdle()

        eidFlow.value = EidInteractionEvent.AuthenticationStarted
        advanceUntilIdle()

        identificationFetchMetaData.assertIsDisplayed()
        Assert.assertEquals(identificationFetchMetaData.trackingIdentifier, trackingRouteSlot.captured)

        eidFlow.value = EidInteractionEvent.RequestAuthenticationRequestConfirmation(
            EidAuthenticationRequest(
                TestScreen.IdentificationAttributeConsent.RequestData.issuer,
                TestScreen.IdentificationAttributeConsent.RequestData.issuerURL,
                TestScreen.IdentificationAttributeConsent.RequestData.subject,
                TestScreen.IdentificationAttributeConsent.RequestData.subjectURL,
                TestScreen.IdentificationAttributeConsent.RequestData.validity,
                AuthenticationTerms.Text(TestScreen.IdentificationAttributeConsent.RequestData.authenticationTerms),
                TestScreen.IdentificationAttributeConsent.RequestData.transactionInfo,
                TestScreen.IdentificationAttributeConsent.RequestData.readAttributes
            )
        ) {
            eidFlow.value = EidInteractionEvent.RequestPin(attempts = null, pinCallback = {
                eidFlow.value = EidInteractionEvent.RequestCardInsertion
            })
        }

        advanceUntilIdle()
        identificationAttributeConsent.continueBtn.click()
        advanceUntilIdle()
        composeTestRule.performPinInput(personalPin)
        composeTestRule.pressReturn()
        advanceUntilIdle()
        eidFlow.value = EidInteractionEvent.CardRecognized
        advanceUntilIdle()

        eidFlow.value = EidInteractionEvent.Error(IdCardInteractionException.CardDeactivated)
        advanceUntilIdle()
        composeTestRule.waitForIdle()

        Assert.assertEquals(cardDeactivated.trackingIdentifier, trackingRouteSlot.captured)
    }

    @Test
    fun identCardBlocked() = runTest {
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
        val personalPin = "123456"

        // Define screens to be tested
        val identificationFetchMetaData = TestScreen.IdentificationFetchMetaData(composeTestRule)
        val identificationAttributeConsent = TestScreen.IdentificationAttributeConsent(composeTestRule)
        val cardBlocked = TestScreen.ErrorCardBlocked(composeTestRule).setIdent(true)

        composeTestRule.waitForIdle()

        appCoordinator.handleDeepLink(deepLink)
        advanceUntilIdle()

        eidFlow.value = EidInteractionEvent.AuthenticationStarted
        advanceUntilIdle()

        identificationFetchMetaData.assertIsDisplayed()
        Assert.assertEquals(identificationFetchMetaData.trackingIdentifier, trackingRouteSlot.captured)

        eidFlow.value = EidInteractionEvent.RequestAuthenticationRequestConfirmation(
            EidAuthenticationRequest(
                TestScreen.IdentificationAttributeConsent.RequestData.issuer,
                TestScreen.IdentificationAttributeConsent.RequestData.issuerURL,
                TestScreen.IdentificationAttributeConsent.RequestData.subject,
                TestScreen.IdentificationAttributeConsent.RequestData.subjectURL,
                TestScreen.IdentificationAttributeConsent.RequestData.validity,
                AuthenticationTerms.Text(TestScreen.IdentificationAttributeConsent.RequestData.authenticationTerms),
                TestScreen.IdentificationAttributeConsent.RequestData.transactionInfo,
                TestScreen.IdentificationAttributeConsent.RequestData.readAttributes
            )
        ) {
            eidFlow.value = EidInteractionEvent.RequestPin(attempts = null, pinCallback = {
                eidFlow.value = EidInteractionEvent.RequestCardInsertion
            })
        }

        advanceUntilIdle()
        identificationAttributeConsent.continueBtn.click()
        advanceUntilIdle()
        composeTestRule.performPinInput(personalPin)
        composeTestRule.pressReturn()
        advanceUntilIdle()
        eidFlow.value = EidInteractionEvent.CardRecognized
        advanceUntilIdle()

        eidFlow.value = EidInteractionEvent.Error(IdCardInteractionException.CardBlocked)
        advanceUntilIdle()
        composeTestRule.waitForIdle()

        Assert.assertEquals(cardBlocked.trackingIdentifier, trackingRouteSlot.captured)
    }

    @Test
    fun identCardUnreadable() = runTest {
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
        val personalPin = "123456"

        // Define screens to be tested
        val identificationFetchMetaData = TestScreen.IdentificationFetchMetaData(composeTestRule)
        val identificationAttributeConsent = TestScreen.IdentificationAttributeConsent(composeTestRule)
        val cardUnreadable = TestScreen.ErrorCardUnreadable(composeTestRule).setIdentPending(true)

        composeTestRule.waitForIdle()

        appCoordinator.handleDeepLink(deepLink)
        advanceUntilIdle()

        eidFlow.value = EidInteractionEvent.AuthenticationStarted
        advanceUntilIdle()

        identificationFetchMetaData.assertIsDisplayed()
        Assert.assertEquals(identificationFetchMetaData.trackingIdentifier, trackingRouteSlot.captured)

        eidFlow.value = EidInteractionEvent.RequestAuthenticationRequestConfirmation(
            EidAuthenticationRequest(
                TestScreen.IdentificationAttributeConsent.RequestData.issuer,
                TestScreen.IdentificationAttributeConsent.RequestData.issuerURL,
                TestScreen.IdentificationAttributeConsent.RequestData.subject,
                TestScreen.IdentificationAttributeConsent.RequestData.subjectURL,
                TestScreen.IdentificationAttributeConsent.RequestData.validity,
                AuthenticationTerms.Text(TestScreen.IdentificationAttributeConsent.RequestData.authenticationTerms),
                TestScreen.IdentificationAttributeConsent.RequestData.transactionInfo,
                TestScreen.IdentificationAttributeConsent.RequestData.readAttributes
            )
        ) {
            eidFlow.value = EidInteractionEvent.RequestPin(attempts = null, pinCallback = {
                eidFlow.value = EidInteractionEvent.RequestCardInsertion
            })
        }

        advanceUntilIdle()
        identificationAttributeConsent.continueBtn.click()
        advanceUntilIdle()
        composeTestRule.performPinInput(personalPin)
        composeTestRule.pressReturn()
        advanceUntilIdle()
        eidFlow.value = EidInteractionEvent.CardRecognized
        advanceUntilIdle()

        eidFlow.value = EidInteractionEvent.Error(IdCardInteractionException.ProcessFailed(ActivationResultCode.INTERNAL_ERROR, null, null))
        advanceUntilIdle()
        composeTestRule.waitForIdle()

        Assert.assertEquals(cardUnreadable.trackingIdentifier, trackingRouteSlot.captured)
    }
}
