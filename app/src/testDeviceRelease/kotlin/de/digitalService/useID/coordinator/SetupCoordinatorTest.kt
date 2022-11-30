package de.digitalService.useID.coordinator

import android.content.Context
import com.ramcosta.composedestinations.spec.Direction
import de.digitalService.useID.analytics.IssueTrackerManagerType
import de.digitalService.useID.idCardInterface.EIDInteractionEvent
import de.digitalService.useID.idCardInterface.IDCardInteractionException
import de.digitalService.useID.idCardInterface.IDCardManager
import de.digitalService.useID.ui.coordinators.AppCoordinator
import de.digitalService.useID.ui.coordinators.SetupCoordinator
import de.digitalService.useID.ui.screens.destinations.*
import de.digitalService.useID.util.CoroutineContextProvider
import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.openecard.mobile.activation.ActivationResultCode

@ExtendWith(MockKExtension::class)
class SetupCoordinatorTest {

    @MockK(relaxUnitFun = true)
    lateinit var mockAppCoordinator: AppCoordinator

    @MockK(relaxUnitFun = true)
    lateinit var mockIdCardManager: IDCardManager

    @MockK(relaxUnitFun = true)
    lateinit var mockIssueTrackerManager: IssueTrackerManagerType

    @MockK(relaxUnitFun = true)
    lateinit var mockCoroutineContextProvider: CoroutineContextProvider

    @MockK(relaxUnitFun = true)
    lateinit var mockContext: Context

    @OptIn(ExperimentalCoroutinesApi::class)
    val dispatcher = StandardTestDispatcher()

    private val navigationDestinationSlot = slot<Direction>()
    private val navigationPoppingDestinationSlot = slot<Direction>()

    @OptIn(ExperimentalCoroutinesApi::class)
    @BeforeEach
    fun beforeEach() {
        Dispatchers.setMain(dispatcher)

        every { mockAppCoordinator.navigate(capture(navigationDestinationSlot)) } returns Unit
        every { mockAppCoordinator.navigatePopping(capture(navigationPoppingDestinationSlot)) } returns Unit
    }

    @Test
    fun startSetupIDCard() {
        val setupCoordinator = SetupCoordinator(mockContext, mockAppCoordinator, mockIdCardManager, mockIssueTrackerManager, mockCoroutineContextProvider)

        setupCoordinator.startSetupIDCard()

        verify(exactly = 1) { mockAppCoordinator.navigate(SetupPINLetterDestination) }
    }

    @Test
    fun setupWithPINLetter() {
        val setupCoordinator = SetupCoordinator(mockContext, mockAppCoordinator, mockIdCardManager, mockIssueTrackerManager, mockCoroutineContextProvider)

        setupCoordinator.setupWithPINLetter()

        verify(exactly = 1) { mockAppCoordinator.navigate(any()) }

        val navigationParameter = navigationDestinationSlot.captured
        Assertions.assertEquals(SetupTransportPINDestination(null).route, navigationParameter.route)
    }

    @Test
    fun setupWithoutPINLetter() {
        val setupCoordinator = SetupCoordinator(mockContext, mockAppCoordinator, mockIdCardManager, mockIssueTrackerManager, mockCoroutineContextProvider)

        setupCoordinator.setupWithoutPINLetter()

        verify(exactly = 1) { mockAppCoordinator.navigate(SetupResetPersonalPINDestination) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun pinChange() = runTest {
        val setupCoordinator = SetupCoordinator(mockContext, mockAppCoordinator, mockIdCardManager, mockIssueTrackerManager, mockCoroutineContextProvider)

        every { mockCoroutineContextProvider.IO } returns dispatcher

        val transportPin = "12345"

        setupCoordinator.onTransportPINEntered(transportPin)

        verify(exactly = 1) { mockAppCoordinator.navigate(SetupPersonalPINIntroDestination) }

        val personalPin = "000000"
        setupCoordinator.onPersonalPinInput(personalPin)

        verify(exactly = 1) { mockAppCoordinator.navigate(SetupPersonalPinConfirmDestination) }

        var progress = false
        val scanJob = setupCoordinator.scanInProgress
            .onEach { progress = it }
            .launchIn(CoroutineScope(dispatcher))

        val idCardManagerFlow = MutableStateFlow<EIDInteractionEvent>(EIDInteractionEvent.PINManagementStarted)
        every { mockIdCardManager.changePin(mockContext) } returns idCardManagerFlow

        val confirmationResult = setupCoordinator.confirmPersonalPin(personalPin)
        verify(exactly = 1) { mockAppCoordinator.startNFCTagHandling() }
        Assertions.assertTrue(confirmationResult)

        advanceUntilIdle()

        idCardManagerFlow.value = EIDInteractionEvent.RequestCardInsertion
        advanceUntilIdle()

        verify(exactly = 1) { mockAppCoordinator.navigatePopping(SetupScanDestination) }
        Assertions.assertFalse(progress)

        idCardManagerFlow.value = EIDInteractionEvent.CardRecognized
        advanceUntilIdle()

        Assertions.assertTrue(progress)

        val pinCallback = mockk<(String, String) -> Unit>()
        every { pinCallback(transportPin, personalPin) } just Runs

        idCardManagerFlow.value = EIDInteractionEvent.RequestChangedPIN(null, pinCallback)
        advanceUntilIdle()

        verify(exactly = 1) { pinCallback(transportPin, personalPin) }

        idCardManagerFlow.value = EIDInteractionEvent.ProcessCompletedSuccessfullyWithoutResult
        advanceUntilIdle()

        Assertions.assertFalse(progress)
        verify(exactly = 1) { mockAppCoordinator.setIsNotFirstTimeUser() }
        verify(exactly = 1) { mockAppCoordinator.navigate(SetupFinishDestination) }
        verify(exactly = 1) { mockAppCoordinator.stopNFCTagHandling() }

        scanJob.cancel()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun pinChangeIncorrectPinConfirmation() = runTest {
        val setupCoordinator = SetupCoordinator(mockContext, mockAppCoordinator, mockIdCardManager, mockIssueTrackerManager, mockCoroutineContextProvider)

        every { mockCoroutineContextProvider.IO } returns dispatcher

        val transportPin = "12345"

        setupCoordinator.onTransportPINEntered(transportPin)

        verify(exactly = 1) { mockAppCoordinator.navigate(SetupPersonalPINIntroDestination) }

        val personalPin = "000000"
        setupCoordinator.onPersonalPinInput(personalPin)

        verify(exactly = 1) { mockAppCoordinator.navigate(SetupPersonalPinConfirmDestination) }

        val confirmationResult = setupCoordinator.confirmPersonalPin("111111")
        Assertions.assertFalse(confirmationResult)
        verify(exactly = 0) { mockAppCoordinator.startNFCTagHandling() }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun pinChangeIncorrectTransportPin() = runTest {
        val setupCoordinator = SetupCoordinator(mockContext, mockAppCoordinator, mockIdCardManager, mockIssueTrackerManager, mockCoroutineContextProvider)

        every { mockCoroutineContextProvider.IO } returns dispatcher

        val transportPin = "12345"
        val personalPin = "000000"

        setupCoordinator.onTransportPINEntered(transportPin)
        setupCoordinator.onPersonalPinInput(personalPin)

        var progress = false
        val scanJob = setupCoordinator.scanInProgress
            .onEach { progress = it }
            .launchIn(CoroutineScope(dispatcher))

        val idCardManagerFlow = MutableStateFlow<EIDInteractionEvent>(EIDInteractionEvent.PINManagementStarted)
        every { mockIdCardManager.changePin(mockContext) } returns idCardManagerFlow

        val confirmationResult = setupCoordinator.confirmPersonalPin(personalPin)
        verify(exactly = 1) { mockAppCoordinator.startNFCTagHandling() }
        Assertions.assertTrue(confirmationResult)

        advanceUntilIdle()

        idCardManagerFlow.value = EIDInteractionEvent.RequestCardInsertion
        advanceUntilIdle()

        verify(exactly = 1) { mockAppCoordinator.navigatePopping(SetupScanDestination) }
        Assertions.assertFalse(progress)

        idCardManagerFlow.value = EIDInteractionEvent.CardRecognized
        advanceUntilIdle()

        Assertions.assertTrue(progress)

        val pinCallback = mockk<(String, String) -> Unit>()
        every { pinCallback(transportPin, personalPin) } just Runs

        idCardManagerFlow.value = EIDInteractionEvent.RequestChangedPIN(null, pinCallback)
        advanceUntilIdle()

        verify(exactly = 1) { pinCallback(transportPin, personalPin) }

        val attempts = 2
        idCardManagerFlow.value = EIDInteractionEvent.RequestChangedPIN(attempts, pinCallback)
        advanceUntilIdle()

        Assertions.assertFalse(progress)
        verify(exactly = 1) { mockAppCoordinator.stopNFCTagHandling() }

        val navigationParameter = navigationDestinationSlot.captured
        Assertions.assertEquals(SetupTransportPINDestination(attempts).route, navigationParameter.route)

        idCardManagerFlow.value = EIDInteractionEvent.PINManagementStarted

        val newTransportPin = "54321"
        setupCoordinator.onTransportPINEntered(newTransportPin)

        advanceUntilIdle()

        verify(exactly = 1) { mockAppCoordinator.navigate(SetupPersonalPINIntroDestination) }
        verify(exactly = 2) { mockIdCardManager.changePin(mockContext) }

        idCardManagerFlow.value = EIDInteractionEvent.RequestCardInsertion
        idCardManagerFlow.value = EIDInteractionEvent.CardRecognized
        advanceUntilIdle()

        Assertions.assertTrue(progress)

        idCardManagerFlow.value = EIDInteractionEvent.RequestChangedPIN(null, pinCallback)
        advanceUntilIdle()

        verify(exactly = 1) { pinCallback(newTransportPin, personalPin) }

        scanJob.cancel()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun cardSuspended() = runTest {
        val setupCoordinator = SetupCoordinator(mockContext, mockAppCoordinator, mockIdCardManager, mockIssueTrackerManager, mockCoroutineContextProvider)

        every { mockCoroutineContextProvider.IO } returns dispatcher

        val transportPin = "12345"

        setupCoordinator.onTransportPINEntered(transportPin)

        verify(exactly = 1) { mockAppCoordinator.navigate(SetupPersonalPINIntroDestination) }

        val personalPin = "000000"
        setupCoordinator.onPersonalPinInput(personalPin)

        verify(exactly = 1) { mockAppCoordinator.navigate(SetupPersonalPinConfirmDestination) }

        var progress = false
        val scanJob = setupCoordinator.scanInProgress
            .onEach { progress = it }
            .launchIn(CoroutineScope(dispatcher))

        val idCardManagerFlow = MutableStateFlow<EIDInteractionEvent>(EIDInteractionEvent.PINManagementStarted)
        every { mockIdCardManager.changePin(mockContext) } returns idCardManagerFlow

        val confirmationResult = setupCoordinator.confirmPersonalPin(personalPin)
        verify(exactly = 1) { mockAppCoordinator.startNFCTagHandling() }
        Assertions.assertTrue(confirmationResult)

        advanceUntilIdle()

        idCardManagerFlow.value = EIDInteractionEvent.RequestCardInsertion
        advanceUntilIdle()

        verify(exactly = 1) { mockAppCoordinator.navigatePopping(SetupScanDestination) }
        Assertions.assertFalse(progress)

        idCardManagerFlow.value = EIDInteractionEvent.CardRecognized
        advanceUntilIdle()

        Assertions.assertTrue(progress)

        idCardManagerFlow.value = EIDInteractionEvent.RequestCANAndChangedPIN { _, _, _ -> }
        advanceUntilIdle()

        Assertions.assertFalse(progress)
        verify(exactly = 1) { mockAppCoordinator.navigate(SetupCardSuspendedDestination) }

        scanJob.cancel()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun cardBlocked() = runTest {
        val setupCoordinator = SetupCoordinator(mockContext, mockAppCoordinator, mockIdCardManager, mockIssueTrackerManager, mockCoroutineContextProvider)

        every { mockCoroutineContextProvider.IO } returns dispatcher

        val transportPin = "12345"

        setupCoordinator.onTransportPINEntered(transportPin)

        verify(exactly = 1) { mockAppCoordinator.navigate(SetupPersonalPINIntroDestination) }

        val personalPin = "000000"
        setupCoordinator.onPersonalPinInput(personalPin)

        verify(exactly = 1) { mockAppCoordinator.navigate(SetupPersonalPinConfirmDestination) }

        var progress = false
        val scanJob = setupCoordinator.scanInProgress
            .onEach { progress = it }
            .launchIn(CoroutineScope(dispatcher))

        val idCardManagerFlow = MutableStateFlow<EIDInteractionEvent>(EIDInteractionEvent.PINManagementStarted)
        every { mockIdCardManager.changePin(mockContext) } returns idCardManagerFlow

        val confirmationResult = setupCoordinator.confirmPersonalPin(personalPin)
        verify(exactly = 1) { mockAppCoordinator.startNFCTagHandling() }
        Assertions.assertTrue(confirmationResult)

        advanceUntilIdle()

        idCardManagerFlow.value = EIDInteractionEvent.RequestCardInsertion
        advanceUntilIdle()

        verify(exactly = 1) { mockAppCoordinator.navigatePopping(SetupScanDestination) }
        Assertions.assertFalse(progress)

        idCardManagerFlow.value = EIDInteractionEvent.CardRecognized
        advanceUntilIdle()

        Assertions.assertTrue(progress)

        idCardManagerFlow.value = EIDInteractionEvent.RequestPUK { }
        advanceUntilIdle()

        Assertions.assertFalse(progress)
        verify(exactly = 1) { mockAppCoordinator.navigate(SetupCardBlockedDestination) }

        scanJob.cancel()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun unexpectedEvent() = runTest {
        val setupCoordinator = SetupCoordinator(mockContext, mockAppCoordinator, mockIdCardManager, mockIssueTrackerManager, mockCoroutineContextProvider)

        every { mockCoroutineContextProvider.IO } returns dispatcher

        val transportPin = "12345"

        setupCoordinator.onTransportPINEntered(transportPin)

        verify(exactly = 1) { mockAppCoordinator.navigate(SetupPersonalPINIntroDestination) }

        val personalPin = "000000"
        setupCoordinator.onPersonalPinInput(personalPin)

        verify(exactly = 1) { mockAppCoordinator.navigate(SetupPersonalPinConfirmDestination) }

        var progress = false
        val scanJob = setupCoordinator.scanInProgress
            .onEach { progress = it }
            .launchIn(CoroutineScope(dispatcher))

        val idCardManagerFlow = MutableStateFlow<EIDInteractionEvent>(EIDInteractionEvent.PINManagementStarted)
        every { mockIdCardManager.changePin(mockContext) } returns idCardManagerFlow

        val confirmationResult = setupCoordinator.confirmPersonalPin(personalPin)
        verify(exactly = 1) { mockAppCoordinator.startNFCTagHandling() }
        Assertions.assertTrue(confirmationResult)

        advanceUntilIdle()

        idCardManagerFlow.value = EIDInteractionEvent.RequestCardInsertion
        advanceUntilIdle()

        verify(exactly = 1) { mockAppCoordinator.navigatePopping(SetupScanDestination) }
        Assertions.assertFalse(progress)

        idCardManagerFlow.value = EIDInteractionEvent.CardRecognized
        advanceUntilIdle()

        Assertions.assertTrue(progress)

        idCardManagerFlow.value = EIDInteractionEvent.RequestCAN { }
        advanceUntilIdle()

        Assertions.assertFalse(progress)
        verify(exactly = 1) { mockAppCoordinator.navigate(SetupOtherErrorDestination) }
        verify(exactly = 1) { mockIssueTrackerManager.capture(idCardManagerFlow.value.redacted) }

        scanJob.cancel()
    }

    @OptIn(ExperimentalCoroutinesApi::class, ObsoleteCoroutinesApi::class)
    @Test
    fun errorCardDeactivated() = runTest {
        val setupCoordinator = SetupCoordinator(mockContext, mockAppCoordinator, mockIdCardManager, mockIssueTrackerManager, mockCoroutineContextProvider)

        every { mockCoroutineContextProvider.IO } returns dispatcher

        val transportPin = "12345"

        setupCoordinator.onTransportPINEntered(transportPin)

        verify(exactly = 1) { mockAppCoordinator.navigate(SetupPersonalPINIntroDestination) }

        val personalPin = "000000"
        setupCoordinator.onPersonalPinInput(personalPin)

        verify(exactly = 1) { mockAppCoordinator.navigate(SetupPersonalPinConfirmDestination) }

        var progress = false
        val scanJob = setupCoordinator.scanInProgress
            .onEach { progress = it }
            .launchIn(CoroutineScope(dispatcher))

        val idCardManagerChannel: BroadcastChannel<EIDInteractionEvent> = BroadcastChannel(1)

        every { mockIdCardManager.changePin(mockContext) } returns idCardManagerChannel.asFlow()

        val confirmationResult = setupCoordinator.confirmPersonalPin(personalPin)
        verify(exactly = 1) { mockAppCoordinator.startNFCTagHandling() }
        Assertions.assertTrue(confirmationResult)

        advanceUntilIdle()

        idCardManagerChannel.send(EIDInteractionEvent.RequestCardInsertion)
        advanceUntilIdle()

        verify(exactly = 1) { mockAppCoordinator.navigatePopping(SetupScanDestination) }
        Assertions.assertFalse(progress)

        idCardManagerChannel.cancel(IDCardInteractionException.CardDeactivated)
        advanceUntilIdle()

        Assertions.assertFalse(progress)
        verify(exactly = 1) { mockAppCoordinator.navigate(SetupCardDeactivatedDestination) }

        scanJob.cancel()
    }

    @OptIn(ExperimentalCoroutinesApi::class, ObsoleteCoroutinesApi::class)
    @Test
    fun errorCardBlocked() = runTest {
        val setupCoordinator = SetupCoordinator(mockContext, mockAppCoordinator, mockIdCardManager, mockIssueTrackerManager, mockCoroutineContextProvider)

        every { mockCoroutineContextProvider.IO } returns dispatcher

        val transportPin = "12345"

        setupCoordinator.onTransportPINEntered(transportPin)

        verify(exactly = 1) { mockAppCoordinator.navigate(SetupPersonalPINIntroDestination) }

        val personalPin = "000000"
        setupCoordinator.onPersonalPinInput(personalPin)

        verify(exactly = 1) { mockAppCoordinator.navigate(SetupPersonalPinConfirmDestination) }

        var progress = false
        val scanJob = setupCoordinator.scanInProgress
            .onEach { progress = it }
            .launchIn(CoroutineScope(dispatcher))

        val idCardManagerChannel: BroadcastChannel<EIDInteractionEvent> = BroadcastChannel(1)

        every { mockIdCardManager.changePin(mockContext) } returns idCardManagerChannel.asFlow()

        val confirmationResult = setupCoordinator.confirmPersonalPin(personalPin)
        verify(exactly = 1) { mockAppCoordinator.startNFCTagHandling() }
        Assertions.assertTrue(confirmationResult)

        advanceUntilIdle()

        idCardManagerChannel.send(EIDInteractionEvent.RequestCardInsertion)
        advanceUntilIdle()

        verify(exactly = 1) { mockAppCoordinator.navigatePopping(SetupScanDestination) }
        Assertions.assertFalse(progress)

        idCardManagerChannel.cancel(IDCardInteractionException.CardBlocked)
        advanceUntilIdle()

        Assertions.assertFalse(progress)
        verify(exactly = 1) { mockAppCoordinator.navigate(SetupCardBlockedDestination) }

        scanJob.cancel()
    }

    @OptIn(ExperimentalCoroutinesApi::class, ObsoleteCoroutinesApi::class)
    @Test
    fun errorProcessFailed() = runTest {
        val setupCoordinator = SetupCoordinator(mockContext, mockAppCoordinator, mockIdCardManager, mockIssueTrackerManager, mockCoroutineContextProvider)

        every { mockCoroutineContextProvider.IO } returns dispatcher

        val transportPin = "12345"

        setupCoordinator.onTransportPINEntered(transportPin)

        verify(exactly = 1) { mockAppCoordinator.navigate(SetupPersonalPINIntroDestination) }

        val personalPin = "000000"
        setupCoordinator.onPersonalPinInput(personalPin)

        verify(exactly = 1) { mockAppCoordinator.navigate(SetupPersonalPinConfirmDestination) }

        var progress = false
        val scanJob = setupCoordinator.scanInProgress
            .onEach { progress = it }
            .launchIn(CoroutineScope(dispatcher))

        val idCardManagerChannel: BroadcastChannel<EIDInteractionEvent> = BroadcastChannel(1)

        every { mockIdCardManager.changePin(mockContext) } returns idCardManagerChannel.asFlow()

        val confirmationResult = setupCoordinator.confirmPersonalPin(personalPin)
        verify(exactly = 1) { mockAppCoordinator.startNFCTagHandling() }
        Assertions.assertTrue(confirmationResult)

        advanceUntilIdle()

        idCardManagerChannel.send(EIDInteractionEvent.RequestCardInsertion)
        advanceUntilIdle()

        verify(exactly = 1) { mockAppCoordinator.navigatePopping(SetupScanDestination) }
        Assertions.assertFalse(progress)

        val exception = IDCardInteractionException.ProcessFailed(ActivationResultCode.INTERNAL_ERROR, null, null)
        idCardManagerChannel.cancel(exception)
        advanceUntilIdle()

        Assertions.assertFalse(progress)
        verify(exactly = 1) { mockIssueTrackerManager.capture(any()) }

        val navigationParameter = navigationPoppingDestinationSlot.captured
        Assertions.assertEquals(SetupCardUnreadableDestination(false).route, navigationParameter.route)

        scanJob.cancel()
    }

    @Test
    fun onPersonalPinErrorTryAgain() {
        val setupCoordinator = SetupCoordinator(mockContext, mockAppCoordinator, mockIdCardManager, mockIssueTrackerManager, mockCoroutineContextProvider)

        setupCoordinator.onPersonalPinErrorTryAgain()

        verify(exactly = 1) { mockAppCoordinator.pop() }
    }

    @Test
    fun finishSetup_noTcTokenUrl() {
        val setupCoordinator = SetupCoordinator(mockContext, mockAppCoordinator, mockIdCardManager, mockIssueTrackerManager, mockCoroutineContextProvider)

        setupCoordinator.finishSetup()

        verify(exactly = 1) { mockAppCoordinator.popToRoot() }

        verify(exactly = 0) { mockAppCoordinator.startIdentification(any(), any()) }
    }

    @Test
    fun finishSetup_withTcTokenUrl() {
        val setupCoordinator = SetupCoordinator(mockContext, mockAppCoordinator, mockIdCardManager, mockIssueTrackerManager, mockCoroutineContextProvider)
        val testUrl = "tokenUrl"

        setupCoordinator.setTCTokenURL(testUrl)
        setupCoordinator.finishSetup()

        verify(exactly = 0) { mockAppCoordinator.popToRoot() }

        verify(exactly = 1) { mockAppCoordinator.startIdentification(testUrl, true) }
    }

    @Test
    fun finishSetup_withTcTokenUrlTwice() {
        val setupCoordinator = SetupCoordinator(mockContext, mockAppCoordinator, mockIdCardManager, mockIssueTrackerManager, mockCoroutineContextProvider)
        val testUrl = "tokenUrl"

        setupCoordinator.setTCTokenURL(testUrl)
        setupCoordinator.finishSetup()
        setupCoordinator.finishSetup()

        verify(exactly = 1) { mockAppCoordinator.popToRoot() }

        verify(exactly = 1) { mockAppCoordinator.startIdentification(testUrl, true) }
    }

    @Test
    fun onBack() {
        val setupCoordinator = SetupCoordinator(mockContext, mockAppCoordinator, mockIdCardManager, mockIssueTrackerManager, mockCoroutineContextProvider)

        setupCoordinator.onBackTapped()

        verify(exactly = 1) { mockIdCardManager.cancelTask() }
        verify(exactly = 1) { mockAppCoordinator.pop() }
    }

    @Test
    fun cancelSetup() {
        val setupCoordinator = SetupCoordinator(mockContext, mockAppCoordinator, mockIdCardManager, mockIssueTrackerManager, mockCoroutineContextProvider)
        val testUrl = "tokenUrl"

        setupCoordinator.setTCTokenURL(testUrl)
        setupCoordinator.cancelSetup()

        verify(exactly = 0) { mockAppCoordinator.setIsNotFirstTimeUser() }
        verify(exactly = 1) { mockAppCoordinator.popToRoot() }
        verify(exactly = 1) { mockAppCoordinator.stopNFCTagHandling() }

        verify(exactly = 1) { mockIdCardManager.cancelTask() }

        verify(exactly = 0) { mockAppCoordinator.startIdentification(testUrl, true) }

        setupCoordinator.cancelSetup()

        verify(exactly = 2) { mockAppCoordinator.popToRoot() }
        verify(exactly = 2) { mockAppCoordinator.stopNFCTagHandling() }
    }

    @Test
    fun hasToken() {
        val setupCoordinator = SetupCoordinator(mockContext, mockAppCoordinator, mockIdCardManager, mockIssueTrackerManager, mockCoroutineContextProvider)
        val testUrl = "tokenUrl"

        Assertions.assertFalse(setupCoordinator.identificationPending())

        setupCoordinator.setTCTokenURL(testUrl)

        Assertions.assertTrue(setupCoordinator.identificationPending())
    }
}
