package de.digitalService.useID.userFlowTests.identFlows.canceled

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers.*
import androidx.test.espresso.intent.rule.IntentsTestRule
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import de.digitalService.useID.MainActivity
import de.digitalService.useID.StorageManager
import de.digitalService.useID.analytics.TrackerManagerType
import de.digitalService.useID.hilt.CoroutineContextProviderModule
import de.digitalService.useID.hilt.NfcInterfaceMangerModule
import de.digitalService.useID.hilt.SingletonModule
import de.digitalService.useID.idCardInterface.*
import de.digitalService.useID.models.NfcAvailability
import de.digitalService.useID.ui.UseIDApp
import de.digitalService.useID.ui.coordinators.AppCoordinatorType
import de.digitalService.useID.ui.navigation.Navigator
import de.digitalService.useID.userFlowTests.setupFlows.TestScreen
import de.digitalService.useID.util.*
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.hamcrest.Matchers.allOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject


@UninstallModules(SingletonModule::class, CoroutineContextProviderModule::class, NfcInterfaceMangerModule::class)
@HiltAndroidTest
class IdentCanceledOnAttributesConfirmTest {

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Inject
    lateinit var navigator: Navigator

    @Inject
    lateinit var trackerManager: TrackerManagerType

    @Inject
    lateinit var appCoordinator: AppCoordinatorType

    @BindValue
    val mockIdCardManager: IdCardManager = mockk(relaxed = true)

    @BindValue
    val mockStorageManager: StorageManager = mockk(relaxed = true) {
        every { firstTimeUser } returns false
    }

    @BindValue
    val mockCoroutineContextProvider: CoroutineContextProviderType = mockk {
        every { Main } returns Dispatchers.Main
    }

    @BindValue
    val mockNfcInterfaceManager: NfcInterfaceManagerType = mockk(relaxed = true){
        every { nfcAvailability } returns MutableStateFlow(NfcAvailability.Available)
    }

    @Before
    fun before() {
        hiltRule.inject()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun testIdentCanceledOnAttributesConfirm() = runTest {
        every { mockCoroutineContextProvider.IO } returns StandardTestDispatcher(testScheduler)
        every { mockCoroutineContextProvider.Default } returns StandardTestDispatcher(testScheduler)

        val eidFlow = MutableStateFlow<EidInteractionEvent>(EidInteractionEvent.Idle)
        every { mockIdCardManager.eidFlow } returns eidFlow

        composeTestRule.activity.setContentUsingUseIdTheme {
            UseIDApp(
                nfcAvailability = NfcAvailability.Available,
                navigator = navigator,
                trackerManager = trackerManager
            )
        }

        val deepLink = Uri.parse("bundesident://127.0.0.1:24727/eID-Client?tcTokenURL=https%3A%2F%2Feid.digitalservicebund.de%2Fapi%2Fv1%2Fidentification%2Fsessions%2F30d20d97-cf31-4f01-ab27-35dea918bb83%2Ftc-token")

        // Define screens to be tested
        val identificationFetchMetaData = TestScreen.IdentificationFetchMetaData(composeTestRule)
        val identificationAttributeConsent = TestScreen.IdentificationAttributeConsent(composeTestRule)
        val home = TestScreen.Home(composeTestRule)

        home.assertIsDisplayed()

        appCoordinator.handleDeepLink(deepLink)
        advanceUntilIdle()

        eidFlow.value = EidInteractionEvent.AuthenticationStarted
        advanceUntilIdle()

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

        advanceUntilIdle()

        identificationAttributeConsent.assertIsDisplayed()
        identificationAttributeConsent.cancel.click()
        identificationAttributeConsent.navigationConfirmDialog.assertIsDisplayed()
        identificationAttributeConsent.navigationConfirmDialog.dismiss()

        identificationAttributeConsent.assertIsDisplayed()
        identificationAttributeConsent.cancel.click()
        identificationAttributeConsent.navigationConfirmDialog.assertIsDisplayed()
        identificationAttributeConsent.navigationConfirmDialog.confirm()

        home.assertIsDisplayed()
    }
}
