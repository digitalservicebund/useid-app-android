package de.digitalService.useID.coordinator

import android.content.Context
import com.ramcosta.composedestinations.spec.Direction
import de.digitalService.useID.StorageManager
import de.digitalService.useID.analytics.IssueTrackerManager
import de.digitalService.useID.analytics.IssueTrackerManager_Factory
import de.digitalService.useID.idCardInterface.EidInteractionEvent
import de.digitalService.useID.idCardInterface.IdCardInteractionException
import de.digitalService.useID.idCardInterface.IdCardManager
import de.digitalService.useID.ui.coordinators.IdentificationCoordinator
import de.digitalService.useID.ui.coordinators.PinManagementCoordinator
import de.digitalService.useID.ui.coordinators.PinStatus
import de.digitalService.useID.ui.coordinators.SubCoordinatorState
import de.digitalService.useID.ui.navigation.Navigator
import de.digitalService.useID.ui.screens.destinations.*
import de.digitalService.useID.util.CoroutineContextProvider
import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
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
class PinManagementCoordinatorTest {

    @MockK(relaxUnitFun = true)
    lateinit var mockContext: Context

    @MockK(relaxUnitFun = true)
    lateinit var mockNavigator: Navigator

    @MockK(relaxUnitFun = true)
    lateinit var mockIdCardManager: IdCardManager

    @MockK(relaxUnitFun = true)
    lateinit var mockIssueTrackerManager: IssueTrackerManager

    @MockK(relaxUnitFun = true)
    lateinit var mockCoroutineContextProvider: CoroutineContextProvider

    @OptIn(ExperimentalCoroutinesApi::class)
    val dispatcher = StandardTestDispatcher()

    private val navigationDestinationSlot = slot<Direction>()

    @OptIn(ExperimentalCoroutinesApi::class)
    @BeforeEach
    fun beforeEach() {
        Dispatchers.setMain(dispatcher)

        every { mockNavigator.navigate(capture(navigationDestinationSlot)) } returns Unit
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun pinChangeSuccessful() = runTest {
        val pinManagementCoordinator = PinManagementCoordinator(
            context = mockContext,
            navigator = mockNavigator,
            idCardManager = mockIdCardManager,
            issueTrackerManager = mockIssueTrackerManager,
            coroutineContextProvider = mockCoroutineContextProvider
        )

        every { mockCoroutineContextProvider.IO } returns dispatcher

        val pinManagementStateFlow = pinManagementCoordinator.startPinManagement(pinStatus = PinStatus.TransportPin)
        var pinManagementState = SubCoordinatorState.Idle
        val stateJob = pinManagementStateFlow
            .onEach { pinManagementState = it }
            .launchIn(CoroutineScope(dispatcher))

        var navigationParameter = navigationDestinationSlot.captured
        Assertions.assertEquals(SetupTransportPinDestination(false).route, navigationParameter.route)

        advanceUntilIdle()
        Assertions.assertEquals(pinManagementState, SubCoordinatorState.Active)

        val transportPin = "12345"

        pinManagementCoordinator.setOldPin(oldPin = transportPin)

        verify(exactly = 1) { mockNavigator.navigate(SetupPersonalPinIntroDestination) }

        pinManagementCoordinator.onPersonalPinIntroFinished()

        verify(exactly = 1) { mockNavigator.navigate(SetupPersonalPinInputDestination) }

        val personalPin = "000000"

        pinManagementCoordinator.setNewPin(newPin = personalPin)

        verify(exactly = 1) { mockNavigator.navigate(SetupPersonalPinConfirmDestination) }

        var progress = false
        val scanJob = pinManagementCoordinator.scanInProgress
            .onEach { progress = it }
            .launchIn(CoroutineScope(dispatcher))

        val idCardManagerFlow = MutableStateFlow<EidInteractionEvent>(EidInteractionEvent.PinManagementStarted)
        every { mockIdCardManager.eidFlow } returns idCardManagerFlow

        val confirmationResult = pinManagementCoordinator.confirmNewPin(newPin = personalPin)

        verify(exactly = 1) { mockIdCardManager.changePin(mockContext) }

        Assertions.assertTrue(confirmationResult)
        advanceUntilIdle()

        idCardManagerFlow.value = EidInteractionEvent.RequestCardInsertion
        advanceUntilIdle()

        verify(exactly = 1) { mockNavigator.navigatePopping(SetupScanDestination) }
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
        Assertions.assertEquals(pinManagementState, SubCoordinatorState.Finished)

        scanJob.cancel()
        stateJob.cancel()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun pinChangeIncorrectPinConfirmation() = runTest {

        val pinManagementCoordinator = PinManagementCoordinator(
            context = mockContext,
            navigator = mockNavigator,
            idCardManager = mockIdCardManager,
            issueTrackerManager = mockIssueTrackerManager,
            coroutineContextProvider = mockCoroutineContextProvider
        )

        every { mockCoroutineContextProvider.IO } returns dispatcher

        pinManagementCoordinator.startPinManagement(pinStatus = PinStatus.TransportPin)

        var navigationParameter = navigationDestinationSlot.captured
        Assertions.assertEquals(SetupTransportPinDestination(false).route, navigationParameter.route)

        val transportPin = "12345"

        pinManagementCoordinator.setOldPin(oldPin = transportPin)

        verify(exactly = 1) { mockNavigator.navigate(SetupPersonalPinIntroDestination) }

        pinManagementCoordinator.onPersonalPinIntroFinished()

        verify(exactly = 1) { mockNavigator.navigate(SetupPersonalPinInputDestination) }

        var personalPin = "000000"

        pinManagementCoordinator.setNewPin(newPin = personalPin)

        verify(exactly = 1) { mockNavigator.navigate(SetupPersonalPinConfirmDestination) }

        val idCardManagerFlow = MutableStateFlow<EidInteractionEvent>(EidInteractionEvent.PinManagementStarted)
        every { mockIdCardManager.eidFlow } returns idCardManagerFlow

        personalPin = "111111"

        val confirmationResult = pinManagementCoordinator.confirmNewPin(newPin = personalPin)

        verify(exactly = 0) { mockIdCardManager.changePin(mockContext) }
        Assertions.assertFalse(confirmationResult)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun pinChangeIncorrectTransportPin() = runTest {
        val pinManagementCoordinator = PinManagementCoordinator(
            context = mockContext,
            navigator = mockNavigator,
            idCardManager = mockIdCardManager,
            issueTrackerManager = mockIssueTrackerManager,
            coroutineContextProvider = mockCoroutineContextProvider
        )

        every { mockCoroutineContextProvider.IO } returns dispatcher

        val pinManagementStateFlow = pinManagementCoordinator.startPinManagement(pinStatus = PinStatus.TransportPin)
        var pinManagementState = SubCoordinatorState.Idle
        val stateJob = pinManagementStateFlow
            .onEach { pinManagementState = it }
            .launchIn(CoroutineScope(dispatcher))

        var navigationParameter = navigationDestinationSlot.captured
        Assertions.assertEquals(SetupTransportPinDestination(false).route, navigationParameter.route)

        advanceUntilIdle()
        Assertions.assertEquals(pinManagementState, SubCoordinatorState.Active)

        val transportPin = "12345"

        pinManagementCoordinator.setOldPin(oldPin = transportPin)

        verify(exactly = 1) { mockNavigator.navigate(SetupPersonalPinIntroDestination) }

        pinManagementCoordinator.onPersonalPinIntroFinished()

        verify(exactly = 1) { mockNavigator.navigate(SetupPersonalPinInputDestination) }

        val personalPin = "000000"

        pinManagementCoordinator.setNewPin(newPin = personalPin)

        verify(exactly = 1) { mockNavigator.navigate(SetupPersonalPinConfirmDestination) }

        var progress = false
        val scanJob = pinManagementCoordinator.scanInProgress
            .onEach { progress = it }
            .launchIn(CoroutineScope(dispatcher))

        val idCardManagerFlow = MutableStateFlow<EidInteractionEvent>(EidInteractionEvent.PinManagementStarted)
        every { mockIdCardManager.eidFlow } returns idCardManagerFlow

        val confirmationResult = pinManagementCoordinator.confirmNewPin(newPin = personalPin)

        verify(exactly = 1) { mockIdCardManager.changePin(mockContext) }
        Assertions.assertTrue(confirmationResult)
        advanceUntilIdle()

        idCardManagerFlow.value = EidInteractionEvent.RequestCardInsertion
        advanceUntilIdle()

        verify(exactly = 1) { mockNavigator.navigatePopping(SetupScanDestination) }
        Assertions.assertFalse(progress)

        idCardManagerFlow.value = EidInteractionEvent.CardRecognized
        advanceUntilIdle()

        Assertions.assertTrue(progress)

        val pinCallback = mockk<(String, String) -> Unit>()
        every { pinCallback(any(), any()) } just Runs

        idCardManagerFlow.value = EidInteractionEvent.RequestChangedPin(null, pinCallback)
        advanceUntilIdle()

        verify(exactly = 1) { pinCallback(transportPin, personalPin) }

        val attempts = 2
        idCardManagerFlow.value = EidInteractionEvent.RequestChangedPin(attempts, pinCallback)
        advanceUntilIdle()

        Assertions.assertFalse(progress)
        Assertions.assertEquals(SubCoordinatorState.Active, pinManagementState)

        navigationParameter = navigationDestinationSlot.captured
        Assertions.assertEquals(SetupTransportPinDestination(true).route, navigationParameter.route)
        verify(exactly = 1) { pinManagementCoordinator.cancelPinManagement() }

        idCardManagerFlow.value = EidInteractionEvent.PinManagementStarted

        val newTransportPin = "54321"
        pinManagementCoordinator.setOldPin(newTransportPin)

        advanceUntilIdle()

        verify(exactly = 1) { mockNavigator.navigate(SetupPersonalPinIntroDestination) }
        verify(exactly = 2) { mockIdCardManager.changePin(mockContext) }

        idCardManagerFlow.value = EidInteractionEvent.RequestCardInsertion
        idCardManagerFlow.value = EidInteractionEvent.CardRecognized
        advanceUntilIdle()

        Assertions.assertTrue(progress)

        idCardManagerFlow.value = EidInteractionEvent.RequestChangedPin(null, pinCallback)
        advanceUntilIdle()

        verify(exactly = 1) { pinCallback(newTransportPin, personalPin) }

        idCardManagerFlow.value = EidInteractionEvent.ProcessCompletedSuccessfullyWithoutResult
        advanceUntilIdle()

        Assertions.assertFalse(progress)
        Assertions.assertEquals(SubCoordinatorState.Finished, pinManagementState)

        scanJob.cancel()
        stateJob.cancel()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun cardSuspended() = runTest {

        val pinManagementCoordinator = PinManagementCoordinator(
            context = mockContext,
            navigator = mockNavigator,
            idCardManager = mockIdCardManager,
            issueTrackerManager = mockIssueTrackerManager,
            coroutineContextProvider = mockCoroutineContextProvider
        )

        every { mockCoroutineContextProvider.IO } returns dispatcher

        val pinManagementStateFlow = pinManagementCoordinator.startPinManagement(pinStatus = PinStatus.TransportPin)
        var pinManagementState = SubCoordinatorState.Idle
        val stateJob = pinManagementStateFlow
            .onEach { pinManagementState = it }
            .launchIn(CoroutineScope(dispatcher))

        var navigationParameter = navigationDestinationSlot.captured
        Assertions.assertEquals(SetupTransportPinDestination(false).route, navigationParameter.route)

        advanceUntilIdle()
        Assertions.assertEquals(pinManagementState, SubCoordinatorState.Active)

        val transportPin = "12345"

        pinManagementCoordinator.setOldPin(oldPin = transportPin)

        verify(exactly = 1) { mockNavigator.navigate(SetupPersonalPinIntroDestination) }

        pinManagementCoordinator.onPersonalPinIntroFinished()

        verify(exactly = 1) { mockNavigator.navigate(SetupPersonalPinInputDestination) }

        val personalPin = "000000"

        pinManagementCoordinator.setNewPin(newPin = personalPin)

        verify(exactly = 1) { mockNavigator.navigate(SetupPersonalPinConfirmDestination) }

        var progress = false
        val scanJob = pinManagementCoordinator.scanInProgress
            .onEach { progress = it }
            .launchIn(CoroutineScope(dispatcher))

        val idCardManagerFlow = MutableStateFlow<EidInteractionEvent>(EidInteractionEvent.PinManagementStarted)
        every { mockIdCardManager.eidFlow } returns idCardManagerFlow

        val confirmationResult = pinManagementCoordinator.confirmNewPin(newPin = personalPin)

        verify(exactly = 1) { mockIdCardManager.changePin(mockContext) }

        Assertions.assertTrue(confirmationResult)
        advanceUntilIdle()

        idCardManagerFlow.value = EidInteractionEvent.RequestCardInsertion
        advanceUntilIdle()

        verify(exactly = 1) { mockNavigator.navigatePopping(SetupScanDestination) }
        Assertions.assertFalse(progress)

        idCardManagerFlow.value = EidInteractionEvent.CardRecognized
        advanceUntilIdle()

        Assertions.assertTrue(progress)

        idCardManagerFlow.value = EidInteractionEvent.RequestCanAndChangedPin { _, _, _ -> }
        advanceUntilIdle()

        Assertions.assertFalse(progress)
        Assertions.assertEquals(pinManagementState, SubCoordinatorState.Cancelled) // Or Active because the CAN will be requested?
        verify(exactly = 1) { mockNavigator.navigate(SetupCardSuspendedDestination) }

        scanJob.cancel()
        stateJob.cancel()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun cardBlocked() = runTest {
        val pinManagementCoordinator = PinManagementCoordinator(
            context = mockContext,
            navigator = mockNavigator,
            idCardManager = mockIdCardManager,
            issueTrackerManager = mockIssueTrackerManager,
            coroutineContextProvider = mockCoroutineContextProvider
        )

        every { mockCoroutineContextProvider.IO } returns dispatcher

        val pinManagementStateFlow = pinManagementCoordinator.startPinManagement(pinStatus = PinStatus.TransportPin)
        var pinManagementState = SubCoordinatorState.Idle
        val stateJob = pinManagementStateFlow
            .onEach { pinManagementState = it }
            .launchIn(CoroutineScope(dispatcher))

        var navigationParameter = navigationDestinationSlot.captured
        Assertions.assertEquals(SetupTransportPinDestination(false).route, navigationParameter.route)

        advanceUntilIdle()
        Assertions.assertEquals(pinManagementState, SubCoordinatorState.Active)

        val transportPin = "12345"

        pinManagementCoordinator.setOldPin(oldPin = transportPin)

        verify(exactly = 1) { mockNavigator.navigate(SetupPersonalPinIntroDestination) }

        pinManagementCoordinator.onPersonalPinIntroFinished()

        verify(exactly = 1) { mockNavigator.navigate(SetupPersonalPinInputDestination) }

        val personalPin = "000000"

        pinManagementCoordinator.setNewPin(newPin = personalPin)

        verify(exactly = 1) { mockNavigator.navigate(SetupPersonalPinConfirmDestination) }

        var progress = false
        val scanJob = pinManagementCoordinator.scanInProgress
            .onEach { progress = it }
            .launchIn(CoroutineScope(dispatcher))

        val idCardManagerFlow = MutableStateFlow<EidInteractionEvent>(EidInteractionEvent.PinManagementStarted)
        every { mockIdCardManager.eidFlow } returns idCardManagerFlow

        val confirmationResult = pinManagementCoordinator.confirmNewPin(newPin = personalPin)

        verify(exactly = 1) { mockIdCardManager.changePin(mockContext) }

        Assertions.assertTrue(confirmationResult)
        advanceUntilIdle()

        idCardManagerFlow.value = EidInteractionEvent.RequestCardInsertion
        advanceUntilIdle()

        verify(exactly = 1) { mockNavigator.navigatePopping(SetupScanDestination) }
        Assertions.assertFalse(progress)

        idCardManagerFlow.value = EidInteractionEvent.CardRecognized
        advanceUntilIdle()

        Assertions.assertTrue(progress)

        idCardManagerFlow.value = EidInteractionEvent.RequestPUK { }
        advanceUntilIdle()

        Assertions.assertFalse(progress)
        Assertions.assertEquals(pinManagementState, SubCoordinatorState.Cancelled)
        verify(exactly = 1) { mockNavigator.navigate(SetupCardBlockedDestination) }

        scanJob.cancel()
        stateJob.cancel()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun unexpectedEvent() = runTest {
        val pinManagementCoordinator = PinManagementCoordinator(
            context = mockContext,
            navigator = mockNavigator,
            idCardManager = mockIdCardManager,
            issueTrackerManager = mockIssueTrackerManager,
            coroutineContextProvider = mockCoroutineContextProvider
        )

        every { mockCoroutineContextProvider.IO } returns dispatcher

        val pinManagementStateFlow = pinManagementCoordinator.startPinManagement(pinStatus = PinStatus.TransportPin)
        var pinManagementState = SubCoordinatorState.Idle
        val stateJob = pinManagementStateFlow
            .onEach { pinManagementState = it }
            .launchIn(CoroutineScope(dispatcher))

        var navigationParameter = navigationDestinationSlot.captured
        Assertions.assertEquals(SetupTransportPinDestination(false).route, navigationParameter.route)

        advanceUntilIdle()
        Assertions.assertEquals(pinManagementState, SubCoordinatorState.Active)

        val transportPin = "12345"

        pinManagementCoordinator.setOldPin(oldPin = transportPin)

        verify(exactly = 1) { mockNavigator.navigate(SetupPersonalPinIntroDestination) }

        pinManagementCoordinator.onPersonalPinIntroFinished()

        verify(exactly = 1) { mockNavigator.navigate(SetupPersonalPinInputDestination) }

        val personalPin = "000000"

        pinManagementCoordinator.setNewPin(newPin = personalPin)

        verify(exactly = 1) { mockNavigator.navigate(SetupPersonalPinConfirmDestination) }

        var progress = false
        val scanJob = pinManagementCoordinator.scanInProgress
            .onEach { progress = it }
            .launchIn(CoroutineScope(dispatcher))

        val idCardManagerFlow = MutableStateFlow<EidInteractionEvent>(EidInteractionEvent.PinManagementStarted)
        every { mockIdCardManager.eidFlow } returns idCardManagerFlow

        val confirmationResult = pinManagementCoordinator.confirmNewPin(newPin = personalPin)

        verify(exactly = 1) { mockIdCardManager.changePin(mockContext) }

        Assertions.assertTrue(confirmationResult)
        advanceUntilIdle()

        idCardManagerFlow.value = EidInteractionEvent.RequestCardInsertion
        advanceUntilIdle()

        verify(exactly = 1) { mockNavigator.navigatePopping(SetupScanDestination) }
        Assertions.assertFalse(progress)

        idCardManagerFlow.value = EidInteractionEvent.CardRecognized
        advanceUntilIdle()

        Assertions.assertTrue(progress)

        idCardManagerFlow.value = EidInteractionEvent.RequestCan { }
        advanceUntilIdle()

        // Unexpected event doesn't seem to be handled
        Assertions.assertFalse(progress)
        Assertions.assertEquals(pinManagementState, SubCoordinatorState.Cancelled)
        verify(exactly = 1) { mockNavigator.navigate(SetupOtherErrorDestination) }

        scanJob.cancel()
        stateJob.cancel()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun errorCardDeactivated() = runTest {
        val pinManagementCoordinator = PinManagementCoordinator(
            context = mockContext,
            navigator = mockNavigator,
            idCardManager = mockIdCardManager,
            issueTrackerManager = mockIssueTrackerManager,
            coroutineContextProvider = mockCoroutineContextProvider
        )

        every { mockCoroutineContextProvider.IO } returns dispatcher

        val pinManagementStateFlow = pinManagementCoordinator.startPinManagement(pinStatus = PinStatus.TransportPin)
        var pinManagementState = SubCoordinatorState.Idle
        val stateJob = pinManagementStateFlow
            .onEach { pinManagementState = it }
            .launchIn(CoroutineScope(dispatcher))

        var navigationParameter = navigationDestinationSlot.captured
        Assertions.assertEquals(SetupTransportPinDestination(false).route, navigationParameter.route)

        advanceUntilIdle()
        Assertions.assertEquals(pinManagementState, SubCoordinatorState.Active)

        val transportPin = "12345"

        pinManagementCoordinator.setOldPin(oldPin = transportPin)

        verify(exactly = 1) { mockNavigator.navigate(SetupPersonalPinIntroDestination) }

        pinManagementCoordinator.onPersonalPinIntroFinished()

        verify(exactly = 1) { mockNavigator.navigate(SetupPersonalPinInputDestination) }

        val personalPin = "000000"

        pinManagementCoordinator.setNewPin(newPin = personalPin)

        verify(exactly = 1) { mockNavigator.navigate(SetupPersonalPinConfirmDestination) }

        var progress = false
        val scanJob = pinManagementCoordinator.scanInProgress
            .onEach { progress = it }
            .launchIn(CoroutineScope(dispatcher))

        val idCardManagerFlow = MutableStateFlow<EidInteractionEvent>(EidInteractionEvent.PinManagementStarted)
        every { mockIdCardManager.eidFlow } returns idCardManagerFlow

        val confirmationResult = pinManagementCoordinator.confirmNewPin(newPin = personalPin)

        verify(exactly = 1) { mockIdCardManager.changePin(mockContext) }

        Assertions.assertTrue(confirmationResult)
        advanceUntilIdle()

        idCardManagerFlow.value = EidInteractionEvent.RequestCardInsertion
        advanceUntilIdle()

        verify(exactly = 1) { mockNavigator.navigatePopping(SetupScanDestination) }
        Assertions.assertFalse(progress)

        idCardManagerFlow.value = EidInteractionEvent.CardRecognized
        advanceUntilIdle()

        Assertions.assertTrue(progress)

        idCardManagerFlow.value = EidInteractionEvent.Error(exception = IdCardInteractionException.CardDeactivated)
        advanceUntilIdle()

        Assertions.assertFalse(progress)
        Assertions.assertEquals(SubCoordinatorState.Idle, pinManagementState)
        verify(exactly = 1) { mockNavigator.navigate(SetupCardDeactivatedDestination) }

        scanJob.cancel()
        stateJob.cancel()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun errorCardBlocked() = runTest {
        val pinManagementCoordinator = PinManagementCoordinator(
            context = mockContext,
            navigator = mockNavigator,
            idCardManager = mockIdCardManager,
            issueTrackerManager = mockIssueTrackerManager,
            coroutineContextProvider = mockCoroutineContextProvider
        )

        every { mockCoroutineContextProvider.IO } returns dispatcher

        val pinManagementStateFlow = pinManagementCoordinator.startPinManagement(pinStatus = PinStatus.TransportPin)
        var pinManagementState = SubCoordinatorState.Idle
        val stateJob = pinManagementStateFlow
            .onEach { pinManagementState = it }
            .launchIn(CoroutineScope(dispatcher))

        var navigationParameter = navigationDestinationSlot.captured
        Assertions.assertEquals(SetupTransportPinDestination(false).route, navigationParameter.route)

        advanceUntilIdle()
        Assertions.assertEquals(pinManagementState, SubCoordinatorState.Active)

        val transportPin = "12345"

        pinManagementCoordinator.setOldPin(oldPin = transportPin)

        verify(exactly = 1) { mockNavigator.navigate(SetupPersonalPinIntroDestination) }

        pinManagementCoordinator.onPersonalPinIntroFinished()

        verify(exactly = 1) { mockNavigator.navigate(SetupPersonalPinInputDestination) }

        val personalPin = "000000"

        pinManagementCoordinator.setNewPin(newPin = personalPin)

        verify(exactly = 1) { mockNavigator.navigate(SetupPersonalPinConfirmDestination) }

        var progress = false
        val scanJob = pinManagementCoordinator.scanInProgress
            .onEach { progress = it }
            .launchIn(CoroutineScope(dispatcher))

        val idCardManagerFlow = MutableStateFlow<EidInteractionEvent>(EidInteractionEvent.PinManagementStarted)
        every { mockIdCardManager.eidFlow } returns idCardManagerFlow

        val confirmationResult = pinManagementCoordinator.confirmNewPin(newPin = personalPin)

        verify(exactly = 1) { mockIdCardManager.changePin(mockContext) }

        Assertions.assertTrue(confirmationResult)
        advanceUntilIdle()

        idCardManagerFlow.value = EidInteractionEvent.RequestCardInsertion
        advanceUntilIdle()

        verify(exactly = 1) { mockNavigator.navigatePopping(SetupScanDestination) }
        Assertions.assertFalse(progress)

        idCardManagerFlow.value = EidInteractionEvent.CardRecognized
        advanceUntilIdle()

        Assertions.assertTrue(progress)

        idCardManagerFlow.value = EidInteractionEvent.Error(exception = IdCardInteractionException.CardBlocked)
        advanceUntilIdle()

        Assertions.assertFalse(progress)
        Assertions.assertEquals(SubCoordinatorState.Idle, pinManagementState)
        verify(exactly = 1) { mockNavigator.navigate(SetupCardBlockedDestination) }

        scanJob.cancel()
        stateJob.cancel()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun errorProcessFailed() = runTest {

        val pinManagementCoordinator = PinManagementCoordinator(
            context = mockContext,
            navigator = mockNavigator,
            idCardManager = mockIdCardManager,
            issueTrackerManager = mockIssueTrackerManager,
            coroutineContextProvider = mockCoroutineContextProvider
        )

        every { mockCoroutineContextProvider.IO } returns dispatcher

        val pinManagementStateFlow = pinManagementCoordinator.startPinManagement(pinStatus = PinStatus.TransportPin)
        var pinManagementState = SubCoordinatorState.Idle
        val stateJob = pinManagementStateFlow
            .onEach { pinManagementState = it }
            .launchIn(CoroutineScope(dispatcher))

        var navigationParameter = navigationDestinationSlot.captured
        Assertions.assertEquals(SetupTransportPinDestination(false).route, navigationParameter.route)

        advanceUntilIdle()
        Assertions.assertEquals(pinManagementState, SubCoordinatorState.Active)

        val transportPin = "12345"

        pinManagementCoordinator.setOldPin(oldPin = transportPin)

        verify(exactly = 1) { mockNavigator.navigate(SetupPersonalPinIntroDestination) }

        pinManagementCoordinator.onPersonalPinIntroFinished()

        verify(exactly = 1) { mockNavigator.navigate(SetupPersonalPinInputDestination) }

        val personalPin = "000000"

        pinManagementCoordinator.setNewPin(newPin = personalPin)

        verify(exactly = 1) { mockNavigator.navigate(SetupPersonalPinConfirmDestination) }

        var progress = false
        val scanJob = pinManagementCoordinator.scanInProgress
            .onEach { progress = it }
            .launchIn(CoroutineScope(dispatcher))

        val idCardManagerFlow = MutableStateFlow<EidInteractionEvent>(EidInteractionEvent.PinManagementStarted)
        every { mockIdCardManager.eidFlow } returns idCardManagerFlow

        val confirmationResult = pinManagementCoordinator.confirmNewPin(newPin = personalPin)

        verify(exactly = 1) { mockIdCardManager.changePin(mockContext) }

        Assertions.assertTrue(confirmationResult)
        advanceUntilIdle()

        idCardManagerFlow.value = EidInteractionEvent.RequestCardInsertion
        advanceUntilIdle()

        verify(exactly = 1) { mockNavigator.navigatePopping(SetupScanDestination) }
        Assertions.assertFalse(progress)

        idCardManagerFlow.value = EidInteractionEvent.CardRecognized
        advanceUntilIdle()

        Assertions.assertTrue(progress)

        idCardManagerFlow.value = EidInteractionEvent.Error(exception = IdCardInteractionException.ProcessFailed(ActivationResultCode.INTERNAL_ERROR, null, null))
        advanceUntilIdle()

        Assertions.assertFalse(progress)
        Assertions.assertEquals(SubCoordinatorState.Idle, pinManagementState)
        navigationParameter = navigationDestinationSlot.captured
        Assertions.assertEquals(SetupCardUnreadableDestination(false).route, navigationParameter.route)

        scanJob.cancel()
        stateJob.cancel()
    }
}
