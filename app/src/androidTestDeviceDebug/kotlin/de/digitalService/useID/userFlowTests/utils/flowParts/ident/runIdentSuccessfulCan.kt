package de.digitalService.useID.userFlowTests.utils.flowParts.ident

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import de.digitalService.useID.idCardInterface.EidInteractionEvent
import de.digitalService.useID.userFlowTests.setupFlows.TestScreen
import de.digitalService.useID.userFlowTests.utils.flowParts.ident.helper.runIdentUpToCan
import de.digitalService.useID.util.ComposeTestRule
import de.digitalService.useID.util.performPinInput
import de.digitalService.useID.util.pressReturn
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import org.hamcrest.Matchers

@OptIn(ExperimentalCoroutinesApi::class)
fun runIdentSuccessfulCan(testRule: ComposeTestRule, eidFlow: MutableStateFlow<EidInteractionEvent>, testScope: TestScope) {
    val redirectUrl = "test.url.com"
    val personalPin = "123456"
    val can = "123456"

    // Define screens to be tested
    val identificationPersonalPin = TestScreen.IdentificationPersonalPin(testRule)
    val identificationScan = TestScreen.Scan(testRule)
    val identificationCanPinForgotten = TestScreen.IdentificationCanPinForgotten(testRule)
    val identificationCanIntro = TestScreen.CanIntro(testRule)
    val identificationCanInput = TestScreen.CanInput(testRule)

    runIdentUpToCan(
        testRule = testRule,
        eidFlow = eidFlow,
        testScope = testScope
    )

    identificationCanPinForgotten.assertIsDisplayed()
    identificationCanPinForgotten.tryAgainBtn.click()

    identificationCanIntro.setBackAllowed(true).setIdentPending(true).assertIsDisplayed()
    identificationCanIntro.enterCanNowBtn.click()

    identificationCanInput.assertIsDisplayed()
    identificationCanInput.canEntryField.assertLength(0)
    testRule.performPinInput(can)
    identificationCanInput.canEntryField.assertLength(can.length)
    testRule.pressReturn()

    // ENTER CORRECT PIN 3RD TIME
    identificationPersonalPin.setAttemptsLeft(1).assertIsDisplayed()
    identificationPersonalPin.personalPinField.assertLength(0)
    testRule.performPinInput(personalPin)
    identificationPersonalPin.personalPinField.assertLength(personalPin.length)
    testRule.pressReturn()

    eidFlow.value = EidInteractionEvent.RequestCardInsertion
    testScope.advanceUntilIdle()

    identificationScan
        .setIdentPending(true)
        .setBackAllowed(false)
        .setProgress(false)
        .assertIsDisplayed()

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