package de.digitalService.useID.userFlowTests.utils.flowParts

import de.digitalService.useID.idCardInterface.EidInteractionEvent
import de.digitalService.useID.userFlowTests.setupFlows.TestScreen
import de.digitalService.useID.userFlowTests.utils.flowParts.setup.helper.runSetupUpToCan
import de.digitalService.useID.util.ComposeTestRule
import de.digitalService.useID.util.performPinInput
import de.digitalService.useID.util.pressReturn
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle

@OptIn(ExperimentalCoroutinesApi::class)
fun runSetupSuccessfulCan(testRule: ComposeTestRule, eidFlow: MutableStateFlow<EidInteractionEvent>, testScope: TestScope) {
    val transportPin = "12345"
    val wrongTransportPin = "11111"
    val can = "111222"

    // Define screens to be tested
    val setupTransportPin = TestScreen.SetupTransportPin(testRule)
    val setupScan = TestScreen.Scan(testRule)
    val setupCanConfirmTransportPin = TestScreen.SetupCanConfirmTransportPin(testRule)
    val setupCanIntro = TestScreen.CanIntro(testRule)
    val setupCanInput = TestScreen.CanInput(testRule)

    runSetupUpToCan(
        testRule = testRule,
        eidFlow = eidFlow,
        testScope = testScope
    )

    setupCanConfirmTransportPin.setTransportPin(wrongTransportPin).assertIsDisplayed()
    setupCanConfirmTransportPin.retryInputBtn.click()

    testScope.advanceUntilIdle()

    setupCanIntro.setBackAllowed(true).assertIsDisplayed()
    setupCanIntro.back.click()

    testScope.advanceUntilIdle()

    setupCanConfirmTransportPin.setTransportPin(wrongTransportPin).assertIsDisplayed()
    setupCanConfirmTransportPin.retryInputBtn.click()

    testScope.advanceUntilIdle()

    setupCanIntro.setBackAllowed(true).assertIsDisplayed()
    setupCanIntro.enterCanNowBtn.click()

    testScope.advanceUntilIdle()

    // ENTER CORRECT CAN
    setupCanInput.assertIsDisplayed()
    setupCanInput.canEntryField.assertLength(0)
    testRule.performPinInput(can)
    setupCanInput.canEntryField.assertLength(can.length)
    testRule.pressReturn()

    testScope.advanceUntilIdle()

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

//    eidFlow.value = EidInteractionEvent.RequestChangedPin(null) {_, _ -> }
//    testScope.advanceUntilIdle()

    eidFlow.value = EidInteractionEvent.ProcessCompletedSuccessfullyWithoutResult
    testScope.advanceUntilIdle()
}
