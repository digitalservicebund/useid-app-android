package de.digitalService.useID.userFlowTests.setupFlows

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import de.digitalService.useID.MainActivity
import de.digitalService.useID.analytics.TrackerManagerType
import de.digitalService.useID.hilt.CoroutineContextProviderModule
import de.digitalService.useID.hilt.SingletonModule
import de.digitalService.useID.idCardInterface.EidInteractionEvent
import de.digitalService.useID.idCardInterface.IdCardManager
import de.digitalService.useID.models.NfcAvailability
import de.digitalService.useID.ui.UseIDApp
import de.digitalService.useID.ui.navigation.Navigator
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

@UninstallModules(SingletonModule::class, CoroutineContextProviderModule::class)
@HiltAndroidTest
class SetupSuccessfulFirstTimeUserNavigationTest {

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Inject
    lateinit var navigator: Navigator

    @Inject
    lateinit var trackerManager: TrackerManagerType

    @BindValue
    val mockIdCardManager: IdCardManager = mockk(relaxed = true)

    @BindValue
    val mockCoroutineContextProvider: CoroutineContextProviderType = mockk {
        every { Main } returns Dispatchers.Main
    }

    @Before
    fun before() {
        hiltRule.inject()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun setupSuccessfulFirstTimeUserNavigation() = runTest {
        every { mockCoroutineContextProvider.IO } returns StandardTestDispatcher(testScheduler)

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
        val personalPin = "123456"

        // Define screens to be tested
        val setupIntro = TestScreen.SetupIntro(composeTestRule)
        val setupPinLetter = TestScreen.SetupPinLetter(composeTestRule)
        val setupTransportPin = TestScreen.SetupTransportPin(composeTestRule)
        val setupPersonalPinIntro = TestScreen.SetupPersonalPinIntro(composeTestRule)
        val setupPersonalPinInput = TestScreen.SetupPersonalPinInput(composeTestRule)
        val setupPersonalPinConfirm = TestScreen.SetupPersonalPinConfirm(composeTestRule)
        val setupScan = TestScreen.SetupScan(composeTestRule)
        val setupFinish = TestScreen.SetupFinish(composeTestRule)
        val home = TestScreen.Home(composeTestRule)
        val resetPersonalPin = TestScreen.ResetPersonalPin(composeTestRule)

        setupIntro.assertIsDisplayed()
        setupIntro.setupIdBtn.click()

        setupPinLetter.assertIsDisplayed()
        setupPinLetter.back.click()

        setupIntro.assertIsDisplayed()
        setupIntro.setupIdBtn.click()

        setupPinLetter.assertIsDisplayed()
        setupPinLetter.noLetterBtn.click()

        resetPersonalPin.assertIsDisplayed()
        resetPersonalPin.back.click()

        setupPinLetter.assertIsDisplayed()
        setupPinLetter.letterPresentBtn.click()

        advanceUntilIdle()

        setupTransportPin.assertIsDisplayed()
        setupTransportPin.navigationIcon.click()

        setupPinLetter.assertIsDisplayed()
        setupPinLetter.letterPresentBtn.click()

        advanceUntilIdle()

        setupTransportPin.assertIsDisplayed()
        setupTransportPin.transportPinField.assertLength(0)
        composeTestRule.performPinInput(transportPin)
        setupTransportPin.transportPinField.assertLength(transportPin.length)
        setupTransportPin.navigationIcon.click()

        setupPinLetter.assertIsDisplayed()
        setupPinLetter.letterPresentBtn.click()

        advanceUntilIdle()

        setupTransportPin.assertIsDisplayed()
        setupTransportPin.transportPinField.assertLength(0)
        composeTestRule.performPinInput(transportPin)
        setupTransportPin.transportPinField.assertLength(transportPin.length)
        composeTestRule.pressReturn()

        setupPersonalPinIntro.assertIsDisplayed()
        setupPersonalPinIntro.back.click()

        setupTransportPin.assertIsDisplayed()
//        setupTransportPin.transportPinField.assertLength(transportPin.length) // TODO: This should work, needs to be adapted in the app
        composeTestRule.performPinInput(transportPin) // TODO: Remove when above is fixed
        composeTestRule.pressReturn()

        setupPersonalPinIntro.assertIsDisplayed()
        setupPersonalPinIntro.continueBtn.click()

        setupPersonalPinInput.assertIsDisplayed()
        setupPersonalPinInput.personalPinField.assertLength(0)
        composeTestRule.performPinInput(personalPin)
        setupPersonalPinInput.personalPinField.assertLength(personalPin.length)
        setupPersonalPinInput.back.click()

        setupPersonalPinIntro.assertIsDisplayed()
        setupPersonalPinIntro.continueBtn.click()

        setupPersonalPinInput.assertIsDisplayed()
        setupPersonalPinInput.personalPinField.assertLength(0)
        composeTestRule.performPinInput(personalPin)
        setupPersonalPinInput.personalPinField.assertLength(personalPin.length)
        composeTestRule.pressReturn()

        setupPersonalPinConfirm.assertIsDisplayed()
        setupPersonalPinConfirm.personalPinField.assertLength(0)
        composeTestRule.performPinInput(personalPin)
        setupPersonalPinConfirm.personalPinField.assertLength(personalPin.length)
        setupPersonalPinConfirm.back.click()

        setupPersonalPinInput.assertIsDisplayed()
        setupPersonalPinInput.personalPinField.assertLength(0)
        composeTestRule.performPinInput(personalPin)
        setupPersonalPinInput.personalPinField.assertLength(personalPin.length)
        composeTestRule.pressReturn()

        setupPersonalPinConfirm.assertIsDisplayed()
        setupPersonalPinConfirm.personalPinField.assertLength(0)
        composeTestRule.performPinInput(personalPin)
        setupPersonalPinConfirm.personalPinField.assertLength(personalPin.length)
        composeTestRule.pressReturn()

        eidFlow.value = EidInteractionEvent.RequestCardInsertion
        advanceUntilIdle()

        setupScan.assertIsDisplayed()

        setupScan.navigationIcon.click()

        setupPersonalPinInput.assertIsDisplayed()
        setupPersonalPinInput.personalPinField.assertLength(0)
        composeTestRule.performPinInput(personalPin)
        setupPersonalPinInput.personalPinField.assertLength(personalPin.length)
        composeTestRule.pressReturn()

        setupPersonalPinConfirm.assertIsDisplayed()
        setupPersonalPinConfirm.personalPinField.assertLength(0)
        composeTestRule.performPinInput(personalPin)
        setupPersonalPinConfirm.personalPinField.assertLength(personalPin.length)
        composeTestRule.pressReturn()

        eidFlow.value = EidInteractionEvent.RequestCardInsertion
        advanceUntilIdle()

        setupScan.assertIsDisplayed()

        setupScan.nfcHelpBtn.click()
        setupScan.nfcDialog.assertIsDisplayed()
        setupScan.nfcDialog.dismiss()
        setupScan.assertIsDisplayed()

        setupScan.scanHelpBtn.click()
        setupScan.helpDialog.assertIsDisplayed()
        setupScan.helpDialog.dismiss()
        setupScan.assertIsDisplayed()

        eidFlow.value = EidInteractionEvent.CardRecognized
        advanceUntilIdle()

        setupScan.progress(true).assertIsDisplayed()

        eidFlow.value = EidInteractionEvent.RequestChangedPin(null) {_, _ -> }
        advanceUntilIdle()

        eidFlow.value = EidInteractionEvent.ProcessCompletedSuccessfullyWithoutResult
        advanceUntilIdle()

        setupFinish.assertIsDisplayed()
        setupFinish.cancel.click()

        home.assertIsDisplayed()
    }
}
