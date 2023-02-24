package de.digitalService.useID.userFlowTests.utils.flowFragments

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import de.digitalService.useID.idCardInterface.AuthenticationTerms
import de.digitalService.useID.idCardInterface.EidAuthenticationRequest
import de.digitalService.useID.idCardInterface.EidInteractionEvent
import de.digitalService.useID.userFlowTests.setupFlows.TestScreen
import de.digitalService.useID.util.ComposeTestRule
import de.digitalService.useID.util.performPinInput
import de.digitalService.useID.util.pressReturn
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import org.hamcrest.Matchers

fun runSetupUpToCan(testRule: ComposeTestRule, eidFlow: MutableStateFlow<EidInteractionEvent>, testScope: TestScope) {
    val wrongTransportPin = "11111"
    val personalPin = "123456"

    // Define screens to be tested
    val setupIntro = TestScreen.SetupIntro(testRule)
    val setupPinLetter = TestScreen.SetupPinLetter(testRule)
    val setupTransportPin = TestScreen.SetupTransportPin(testRule)
    val setupPersonalPinIntro = TestScreen.SetupPersonalPinIntro(testRule)
    val setupPersonalPinInput = TestScreen.SetupPersonalPinInput(testRule)
    val setupPersonalPinConfirm = TestScreen.SetupPersonalPinConfirm(testRule)
    val setupScan = TestScreen.Scan(testRule)

    setupIntro.assertIsDisplayed()
    setupIntro.setupIdBtn.click()

    setupPinLetter.assertIsDisplayed()
    setupPinLetter.letterPresentBtn.click()

    testScope.advanceUntilIdle()

    // ENTER INCORRECT TRANSPORT PIN 1ST TIME
    setupTransportPin.assertIsDisplayed()
    setupTransportPin.transportPinField.assertLength(0)
    testRule.performPinInput(wrongTransportPin)
    setupTransportPin.transportPinField.assertLength(wrongTransportPin.length)
    testRule.pressReturn()

    setupPersonalPinIntro.assertIsDisplayed()
    setupPersonalPinIntro.continueBtn.click()

    setupPersonalPinInput.assertIsDisplayed()
    setupPersonalPinInput.personalPinField.assertLength(0)
    testRule.performPinInput(personalPin)
    setupPersonalPinInput.personalPinField.assertLength(personalPin.length)
    testRule.pressReturn()

    setupPersonalPinConfirm.assertIsDisplayed()
    setupPersonalPinConfirm.personalPinField.assertLength(0)
    testRule.performPinInput(personalPin)
    setupPersonalPinConfirm.personalPinField.assertLength(personalPin.length)
    testRule.pressReturn()

    eidFlow.value = EidInteractionEvent.RequestCardInsertion
    testScope.advanceUntilIdle()

    setupScan.assertIsDisplayed()

    eidFlow.value = EidInteractionEvent.CardRecognized
    testScope.advanceUntilIdle()

    setupScan.setProgress(true).assertIsDisplayed()

    eidFlow.value = EidInteractionEvent.RequestChangedPin(null) { _, _ -> }
    testScope.advanceUntilIdle()

    eidFlow.value = EidInteractionEvent.RequestChangedPin(null) { _, _ -> }
    testScope.advanceUntilIdle()

    // ENTER INCORRECT TRANSPORT PIN 2ND TIME
    setupTransportPin.setAttemptsLeft(2).assertIsDisplayed()
    setupTransportPin.transportPinField.assertLength(0)
    testRule.performPinInput(wrongTransportPin)
    setupTransportPin.transportPinField.assertLength(wrongTransportPin.length)
    testRule.pressReturn()

    eidFlow.value = EidInteractionEvent.RequestCardInsertion
    testScope.advanceUntilIdle()

    setupScan.setBackAllowed(false).setProgress(false).assertIsDisplayed()

    eidFlow.value = EidInteractionEvent.CardRecognized
    testScope.advanceUntilIdle()

    setupScan.setBackAllowed(false).setProgress(true).assertIsDisplayed()

    eidFlow.value = EidInteractionEvent.RequestCanAndChangedPin { _, _, _ -> }
    testScope.advanceUntilIdle()
}

fun runSetupUpToCanAfterSomeTime(withWrongTransportPin: Boolean, testRule: ComposeTestRule, eidFlow: MutableStateFlow<EidInteractionEvent>, testScope: TestScope) {

    val transportPin = if (withWrongTransportPin) "11111" else "12345"
    val personalPin = "123456"

    // Define screens to be tested
    val setupIntro = TestScreen.SetupIntro(testRule)
    val setupPinLetter = TestScreen.SetupPinLetter(testRule)
    val setupTransportPin = TestScreen.SetupTransportPin(testRule)
    val setupPersonalPinIntro = TestScreen.SetupPersonalPinIntro(testRule)
    val setupPersonalPinInput = TestScreen.SetupPersonalPinInput(testRule)
    val setupPersonalPinConfirm = TestScreen.SetupPersonalPinConfirm(testRule)
    val setupScan = TestScreen.Scan(testRule)
    val home = TestScreen.Home(testRule)

    home.assertIsDisplayed()
    home.setupIdBtn.click()

    setupIntro.assertIsDisplayed()
    setupIntro.setupIdBtn.click()

    setupPinLetter.assertIsDisplayed()
    setupPinLetter.letterPresentBtn.click()

    testScope.advanceUntilIdle()

    setupTransportPin.assertIsDisplayed()
    setupTransportPin.transportPinField.assertLength(0)
    testRule.performPinInput(transportPin)
    setupTransportPin.transportPinField.assertLength(transportPin.length)
    testRule.pressReturn()

    setupPersonalPinIntro.assertIsDisplayed()
    setupPersonalPinIntro.continueBtn.click()

    setupPersonalPinInput.assertIsDisplayed()
    setupPersonalPinInput.personalPinField.assertLength(0)
    testRule.performPinInput(personalPin)
    setupPersonalPinInput.personalPinField.assertLength(personalPin.length)
    testRule.pressReturn()

    setupPersonalPinConfirm.assertIsDisplayed()
    setupPersonalPinConfirm.personalPinField.assertLength(0)
    testRule.performPinInput(personalPin)
    setupPersonalPinConfirm.personalPinField.assertLength(personalPin.length)
    testRule.pressReturn()

    eidFlow.value = EidInteractionEvent.RequestCardInsertion
    testScope.advanceUntilIdle()

    setupScan.assertIsDisplayed()

    eidFlow.value = EidInteractionEvent.CardRecognized
    testScope.advanceUntilIdle()

    setupScan.setProgress(true).assertIsDisplayed()

    eidFlow.value = EidInteractionEvent.RequestCanAndChangedPin { _, _, _ -> }

    testScope.advanceUntilIdle()
}

fun runSetupCanSuccessful(testRule: ComposeTestRule, eidFlow: MutableStateFlow<EidInteractionEvent>, testScope: TestScope) {
    val transportPin = "12345"
    val wrongTransportPin = "11111"
    val can = "111222"

    // Define screens to be tested
    val setupTransportPin = TestScreen.SetupTransportPin(testRule)
    val setupScan = TestScreen.Scan(testRule)
    val setupCanConfirmTransportPin = TestScreen.SetupCanConfirmTransportPin(testRule)
    val setupCanIntro = TestScreen.CanIntro(testRule)
    val setupCanInput = TestScreen.CanInput(testRule)

    setupCanConfirmTransportPin.setTransportPin(wrongTransportPin).assertIsDisplayed()
    setupCanConfirmTransportPin.retryInputBtn.click()

    setupCanIntro.setBackAllowed(true).assertIsDisplayed()
    setupCanIntro.back.click()

    setupCanConfirmTransportPin.setTransportPin(wrongTransportPin).assertIsDisplayed()
    setupCanConfirmTransportPin.retryInputBtn.click()

    setupCanIntro.setBackAllowed(true).assertIsDisplayed()
    setupCanIntro.enterCanNowBtn.click()

    // ENTER CORRECT CAN
    setupCanInput.assertIsDisplayed()
    setupCanInput.canEntryField.assertLength(0)
    testRule.performPinInput(can)
    setupCanInput.canEntryField.assertLength(can.length)
    testRule.pressReturn()

    // ENTER CORRECT TRANSPORT PIN
    setupTransportPin.setAttemptsLeft(1).assertIsDisplayed()
    setupTransportPin.transportPinField.assertLength(0)
    testRule.performPinInput(transportPin)
    setupTransportPin.transportPinField.assertLength(transportPin.length)
    testRule.pressReturn()

    eidFlow.value = EidInteractionEvent.RequestCardInsertion
    testScope.advanceUntilIdle()

    setupScan.setBackAllowed(false).setProgress(false).assertIsDisplayed()

    eidFlow.value = EidInteractionEvent.CardRecognized
    testScope.advanceUntilIdle()

    setupScan.setProgress(true).assertIsDisplayed()

    eidFlow.value = EidInteractionEvent.RequestChangedPin(null) {_, _ -> }
    testScope.advanceUntilIdle()

    eidFlow.value = EidInteractionEvent.ProcessCompletedSuccessfullyWithoutResult
    testScope.advanceUntilIdle()
}

fun runSetupCanSuccessfulAfterCanWrongOnce(testRule: ComposeTestRule, eidFlow: MutableStateFlow<EidInteractionEvent>, testScope: TestScope) {
    val transportPin = "12345"
    val wrongTransportPin = "11111"
    val can = "111222"
    val wrongCan = "000000"

    // Define screens to be tested
    val setupTransportPin = TestScreen.SetupTransportPin(testRule)
    val setupScan = TestScreen.Scan(testRule)
    val setupCanConfirmTransportPin = TestScreen.SetupCanConfirmTransportPin(testRule)
    val setupCanIntro = TestScreen.CanIntro(testRule)
    val setupCanInput = TestScreen.CanInput(testRule)

    // CAN FLOW
    setupCanConfirmTransportPin.setTransportPin(wrongTransportPin).assertIsDisplayed()
    setupCanConfirmTransportPin.retryInputBtn.click()

    setupCanIntro.setBackAllowed(true).assertIsDisplayed()
    setupCanIntro.back.click()

    setupCanConfirmTransportPin.setTransportPin(wrongTransportPin).assertIsDisplayed()
    setupCanConfirmTransportPin.retryInputBtn.click()

    setupCanIntro.setBackAllowed(true).assertIsDisplayed()
    setupCanIntro.enterCanNowBtn.click()

    // ENTER WRONG CAN
    setupCanInput.assertIsDisplayed()
    setupCanInput.canEntryField.assertLength(0)
    testRule.performPinInput(wrongCan)
    setupCanInput.canEntryField.assertLength(wrongCan.length)
    testRule.pressReturn()

    // ENTER CORRECT TRANSPORT PIN
    setupTransportPin.setAttemptsLeft(1).assertIsDisplayed()
    setupTransportPin.transportPinField.assertLength(0)
    testRule.performPinInput(transportPin)
    setupTransportPin.transportPinField.assertLength(transportPin.length)
    testRule.pressReturn()

    eidFlow.value = EidInteractionEvent.RequestCardInsertion
    testScope.advanceUntilIdle()

    setupScan.setBackAllowed(false).setProgress(false).assertIsDisplayed()

    eidFlow.value = EidInteractionEvent.CardRecognized
    testScope.advanceUntilIdle()

    setupScan.setProgress(true).assertIsDisplayed()

    eidFlow.value = EidInteractionEvent.RequestCanAndChangedPin { _, _, _ -> }
    testScope.advanceUntilIdle()

    // ENTER CORRECT
    setupCanInput.setRetry(true).assertIsDisplayed()
    setupCanInput.canEntryField.assertLength(0)
    testRule.performPinInput(can)
    setupCanInput.canEntryField.assertLength(can.length)
    testRule.pressReturn()

    eidFlow.value = EidInteractionEvent.RequestCardInsertion
    testScope.advanceUntilIdle()

    setupScan.setProgress(false).assertIsDisplayed()

    eidFlow.value = EidInteractionEvent.CardRecognized
    testScope.advanceUntilIdle()

    setupScan.setProgress(true).assertIsDisplayed()

    eidFlow.value = EidInteractionEvent.RequestChangedPin(null) {_, _ -> }
    testScope.advanceUntilIdle()

    eidFlow.value = EidInteractionEvent.ProcessCompletedSuccessfullyWithoutResult
    testScope.advanceUntilIdle()
}

fun runSuccessfulIdentAfterSetup(testRule: ComposeTestRule, eidFlow: MutableStateFlow<EidInteractionEvent>, testScope: TestScope) {

    val redirectUrl = "test.url.com"
    val personalPin = "123456"

    // Define screens to be tested
    val identificationFetchMetaData = TestScreen.IdentificationFetchMetaData(testRule)
    val identificationAttributeConsent = TestScreen.IdentificationAttributeConsent(testRule)
    val identificationPersonalPin = TestScreen.IdentificationPersonalPin(testRule)
    val identificationScan = TestScreen.Scan(testRule)

    eidFlow.value = EidInteractionEvent.AuthenticationStarted
    testScope.advanceUntilIdle()

    identificationFetchMetaData.assertIsDisplayed()

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
        eidFlow.value =  EidInteractionEvent.RequestCardInsertion
    }

    testScope.advanceUntilIdle()

    identificationAttributeConsent.assertIsDisplayed()
    identificationAttributeConsent.continueBtn.click()

    eidFlow.value = EidInteractionEvent.RequestPin(attempts = null, pinCallback = {})
    testScope.advanceUntilIdle()

    identificationPersonalPin.assertIsDisplayed()
    identificationPersonalPin.personalPinField.assertLength(0)
    testRule.performPinInput(personalPin)
    identificationPersonalPin.personalPinField.assertLength(personalPin.length)
    testRule.pressReturn()

    eidFlow.value = EidInteractionEvent.RequestCardInsertion
    testScope.advanceUntilIdle()

    identificationScan.setIdentPending(true).setBackAllowed(false).assertIsDisplayed()

    eidFlow.value = EidInteractionEvent.CardRecognized
    testScope.advanceUntilIdle()

    identificationScan.setProgress(true).assertIsDisplayed()

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
    testScope.advanceUntilIdle()
}
