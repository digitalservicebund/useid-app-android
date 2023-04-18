package de.digitalService.useID.userFlowTests.utils.flowParts.setup

import de.digitalService.useID.idCardInterface.EidInteractionEvent
import de.digitalService.useID.userFlowTests.setupFlows.TestScreen
import de.digitalService.useID.util.ComposeTestRule
import de.digitalService.useID.util.performPinInput
import de.digitalService.useID.util.pressReturn
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle

@OptIn(ExperimentalCoroutinesApi::class)
fun runSetupSuccessfulAfterTransportPinWrongOnceAndThenCorrect(testRule: ComposeTestRule, eidFlow: MutableStateFlow<EidInteractionEvent>, testScope: TestScope) {
    val wrongTransportPin = "11111"
    val transportPin = "12345"
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

    testScope.advanceUntilIdle()

    setupPinLetter.assertIsDisplayed()
    setupPinLetter.letterPresentBtn.click()

    testScope.advanceUntilIdle()

    setupTransportPin.assertIsDisplayed()
    setupTransportPin.transportPinField.assertLength(0)
    testRule.performPinInput(wrongTransportPin)
    setupTransportPin.transportPinField.assertLength(wrongTransportPin.length)
    testRule.pressReturn()

    testScope.advanceUntilIdle()

    setupPersonalPinIntro.assertIsDisplayed()
    setupPersonalPinIntro.continueBtn.click()

    testScope.advanceUntilIdle()

    setupPersonalPinInput.assertIsDisplayed()
    setupPersonalPinInput.personalPinField.assertLength(0)
    testRule.performPinInput(personalPin)
    setupPersonalPinInput.personalPinField.assertLength(personalPin.length)
    testRule.pressReturn()

    testScope.advanceUntilIdle()

    setupPersonalPinConfirm.assertIsDisplayed()
    setupPersonalPinConfirm.personalPinField.assertLength(0)
    testRule.performPinInput(personalPin)
    setupPersonalPinConfirm.personalPinField.assertLength(personalPin.length)
    testRule.pressReturn()

    eidFlow.value = EidInteractionEvent.CardInsertionRequested
    testScope.advanceUntilIdle()

    setupScan.assertIsDisplayed()

    eidFlow.value = EidInteractionEvent.CardRecognized
    testScope.advanceUntilIdle()

    setupScan.setProgress(true).assertIsDisplayed()

    eidFlow.value = EidInteractionEvent.PinRequested(3)
    testScope.advanceUntilIdle()

    eidFlow.value = EidInteractionEvent.PinRequested(2)
    testScope.advanceUntilIdle()

    eidFlow.value = EidInteractionEvent.CardRemoved
    testScope.advanceUntilIdle()

    setupTransportPin.setAttemptsLeft(2).assertIsDisplayed()
    setupTransportPin.transportPinField.assertLength(0)
    testRule.performPinInput(transportPin)
    setupTransportPin.transportPinField.assertLength(transportPin.length)
    testRule.pressReturn()

    eidFlow.value = EidInteractionEvent.CardInsertionRequested
    testScope.advanceUntilIdle()

    setupScan.setProgress(false).assertIsDisplayed()

    eidFlow.value = EidInteractionEvent.CardRecognized
    testScope.advanceUntilIdle()

    setupScan.setProgress(true).assertIsDisplayed()

    eidFlow.value = EidInteractionEvent.NewPinRequested(null)
    testScope.advanceUntilIdle()

    eidFlow.value = EidInteractionEvent.PinChangeSucceeded
    testScope.advanceUntilIdle()
}
