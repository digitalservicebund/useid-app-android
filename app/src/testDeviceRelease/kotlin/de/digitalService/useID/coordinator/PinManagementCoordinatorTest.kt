package de.digitalService.useID.coordinator

import android.content.Context
import android.net.Uri
import com.ramcosta.composedestinations.spec.Direction
import de.digitalService.useID.analytics.IssueTrackerManager
import de.digitalService.useID.flows.*
import de.digitalService.useID.idCardInterface.EidInteractionEvent
import de.digitalService.useID.idCardInterface.IdCardInteractionException
import de.digitalService.useID.idCardInterface.IdCardManager
import de.digitalService.useID.ui.coordinators.CanCoordinator
import de.digitalService.useID.ui.coordinators.PinManagementCoordinator
import de.digitalService.useID.ui.coordinators.SubCoordinatorState
import de.digitalService.useID.ui.navigation.Navigator
import de.digitalService.useID.ui.screens.destinations.*
import de.digitalService.useID.util.CoroutineContextProvider
import de.digitalService.useID.util.PinManagementStateFactory
import de.jodamob.junit5.SealedClassesSource
import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.openecard.mobile.activation.ActivationResultCode

@OptIn(ExperimentalCoroutinesApi::class)
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
    lateinit var mockPinManagementStateMachine: PinManagementStateMachine

    @MockK(relaxUnitFun = true)
    lateinit var mockCanStateMachine: CanStateMachine

    @MockK(relaxUnitFun = true)
    lateinit var mockCoroutineContextProvider: CoroutineContextProvider

    @OptIn(ExperimentalCoroutinesApi::class)
    val dispatcher = StandardTestDispatcher()

    private val navigationDestinationSlot = slot<Direction>()
    private val navigationPoppingDestinationSlot = slot<Direction>()
    private val navigationPopUpToOrNavigateDestinationSlot = slot<Direction>()

    val stateFlow: MutableStateFlow<Pair<PinManagementStateMachine.Event, PinManagementStateMachine.State>> = MutableStateFlow(Pair(
        PinManagementStateMachine.Event.Invalidate, PinManagementStateMachine.State.Invalid))

    val pinManagementCallback: PinManagementCallback = mockk()
    val pinManagementCanCallback: PinManagementCanCallback = mockk()

    @OptIn(ExperimentalCoroutinesApi::class)
    @BeforeEach
    fun beforeEach() {
        Dispatchers.setMain(dispatcher)

        every { mockNavigator.navigate(capture(navigationDestinationSlot)) } returns Unit
        every { mockNavigator.navigatePopping(capture(navigationPoppingDestinationSlot)) } returns Unit
        every { mockNavigator.popUpToOrNavigate(capture(navigationPopUpToOrNavigateDestinationSlot), any()) } returns Unit

        every { mockCoroutineContextProvider.Default } returns dispatcher
        every { mockCoroutineContextProvider.IO } returns dispatcher

        // For supporting destinations with String nav arguments
        mockkStatic("android.net.Uri")
        every { Uri.encode(any()) } answers { value }
        every { Uri.decode(any()) } answers { value }

        every { mockPinManagementStateMachine.state } returns stateFlow
    }

    @Nested
    inner class PinManagementStateChangeHandling {
        private fun testTransition(event: PinManagementStateMachine.Event, state: PinManagementStateMachine.State, testScope: TestScope) {
            val pinManagementCoordinator = PinManagementCoordinator(
                mockContext,
                mockCanCoordinator,
                mockNavigator,
                mockIdCardManager,
                mockPinManagementStateMachine,
                mockCanStateMachine,
                mockCoroutineContextProvider
            )
            pinManagementCoordinator.startPinManagement(false, false)

            stateFlow.value = Pair(event, state)
            testScope.advanceUntilIdle()
        }

        @ParameterizedTest
        @SealedClassesSource(names = [] , mode = SealedClassesSource.Mode.EXCLUDE, factoryClass = PinManagementStateFactory::class)
        fun back(state: PinManagementStateMachine.State) = runTest {
            testTransition(PinManagementStateMachine.Event.Back, state, this)

            verify { mockNavigator.pop() }
        }

        @Test
        fun `back from scan screen`() = runTest {
            val state = PinManagementStateMachine.State.NewPinInput(false, false, "")
            testTransition(PinManagementStateMachine.Event.Back, state, this)

            verify { mockNavigator.pop() }
            verify { mockIdCardManager.cancelTask() }
        }

        @Test
        fun `backing down`() = runTest {
            val state = PinManagementStateMachine.State.Invalid

            val pinManagementCoordinator = PinManagementCoordinator(
                mockContext,
                mockCanCoordinator,
                mockNavigator,
                mockIdCardManager,
                mockPinManagementStateMachine,
                mockCanStateMachine,
                mockCoroutineContextProvider
            )
            pinManagementCoordinator.startPinManagement(false, false)

            stateFlow.value = Pair(PinManagementStateMachine.Event.Back, state)
            advanceUntilIdle()

            verify { mockNavigator.pop() }
            verify { mockIdCardManager.cancelTask() }
            Assertions.assertEquals(SubCoordinatorState.BACKED_DOWN, pinManagementCoordinator.stateFlow.value)
        }

        @ParameterizedTest
        @ValueSource(booleans = [true, false])
        fun `old transport PIN input`(identificationPending: Boolean) = runTest {
            val state = PinManagementStateMachine.State.OldTransportPinInput(identificationPending)
            testTransition(PinManagementStateMachine.Event.Invalidate, state, this)

            verify { mockNavigator.navigate(any()) }

            Assertions.assertEquals(SetupTransportPinDestination(false, identificationPending).route, navigationDestinationSlot.captured.route)
        }

        @Test
        fun `new PIN intro`() = runTest {
            val state = PinManagementStateMachine.State.NewPinIntro(false, true, "12345")
            testTransition(PinManagementStateMachine.Event.Invalidate, state, this)

            verify { mockNavigator.navigate(SetupPersonalPinIntroDestination) }
        }

        @Test
        fun `new PIN input with retry event`() = runTest {
            val state = PinManagementStateMachine.State.NewPinInput(false, true, "12345")
            testTransition(PinManagementStateMachine.Event.RetryNewPinConfirmation, state, this)

            verify { mockNavigator.pop() }
        }

        @Test
        fun `new PIN input with first visit`() = runTest {
            val state = PinManagementStateMachine.State.NewPinInput(false, true, "12345")
            testTransition(PinManagementStateMachine.Event.Invalidate, state, this)

            verify { mockNavigator.navigate(SetupPersonalPinInputDestination) }
        }

        @Test
        fun `ready for scan`() = runTest {
            val state = PinManagementStateMachine.State.ReadyForScan(false, true, "12345", "000000")
            testTransition(PinManagementStateMachine.Event.Invalidate, state, this)
            advanceUntilIdle()

            verify { mockIdCardManager.cancelTask() }
            verify { mockIdCardManager.eidFlow }
            verify { mockIdCardManager.changePin(mockContext) }
        }

        @ParameterizedTest
        @ValueSource(booleans = [true, false])
        fun `waiting for first card attachment`(identificationPending: Boolean) = runTest {
            val state = PinManagementStateMachine.State.WaitingForFirstCardAttachment(identificationPending, true, "12345", "000000")
            testTransition(PinManagementStateMachine.Event.Invalidate, state, this)

            verify { mockNavigator.popUpToOrNavigate(any(), true) }

            Assertions.assertEquals(SetupScanDestination(true, identificationPending).route, navigationPopUpToOrNavigateDestinationSlot.captured.route)
        }

        @ParameterizedTest
        @ValueSource(booleans = [true, false])
        fun `waiting for card re-attachment`(identificationPending: Boolean) = runTest {
            val state = PinManagementStateMachine.State.WaitingForCardReAttachment(identificationPending, true, "12345", "000000")
            testTransition(PinManagementStateMachine.Event.Invalidate, state, this)

            verify { mockNavigator.popUpToOrNavigate(any(), true) }

            Assertions.assertEquals(SetupScanDestination(false, identificationPending).route, navigationPopUpToOrNavigateDestinationSlot.captured.route)
        }

        @ParameterizedTest
        @ValueSource(booleans = [true, false])
        fun `framework ready for PIN management`(identificationPending: Boolean) = runTest {
            val oldPin = "123456"
            val newPin = "000000"

            val state = PinManagementStateMachine.State.FrameworkReadyForPinManagement(identificationPending, true, oldPin, newPin, pinManagementCallback)
            testTransition(PinManagementStateMachine.Event.Invalidate, state, this)

            verify { pinManagementCallback(oldPin, newPin) }
        }

        @ParameterizedTest
        @ValueSource(booleans = [true, false])
        fun `CAN requested with short flow`(identificationPending: Boolean) = runTest {
            val oldPin = "123456"
            val newPin = "000000"

            every { mockCanCoordinator.stateFlow } returns MutableStateFlow(SubCoordinatorState.FINISHED)

            val state = PinManagementStateMachine.State.CanRequested(identificationPending, true, oldPin, newPin, true)
            testTransition(PinManagementStateMachine.Event.Invalidate, state, this)
            advanceUntilIdle()

            verify { mockCanCoordinator.startPinManagementCanFlow(identificationPending, oldPin, newPin, true) }
        }

        @ParameterizedTest
        @ValueSource(booleans = [true, false])
        fun `CAN requested with long flow`(identificationPending: Boolean) = runTest {
            val oldPin = "123456"
            val newPin = "000000"

            every { mockCanCoordinator.stateFlow } returns MutableStateFlow(SubCoordinatorState.FINISHED)

            val state = PinManagementStateMachine.State.CanRequested(identificationPending, true, oldPin, newPin, false)
            testTransition(PinManagementStateMachine.Event.Invalidate, state, this)
            advanceUntilIdle()

            verify { mockCanCoordinator.startPinManagementCanFlow(identificationPending, oldPin, newPin, false) }
        }

        @ParameterizedTest
        @ValueSource(booleans = [true, false])
        fun `CAN requested with short flow with CAN flow already active`(identificationPending: Boolean) = runTest {
            val oldPin = "123456"
            val newPin = "000000"

            every { mockCanCoordinator.stateFlow } returns MutableStateFlow(SubCoordinatorState.ACTIVE)

            val state = PinManagementStateMachine.State.CanRequested(identificationPending, true, oldPin, newPin, true)
            testTransition(PinManagementStateMachine.Event.Invalidate, state, this)
            advanceUntilIdle()

            verify(exactly = 0) { mockCanCoordinator.startPinManagementCanFlow(identificationPending, oldPin, newPin, true) }
        }

        @ParameterizedTest
        @ValueSource(booleans = [true, false])
        fun `CAN requested with long flow with CAN flow already active`(identificationPending: Boolean) = runTest {
            val oldPin = "123456"
            val newPin = "000000"

            every { mockCanCoordinator.stateFlow } returns MutableStateFlow(SubCoordinatorState.ACTIVE)

            val state = PinManagementStateMachine.State.CanRequested(identificationPending, true, oldPin, newPin, false)
            testTransition(PinManagementStateMachine.Event.Invalidate, state, this)
            advanceUntilIdle()

            verify(exactly = 0) { mockCanCoordinator.startPinManagementCanFlow(identificationPending, oldPin, newPin, false) }
        }

        @ParameterizedTest
        @ValueSource(booleans = [true, false])
        fun `old transport PIN retry`(identificationPending: Boolean) = runTest {
            val oldPin = "123456"
            val newPin = "000000"

            val state = PinManagementStateMachine.State.OldTransportPinRetry(identificationPending, "000000", pinManagementCallback)
            testTransition(PinManagementStateMachine.Event.Invalidate, state, this)

            verify { mockNavigator.navigate(any()) }

            Assertions.assertEquals(SetupTransportPinDestination(true, identificationPending).route, navigationDestinationSlot.captured.route)
        }

        @Test
        fun finished() = runTest {
            val state = PinManagementStateMachine.State.Finished

            val pinManagementCoordinator = PinManagementCoordinator(
                mockContext,
                mockCanCoordinator,
                mockNavigator,
                mockIdCardManager,
                mockPinManagementStateMachine,
                mockCanStateMachine,
                mockCoroutineContextProvider
            )
            pinManagementCoordinator.startPinManagement(false, false)

            stateFlow.value = Pair(PinManagementStateMachine.Event.Invalidate, state)
            advanceUntilIdle()

            Assertions.assertEquals(SubCoordinatorState.FINISHED, pinManagementCoordinator.stateFlow.value)
            verify { mockPinManagementStateMachine.transition(PinManagementStateMachine.Event.Invalidate) }
            verify { mockIdCardManager.cancelTask() }
        }

        @Test
        fun cancelled() = runTest {
            val state = PinManagementStateMachine.State.Cancelled

            val pinManagementCoordinator = PinManagementCoordinator(
                mockContext,
                mockCanCoordinator,
                mockNavigator,
                mockIdCardManager,
                mockPinManagementStateMachine,
                mockCanStateMachine,
                mockCoroutineContextProvider
            )
            pinManagementCoordinator.startPinManagement(false, false)

            stateFlow.value = Pair(PinManagementStateMachine.Event.Invalidate, state)
            advanceUntilIdle()

            Assertions.assertEquals(SubCoordinatorState.CANCELLED, pinManagementCoordinator.stateFlow.value)
            verify { mockPinManagementStateMachine.transition(PinManagementStateMachine.Event.Invalidate) }
            verify { mockIdCardManager.cancelTask() }
        }

        @Test
        fun `card deactivated`() = runTest {
            val state = PinManagementStateMachine.State.CardDeactivated
            testTransition(PinManagementStateMachine.Event.Invalidate, state, this)

            verify { mockNavigator.navigate(SetupCardDeactivatedDestination) }
        }

        @Test
        fun `card blocked`() = runTest {
            val state = PinManagementStateMachine.State.CardBlocked
            testTransition(PinManagementStateMachine.Event.Invalidate, state, this)

            verify { mockNavigator.navigate(SetupCardBlockedDestination) }
        }

        @Test
        fun `card unreadable`() = runTest {
            val state = PinManagementStateMachine.State.ProcessFailed(false, true, "12345", "000000", true)
            testTransition(PinManagementStateMachine.Event.Invalidate, state, this)

            verify { mockNavigator.navigate(any()) }

            Assertions.assertEquals(SetupCardUnreadableDestination(false).route, navigationDestinationSlot.captured.route)
        }

        @Test
        fun `unknown error`() = runTest {
            val state = PinManagementStateMachine.State.UnknownError
            testTransition(PinManagementStateMachine.Event.Invalidate, state, this)

            verify { mockNavigator.navigate(SetupOtherErrorDestination) }
        }
    }

    @Nested
    inner class EidEventHandling {
        @Test
        fun `request card insertion`() = runTest {
            val pinManagementCoordinator = PinManagementCoordinator(
                mockContext,
                mockCanCoordinator,
                mockNavigator,
                mockIdCardManager,
                mockPinManagementStateMachine,
                mockCanStateMachine,
                mockCoroutineContextProvider
            )
            pinManagementCoordinator.startPinManagement(false, false)

            val eIdFlow = MutableStateFlow(EidInteractionEvent.RequestCardInsertion)
            every { mockIdCardManager.eidFlow} returns eIdFlow

            val newState = PinManagementStateMachine.State.ReadyForScan(false, false, "12345", "000000")
            stateFlow.value = Pair(PinManagementStateMachine.Event.Invalidate, newState)
            advanceUntilIdle()

            verify { mockPinManagementStateMachine.transition(PinManagementStateMachine.Event.RequestCardInsertion) }
        }

        @Test
        fun `card recognized and removed`() = runTest {
            val pinManagementCoordinator = PinManagementCoordinator(
                mockContext,
                mockCanCoordinator,
                mockNavigator,
                mockIdCardManager,
                mockPinManagementStateMachine,
                mockCanStateMachine,
                mockCoroutineContextProvider
            )
            pinManagementCoordinator.startPinManagement(false, false)

            val eIdFlow: MutableStateFlow<EidInteractionEvent> = MutableStateFlow(EidInteractionEvent.CardRecognized)
            every { mockIdCardManager.eidFlow} returns eIdFlow

            val newState = PinManagementStateMachine.State.ReadyForScan(false, false, "12345", "000000")
            stateFlow.value = Pair(PinManagementStateMachine.Event.Invalidate, newState)
            advanceUntilIdle()

            Assertions.assertTrue(pinManagementCoordinator.scanInProgress.value)

            eIdFlow.value = EidInteractionEvent.CardRemoved
            advanceUntilIdle()

            Assertions.assertFalse(pinManagementCoordinator.scanInProgress.value)
        }

        @Test
        fun `process completed successfully`() = runTest {
            val pinManagementCoordinator = PinManagementCoordinator(
                mockContext,
                mockCanCoordinator,
                mockNavigator,
                mockIdCardManager,
                mockPinManagementStateMachine,
                mockCanStateMachine,
                mockCoroutineContextProvider
            )
            pinManagementCoordinator.startPinManagement(false, false)

            val eIdFlow = MutableStateFlow(EidInteractionEvent.ProcessCompletedSuccessfullyWithoutResult)
            every { mockIdCardManager.eidFlow} returns eIdFlow

            val newState = PinManagementStateMachine.State.ReadyForScan(false, false, "12345", "000000")
            stateFlow.value = Pair(PinManagementStateMachine.Event.Invalidate, newState)
            advanceUntilIdle()

            verify { mockPinManagementStateMachine.transition(PinManagementStateMachine.Event.Finish) }
        }

        @Test
        fun `request changed PIN`() = runTest {
            val pinManagementCoordinator = PinManagementCoordinator(
                mockContext,
                mockCanCoordinator,
                mockNavigator,
                mockIdCardManager,
                mockPinManagementStateMachine,
                mockCanStateMachine,
                mockCoroutineContextProvider
            )
            pinManagementCoordinator.startPinManagement(false, false)

            val eIdFlow = MutableStateFlow(EidInteractionEvent.RequestChangedPin(null, pinManagementCallback))
            every { mockIdCardManager.eidFlow} returns eIdFlow

            val newState = PinManagementStateMachine.State.ReadyForScan(false, false, "12345", "000000")
            stateFlow.value = Pair(PinManagementStateMachine.Event.Invalidate, newState)
            advanceUntilIdle()

            verify { mockPinManagementStateMachine.transition(PinManagementStateMachine.Event.FrameworkRequestsChangedPin(pinManagementCallback)) }
        }

        @Test
        fun `request changed PIN and CAN`() = runTest {
            val pinManagementCoordinator = PinManagementCoordinator(
                mockContext,
                mockCanCoordinator,
                mockNavigator,
                mockIdCardManager,
                mockPinManagementStateMachine,
                mockCanStateMachine,
                mockCoroutineContextProvider
            )
            pinManagementCoordinator.startPinManagement(false, false)

            val eIdFlow = MutableStateFlow(EidInteractionEvent.RequestCanAndChangedPin(pinManagementCanCallback))
            every { mockIdCardManager.eidFlow} returns eIdFlow

            val newState = PinManagementStateMachine.State.ReadyForScan(false, false, "12345", "000000")
            stateFlow.value = Pair(PinManagementStateMachine.Event.Invalidate, newState)
            advanceUntilIdle()

            verify { mockPinManagementStateMachine.transition(PinManagementStateMachine.Event.FrameworkRequestsCan) }
        }

        @Test
        fun `request PUK`() = runTest {
            val pinManagementCoordinator = PinManagementCoordinator(
                mockContext,
                mockCanCoordinator,
                mockNavigator,
                mockIdCardManager,
                mockPinManagementStateMachine,
                mockCanStateMachine,
                mockCoroutineContextProvider
            )
            pinManagementCoordinator.startPinManagement(false, false)

            val eIdFlow = MutableStateFlow(EidInteractionEvent.RequestPuk({_ -> }))
            every { mockIdCardManager.eidFlow} returns eIdFlow

            val newState = PinManagementStateMachine.State.ReadyForScan(false, false, "12345", "000000")
            stateFlow.value = Pair(PinManagementStateMachine.Event.Invalidate, newState)
            advanceUntilIdle()

            verify { mockPinManagementStateMachine.transition(PinManagementStateMachine.Event.Error(IdCardInteractionException.CardBlocked)) }
        }

        @Test
        fun `card deactivated`() = runTest {
            val pinManagementCoordinator = PinManagementCoordinator(
                mockContext,
                mockCanCoordinator,
                mockNavigator,
                mockIdCardManager,
                mockPinManagementStateMachine,
                mockCanStateMachine,
                mockCoroutineContextProvider
            )
            pinManagementCoordinator.startPinManagement(false, false)

            val eIdFlow = MutableStateFlow(EidInteractionEvent.Error(IdCardInteractionException.CardDeactivated))
            every { mockIdCardManager.eidFlow} returns eIdFlow

            val newState = PinManagementStateMachine.State.ReadyForScan(false, false, "12345", "000000")
            stateFlow.value = Pair(PinManagementStateMachine.Event.Invalidate, newState)
            advanceUntilIdle()

            verify { mockPinManagementStateMachine.transition(PinManagementStateMachine.Event.Error(IdCardInteractionException.CardDeactivated)) }
        }

        @Test
        fun `process failed`() = runTest {
            val pinManagementCoordinator = PinManagementCoordinator(
                mockContext,
                mockCanCoordinator,
                mockNavigator,
                mockIdCardManager,
                mockPinManagementStateMachine,
                mockCanStateMachine,
                mockCoroutineContextProvider
            )
            pinManagementCoordinator.startPinManagement(false, false)

            val exception = IdCardInteractionException.ProcessFailed(ActivationResultCode.INTERRUPTED, null, null)
            val eIdFlow = MutableStateFlow(EidInteractionEvent.Error(exception))
            every { mockIdCardManager.eidFlow} returns eIdFlow

            val newState = PinManagementStateMachine.State.ReadyForScan(false, false, "12345", "000000")
            stateFlow.value = Pair(PinManagementStateMachine.Event.Invalidate, newState)
            advanceUntilIdle()

            verify { mockPinManagementStateMachine.transition(PinManagementStateMachine.Event.Error(exception)) }
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun `start PIN management`(identificationPending: Boolean) = runTest {
        val pinManagementCoordinator = PinManagementCoordinator(
            mockContext,
            mockCanCoordinator,
            mockNavigator,
            mockIdCardManager,
            mockPinManagementStateMachine,
            mockCanStateMachine,
            mockCoroutineContextProvider
        )

        pinManagementCoordinator.startPinManagement(identificationPending, true)

        Assertions.assertEquals(SubCoordinatorState.ACTIVE, pinManagementCoordinator.stateFlow.value)
        verify { mockPinManagementStateMachine.transition(PinManagementStateMachine.Event.StartPinManagement(identificationPending, true)) }
        verify { mockCanStateMachine.transition(CanStateMachine.Event.Invalidate) }
    }

    @Test
    fun `old PIN entered`() = runTest {
        val pinManagementCoordinator = PinManagementCoordinator(
            mockContext,
            mockCanCoordinator,
            mockNavigator,
            mockIdCardManager,
            mockPinManagementStateMachine,
            mockCanStateMachine,
            mockCoroutineContextProvider
        )

        val oldPin = "123456"
        pinManagementCoordinator.onOldPinEntered(oldPin)

        verify { mockPinManagementStateMachine.transition(PinManagementStateMachine.Event.EnterOldPin(oldPin)) }
    }

    @Test
    fun `personal PIN intro finished`() = runTest {
        val pinManagementCoordinator = PinManagementCoordinator(
            mockContext,
            mockCanCoordinator,
            mockNavigator,
            mockIdCardManager,
            mockPinManagementStateMachine,
            mockCanStateMachine,
            mockCoroutineContextProvider
        )

        val oldPin = "123456"
        pinManagementCoordinator.onPersonalPinIntroFinished()

        verify { mockPinManagementStateMachine.transition(PinManagementStateMachine.Event.ConfirmNewPinIntro) }
    }

    @Test
    fun `confirm matching PIN`() = runTest {
        val pinManagementCoordinator = PinManagementCoordinator(
            mockContext,
            mockCanCoordinator,
            mockNavigator,
            mockIdCardManager,
            mockPinManagementStateMachine,
            mockCanStateMachine,
            mockCoroutineContextProvider
        )

        val newPin = "000000"
        val matching = pinManagementCoordinator.confirmNewPin(newPin)

        verify { mockPinManagementStateMachine.transition(PinManagementStateMachine.Event.ConfirmNewPin(newPin)) }
        Assertions.assertTrue(matching)
    }

    @Test
    fun `confirm PIN not matching`() = runTest {
        val pinManagementCoordinator = PinManagementCoordinator(
            mockContext,
            mockCanCoordinator,
            mockNavigator,
            mockIdCardManager,
            mockPinManagementStateMachine,
            mockCanStateMachine,
            mockCoroutineContextProvider
        )

        val newPin = "000000"
        every { mockPinManagementStateMachine.transition(PinManagementStateMachine.Event.ConfirmNewPin(newPin)) } throws PinManagementStateMachine.Error.PinConfirmationFailed
        val matching = pinManagementCoordinator.confirmNewPin(newPin)

        verify { mockPinManagementStateMachine.transition(PinManagementStateMachine.Event.ConfirmNewPin(newPin)) }
        Assertions.assertFalse(matching)
    }

    @Test
    fun `confirm PIN mismatch error`() = runTest {
        val pinManagementCoordinator = PinManagementCoordinator(
            mockContext,
            mockCanCoordinator,
            mockNavigator,
            mockIdCardManager,
            mockPinManagementStateMachine,
            mockCanStateMachine,
            mockCoroutineContextProvider
        )

        pinManagementCoordinator.onConfirmPinMismatchError()

        verify { mockPinManagementStateMachine.transition(PinManagementStateMachine.Event.RetryNewPinConfirmation) }
    }

    @Test
    fun `on back`() = runTest {
        val pinManagementCoordinator = PinManagementCoordinator(
            mockContext,
            mockCanCoordinator,
            mockNavigator,
            mockIdCardManager,
            mockPinManagementStateMachine,
            mockCanStateMachine,
            mockCoroutineContextProvider
        )

        pinManagementCoordinator.onBack()

        verify { mockPinManagementStateMachine.transition(PinManagementStateMachine.Event.Back) }
    }

    @Test
    fun `cancel PIN management`() = runTest {
        val pinManagementCoordinator = PinManagementCoordinator(
            mockContext,
            mockCanCoordinator,
            mockNavigator,
            mockIdCardManager,
            mockPinManagementStateMachine,
            mockCanStateMachine,
            mockCoroutineContextProvider
        )

        pinManagementCoordinator.cancelPinManagement()

        Assertions.assertEquals(SubCoordinatorState.CANCELLED, pinManagementCoordinator.stateFlow.value)
        verify { mockPinManagementStateMachine.transition(PinManagementStateMachine.Event.Invalidate) }
        verify { mockIdCardManager.cancelTask() }
    }

    @Test
    fun `confirm card unreadable`() = runTest {
        val pinManagementCoordinator = PinManagementCoordinator(
            mockContext,
            mockCanCoordinator,
            mockNavigator,
            mockIdCardManager,
            mockPinManagementStateMachine,
            mockCanStateMachine,
            mockCoroutineContextProvider
        )

        pinManagementCoordinator.confirmCardUnreadableError()

        verify { mockPinManagementStateMachine.transition(PinManagementStateMachine.Event.ProceedAfterError) }
    }

    @Nested
    inner class CanEventHandling{
        @ParameterizedTest
        @ValueSource(booleans = [true, false])
        fun cancelled(shortFlow: Boolean) = runTest {
            val pinManagementCoordinator = PinManagementCoordinator(
                mockContext,
                mockCanCoordinator,
                mockNavigator,
                mockIdCardManager,
                mockPinManagementStateMachine,
                mockCanStateMachine,
                mockCoroutineContextProvider
            )
            pinManagementCoordinator.startPinManagement(false, false)

            val oldPin = "123456"
            val newPin = "000000"

            every { mockCanCoordinator.stateFlow } returns MutableStateFlow(SubCoordinatorState.FINISHED)
            every { mockCanCoordinator.startPinManagementCanFlow(false, oldPin, newPin, shortFlow) } returns MutableStateFlow(SubCoordinatorState.CANCELLED)

            val state = PinManagementStateMachine.State.CanRequested(false, true, oldPin, newPin, shortFlow)
            stateFlow.value = Pair(PinManagementStateMachine.Event.Invalidate, state)
            advanceUntilIdle()

            Assertions.assertEquals(SubCoordinatorState.CANCELLED, pinManagementCoordinator.stateFlow.value)
            verify { mockPinManagementStateMachine.transition(PinManagementStateMachine.Event.Invalidate) }
            verify { mockIdCardManager.cancelTask() }
        }

        @ParameterizedTest
        @ValueSource(booleans = [true, false])
        fun skipped(shortFlow: Boolean) = runTest {
            val pinManagementCoordinator = PinManagementCoordinator(
                mockContext,
                mockCanCoordinator,
                mockNavigator,
                mockIdCardManager,
                mockPinManagementStateMachine,
                mockCanStateMachine,
                mockCoroutineContextProvider
            )
            pinManagementCoordinator.startPinManagement(false, false)

            val oldPin = "123456"
            val newPin = "000000"

            every { mockCanCoordinator.stateFlow } returns MutableStateFlow(SubCoordinatorState.FINISHED)
            every { mockCanCoordinator.startPinManagementCanFlow(false, oldPin, newPin, shortFlow) } returns MutableStateFlow(SubCoordinatorState.SKIPPED)

            val state = PinManagementStateMachine.State.CanRequested(false, true, oldPin, newPin, shortFlow)
            stateFlow.value = Pair(PinManagementStateMachine.Event.Invalidate, state)
            advanceUntilIdle()

            Assertions.assertEquals(SubCoordinatorState.SKIPPED, pinManagementCoordinator.stateFlow.value)
            verify { mockPinManagementStateMachine.transition(PinManagementStateMachine.Event.Invalidate) }
            verify { mockIdCardManager.cancelTask() }
        }
    }
}
