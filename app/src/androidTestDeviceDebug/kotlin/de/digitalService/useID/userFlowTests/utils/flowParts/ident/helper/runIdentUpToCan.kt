package de.digitalService.useID.userFlowTests.utils.flowParts.ident.helper

import de.digitalService.useID.idCardInterface.*
import de.digitalService.useID.userFlowTests.utils.TestScreen
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


    eidFlow.value = EidInteractionEvent.IdentificationStarted
    testScope.advanceUntilIdle()

    identificationFetchMetaData.assertIsDisplayed()

    eidFlow.value = EidInteractionEvent.IdentificationRequestConfirmationRequested(
        IdentificationRequest(
            TestScreen.IdentificationAttributeConsent.RequestData.requiredAttributes,
            TestScreen.IdentificationAttributeConsent.RequestData.transactionInfo
        )
    )

    testScope.advanceUntilIdle()

    eidFlow.value = EidInteractionEvent.CertificateDescriptionReceived(
        CertificateDescription(
            TestScreen.IdentificationAttributeConsent.CertificateDescription.issuerName,
            TestScreen.IdentificationAttributeConsent.CertificateDescription.issuerUrl,
            TestScreen.IdentificationAttributeConsent.CertificateDescription.purpose,
            TestScreen.IdentificationAttributeConsent.CertificateDescription.subjectName,
            TestScreen.IdentificationAttributeConsent.CertificateDescription.subjectUrl,
            TestScreen.IdentificationAttributeConsent.CertificateDescription.termsOfUsage,
        )
    )

    testScope.advanceUntilIdle()

    identificationAttributeConsent.assertIsDisplayed()
    identificationAttributeConsent.continueBtn.click()

    testScope.advanceUntilIdle()

    // ENTER WRONG PIN 1ST TIME
    identificationPersonalPin.assertIsDisplayed()
    identificationPersonalPin.personalPinField.assertLength(0)
    testRule.performPinInput(wrongPersonalPin)
    identificationPersonalPin.personalPinField.assertLength(wrongPersonalPin.length)
    testRule.pressReturn()

    testScope.advanceUntilIdle()

    identificationScan.setIdentPending(true).setBackAllowed(false).assertIsDisplayed()

    eidFlow.value = EidInteractionEvent.CardRecognized
    testScope.advanceUntilIdle()

    identificationScan.setProgress(true).assertIsDisplayed()

    eidFlow.value = EidInteractionEvent.PinRequested(attempts = 3)
    testScope.advanceUntilIdle()

    eidFlow.value = EidInteractionEvent.CardInsertionRequested

    eidFlow.value = EidInteractionEvent.CardRemoved
    testScope.advanceUntilIdle()

    eidFlow.value = EidInteractionEvent.PinRequested(attempts = 2)
    testScope.advanceUntilIdle()
    // ENTER WRONG PIN 2ND TIME
    identificationPersonalPin.setAttemptsLeft(2).assertIsDisplayed()
    identificationPersonalPin.personalPinField.assertLength(0)
    testRule.performPinInput(wrongPersonalPin)
    identificationPersonalPin.personalPinField.assertLength(wrongPersonalPin.length)
    testRule.pressReturn()

    testScope.advanceUntilIdle()

    identificationScan.setProgress(false).assertIsDisplayed() // TODO: there should no progress spinner be shown at this point! Ticket: https://digitalservicebund.atlassian.net/browse/USEID-907

    eidFlow.value = EidInteractionEvent.CardRecognized
    testScope.advanceUntilIdle()

    identificationScan.setProgress(true).assertIsDisplayed()

    eidFlow.value = EidInteractionEvent.CanRequested()
    testScope.advanceUntilIdle()

    eidFlow.value = EidInteractionEvent.CardRemoved
    testScope.advanceUntilIdle()
}
