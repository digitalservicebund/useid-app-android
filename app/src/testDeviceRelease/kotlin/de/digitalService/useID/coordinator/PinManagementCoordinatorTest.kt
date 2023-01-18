package de.digitalService.useID.coordinator

import android.content.Context
import com.ramcosta.composedestinations.spec.Direction
import de.digitalService.useID.analytics.IssueTrackerManager
import de.digitalService.useID.idCardInterface.EidInteractionEvent
import de.digitalService.useID.idCardInterface.IdCardInteractionException
import de.digitalService.useID.idCardInterface.IdCardManager
import de.digitalService.useID.ui.coordinators.CanCoordinator
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
    lateinit var mockCanCoordinator: CanCoordinator

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

        // SETUP
        every { mockCoroutineContextProvider.IO } returns dispatcher

        val idCardManagerFlow = MutableStateFlow<EidInteractionEvent>(EidInteractionEvent.PinManagementStarted)
        every { mockIdCardManager.eidFlow } returns idCardManagerFlow

        val canCoordinatorStateFlow = MutableStateFlow(SubCoordinatorState.Cancelled)
        every { mockCanCoordinator.stateFlow } returns canCoordinatorStateFlow

        val pinManagementCoordinator = PinManagementCoordinator(
            context = mockContext,
            canCoordinator = mockCanCoordinator,
            navigator = mockNavigator,
            idCardManager = mockIdCardManager,
            issueTrackerManager = mockIssueTrackerManager,
            coroutineContextProvider = mockCoroutineContextProvider
        )

        var progress = false
        val scanJob = pinManagementCoordinator.scanInProgress
            .onEach { progress = it }
            .launchIn(CoroutineScope(dispatcher))

        // START PIN MANAGEMENT
        val pinManagementStateFlow = pinManagementCoordinator.startPinManagement(pinStatus = PinStatus.TransportPin)
        var pinManagementState = SubCoordinatorState.Finished
        val stateJob = pinManagementStateFlow
            .onEach { pinManagementState = it }
            .launchIn(CoroutineScope(dispatcher))

        var navigationParameter = navigationDestinationSlot.captured
        Assertions.assertEquals(SetupTransportPinDestination(false).route, navigationParameter.route)

        advanceUntilIdle()

        Assertions.assertEquals(SubCoordinatorState.Active, pinManagementState)

        // SET CORRECT TRANSPORT PIN
        val transportPin = "12345"
        pinManagementCoordinator.setOldPin(oldPin = transportPin)

        verify(exactly = 1) { mockNavigator.navigate(SetupPersonalPinIntroDestination) }

        pinManagementCoordinator.onPersonalPinIntroFinished()

        verify(exactly = 1) { mockNavigator.navigate(SetupPersonalPinInputDestination) }

        // SET NEW PERSONAL PIN
        val personalPin = "000000"
        pinManagementCoordinator.setNewPin(newPin = personalPin)

        verify(exactly = 1) { mockNavigator.navigate(SetupPersonalPinConfirmDestination) }

        // CONFIRM NEW PERSONAL PIN
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
        Assertions.assertEquals(SubCoordinatorState.Finished, pinManagementState)

        scanJob.cancel()
        stateJob.cancel()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun pinChangeIncorrectPinConfirmation() = runTest {

        // SETUP
        every { mockCoroutineContextProvider.IO } returns dispatcher

        val idCardManagerFlow = MutableStateFlow<EidInteractionEvent>(EidInteractionEvent.PinManagementStarted)
        every { mockIdCardManager.eidFlow } returns idCardManagerFlow

        val pinManagementCoordinator = PinManagementCoordinator(
            context = mockContext,
            canCoordinator = mockCanCoordinator,
            navigator = mockNavigator,
            idCardManager = mockIdCardManager,
            issueTrackerManager = mockIssueTrackerManager,
            coroutineContextProvider = mockCoroutineContextProvider
        )

        // START PIN MANAGEMENT
        pinManagementCoordinator.startPinManagement(pinStatus = PinStatus.TransportPin)

        var navigationParameter = navigationDestinationSlot.captured
        Assertions.assertEquals(SetupTransportPinDestination(false).route, navigationParameter.route)

        // SET TRANSPORT PIN
        val transportPin = "12345"
        pinManagementCoordinator.setOldPin(oldPin = transportPin)

        verify(exactly = 1) { mockNavigator.navigate(SetupPersonalPinIntroDestination) }

        pinManagementCoordinator.onPersonalPinIntroFinished()

        verify(exactly = 1) { mockNavigator.navigate(SetupPersonalPinInputDestination) }

        // SET NEW PERSONAL PIN
        var personalPin = "000000"
        pinManagementCoordinator.setNewPin(newPin = personalPin)

        verify(exactly = 1) { mockNavigator.navigate(SetupPersonalPinConfirmDestination) }

        // ENTER WRONG PERSONAL PIN
        personalPin = "111111"
        val confirmationResult = pinManagementCoordinator.confirmNewPin(newPin = personalPin)

        Assertions.assertFalse(confirmationResult)
        verify(exactly = 0) { mockIdCardManager.changePin(mockContext) }

        pinManagementCoordinator.onConfirmPinMismatchError()
        verify(exactly = 1) { mockNavigator.pop() }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun pinChangeIncorrectTransportPin() = runTest {

        // SETUP
        every { mockCoroutineContextProvider.IO } returns dispatcher

        val idCardManagerFlow = MutableStateFlow<EidInteractionEvent>(EidInteractionEvent.PinManagementStarted)
        every { mockIdCardManager.eidFlow } returns idCardManagerFlow

        val canCoordinatorStateFlow = MutableStateFlow(SubCoordinatorState.Cancelled)
        every { mockCanCoordinator.stateFlow } returns canCoordinatorStateFlow

        val pinManagementCoordinator = PinManagementCoordinator(
            context = mockContext,
            canCoordinator = mockCanCoordinator,
            navigator = mockNavigator,
            idCardManager = mockIdCardManager,
            issueTrackerManager = mockIssueTrackerManager,
            coroutineContextProvider = mockCoroutineContextProvider
        )

        var progress = false
        val scanJob = pinManagementCoordinator.scanInProgress
            .onEach { progress = it }
            .launchIn(CoroutineScope(dispatcher))

        // START PIN MANAGEMENT
        val pinManagementStateFlow = pinManagementCoordinator.startPinManagement(pinStatus = PinStatus.TransportPin)
        var pinManagementState = SubCoordinatorState.Finished
        val stateJob = pinManagementStateFlow
            .onEach { pinManagementState = it }
            .launchIn(CoroutineScope(dispatcher))

        var navigationParameter = navigationDestinationSlot.captured
        Assertions.assertEquals(SetupTransportPinDestination(false).route, navigationParameter.route)

        advanceUntilIdle()
        Assertions.assertEquals(SubCoordinatorState.Active, pinManagementState)

        // SET WRONG TRANSPORT PIN
        val transportPin = "12345"
        pinManagementCoordinator.setOldPin(oldPin = transportPin)

        verify(exactly = 1) { mockNavigator.navigate(SetupPersonalPinIntroDestination) }

        pinManagementCoordinator.onPersonalPinIntroFinished()

        verify(exactly = 1) { mockNavigator.navigate(SetupPersonalPinInputDestination) }

        // SET NEW PERSONAL PIN
        val personalPin = "000000"
        pinManagementCoordinator.setNewPin(newPin = personalPin)

        verify(exactly = 1) { mockNavigator.navigate(SetupPersonalPinConfirmDestination) }

        // CONFIRM NEW PIN
        val confirmationResult = pinManagementCoordinator.confirmNewPin(newPin = personalPin)

        verify(exactly = 1) { mockIdCardManager.changePin(mockContext) }
        Assertions.assertTrue(confirmationResult)
        advanceUntilIdle()

        // REQUEST CARD
        idCardManagerFlow.value = EidInteractionEvent.RequestCardInsertion
        advanceUntilIdle()

        verify(exactly = 1) { mockNavigator.navigatePopping(SetupScanDestination) }
        Assertions.assertFalse(progress)

        // CARD RECOGNIZED
        idCardManagerFlow.value = EidInteractionEvent.CardRecognized
        advanceUntilIdle()

        Assertions.assertTrue(progress)

        val pinCallback = mockk<(String, String) -> Unit>()
        every { pinCallback(any(), any()) } just Runs

        // WRONG TRANSPORT PIN
        idCardManagerFlow.value = EidInteractionEvent.RequestChangedPin(null, pinCallback)
        advanceUntilIdle()

        verify(exactly = 1) { pinCallback(transportPin, personalPin) }

        // TRY AGAIN
        val attempts = 2
        idCardManagerFlow.value = EidInteractionEvent.RequestChangedPin(attempts, pinCallback)
        advanceUntilIdle()

        Assertions.assertFalse(progress)
        Assertions.assertEquals(SubCoordinatorState.Active, pinManagementState)

        navigationParameter = navigationDestinationSlot.captured
        Assertions.assertEquals(SetupTransportPinDestination(true).route, navigationParameter.route)

        // ENTER CORRECT TRANSPORT PIN

        val newTransportPin = "54321"
        pinManagementCoordinator.setOldPin(newTransportPin)
        advanceUntilIdle()
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
        Assertions.assertEquals(SubCoordinatorState.Finished, pinManagementState) // Why is this failing?

        scanJob.cancel()
        stateJob.cancel()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun cardSuspended() = runTest {

        // SETUP
        every { mockCoroutineContextProvider.IO } returns dispatcher

        val idCardManagerFlow = MutableStateFlow<EidInteractionEvent>(EidInteractionEvent.PinManagementStarted)
        every { mockIdCardManager.eidFlow } returns idCardManagerFlow

        val canCoordinatorStateFlow = MutableStateFlow(SubCoordinatorState.Cancelled)
        every { mockCanCoordinator.stateFlow } returns canCoordinatorStateFlow

        val pinManagementCoordinator = PinManagementCoordinator(
            context = mockContext,
            canCoordinator = mockCanCoordinator,
            navigator = mockNavigator,
            idCardManager = mockIdCardManager,
            issueTrackerManager = mockIssueTrackerManager,
            coroutineContextProvider = mockCoroutineContextProvider
        )

        var progress = false
        val scanJob = pinManagementCoordinator.scanInProgress
            .onEach { progress = it }
            .launchIn(CoroutineScope(dispatcher))

        // START PIN MANAGEMENT
        val pinManagementStateFlow = pinManagementCoordinator.startPinManagement(pinStatus = PinStatus.TransportPin)
        var pinManagementState = SubCoordinatorState.Finished
        val stateJob = pinManagementStateFlow
            .onEach { pinManagementState = it }
            .launchIn(CoroutineScope(dispatcher))

        var navigationParameter = navigationDestinationSlot.captured
        Assertions.assertEquals(SetupTransportPinDestination(false).route, navigationParameter.route)

        advanceUntilIdle()
        Assertions.assertEquals(pinManagementState, SubCoordinatorState.Active)

        // SET TRANSPORT PIN
        val transportPin = "12345"
        pinManagementCoordinator.setOldPin(oldPin = transportPin)

        verify(exactly = 1) { mockNavigator.navigate(SetupPersonalPinIntroDestination) }

        pinManagementCoordinator.onPersonalPinIntroFinished()

        verify(exactly = 1) { mockNavigator.navigate(SetupPersonalPinInputDestination) }

        // SET NEW PERSONAL PIN
        val personalPin = "000000"
        pinManagementCoordinator.setNewPin(newPin = personalPin)

        verify(exactly = 1) { mockNavigator.navigate(SetupPersonalPinConfirmDestination) }

        // CONFIRM NEW PERSONAL PIN
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


        every { mockCanCoordinator.startSetupCanFlow(transportPin, personalPin) } returns canCoordinatorStateFlow
        idCardManagerFlow.value = EidInteractionEvent.RequestCanAndChangedPin { _, _, _ -> }
        advanceUntilIdle()

        Assertions.assertFalse(progress)
        verify(exactly = 1) { mockCanCoordinator.startSetupCanFlow(transportPin, personalPin) }

        scanJob.cancel()
        stateJob.cancel()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun cardBlocked() = runTest {

        // SETUP
        every { mockCoroutineContextProvider.IO } returns dispatcher

        val idCardManagerFlow = MutableStateFlow<EidInteractionEvent>(EidInteractionEvent.PinManagementStarted)
        every { mockIdCardManager.eidFlow } returns idCardManagerFlow

        val canCoordinatorStateFlow = MutableStateFlow(SubCoordinatorState.Cancelled)
        every { mockCanCoordinator.stateFlow } returns canCoordinatorStateFlow

        val pinManagementCoordinator = PinManagementCoordinator(
            context = mockContext,
            canCoordinator = mockCanCoordinator,
            navigator = mockNavigator,
            idCardManager = mockIdCardManager,
            issueTrackerManager = mockIssueTrackerManager,
            coroutineContextProvider = mockCoroutineContextProvider
        )

        var progress = false
        val scanJob = pinManagementCoordinator.scanInProgress
            .onEach { progress = it }
            .launchIn(CoroutineScope(dispatcher))

        // START PIN MANAGEMENT
        val pinManagementStateFlow = pinManagementCoordinator.startPinManagement(pinStatus = PinStatus.TransportPin)
        var pinManagementState = SubCoordinatorState.Finished
        val stateJob = pinManagementStateFlow
            .onEach { pinManagementState = it }
            .launchIn(CoroutineScope(dispatcher))

        var navigationParameter = navigationDestinationSlot.captured
        Assertions.assertEquals(SetupTransportPinDestination(false).route, navigationParameter.route)

        advanceUntilIdle()
        Assertions.assertEquals(SubCoordinatorState.Active, pinManagementState)

        // SET TRANSPORT PIN
        val transportPin = "12345"
        pinManagementCoordinator.setOldPin(oldPin = transportPin)

        verify(exactly = 1) { mockNavigator.navigate(SetupPersonalPinIntroDestination) }

        pinManagementCoordinator.onPersonalPinIntroFinished()

        verify(exactly = 1) { mockNavigator.navigate(SetupPersonalPinInputDestination) }

        // SET NEW PERSONAL PIN
        val personalPin = "000000"
        pinManagementCoordinator.setNewPin(newPin = personalPin)

        verify(exactly = 1) { mockNavigator.navigate(SetupPersonalPinConfirmDestination) }

        // CONFIRM NEW PIN
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

        idCardManagerFlow.value = EidInteractionEvent.RequestPuk { }
        advanceUntilIdle()

        Assertions.assertFalse(progress)
        Assertions.assertEquals(SubCoordinatorState.Cancelled, pinManagementState)
        verify(exactly = 1) { mockNavigator.navigate(SetupCardBlockedDestination) }

        scanJob.cancel()
        stateJob.cancel()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun unexpectedEvent() = runTest {

        // SETUP
        every { mockCoroutineContextProvider.IO } returns dispatcher

        val idCardManagerFlow = MutableStateFlow<EidInteractionEvent>(EidInteractionEvent.PinManagementStarted)
        every { mockIdCardManager.eidFlow } returns idCardManagerFlow

        val canCoordinatorStateFlow = MutableStateFlow(SubCoordinatorState.Cancelled)
        every { mockCanCoordinator.stateFlow } returns canCoordinatorStateFlow

        val pinManagementCoordinator = PinManagementCoordinator(
            context = mockContext,
            canCoordinator = mockCanCoordinator,
            navigator = mockNavigator,
            idCardManager = mockIdCardManager,
            issueTrackerManager = mockIssueTrackerManager,
            coroutineContextProvider = mockCoroutineContextProvider
        )

        var progress = false
        val scanJob = pinManagementCoordinator.scanInProgress
            .onEach { progress = it }
            .launchIn(CoroutineScope(dispatcher))

        // START PIN MANAGEMENT
        val pinManagementStateFlow = pinManagementCoordinator.startPinManagement(pinStatus = PinStatus.TransportPin)
        var pinManagementState = SubCoordinatorState.Finished
        val stateJob = pinManagementStateFlow
            .onEach { pinManagementState = it }
            .launchIn(CoroutineScope(dispatcher))

        var navigationParameter = navigationDestinationSlot.captured
        Assertions.assertEquals(SetupTransportPinDestination(false).route, navigationParameter.route)

        advanceUntilIdle()
        Assertions.assertEquals(SubCoordinatorState.Active, pinManagementState)

        // SET TRANSPORT PIN
        val transportPin = "12345"
        pinManagementCoordinator.setOldPin(oldPin = transportPin)

        verify(exactly = 1) { mockNavigator.navigate(SetupPersonalPinIntroDestination) }

        pinManagementCoordinator.onPersonalPinIntroFinished()

        verify(exactly = 1) { mockNavigator.navigate(SetupPersonalPinInputDestination) }

        // SET NEW PERSONAL PIN
        val personalPin = "000000"
        pinManagementCoordinator.setNewPin(newPin = personalPin)

        verify(exactly = 1) { mockNavigator.navigate(SetupPersonalPinConfirmDestination) }

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

        idCardManagerFlow.value = EidInteractionEvent.AuthenticationSuccessful
        advanceUntilIdle()

        Assertions.assertFalse(progress)
        Assertions.assertEquals(SubCoordinatorState.Cancelled, pinManagementState)
        verify(exactly = 1) { mockNavigator.navigate(SetupOtherErrorDestination) }

        scanJob.cancel()
        stateJob.cancel()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun errorCardDeactivated() = runTest {

        // SETUP
        every { mockCoroutineContextProvider.IO } returns dispatcher

        val idCardManagerFlow = MutableStateFlow<EidInteractionEvent>(EidInteractionEvent.PinManagementStarted)
        every { mockIdCardManager.eidFlow } returns idCardManagerFlow

        val canCoordinatorStateFlow = MutableStateFlow(SubCoordinatorState.Cancelled)
        every { mockCanCoordinator.stateFlow } returns canCoordinatorStateFlow

        val pinManagementCoordinator = PinManagementCoordinator(
            context = mockContext,
            canCoordinator = mockCanCoordinator,
            navigator = mockNavigator,
            idCardManager = mockIdCardManager,
            issueTrackerManager = mockIssueTrackerManager,
            coroutineContextProvider = mockCoroutineContextProvider
        )

        var progress = false
        val scanJob = pinManagementCoordinator.scanInProgress
            .onEach { progress = it }
            .launchIn(CoroutineScope(dispatcher))

        // START PIN MANAGEMENT
        val pinManagementStateFlow = pinManagementCoordinator.startPinManagement(pinStatus = PinStatus.TransportPin)
        var pinManagementState = SubCoordinatorState.Finished
        val stateJob = pinManagementStateFlow
            .onEach { pinManagementState = it }
            .launchIn(CoroutineScope(dispatcher))

        var navigationParameter = navigationDestinationSlot.captured
        Assertions.assertEquals(SetupTransportPinDestination(false).route, navigationParameter.route)

        advanceUntilIdle()
        Assertions.assertEquals(SubCoordinatorState.Active, pinManagementState)

        // SET TRANSPORT PIN
        val transportPin = "12345"
        pinManagementCoordinator.setOldPin(oldPin = transportPin)

        verify(exactly = 1) { mockNavigator.navigate(SetupPersonalPinIntroDestination) }

        pinManagementCoordinator.onPersonalPinIntroFinished()

        verify(exactly = 1) { mockNavigator.navigate(SetupPersonalPinInputDestination) }

        // SET NEW PERSONAL PIN
        val personalPin = "000000"
        pinManagementCoordinator.setNewPin(newPin = personalPin)

        verify(exactly = 1) { mockNavigator.navigate(SetupPersonalPinConfirmDestination) }

        // CONFIRM NEW PERSONAL PIN
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
        Assertions.assertEquals(SubCoordinatorState.Cancelled, pinManagementState)
        verify(exactly = 1) { mockNavigator.navigate(SetupCardDeactivatedDestination) }

        scanJob.cancel()
        stateJob.cancel()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun errorCardBlocked() = runTest {

        // SETUP
        every { mockCoroutineContextProvider.IO } returns dispatcher

        val idCardManagerFlow = MutableStateFlow<EidInteractionEvent>(EidInteractionEvent.PinManagementStarted)
        every { mockIdCardManager.eidFlow } returns idCardManagerFlow

        val canCoordinatorStateFlow = MutableStateFlow(SubCoordinatorState.Cancelled)
        every { mockCanCoordinator.stateFlow } returns canCoordinatorStateFlow

        val pinManagementCoordinator = PinManagementCoordinator(
            context = mockContext,
            canCoordinator = mockCanCoordinator,
            navigator = mockNavigator,
            idCardManager = mockIdCardManager,
            issueTrackerManager = mockIssueTrackerManager,
            coroutineContextProvider = mockCoroutineContextProvider
        )

        var progress = false
        val scanJob = pinManagementCoordinator.scanInProgress
            .onEach { progress = it }
            .launchIn(CoroutineScope(dispatcher))

        // START PIN MANAGEMENT
        val pinManagementStateFlow = pinManagementCoordinator.startPinManagement(pinStatus = PinStatus.TransportPin)
        var pinManagementState = SubCoordinatorState.Finished
        val stateJob = pinManagementStateFlow
            .onEach { pinManagementState = it }
            .launchIn(CoroutineScope(dispatcher))

        var navigationParameter = navigationDestinationSlot.captured
        Assertions.assertEquals(SetupTransportPinDestination(false).route, navigationParameter.route)

        advanceUntilIdle()
        Assertions.assertEquals(SubCoordinatorState.Active, pinManagementState)

        // SET TRANSPORT PIN
        val transportPin = "12345"
        pinManagementCoordinator.setOldPin(oldPin = transportPin)

        verify(exactly = 1) { mockNavigator.navigate(SetupPersonalPinIntroDestination) }

        pinManagementCoordinator.onPersonalPinIntroFinished()

        verify(exactly = 1) { mockNavigator.navigate(SetupPersonalPinInputDestination) }

        // SET NEW PERSONAL PIN
        val personalPin = "000000"
        pinManagementCoordinator.setNewPin(newPin = personalPin)

        verify(exactly = 1) { mockNavigator.navigate(SetupPersonalPinConfirmDestination) }

        // CONFIRM NEW PERSONAL PIN
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
        Assertions.assertEquals(SubCoordinatorState.Cancelled, pinManagementState)
        verify(exactly = 1) { mockNavigator.navigate(SetupCardBlockedDestination) }

        scanJob.cancel()
        stateJob.cancel()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun errorCardUnreadable() = runTest {

        // SETUP
        every { mockCoroutineContextProvider.IO } returns dispatcher

        val idCardManagerFlow = MutableStateFlow<EidInteractionEvent>(EidInteractionEvent.PinManagementStarted)
        every { mockIdCardManager.eidFlow } returns idCardManagerFlow

        val canCoordinatorStateFlow = MutableStateFlow(SubCoordinatorState.Cancelled)
        every { mockCanCoordinator.stateFlow } returns canCoordinatorStateFlow

        val pinManagementCoordinator = PinManagementCoordinator(
            context = mockContext,
            canCoordinator = mockCanCoordinator,
            navigator = mockNavigator,
            idCardManager = mockIdCardManager,
            issueTrackerManager = mockIssueTrackerManager,
            coroutineContextProvider = mockCoroutineContextProvider
        )

        var progress = false
        val scanJob = pinManagementCoordinator.scanInProgress
            .onEach { progress = it }
            .launchIn(CoroutineScope(dispatcher))

        // START PIN MANAGEMENT
        val pinManagementStateFlow = pinManagementCoordinator.startPinManagement(pinStatus = PinStatus.TransportPin)
        var pinManagementState = SubCoordinatorState.Finished
        val stateJob = pinManagementStateFlow
            .onEach { pinManagementState = it }
            .launchIn(CoroutineScope(dispatcher))

        var navigationParameter = navigationDestinationSlot.captured
        Assertions.assertEquals(SetupTransportPinDestination(false).route, navigationParameter.route)

        advanceUntilIdle()
        Assertions.assertEquals(SubCoordinatorState.Active, pinManagementState)

        // SET TRANSPORT PIN
        val transportPin = "12345"
        pinManagementCoordinator.setOldPin(oldPin = transportPin)

        verify(exactly = 1) { mockNavigator.navigate(SetupPersonalPinIntroDestination) }

        pinManagementCoordinator.onPersonalPinIntroFinished()

        verify(exactly = 1) { mockNavigator.navigate(SetupPersonalPinInputDestination) }

        // SET NEW PERSONAL PIN
        val personalPin = "000000"
        pinManagementCoordinator.setNewPin(newPin = personalPin)

        verify(exactly = 1) { mockNavigator.navigate(SetupPersonalPinConfirmDestination) }

        // CONFIRM NEW PERSONAL PIN
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
        Assertions.assertEquals(SubCoordinatorState.Active, pinManagementState)
        navigationParameter = navigationDestinationSlot.captured
        Assertions.assertEquals(SetupCardUnreadableDestination(false).route, navigationParameter.route)

        pinManagementCoordinator.retryPinManagement()
        advanceUntilIdle()

        Assertions.assertEquals(SubCoordinatorState.Active, pinManagementState)
        verify(exactly = 2) { mockIdCardManager.changePin(mockContext) }

        scanJob.cancel()
        stateJob.cancel()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun errorOther() = runTest {

        // SETUP
        every { mockCoroutineContextProvider.IO } returns dispatcher

        val idCardManagerFlow = MutableStateFlow<EidInteractionEvent>(EidInteractionEvent.PinManagementStarted)
        every { mockIdCardManager.eidFlow } returns idCardManagerFlow

        val canCoordinatorStateFlow = MutableStateFlow(SubCoordinatorState.Cancelled)
        every { mockCanCoordinator.stateFlow } returns canCoordinatorStateFlow

        val pinManagementCoordinator = PinManagementCoordinator(
            context = mockContext,
            canCoordinator = mockCanCoordinator,
            navigator = mockNavigator,
            idCardManager = mockIdCardManager,
            issueTrackerManager = mockIssueTrackerManager,
            coroutineContextProvider = mockCoroutineContextProvider
        )

        var progress = false
        val scanJob = pinManagementCoordinator.scanInProgress
            .onEach { progress = it }
            .launchIn(CoroutineScope(dispatcher))

        // START PIN MANAGEMENT
        val pinManagementStateFlow = pinManagementCoordinator.startPinManagement(pinStatus = PinStatus.TransportPin)
        var pinManagementState = SubCoordinatorState.Finished
        val stateJob = pinManagementStateFlow
            .onEach { pinManagementState = it }
            .launchIn(CoroutineScope(dispatcher))

        var navigationParameter = navigationDestinationSlot.captured
        Assertions.assertEquals(SetupTransportPinDestination(false).route, navigationParameter.route)

        advanceUntilIdle()
        Assertions.assertEquals(SubCoordinatorState.Active, pinManagementState)

        // SET TRANSPORT PIN
        val transportPin = "12345"
        pinManagementCoordinator.setOldPin(oldPin = transportPin)

        verify(exactly = 1) { mockNavigator.navigate(SetupPersonalPinIntroDestination) }

        pinManagementCoordinator.onPersonalPinIntroFinished()

        verify(exactly = 1) { mockNavigator.navigate(SetupPersonalPinInputDestination) }

        // SET NEW PERSONAL PIN
        val personalPin = "000000"
        pinManagementCoordinator.setNewPin(newPin = personalPin)

        verify(exactly = 1) { mockNavigator.navigate(SetupPersonalPinConfirmDestination) }

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

        idCardManagerFlow.value = EidInteractionEvent.Error(exception = IdCardInteractionException.FrameworkError())
        advanceUntilIdle()

        Assertions.assertFalse(progress)
        Assertions.assertEquals(SubCoordinatorState.Cancelled, pinManagementState)
        verify(exactly = 1) { mockNavigator.navigate(SetupOtherErrorDestination) }

        scanJob.cancel()
        stateJob.cancel()
    }
}
