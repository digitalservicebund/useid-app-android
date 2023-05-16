package de.digitalService.useID.userFlowTests.utils.flowParts.ident

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import de.digitalService.useID.idCardInterface.*
import de.digitalService.useID.userFlowTests.utils.TestScreen
import de.digitalService.useID.util.ComposeTestRule
import de.digitalService.useID.util.performPinInput
import de.digitalService.useID.util.pressReturn
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import org.hamcrest.Matchers

@OptIn(ExperimentalCoroutinesApi::class)
fun runIdentSuccessfulAfterPersonalPinIncorrectAndThenCorrect(testRule: ComposeTestRule, eidFlow: MutableStateFlow<EidInteractionEvent>, testScope: TestScope) {
    val redirectUrl = "test.url.com"
    val personalPin = "123456"
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

    eidFlow.value = EidInteractionEvent.PinRequested(3)
    testScope.advanceUntilIdle()

    eidFlow.value = EidInteractionEvent.PinRequested(2)
    testScope.advanceUntilIdle()

    eidFlow.value = EidInteractionEvent.CardRemoved
    testScope.advanceUntilIdle()

    identificationPersonalPin.setAttemptsLeft(2).assertIsDisplayed()
    identificationPersonalPin.personalPinField.assertLength(0)
    testRule.performPinInput(personalPin)
    identificationPersonalPin.personalPinField.assertLength(personalPin.length)
    testRule.pressReturn()

    eidFlow.value = EidInteractionEvent.CardInsertionRequested
    testScope.advanceUntilIdle()

    identificationScan.setProgress(false).assertIsDisplayed() // TODO: when this flow is ran after the setup flow, a progress indicator is shown here but shouldn be! Ticket: https://digitalservicebund.atlassian.net/browse/USEID-907

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

    eidFlow.value = EidInteractionEvent.IdentificationSucceededWithRedirect(redirectUrl)
    testScope.advanceUntilIdle()
}
