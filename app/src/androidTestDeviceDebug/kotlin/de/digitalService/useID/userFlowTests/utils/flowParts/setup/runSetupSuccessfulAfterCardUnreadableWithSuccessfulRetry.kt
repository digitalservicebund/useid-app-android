package de.digitalService.useID.userFlowTests.utils.flowParts.setup

import de.digitalService.useID.idCardInterface.EidInteractionEvent
import de.digitalService.useID.idCardInterface.IdCardInteractionException
import de.digitalService.useID.userFlowTests.setupFlows.TestScreen
import de.digitalService.useID.util.ComposeTestRule
import de.digitalService.useID.util.performPinInput
import de.digitalService.useID.util.pressReturn
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import org.openecard.mobile.activation.ActivationResultCode

@OptIn(ExperimentalCoroutinesApi::class)
fun runSetupSuccessfulAfterCardUnreadableWithSuccessfulRetry(testRule: ComposeTestRule, eidFlow: MutableStateFlow<EidInteractionEvent>, testScope: TestScope) {
    val wrongTransportPin = "11111"
    val personalPin = "123456"

    // Define screens to be tested
    val setupIntro = TestScreen.SetupIntro(testRule)
    val setupPinLetter = TestScreen.SetupPinLetter(testRule)
    val setupTransportPin = TestScreen.SetupTransportPin(testRule)
    val setupPersonalPinIntro = TestScreen.SetupPersonalPinIntro(testRule)
    val setupPersonalPinInput = TestScreen.SetupPersonalPinInput(testRule)
    val setupPersonalPinConfirm = TestScreen.SetupPersonalPinConfirm(testRule)
    val errorCardUnreadable = TestScreen.ErrorCardUnreadable(testRule)
    val setupScan = TestScreen.Scan(testRule)

    setupIntro.assertIsDisplayed()
    setupIntro.setupIdBtn.click()

    setupPinLetter.assertIsDisplayed()
    setupPinLetter.letterPresentBtn.click()

    testScope.advanceUntilIdle()

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

    eidFlow.value = EidInteractionEvent.CardInsertionRequested
    testScope.advanceUntilIdle()

    setupScan.assertIsDisplayed()

    eidFlow.value = EidInteractionEvent.CardRecognized
    testScope.advanceUntilIdle()

    setupScan.setProgress(true).assertIsDisplayed()

    eidFlow.value = EidInteractionEvent.CardRemoved
    testScope.advanceUntilIdle()

    eidFlow.value = EidInteractionEvent.Error(
        IdCardInteractionException.ProcessFailed(
            resultCode = ActivationResultCode.CLIENT_ERROR,
            redirectUrl = null,
            resultMinor = null
        )
    )

    testScope.advanceUntilIdle()

    errorCardUnreadable.assertIsDisplayed()
    errorCardUnreadable.closeBtn.click()

    eidFlow.value = EidInteractionEvent.CardInsertionRequested
    testScope.advanceUntilIdle()

    setupScan.setProgress(false).setBackAllowed(true).assertIsDisplayed() // TODO: navigating back should be possible here. Ticket: https://digitalservicebund.atlassian.net/browse/USEID-907

    eidFlow.value = EidInteractionEvent.CardRecognized
    testScope.advanceUntilIdle()

    setupScan.setProgress(true).assertIsDisplayed()

    eidFlow.value = EidInteractionEvent.RequestChangedPin(null) {_, _ -> }
    testScope.advanceUntilIdle()

    eidFlow.value = EidInteractionEvent.ProcessCompletedSuccessfullyWithoutResult
    testScope.advanceUntilIdle()
}
