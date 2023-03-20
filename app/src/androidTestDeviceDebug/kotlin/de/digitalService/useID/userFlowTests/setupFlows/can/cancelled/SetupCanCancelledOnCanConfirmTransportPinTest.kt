package de.digitalService.useID.userFlowTests.setupFlows.can.cancelled

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import de.digitalService.useID.MainActivity
import de.digitalService.useID.StorageManager
import de.digitalService.useID.analytics.TrackerManagerType
import de.digitalService.useID.hilt.CoroutineContextProviderModule
import de.digitalService.useID.hilt.SingletonModule
import de.digitalService.useID.idCardInterface.EidInteractionEvent
import de.digitalService.useID.idCardInterface.IdCardManager
import de.digitalService.useID.models.NfcAvailability
import de.digitalService.useID.ui.UseIDApp
import de.digitalService.useID.ui.navigation.Navigator
import de.digitalService.useID.userFlowTests.setupFlows.TestScreen
import de.digitalService.useID.userFlowTests.utils.flowParts.setup.helper.runSetupUpToCan
import de.digitalService.useID.util.*
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@UninstallModules(SingletonModule::class, CoroutineContextProviderModule::class)
@HiltAndroidTest
class SetupCanCancelledOnCanConfirmTransportPinTest {

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Inject
    lateinit var navigator: Navigator

    @Inject
    lateinit var trackerManager: TrackerManagerType

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

    @Before
    fun before() {
        hiltRule.inject()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun testSetupCanCancelledOnCanConfirmTransportPin() = runTest {
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

        val wrongTransportPin = "11111"

        // Define screens to be tested
        val setupCanConfirmTransportPin = TestScreen.SetupCanConfirmTransportPin(composeTestRule)
        val home = TestScreen.Home(composeTestRule)

        home.assertIsDisplayed()
        home.setupIdBtn.click()

        advanceUntilIdle()

        runSetupUpToCan(
            testRule = composeTestRule,
            eidFlow = eidFlow,
            testScope = this
        )

        setupCanConfirmTransportPin.setTransportPin(wrongTransportPin).assertIsDisplayed()
        setupCanConfirmTransportPin.cancel.click()
        setupCanConfirmTransportPin.navigationConfirmDialog.assertIsDisplayed()
        setupCanConfirmTransportPin.navigationConfirmDialog.dismiss()

        setupCanConfirmTransportPin.setTransportPin(wrongTransportPin).assertIsDisplayed()
        setupCanConfirmTransportPin.cancel.click()
        setupCanConfirmTransportPin.navigationConfirmDialog.assertIsDisplayed()
        setupCanConfirmTransportPin.navigationConfirmDialog.confirm()

        advanceUntilIdle()

        home.assertIsDisplayed()
    }
}
