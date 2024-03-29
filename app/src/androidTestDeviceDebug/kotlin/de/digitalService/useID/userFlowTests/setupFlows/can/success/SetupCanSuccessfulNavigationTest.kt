//package de.digitalService.useID.userFlowTests.setupFlows.can.success
//
//import androidx.compose.ui.test.*
//import androidx.compose.ui.test.junit4.createAndroidComposeRule
//import dagger.hilt.android.testing.BindValue
//import dagger.hilt.android.testing.HiltAndroidRule
//import dagger.hilt.android.testing.HiltAndroidTest
//import dagger.hilt.android.testing.UninstallModules
//import de.digitalService.useID.MainActivity
//import de.digitalService.useID.StorageManager
//import de.digitalService.useID.analytics.TrackerManagerType
//import de.digitalService.useID.hilt.CoroutineContextProviderModule
//import de.digitalService.useID.hilt.SingletonModule
//import de.digitalService.useID.idCardInterface.EidInteractionEvent
//import de.digitalService.useID.idCardInterface.IdCardManager
//import de.digitalService.useID.models.NfcAvailability
//import de.digitalService.useID.ui.UseIDApp
//import de.digitalService.useID.ui.navigation.Navigator
//import de.digitalService.useID.userFlowTests.utils.TestScreen
//import de.digitalService.useID.userFlowTests.utils.flowParts.setup.helper.runSetupUpToCan
//import de.digitalService.useID.util.*
//import io.mockk.every
//import io.mockk.mockk
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.ExperimentalCoroutinesApi
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.test.StandardTestDispatcher
//import kotlinx.coroutines.test.advanceUntilIdle
//import kotlinx.coroutines.test.runTest
//import org.junit.Before
//import org.junit.Rule
//import org.junit.Test
//import javax.inject.Inject
//
//@UninstallModules(SingletonModule::class, CoroutineContextProviderModule::class)
//@HiltAndroidTest
//class SetupCanSuccessfulNavigationTest {
//
//    @get:Rule(order = 0)
//    var hiltRule = HiltAndroidRule(this)
//
//    @get:Rule(order = 1)
//    val composeTestRule = createAndroidComposeRule<MainActivity>()
//
//    @Inject
//    lateinit var navigator: Navigator
//
//    @Inject
//    lateinit var trackerManager: TrackerManagerType
//
//    @BindValue
//    val mockIdCardManager: IdCardManager = mockk(relaxed = true)
//
//    @BindValue
//    val mockStorageManager: StorageManager = mockk(relaxed = true) {
//        every { firstTimeUser } returns false
//    }
//
//    @BindValue
//    val mockCoroutineContextProvider: CoroutineContextProviderType = mockk {
//        every { Main } returns Dispatchers.Main
//    }
//
//    @Before
//    fun before() {
//        hiltRule.inject()
//    }
//
//    @OptIn(ExperimentalCoroutinesApi::class)
//    @Test
//    fun testSetupCanSuccessfulNavigation() = runTest {
//        every { mockCoroutineContextProvider.IO } returns StandardTestDispatcher(testScheduler)
//        every { mockCoroutineContextProvider.Default } returns StandardTestDispatcher(testScheduler)
//
//        val eidFlow = MutableStateFlow<EidInteractionEvent>(EidInteractionEvent.Idle)
//        every { mockIdCardManager.eidFlow } returns eidFlow
//
//        composeTestRule.activity.setContentUsingUseIdTheme {
//            UseIDApp(
//                nfcAvailability = NfcAvailability.Available,
//                navigator = navigator,
//                trackerManager = trackerManager
//            )
//        }
//
//        val transportPin = "12345"
//        val wrongTransportPin = "11111"
//        val can = "123456"
//        val wrongCan = "111222"
//
//        // Define screens to be tested
//        val setupTransportPin = TestScreen.SetupTransportPin(composeTestRule)
//        val setupScan = TestScreen.Scan(composeTestRule)
//        val setupCanConfirmTransportPin = TestScreen.SetupCanConfirmTransportPin(composeTestRule)
//        val setupCanAlreadySetup = TestScreen.SetupCanAlreadySetup(composeTestRule)
//        val setupCanIntro = TestScreen.CanIntro(composeTestRule)
//        val setupCanInput = TestScreen.CanInput(composeTestRule)
//        val setupFinish = TestScreen.SetupFinish(composeTestRule)
//        val setupResetPersonalPin = TestScreen.ResetPersonalPin(composeTestRule)
//        val home = TestScreen.Home(composeTestRule)
//
//        home.assertIsDisplayed()
//        home.setupButton.click()
//        advanceUntilIdle()
//
//        runSetupUpToCan(
//            testRule = composeTestRule,
//            eidFlow = eidFlow,
//            testScope = this
//        )
//
//        // CAN FLOW
//        setupCanConfirmTransportPin.setTransportPin(wrongTransportPin).assertIsDisplayed()
//        setupCanConfirmTransportPin.inputCorrectBtn.click()
//        advanceUntilIdle()
//
//        setupCanAlreadySetup.assertIsDisplayed()
//        setupCanAlreadySetup.personalPinNotAvailableBtn.click()
//        advanceUntilIdle()
//
//        setupResetPersonalPin.assertIsDisplayed()
//        setupResetPersonalPin.back.click()
//        advanceUntilIdle()
//
//        setupCanAlreadySetup.assertIsDisplayed()
//        setupCanAlreadySetup.back.click()
//        advanceUntilIdle()
//
//        setupCanConfirmTransportPin.setTransportPin(wrongTransportPin).assertIsDisplayed()
//        setupCanConfirmTransportPin.retryInputBtn.click()
//        advanceUntilIdle()
//
//        setupCanIntro.setBackAllowed(true).assertIsDisplayed()
//        setupCanIntro.back.click()
//        advanceUntilIdle()
//
//        setupCanConfirmTransportPin.setTransportPin(wrongTransportPin).assertIsDisplayed()
//        setupCanConfirmTransportPin.retryInputBtn.click()
//        advanceUntilIdle()
//
//        setupCanIntro.setBackAllowed(true).assertIsDisplayed()
//        setupCanIntro.enterCanNowBtn.click()
//        advanceUntilIdle()
//
//        // ENTER WRONG CAN
//        setupCanInput.assertIsDisplayed()
//        setupCanInput.canEntryField.assertLength(0)
//        composeTestRule.performPinInput(wrongCan)
//        setupCanInput.canEntryField.assertLength(wrongCan.length)
//        setupCanInput.back.click()
//        advanceUntilIdle()
//
//        setupCanIntro.setBackAllowed(true).assertIsDisplayed()
//        setupCanIntro.enterCanNowBtn.click()
//        advanceUntilIdle()
//
//        setupCanInput.assertIsDisplayed()
//        setupCanInput.canEntryField.assertLength(0)
//        composeTestRule.performPinInput(wrongCan)
//        setupCanInput.canEntryField.assertLength(wrongCan.length)
//        composeTestRule.pressReturn()
//        advanceUntilIdle()
//
//        // ENTER CORRECT TRANSPORT PIN
//        setupTransportPin.setAttemptsLeft(1).assertIsDisplayed()
//        setupTransportPin.transportPinField.assertLength(0)
//        composeTestRule.performPinInput(transportPin)
//        setupTransportPin.transportPinField.assertLength(transportPin.length)
//        setupTransportPin.back.click()
//        advanceUntilIdle()
//
//        setupCanInput.assertIsDisplayed()
//        setupCanInput.canEntryField.assertLength(6) // TODO input should be stored. Ticket: https://digitalservicebund.atlassian.net/browse/USEID-907
//        composeTestRule.pressReturn()
//        advanceUntilIdle()
//
//        setupTransportPin.setAttemptsLeft(1).assertIsDisplayed()
//        setupTransportPin.transportPinField.assertLength(0)
//        composeTestRule.performPinInput(transportPin)
//        setupTransportPin.transportPinField.assertLength(transportPin.length)
//        composeTestRule.pressReturn()
//        advanceUntilIdle()
//
//        eidFlow.value = EidInteractionEvent.CardInsertionRequested
//        advanceUntilIdle()
//
//        setupScan.setBackAllowed(false).setProgress(false).assertIsDisplayed()
//
//        eidFlow.value = EidInteractionEvent.CardRecognized
//        advanceUntilIdle()
//
//        setupScan.setProgress(true).assertIsDisplayed()
//
//        eidFlow.value = EidInteractionEvent.CanRequested()
//        advanceUntilIdle()
//
//        eidFlow.value = EidInteractionEvent.CardRemoved
//        advanceUntilIdle()
//
//        // ENTER CORRECT
//        setupCanInput.setRetry(true).assertIsDisplayed()
//        setupCanInput.canEntryField.assertLength(0)
//        composeTestRule.performPinInput(can)
//        setupCanInput.canEntryField.assertLength(can.length)
//        setupCanInput.back.click()
//        advanceUntilIdle()
//
//        setupCanIntro.setBackAllowed(false).assertIsDisplayed()
//        setupCanIntro.enterCanNowBtn.click()
//        advanceUntilIdle()
//
//        setupCanInput.setRetry(false).assertIsDisplayed()
//        setupCanInput.canEntryField.assertLength(0)
//        composeTestRule.performPinInput(can)
//        setupCanInput.canEntryField.assertLength(can.length)
//        composeTestRule.pressReturn()
//        advanceUntilIdle()
//
//        eidFlow.value = EidInteractionEvent.CardInsertionRequested
//        advanceUntilIdle()
//
//        setupScan.setProgress(false).assertIsDisplayed()
//
//        eidFlow.value = EidInteractionEvent.CardRecognized
//        advanceUntilIdle()
//
//        setupScan.setProgress(true).assertIsDisplayed()
//
//        eidFlow.value = EidInteractionEvent.PinRequested(1)
//        advanceUntilIdle()
//
//        eidFlow.value = EidInteractionEvent.NewPinRequested
//        advanceUntilIdle()
//
//        eidFlow.value = EidInteractionEvent.PinChangeSucceeded
//        advanceUntilIdle()
//
//        setupFinish.assertIsDisplayed()
//        setupFinish.cancel.click()
//        advanceUntilIdle()
//
//        home.assertIsDisplayed()
//    }
//}
