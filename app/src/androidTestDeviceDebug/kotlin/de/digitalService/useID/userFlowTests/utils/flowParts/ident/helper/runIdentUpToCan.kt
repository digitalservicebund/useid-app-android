package de.digitalService.useID.userFlowTests.utils.flowParts.ident.helper

import de.digitalService.useID.idCardInterface.AuthenticationTerms
import de.digitalService.useID.idCardInterface.EidAuthenticationRequest
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
fun runIdentUpToCan(testRule: ComposeTestRule, eidFlow: MutableStateFlow<EidInteractionEvent>, testScope: TestScope) {
    val wrongPersonalPin = "111111"

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

    // ENTER WRONG PIN 1ST TIME
    identificationPersonalPin.assertIsDisplayed()
    identificationPersonalPin.personalPinField.assertLength(0)
    testRule.performPinInput(wrongPersonalPin)
    identificationPersonalPin.personalPinField.assertLength(wrongPersonalPin.length)
    testRule.pressReturn()

    eidFlow.value = EidInteractionEvent.RequestCardInsertion
    testScope.advanceUntilIdle()

    identificationScan.setIdentPending(true).setBackAllowed(false).assertIsDisplayed()

    eidFlow.value = EidInteractionEvent.CardRecognized
    testScope.advanceUntilIdle()

    identificationScan.setProgress(true).assertIsDisplayed()

    eidFlow.value = EidInteractionEvent.RequestPin(attempts = 2, pinCallback = {})
    testScope.advanceUntilIdle()

    // ENTER WRONG PIN 2ND TIME
    identificationPersonalPin.setAttemptsLeft(2).assertIsDisplayed()
    identificationPersonalPin.personalPinField.assertLength(0)
    testRule.performPinInput(wrongPersonalPin)
    identificationPersonalPin.personalPinField.assertLength(wrongPersonalPin.length)
    testRule.pressReturn()

    eidFlow.value = EidInteractionEvent.RequestCardInsertion
    testScope.advanceUntilIdle()

    identificationScan.setProgress(false).assertIsDisplayed() // TODO: there should no progress spinner be shown at this point!

    eidFlow.value = EidInteractionEvent.CardRecognized
    testScope.advanceUntilIdle()

    identificationScan.setProgress(true).assertIsDisplayed()

    eidFlow.value = EidInteractionEvent.RequestPinAndCan { _, _ -> }
    testScope.advanceUntilIdle()
}
