package de.digitalService.useID.coordinator

import com.ramcosta.composedestinations.spec.Direction
import de.digitalService.useID.StorageManager
import de.digitalService.useID.ui.coordinators.*
import de.digitalService.useID.ui.navigation.Navigator
import de.digitalService.useID.ui.screens.destinations.*
import de.digitalService.useID.util.CoroutineContextProvider
import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class SetupCoordinatorTest {

    @MockK(relaxUnitFun = true)
    lateinit var mockNavigator: Navigator

    @MockK(relaxUnitFun = true)
    lateinit var mockPinManagementCoordinator: PinManagementCoordinator

    @MockK(relaxUnitFun = true)
    lateinit var mockIdentificationCoordinator: IdentificationCoordinator

    @MockK(relaxUnitFun = true)
    lateinit var mockStorageManager: StorageManager

    @MockK(relaxUnitFun = true)
    lateinit var mockCoroutineContextProvider: CoroutineContextProvider

    @OptIn(ExperimentalCoroutinesApi::class)
    val dispatcher = StandardTestDispatcher()

    private val navigationDestinationSlot = slot<Direction>()
    private val navigationPoppingDestinationSlot = slot<Direction>()

    @OptIn(ExperimentalCoroutinesApi::class)
    @BeforeEach
    fun beforeEach() {
        Dispatchers.setMain(dispatcher)

        every { mockNavigator.navigate(capture(navigationDestinationSlot)) } returns Unit
        every { mockNavigator.navigatePopping(capture(navigationPoppingDestinationSlot)) } returns Unit
    }

    @Test
    fun startSetupIDCard() {
        val setupCoordinator = SetupCoordinator(
            navigator = mockNavigator,
            pinManagementCoordinator = mockPinManagementCoordinator,
            identificationCoordinator = mockIdentificationCoordinator,
            storageManager = mockStorageManager,
            coroutineContextProvider = mockCoroutineContextProvider
        )

        setupCoordinator.startSetupIdCard()

        verify(exactly = 1) { mockNavigator.navigate(SetupPinLetterDestination) }
    }

    @Test
    fun setupWithPinLetter() { runTest {

        every { mockCoroutineContextProvider.IO } returns dispatcher
        every { mockPinManagementCoordinator.startPinManagement(PinStatus.TransportPin) } returns MutableStateFlow(SubCoordinatorState.Active)

        val setupCoordinator = SetupCoordinator(
            navigator = mockNavigator,
            pinManagementCoordinator = mockPinManagementCoordinator,
            identificationCoordinator = mockIdentificationCoordinator,
            storageManager = mockStorageManager,
            coroutineContextProvider = mockCoroutineContextProvider
        )

        setupCoordinator.setupWithPinLetter()
        delay(1)
        verify(exactly = 1) { mockPinManagementCoordinator.startPinManagement(PinStatus.TransportPin)}
    }}

    @Test
    fun setupWithoutPinLetter() {
        val setupCoordinator = SetupCoordinator(
            navigator = mockNavigator,
            pinManagementCoordinator = mockPinManagementCoordinator,
            identificationCoordinator = mockIdentificationCoordinator,
            storageManager = mockStorageManager,
            coroutineContextProvider = mockCoroutineContextProvider
        )

        setupCoordinator.setupWithoutPinLetter()

        verify(exactly = 1) { mockNavigator.navigate(SetupResetPersonalPinDestination) }
    }

    /*@OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun pinChange() = runTest {
        val setupCoordinator = SetupCoordinator(
            navigator = mockNavigator,
            pinManagementCoordinator = mockPinManagementCoordinator,
            identificationCoordinator = mockIdentificationCoordinator,
            storageManager = mockStorageManager,
            coroutineContextProvider = mockCoroutineContextProvider
        )

        every { mockCoroutineContextProvider.IO } returns dispatcher

        val transportPin = "12345"

        setupCoordinator.onTransportPinEntered(transportPin)

        verify(exactly = 1) { mockAppCoordinator.navigate(SetupPersonalPinIntroDestination) }

        val personalPin = "000000"
        setupCoordinator.onPersonalPinInput(personalPin)

        verify(exactly = 1) { mockAppCoordinator.navigate(SetupPersonalPinConfirmDestination) }

        var progress = false
        val scanJob = setupCoordinator.scanInProgress
            .onEach { progress = it }
            .launchIn(CoroutineScope(dispatcher))

        val idCardManagerFlow = MutableStateFlow<EidInteractionEvent>(EidInteractionEvent.PinManagementStarted)
        every { mockIdCardManager.changePin(mockContext) } returns idCardManagerFlow

        val confirmationResult = setupCoordinator.confirmPersonalPin(personalPin)
        verify(exactly = 1) { mockAppCoordinator.startNfcTagHandling() }
        Assertions.assertTrue(confirmationResult)

        advanceUntilIdle()

        idCardManagerFlow.value = EidInteractionEvent.RequestCardInsertion
        advanceUntilIdle()

        verify(exactly = 1) { mockAppCoordinator.navigatePopping(SetupScanDestination) }
        Assertions.assertFalse(progress)

        idCardManagerFlow.value = EidInteractionEvent.CardRecognized
        advanceUntilIdle()

        Assertions.assertTrue(progress)

        val pinCallback = mockk<(String, String) -> Unit>()
        every { pinCallback(transportPin, personalPin) } just Runs

        idCardManagerFlow.value = EidInteractionEvent.RequestChangedPin(null, pinCallback)
        advanceUntilIdle()

        verify(exactly = 1) { pinCallback(transportPin, personalPin) }

        idCardManagerFlow.value = EidInteractionEvent.ProcessCompletedSuccessfullyWithoutResult
        advanceUntilIdle()

        Assertions.assertFalse(progress)
        verify(exactly = 1) { mockAppCoordinator.setIsNotFirstTimeUser() }
        verify(exactly = 1) { mockAppCoordinator.navigate(SetupFinishDestination) }
        verify(exactly = 1) { mockAppCoordinator.stopNfcTagHandling() }

        scanJob.cancel()
    }*/

    /*@OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun pinChangeIncorrectPinConfirmation() = runTest {
        val setupCoordinator = SetupCoordinator(mockContext, mockAppCoordinator, mockIdCardManager, mockIssueTrackerManager, mockCoroutineContextProvider)

        every { mockCoroutineContextProvider.IO } returns dispatcher

        val transportPin = "12345"

        setupCoordinator.onTransportPinEntered(transportPin)

        verify(exactly = 1) { mockAppCoordinator.navigate(SetupPersonalPinIntroDestination) }

        val personalPin = "000000"
        setupCoordinator.onPersonalPinInput(personalPin)

        verify(exactly = 1) { mockAppCoordinator.navigate(SetupPersonalPinConfirmDestination) }

        val confirmationResult = setupCoordinator.confirmPersonalPin("111111")
        Assertions.assertFalse(confirmationResult)
        verify(exactly = 0) { mockAppCoordinator.startNfcTagHandling() }
    }*/

    /*@OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun pinChangeIncorrectTransportPin() = runTest {
        val setupCoordinator = SetupCoordinator(mockContext, mockAppCoordinator, mockIdCardManager, mockIssueTrackerManager, mockCoroutineContextProvider)

        every { mockCoroutineContextProvider.IO } returns dispatcher

        val transportPin = "12345"
        val personalPin = "000000"

        setupCoordinator.onTransportPinEntered(transportPin)
        setupCoordinator.onPersonalPinInput(personalPin)

        var progress = false
        val scanJob = setupCoordinator.scanInProgress
            .onEach { progress = it }
            .launchIn(CoroutineScope(dispatcher))

        val idCardManagerFlow = MutableStateFlow<EidInteractionEvent>(EidInteractionEvent.PinManagementStarted)
        every { mockIdCardManager.changePin(mockContext) } returns idCardManagerFlow

        val confirmationResult = setupCoordinator.confirmPersonalPin(personalPin)
        verify(exactly = 1) { mockAppCoordinator.startNfcTagHandling() }
        Assertions.assertTrue(confirmationResult)

        advanceUntilIdle()

        idCardManagerFlow.value = EidInteractionEvent.RequestCardInsertion
        advanceUntilIdle()

        verify(exactly = 1) { mockAppCoordinator.navigatePopping(SetupScanDestination) }
        Assertions.assertFalse(progress)

        idCardManagerFlow.value = EidInteractionEvent.CardRecognized
        advanceUntilIdle()

        Assertions.assertTrue(progress)

        val pinCallback = mockk<(String, String) -> Unit>()
        every { pinCallback(transportPin, personalPin) } just Runs

        idCardManagerFlow.value = EidInteractionEvent.RequestChangedPin(null, pinCallback)
        advanceUntilIdle()

        verify(exactly = 1) { pinCallback(transportPin, personalPin) }

        val attempts = 2
        idCardManagerFlow.value = EidInteractionEvent.RequestChangedPin(attempts, pinCallback)
        advanceUntilIdle()

        Assertions.assertFalse(progress)
        verify(exactly = 1) { mockAppCoordinator.stopNfcTagHandling() }

        val navigationParameter = navigationDestinationSlot.captured
        Assertions.assertEquals(SetupTransportPinDestination(attempts).route, navigationParameter.route)

        idCardManagerFlow.value = EidInteractionEvent.PinManagementStarted

        val newTransportPin = "54321"
        setupCoordinator.onTransportPinEntered(newTransportPin)

        advanceUntilIdle()

        verify(exactly = 1) { mockAppCoordinator.navigate(SetupPersonalPinIntroDestination) }
        verify(exactly = 2) { mockIdCardManager.changePin(mockContext) }

        idCardManagerFlow.value = EidInteractionEvent.RequestCardInsertion
        idCardManagerFlow.value = EidInteractionEvent.CardRecognized
        advanceUntilIdle()

        Assertions.assertTrue(progress)

        idCardManagerFlow.value = EidInteractionEvent.RequestChangedPin(null, pinCallback)
        advanceUntilIdle()

        verify(exactly = 1) { pinCallback(newTransportPin, personalPin) }

        scanJob.cancel()
    }*/

    /*@OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun cardSuspended() = runTest {
        val setupCoordinator = SetupCoordinator(mockContext, mockAppCoordinator, mockIdCardManager, mockIssueTrackerManager, mockCoroutineContextProvider)

        every { mockCoroutineContextProvider.IO } returns dispatcher

        val transportPin = "12345"

        setupCoordinator.onTransportPinEntered(transportPin)

        verify(exactly = 1) { mockAppCoordinator.navigate(SetupPersonalPinIntroDestination) }

        val personalPin = "000000"
        setupCoordinator.onPersonalPinInput(personalPin)

        verify(exactly = 1) { mockAppCoordinator.navigate(SetupPersonalPinConfirmDestination) }

        var progress = false
        val scanJob = setupCoordinator.scanInProgress
            .onEach { progress = it }
            .launchIn(CoroutineScope(dispatcher))

        val idCardManagerFlow = MutableStateFlow<EidInteractionEvent>(EidInteractionEvent.PinManagementStarted)
        every { mockIdCardManager.changePin(mockContext) } returns idCardManagerFlow

        val confirmationResult = setupCoordinator.confirmPersonalPin(personalPin)
        verify(exactly = 1) { mockAppCoordinator.startNfcTagHandling() }
        Assertions.assertTrue(confirmationResult)

        advanceUntilIdle()

        idCardManagerFlow.value = EidInteractionEvent.RequestCardInsertion
        advanceUntilIdle()

        verify(exactly = 1) { mockAppCoordinator.navigatePopping(SetupScanDestination) }
        Assertions.assertFalse(progress)

        idCardManagerFlow.value = EidInteractionEvent.CardRecognized
        advanceUntilIdle()

        Assertions.assertTrue(progress)

        idCardManagerFlow.value = EidInteractionEvent.RequestCanAndChangedPin { _, _, _ -> }
        advanceUntilIdle()

        Assertions.assertFalse(progress)
        verify(exactly = 1) { mockAppCoordinator.navigate(SetupCardSuspendedDestination) }

        scanJob.cancel()
    }*/

    /*@OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun cardBlocked() = runTest {
        val setupCoordinator = SetupCoordinator(mockContext, mockAppCoordinator, mockIdCardManager, mockIssueTrackerManager, mockCoroutineContextProvider)

        every { mockCoroutineContextProvider.IO } returns dispatcher

        val transportPin = "12345"

        setupCoordinator.onTransportPinEntered(transportPin)

        verify(exactly = 1) { mockAppCoordinator.navigate(SetupPersonalPinIntroDestination) }

        val personalPin = "000000"
        setupCoordinator.onPersonalPinInput(personalPin)

        verify(exactly = 1) { mockAppCoordinator.navigate(SetupPersonalPinConfirmDestination) }

        var progress = false
        val scanJob = setupCoordinator.scanInProgress
            .onEach { progress = it }
            .launchIn(CoroutineScope(dispatcher))

        val idCardManagerFlow = MutableStateFlow<EidInteractionEvent>(EidInteractionEvent.PinManagementStarted)
        every { mockIdCardManager.changePin(mockContext) } returns idCardManagerFlow

        val confirmationResult = setupCoordinator.confirmPersonalPin(personalPin)
        verify(exactly = 1) { mockAppCoordinator.startNfcTagHandling() }
        Assertions.assertTrue(confirmationResult)

        advanceUntilIdle()

        idCardManagerFlow.value = EidInteractionEvent.RequestCardInsertion
        advanceUntilIdle()

        verify(exactly = 1) { mockAppCoordinator.navigatePopping(SetupScanDestination) }
        Assertions.assertFalse(progress)

        idCardManagerFlow.value = EidInteractionEvent.CardRecognized
        advanceUntilIdle()

        Assertions.assertTrue(progress)

        idCardManagerFlow.value = EidInteractionEvent.RequestPUK { }
        advanceUntilIdle()

        Assertions.assertFalse(progress)
        verify(exactly = 1) { mockAppCoordinator.navigate(SetupCardBlockedDestination) }

        scanJob.cancel()
    }*/

    /*@OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun unexpectedEvent() = runTest {
        val setupCoordinator = SetupCoordinator(mockContext, mockAppCoordinator, mockIdCardManager, mockIssueTrackerManager, mockCoroutineContextProvider)

        every { mockCoroutineContextProvider.IO } returns dispatcher

        val transportPin = "12345"

        setupCoordinator.onTransportPinEntered(transportPin)

        verify(exactly = 1) { mockAppCoordinator.navigate(SetupPersonalPinIntroDestination) }

        val personalPin = "000000"
        setupCoordinator.onPersonalPinInput(personalPin)

        verify(exactly = 1) { mockAppCoordinator.navigate(SetupPersonalPinConfirmDestination) }

        var progress = false
        val scanJob = setupCoordinator.scanInProgress
            .onEach { progress = it }
            .launchIn(CoroutineScope(dispatcher))

        val idCardManagerFlow = MutableStateFlow<EidInteractionEvent>(EidInteractionEvent.PinManagementStarted)
        every { mockIdCardManager.changePin(mockContext) } returns idCardManagerFlow

        val confirmationResult = setupCoordinator.confirmPersonalPin(personalPin)
        verify(exactly = 1) { mockAppCoordinator.startNfcTagHandling() }
        Assertions.assertTrue(confirmationResult)

        advanceUntilIdle()

        idCardManagerFlow.value = EidInteractionEvent.RequestCardInsertion
        advanceUntilIdle()

        verify(exactly = 1) { mockAppCoordinator.navigatePopping(SetupScanDestination) }
        Assertions.assertFalse(progress)

        idCardManagerFlow.value = EidInteractionEvent.CardRecognized
        advanceUntilIdle()

        Assertions.assertTrue(progress)

        idCardManagerFlow.value = EidInteractionEvent.RequestCan { }
        advanceUntilIdle()

        Assertions.assertFalse(progress)
        verify(exactly = 1) { mockAppCoordinator.navigate(SetupOtherErrorDestination) }
        verify(exactly = 1) { mockIssueTrackerManager.capture(idCardManagerFlow.value.redacted) }

        scanJob.cancel()
    }*/

    /*@OptIn(ExperimentalCoroutinesApi::class, ObsoleteCoroutinesApi::class)
    @Test
    fun errorCardDeactivated() = runTest {
        val setupCoordinator = SetupCoordinator(mockContext, mockAppCoordinator, mockIdCardManager, mockIssueTrackerManager, mockCoroutineContextProvider)

        every { mockCoroutineContextProvider.IO } returns dispatcher

        val transportPin = "12345"

        setupCoordinator.onTransportPinEntered(transportPin)

        verify(exactly = 1) { mockAppCoordinator.navigate(SetupPersonalPinIntroDestination) }

        val personalPin = "000000"
        setupCoordinator.onPersonalPinInput(personalPin)

        verify(exactly = 1) { mockAppCoordinator.navigate(SetupPersonalPinConfirmDestination) }

        var progress = false
        val scanJob = setupCoordinator.scanInProgress
            .onEach { progress = it }
            .launchIn(CoroutineScope(dispatcher))

        val idCardManagerChannel: BroadcastChannel<EidInteractionEvent> = BroadcastChannel(1)

        every { mockIdCardManager.changePin(mockContext) } returns idCardManagerChannel.asFlow()

        val confirmationResult = setupCoordinator.confirmPersonalPin(personalPin)
        verify(exactly = 1) { mockAppCoordinator.startNfcTagHandling() }
        Assertions.assertTrue(confirmationResult)

        advanceUntilIdle()

        idCardManagerChannel.send(EidInteractionEvent.RequestCardInsertion)
        advanceUntilIdle()

        verify(exactly = 1) { mockAppCoordinator.navigatePopping(SetupScanDestination) }
        Assertions.assertFalse(progress)

        idCardManagerChannel.cancel(IdCardInteractionException.CardDeactivated)
        advanceUntilIdle()

        Assertions.assertFalse(progress)
        verify(exactly = 1) { mockAppCoordinator.navigate(SetupCardDeactivatedDestination) }

        scanJob.cancel()
    }*/

    /*@OptIn(ExperimentalCoroutinesApi::class, ObsoleteCoroutinesApi::class)
    @Test
    fun errorCardBlocked() = runTest {
        val setupCoordinator = SetupCoordinator(mockContext, mockAppCoordinator, mockIdCardManager, mockIssueTrackerManager, mockCoroutineContextProvider)

        every { mockCoroutineContextProvider.IO } returns dispatcher

        val transportPin = "12345"

        setupCoordinator.onTransportPinEntered(transportPin)

        verify(exactly = 1) { mockAppCoordinator.navigate(SetupPersonalPinIntroDestination) }

        val personalPin = "000000"
        setupCoordinator.onPersonalPinInput(personalPin)

        verify(exactly = 1) { mockAppCoordinator.navigate(SetupPersonalPinConfirmDestination) }

        var progress = false
        val scanJob = setupCoordinator.scanInProgress
            .onEach { progress = it }
            .launchIn(CoroutineScope(dispatcher))

        val idCardManagerChannel: BroadcastChannel<EidInteractionEvent> = BroadcastChannel(1)

        every { mockIdCardManager.changePin(mockContext) } returns idCardManagerChannel.asFlow()

        val confirmationResult = setupCoordinator.confirmPersonalPin(personalPin)
        verify(exactly = 1) { mockAppCoordinator.startNfcTagHandling() }
        Assertions.assertTrue(confirmationResult)

        advanceUntilIdle()

        idCardManagerChannel.send(EidInteractionEvent.RequestCardInsertion)
        advanceUntilIdle()

        verify(exactly = 1) { mockAppCoordinator.navigatePopping(SetupScanDestination) }
        Assertions.assertFalse(progress)

        idCardManagerChannel.cancel(IdCardInteractionException.CardBlocked)
        advanceUntilIdle()

        Assertions.assertFalse(progress)
        verify(exactly = 1) { mockAppCoordinator.navigate(SetupCardBlockedDestination) }

        scanJob.cancel()
    }*/

    /*@OptIn(ExperimentalCoroutinesApi::class, ObsoleteCoroutinesApi::class)
    @Test
    fun errorProcessFailed() = runTest {
        val setupCoordinator = SetupCoordinator(mockContext, mockAppCoordinator, mockIdCardManager, mockIssueTrackerManager, mockCoroutineContextProvider)

        every { mockCoroutineContextProvider.IO } returns dispatcher

        val transportPin = "12345"

        setupCoordinator.onTransportPinEntered(transportPin)

        verify(exactly = 1) { mockAppCoordinator.navigate(SetupPersonalPinIntroDestination) }

        val personalPin = "000000"
        setupCoordinator.onPersonalPinInput(personalPin)

        verify(exactly = 1) { mockAppCoordinator.navigate(SetupPersonalPinConfirmDestination) }

        var progress = false
        val scanJob = setupCoordinator.scanInProgress
            .onEach { progress = it }
            .launchIn(CoroutineScope(dispatcher))

        val idCardManagerChannel: BroadcastChannel<EidInteractionEvent> = BroadcastChannel(1)

        every { mockIdCardManager.changePin(mockContext) } returns idCardManagerChannel.asFlow()

        val confirmationResult = setupCoordinator.confirmPersonalPin(personalPin)
        verify(exactly = 1) { mockAppCoordinator.startNfcTagHandling() }
        Assertions.assertTrue(confirmationResult)

        advanceUntilIdle()

        idCardManagerChannel.send(EidInteractionEvent.RequestCardInsertion)
        advanceUntilIdle()

        verify(exactly = 1) { mockAppCoordinator.navigatePopping(SetupScanDestination) }
        Assertions.assertFalse(progress)

        val exception = IdCardInteractionException.ProcessFailed(ActivationResultCode.INTERNAL_ERROR, null, null)
        idCardManagerChannel.cancel(exception)
        advanceUntilIdle()

        Assertions.assertFalse(progress)
        verify(exactly = 1) { mockIssueTrackerManager.capture(any()) }

        val navigationParameter = navigationPoppingDestinationSlot.captured
        Assertions.assertEquals(SetupCardUnreadableDestination(false).route, navigationParameter.route)

        scanJob.cancel()
    }*/

    /*@Test
    fun onPersonalPinErrorTryAgain() {
        val setupCoordinator = SetupCoordinator(mockContext, mockAppCoordinator, mockIdCardManager, mockIssueTrackerManager, mockCoroutineContextProvider)

        setupCoordinator.onPersonalPinErrorTryAgain()

        verify(exactly = 1) { mockAppCoordinator.pop() }
    }*/

    @Test
    fun finishSetup_noTcTokenUrl() {
        val setupCoordinator = SetupCoordinator(
            navigator = mockNavigator,
            pinManagementCoordinator = mockPinManagementCoordinator,
            identificationCoordinator = mockIdentificationCoordinator,
            storageManager = mockStorageManager,
            coroutineContextProvider = mockCoroutineContextProvider
        )

        setupCoordinator.finishSetup()

        verify(exactly = 1) { mockNavigator.popToRoot() }

        verify(exactly = 0) { mockIdentificationCoordinator.startIdentificationProcess(any(), any()) }
    }

    @Test
    fun finishSetup_withTcTokenUrl() {

        every { mockCoroutineContextProvider.Default } returns dispatcher

        val setupCoordinator = SetupCoordinator(
            navigator = mockNavigator,
            pinManagementCoordinator = mockPinManagementCoordinator,
            identificationCoordinator = mockIdentificationCoordinator,
            storageManager = mockStorageManager,
            coroutineContextProvider = mockCoroutineContextProvider
        )

        val testUrl = "tokenUrl"

        setupCoordinator.showSetupIntro(tcTokenUrl = testUrl)
        setupCoordinator.finishSetup()

        verify(exactly = 0) { mockNavigator.popToRoot() }
        verify(exactly = 1) { mockIdentificationCoordinator.startIdentificationProcess(testUrl, false) }
    }

    @Test
    fun finishSetup_withTcTokenUrlTwice() { runTest {

            every { mockCoroutineContextProvider.Default } returns dispatcher
            every { mockIdentificationCoordinator.stateFlow } returns MutableStateFlow(SubCoordinatorState.Finished)

            val setupCoordinator = SetupCoordinator(
                navigator = mockNavigator,
                pinManagementCoordinator = mockPinManagementCoordinator,
                identificationCoordinator = mockIdentificationCoordinator,
                storageManager = mockStorageManager,
                coroutineContextProvider = mockCoroutineContextProvider
            )

            val testUrl = "tokenUrl"

            setupCoordinator.showSetupIntro(tcTokenUrl = testUrl)
            setupCoordinator.finishSetup()
            delay(1)
            setupCoordinator.finishSetup()

            verify(exactly = 1) { mockIdentificationCoordinator.startIdentificationProcess(testUrl, false) }
            verify(exactly = 1) { mockNavigator.popToRoot() }
        }
    }

    @Test
    fun onBack() {
        val setupCoordinator = SetupCoordinator(
            navigator = mockNavigator,
            pinManagementCoordinator = mockPinManagementCoordinator,
            identificationCoordinator = mockIdentificationCoordinator,
            storageManager = mockStorageManager,
            coroutineContextProvider = mockCoroutineContextProvider
        )

        setupCoordinator.onBackClicked()

        verify(exactly = 1) { mockNavigator.pop() }
    }

    @Test
    fun cancelSetup() {
        val setupCoordinator = SetupCoordinator(
            navigator = mockNavigator,
            pinManagementCoordinator = mockPinManagementCoordinator,
            identificationCoordinator = mockIdentificationCoordinator,
            storageManager = mockStorageManager,
            coroutineContextProvider = mockCoroutineContextProvider
        )

        val testUrl = "tokenUrl"

        setupCoordinator.showSetupIntro(tcTokenUrl = testUrl)
        setupCoordinator.cancelSetup()

        verify(exactly = 0) { mockStorageManager.setIsNotFirstTimeUser() }
        verify(exactly = 1) { mockNavigator.popToRoot() }

        verify(exactly = 0) { mockIdentificationCoordinator.startIdentificationProcess(testUrl, false) }

        setupCoordinator.cancelSetup()

        verify(exactly = 2) { mockNavigator.popToRoot() }
    }

    @Test
    fun hasToken() {
        val setupCoordinator = SetupCoordinator(
            navigator = mockNavigator,
            pinManagementCoordinator = mockPinManagementCoordinator,
            identificationCoordinator = mockIdentificationCoordinator,
            storageManager = mockStorageManager,
            coroutineContextProvider = mockCoroutineContextProvider
        )

        val testUrl = "tokenUrl"

        Assertions.assertFalse(setupCoordinator.identificationPending)

        setupCoordinator.showSetupIntro(testUrl)

        Assertions.assertTrue(setupCoordinator.identificationPending)
    }
}
