package de.digitalService.useID.userFlowTests.utils.flowParts.ident

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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import org.hamcrest.Matchers

@OptIn(ExperimentalCoroutinesApi::class)
fun runIdentSuccessful(testRule: ComposeTestRule, eidFlow: MutableStateFlow<EidInteractionEvent>, testScope: TestScope) {
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
