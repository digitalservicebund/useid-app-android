package de.digitalService.useID.userFlowTests.utils.flowParts.setup.helper

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
