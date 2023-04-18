package de.digitalService.useID.userFlowTests.utils.flowParts.ident.helper

import de.digitalService.useID.idCardInterface.*
import de.digitalService.useID.userFlowTests.setupFlows.TestScreen
import de.digitalService.useID.util.ComposeTestRule
import de.digitalService.useID.util.performPinInput
import de.digitalService.useID.util.pressReturn
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle

@OptIn(ExperimentalCoroutinesApi::class)
fun runIdentUpToCanAfterSomeTime(withWrongPersonalPin: Boolean, testRule: ComposeTestRule, eidFlow: MutableStateFlow<EidInteractionEvent>, testScope: TestScope) {

    val personalPin = if (withWrongPersonalPin) "111111" else "123456"

    // Define screens to be tested
    val identificationFetchMetaData = TestScreen.IdentificationFetchMetaData(testRule)
    val identificationAttributeConsent = TestScreen.IdentificationAttributeConsent(testRule)
    val identificationPersonalPin = TestScreen.IdentificationPersonalPin(testRule)
    val identificationScan = TestScreen.Scan(testRule)

    eidFlow.value = EidInteractionEvent.AuthenticationStarted
    testScope.advanceUntilIdle()

    identificationFetchMetaData.assertIsDisplayed()

    eidFlow.value = EidInteractionEvent.AuthenticationRequestConfirmationRequested(
        AuthenticationRequest(
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

    // ENTER CORRECT PIN
    identificationPersonalPin.assertIsDisplayed()
    identificationPersonalPin.personalPinField.assertLength(0)
    testRule.performPinInput(personalPin)
    identificationPersonalPin.personalPinField.assertLength(personalPin.length)
    testRule.pressReturn()

    testScope.advanceUntilIdle()

    identificationScan.setIdentPending(true).setBackAllowed(false).assertIsDisplayed()

    eidFlow.value = EidInteractionEvent.CardRecognized
    testScope.advanceUntilIdle()

    identificationScan.setProgress(true).assertIsDisplayed()

    eidFlow.value = EidInteractionEvent.CanRequested
    testScope.advanceUntilIdle()

    eidFlow.value = EidInteractionEvent.CardRemoved
    testScope.advanceUntilIdle()
}
