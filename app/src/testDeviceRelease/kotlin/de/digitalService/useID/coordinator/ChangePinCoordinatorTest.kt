package de.digitalService.useID.coordinator

import android.content.Context
import android.net.Uri
import com.ramcosta.composedestinations.spec.Direction
import de.digitalService.useID.analytics.IssueTrackerManagerType
import de.digitalService.useID.flows.*
import de.digitalService.useID.idCardInterface.EidInteractionEvent
import de.digitalService.useID.idCardInterface.EidInteractionManager
import de.digitalService.useID.idCardInterface.EidInteractionException
import de.digitalService.useID.ui.coordinators.CanCoordinator
import de.digitalService.useID.ui.coordinators.ChangePinCoordinator
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
import java.lang.Exception

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MockKExtension::class)
class ChangePinCoordinatorTest {

    @MockK(relaxUnitFun = true)
    lateinit var mockContext: Context

    @MockK(relaxUnitFun = true)
    lateinit var mockCanCoordinator: CanCoordinator

    @MockK(relaxUnitFun = true)
    lateinit var mockNavigator: Navigator

    @MockK(relaxUnitFun = true)
    lateinit var mockEidInteractionManager: EidInteractionManager

    @MockK(relaxUnitFun = true)
    lateinit var mockChangePinStateMachine: ChangePinStateMachine

    @MockK(relaxUnitFun = true)
    lateinit var mockCanStateMachine: CanStateMachine

    @MockK(relaxUnitFun = true)
    lateinit var mockCoroutineContextProvider: CoroutineContextProvider

    @MockK(relaxUnitFun = true)
    lateinit var mockIssueTrackerManager: IssueTrackerManagerType

    val dispatcher = StandardTestDispatcher()

    private val navigationDestinationSlot = slot<Direction>()
    private val navigationPoppingDestinationSlot = slot<Direction>()
    private val navigationPopUpToOrNavigateDestinationSlot = slot<Direction>()

    val stateFlow: MutableStateFlow<Pair<ChangePinStateMachine.Event, ChangePinStateMachine.State>> = MutableStateFlow(
        Pair(
            ChangePinStateMachine.Event.Invalidate, ChangePinStateMachine.State.Invalid
        )
    )

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

        every { mockChangePinStateMachine.state } returns stateFlow
    }

    @Test
    fun singleStateObservation() = runTest {
        val changePinCoordinator = ChangePinCoordinator(
            mockContext,
            mockCanCoordinator,
            mockNavigator,
            mockEidInteractionManager,
            mockChangePinStateMachine,
            mockCanStateMachine,
            mockCoroutineContextProvider,
            mockIssueTrackerManager
        )

        changePinCoordinator.startPinChange(false, false)
        advanceUntilIdle()

        verify(exactly = 1) { mockChangePinStateMachine.state }

        changePinCoordinator.startPinChange(false, false)
        advanceUntilIdle()

        verify(exactly = 1) { mockChangePinStateMachine.state }
    }

    @Nested
    inner class PinManagementStateChangeHandling {
        private fun testTransition(event: ChangePinStateMachine.Event, state: ChangePinStateMachine.State, testScope: TestScope) {
            val changePinCoordinator = ChangePinCoordinator(
                mockContext,
                mockCanCoordinator,
                mockNavigator,
                mockEidInteractionManager,
                mockChangePinStateMachine,
                mockCanStateMachine,
                mockCoroutineContextProvider,
                mockIssueTrackerManager
            )
            changePinCoordinator.startPinChange(false, false)

            stateFlow.value = Pair(event, state)
            testScope.advanceUntilIdle()
        }

        @ParameterizedTest
        @SealedClassesSource(names = [], mode = SealedClassesSource.Mode.EXCLUDE, factoryClass = PinManagementStateFactory::class)
        fun back(state: ChangePinStateMachine.State) = runTest {
            testTransition(ChangePinStateMachine.Event.Back, state, this)

            verify { mockNavigator.pop() }
        }

        @Test
        fun `back from scan screen`() = runTest {
            val state = ChangePinStateMachine.State.NewPinInput(false, false, "")
            testTransition(ChangePinStateMachine.Event.Back, state, this)

            verify { mockNavigator.pop() }
            verify { mockEidInteractionManager.cancelTask() }
        }

        @Test
        fun `backing down`() = runTest {
            val state = ChangePinStateMachine.State.Invalid

            val changePinCoordinator = ChangePinCoordinator(
                mockContext,
                mockCanCoordinator,
                mockNavigator,
                mockEidInteractionManager,
                mockChangePinStateMachine,
                mockCanStateMachine,
                mockCoroutineContextProvider,
                mockIssueTrackerManager
            )
            changePinCoordinator.startPinChange(false, false)

            stateFlow.value = Pair(ChangePinStateMachine.Event.Back, state)
            advanceUntilIdle()

            verify { mockNavigator.pop() }
            verify { mockEidInteractionManager.cancelTask() }
            Assertions.assertEquals(SubCoordinatorState.BACKED_DOWN, changePinCoordinator.stateFlow.value)
        }

        @ParameterizedTest
        @ValueSource(booleans = [true, false])
        fun `old transport PIN input`(identificationPending: Boolean) = runTest {
            val state = ChangePinStateMachine.State.OldTransportPinInput(identificationPending)
            testTransition(ChangePinStateMachine.Event.Invalidate, state, this)

            verify { mockNavigator.navigate(any()) }

            Assertions.assertEquals(SetupTransportPinDestination(false, identificationPending).route, navigationDestinationSlot.captured.route)
        }

        @Test
        fun `new PIN intro`() = runTest {
            val state = ChangePinStateMachine.State.NewPinIntro(false, true, "12345")
            testTransition(ChangePinStateMachine.Event.Invalidate, state, this)

            verify { mockNavigator.navigate(SetupPersonalPinIntroDestination) }
        }

        @Test
        fun `new PIN input with retry event`() = runTest {
            val state = ChangePinStateMachine.State.NewPinInput(false, true, "12345")
            testTransition(ChangePinStateMachine.Event.RetryNewPinConfirmation, state, this)

            verify { mockNavigator.pop() }
        }

        @Test
        fun `new PIN input with first visit`() = runTest {
            val state = ChangePinStateMachine.State.NewPinInput(false, true, "12345")
            testTransition(ChangePinStateMachine.Event.Invalidate, state, this)

            verify { mockNavigator.navigate(SetupPersonalPinInputDestination) }
        }

        @Test
        fun `start id card interaction`() = runTest {
            every { mockEidInteractionManager.eidFlow } returns MutableStateFlow(EidInteractionEvent.CardRecognized)

            val state = ChangePinStateMachine.State.StartIdCardInteraction(false, true, "12345", "000000")
            testTransition(ChangePinStateMachine.Event.Invalidate, state, this)
            advanceUntilIdle()

            verify { mockEidInteractionManager.cancelTask() }
            verify { mockEidInteractionManager.eidFlow }
            verify { mockEidInteractionManager.changePin(mockContext) }
            verify { mockNavigator.popUpToOrNavigate(any(), true) }
            Assertions.assertEquals(SetupScanDestination(true, false).route, navigationPopUpToOrNavigateDestinationSlot.captured.route)
        }

        @ParameterizedTest
        @ValueSource(booleans = [true, false])
        fun `ready for subsequent scan`(identificationPending: Boolean) = runTest {
            val oldPin = "12345"
            val state = ChangePinStateMachine.State.ReadyForSubsequentScan(identificationPending, true, oldPin, "000000")
            testTransition(ChangePinStateMachine.Event.Invalidate, state, this)

            verify { mockNavigator.popUpToOrNavigate(any(), true) }

            verify { mockEidInteractionManager.providePin(oldPin) }
            Assertions.assertEquals(SetupScanDestination(false, identificationPending).route, navigationPopUpToOrNavigateDestinationSlot.captured.route)
        }

        @ParameterizedTest
        @ValueSource(booleans = [true, false])
        fun `framework ready for PIN input`(identificationPending: Boolean) = runTest {
            val oldPin = "123456"
            val newPin = "000000"

            val state = ChangePinStateMachine.State.FrameworkReadyForPinInput(identificationPending, true, oldPin, newPin)
            testTransition(ChangePinStateMachine.Event.Invalidate, state, this)

            verify { mockEidInteractionManager.providePin(oldPin) }
        }

        @ParameterizedTest
        @ValueSource(booleans = [true, false])
        fun `framework ready for new PIN input`(identificationPending: Boolean) = runTest {
            val oldPin = "123456"
            val newPin = "000000"

            val state = ChangePinStateMachine.State.FrameworkReadyForNewPinInput(identificationPending, true, oldPin, newPin)
            testTransition(ChangePinStateMachine.Event.Invalidate, state, this)

            verify { mockEidInteractionManager.provideNewPin(newPin) }
        }

        @ParameterizedTest
        @ValueSource(booleans = [true, false])
        fun `CAN requested with short flow`(identificationPending: Boolean) = runTest {
            val oldPin = "123456"
            val newPin = "000000"

            every { mockCanCoordinator.startPinChangeCanFlow(any(), any(), any(), any()) } returns flow {}
            every { mockCanCoordinator.stateFlow } returns MutableStateFlow(SubCoordinatorState.FINISHED)

            val state = ChangePinStateMachine.State.CanRequested(identificationPending, true, oldPin, newPin, true)
            testTransition(ChangePinStateMachine.Event.Invalidate, state, this)
            advanceUntilIdle()

            verify { mockCanCoordinator.startPinChangeCanFlow(identificationPending, oldPin, newPin, true) }
        }

        @ParameterizedTest
        @ValueSource(booleans = [true, false])
        fun `CAN requested with long flow`(identificationPending: Boolean) = runTest {
            val oldPin = "123456"
            val newPin = "000000"

            every { mockCanCoordinator.startPinChangeCanFlow(any(), any(), any(), any()) } returns flow {}
            every { mockCanCoordinator.stateFlow } returns MutableStateFlow(SubCoordinatorState.FINISHED)

            val state = ChangePinStateMachine.State.CanRequested(identificationPending, true, oldPin, newPin, false)
            testTransition(ChangePinStateMachine.Event.Invalidate, state, this)
            advanceUntilIdle()

            verify { mockCanCoordinator.startPinChangeCanFlow(identificationPending, oldPin, newPin, false) }
        }

        @ParameterizedTest
        @ValueSource(booleans = [true, false])
        fun `CAN requested with short flow with CAN flow already active`(identificationPending: Boolean) = runTest {
            val oldPin = "123456"
            val newPin = "000000"

            every { mockCanCoordinator.stateFlow } returns MutableStateFlow(SubCoordinatorState.ACTIVE)

            val state = ChangePinStateMachine.State.CanRequested(identificationPending, true, oldPin, newPin, true)
            testTransition(ChangePinStateMachine.Event.Invalidate, state, this)
            advanceUntilIdle()

            verify(exactly = 0) { mockCanCoordinator.startPinChangeCanFlow(identificationPending, oldPin, newPin, true) }
        }

        @ParameterizedTest
        @ValueSource(booleans = [true, false])
        fun `CAN requested with long flow with CAN flow already active`(identificationPending: Boolean) = runTest {
            val oldPin = "123456"
            val newPin = "000000"

            every { mockCanCoordinator.stateFlow } returns MutableStateFlow(SubCoordinatorState.ACTIVE)

            val state = ChangePinStateMachine.State.CanRequested(identificationPending, true, oldPin, newPin, false)
            testTransition(ChangePinStateMachine.Event.Invalidate, state, this)
            advanceUntilIdle()

            verify(exactly = 0) { mockCanCoordinator.startPinChangeCanFlow(identificationPending, oldPin, newPin, false) }
        }

        @ParameterizedTest
        @ValueSource(booleans = [true, false])
        fun `old transport PIN retry`(identificationPending: Boolean) = runTest {
            val state = ChangePinStateMachine.State.OldTransportPinRetry(identificationPending, "000000")
            testTransition(ChangePinStateMachine.Event.Invalidate, state, this)

            verify { mockNavigator.navigate(any()) }

            Assertions.assertEquals(SetupTransportPinDestination(true, identificationPending).route, navigationDestinationSlot.captured.route)
        }

        @Test
        fun finished() = runTest {
            val state = ChangePinStateMachine.State.Finished

            val changePinCoordinator = ChangePinCoordinator(
                mockContext,
                mockCanCoordinator,
                mockNavigator,
                mockEidInteractionManager,
                mockChangePinStateMachine,
                mockCanStateMachine,
                mockCoroutineContextProvider,
                mockIssueTrackerManager
            )
            changePinCoordinator.startPinChange(false, false)

            stateFlow.value = Pair(ChangePinStateMachine.Event.Invalidate, state)
            advanceUntilIdle()

            Assertions.assertEquals(SubCoordinatorState.FINISHED, changePinCoordinator.stateFlow.value)
            verify { mockChangePinStateMachine.transition(ChangePinStateMachine.Event.Invalidate) }
            verify { mockEidInteractionManager.cancelTask() }
        }

        @Test
        fun cancelled() = runTest {
            val state = ChangePinStateMachine.State.Cancelled

            val changePinCoordinator = ChangePinCoordinator(
                mockContext,
                mockCanCoordinator,
                mockNavigator,
                mockEidInteractionManager,
                mockChangePinStateMachine,
                mockCanStateMachine,
                mockCoroutineContextProvider,
                mockIssueTrackerManager
            )
            changePinCoordinator.startPinChange(false, false)

            stateFlow.value = Pair(ChangePinStateMachine.Event.Invalidate, state)
            advanceUntilIdle()

            Assertions.assertEquals(SubCoordinatorState.CANCELLED, changePinCoordinator.stateFlow.value)
            verify { mockChangePinStateMachine.transition(ChangePinStateMachine.Event.Invalidate) }
            verify { mockEidInteractionManager.cancelTask() }
        }

        @Test
        fun `card deactivated`() = runTest {
            val state = ChangePinStateMachine.State.CardDeactivated
            testTransition(ChangePinStateMachine.Event.Invalidate, state, this)

            verify { mockNavigator.navigate(SetupCardDeactivatedDestination) }
        }

        @Test
        fun `card blocked`() = runTest {
            val state = ChangePinStateMachine.State.CardBlocked
            testTransition(ChangePinStateMachine.Event.Invalidate, state, this)

            verify { mockNavigator.navigate(SetupCardBlockedDestination) }
        }

        @Test
        fun `card unreadable`() = runTest {
            val state = ChangePinStateMachine.State.ProcessFailed(false, true, "12345", "000000", true)
            testTransition(ChangePinStateMachine.Event.Invalidate, state, this)

            verify { mockNavigator.navigate(any()) }

            Assertions.assertEquals(SetupCardUnreadableDestination(false).route, navigationDestinationSlot.captured.route)
        }

        @Test
        fun `unknown error`() = runTest {
            val state = ChangePinStateMachine.State.UnknownError
            testTransition(ChangePinStateMachine.Event.Invalidate, state, this)

            verify { mockNavigator.navigate(SetupOtherErrorDestination) }
        }
    }

    @Nested
    inner class EidEventHandling {
        @Test
        fun `card recognized and removed`() = runTest {
            val changePinCoordinator = ChangePinCoordinator(
                mockContext,
                mockCanCoordinator,
                mockNavigator,
                mockEidInteractionManager,
                mockChangePinStateMachine,
                mockCanStateMachine,
                mockCoroutineContextProvider,
                mockIssueTrackerManager
            )
            changePinCoordinator.startPinChange(false, false)

            val eIdFlow: MutableStateFlow<EidInteractionEvent> = MutableStateFlow(EidInteractionEvent.CardRecognized)
            every { mockEidInteractionManager.eidFlow } returns eIdFlow

            val newState = ChangePinStateMachine.State.StartIdCardInteraction(false, false, "12345", "000000")
            stateFlow.value = Pair(ChangePinStateMachine.Event.Invalidate, newState)
            advanceUntilIdle()

            Assertions.assertTrue(changePinCoordinator.scanInProgress.value)

            eIdFlow.value = EidInteractionEvent.CardRemoved
            advanceUntilIdle()

            Assertions.assertFalse(changePinCoordinator.scanInProgress.value)
        }

        @Test
        fun `PIN change succeeded`() = runTest {
            val changePinCoordinator = ChangePinCoordinator(
                mockContext,
                mockCanCoordinator,
                mockNavigator,
                mockEidInteractionManager,
                mockChangePinStateMachine,
                mockCanStateMachine,
                mockCoroutineContextProvider,
                mockIssueTrackerManager
            )
            changePinCoordinator.startPinChange(false, false)

            val eIdFlow = MutableStateFlow(EidInteractionEvent.PinChangeSucceeded)
            every { mockEidInteractionManager.eidFlow } returns eIdFlow

            val newState = ChangePinStateMachine.State.StartIdCardInteraction(false, false, "12345", "000000")
            stateFlow.value = Pair(ChangePinStateMachine.Event.Invalidate, newState)
            advanceUntilIdle()

            verify { mockChangePinStateMachine.transition(ChangePinStateMachine.Event.Finish) }
        }

        @Test
        fun `PIN requested`() = runTest {
            val changePinCoordinator = ChangePinCoordinator(
                mockContext,
                mockCanCoordinator,
                mockNavigator,
                mockEidInteractionManager,
                mockChangePinStateMachine,
                mockCanStateMachine,
                mockCoroutineContextProvider,
                mockIssueTrackerManager
            )
            changePinCoordinator.startPinChange(false, false)

            val eIdFlow = MutableStateFlow(EidInteractionEvent.PinRequested(3))
            every { mockEidInteractionManager.eidFlow } returns eIdFlow

            every { mockCanCoordinator.stateFlow } returns MutableStateFlow(SubCoordinatorState.FINISHED)

            val newState = ChangePinStateMachine.State.StartIdCardInteraction(false, false, "12345", "000000")
            stateFlow.value = Pair(ChangePinStateMachine.Event.Invalidate, newState)
            advanceUntilIdle()

            verify { mockChangePinStateMachine.transition(ChangePinStateMachine.Event.FrameworkRequestsPin) }
        }

        @Test
        fun `new PIN requested`() = runTest {
            val changePinCoordinator = ChangePinCoordinator(
                mockContext,
                mockCanCoordinator,
                mockNavigator,
                mockEidInteractionManager,
                mockChangePinStateMachine,
                mockCanStateMachine,
                mockCoroutineContextProvider,
                mockIssueTrackerManager
            )
            changePinCoordinator.startPinChange(false, false)

            val eIdFlow = MutableStateFlow(EidInteractionEvent.NewPinRequested)
            every { mockEidInteractionManager.eidFlow } returns eIdFlow

            every { mockCanCoordinator.stateFlow } returns MutableStateFlow(SubCoordinatorState.FINISHED)

            val newState = ChangePinStateMachine.State.StartIdCardInteraction(false, false, "12345", "000000")
            stateFlow.value = Pair(ChangePinStateMachine.Event.Invalidate, newState)
            advanceUntilIdle()

            verify { mockChangePinStateMachine.transition(ChangePinStateMachine.Event.FrameworkRequestsNewPin) }
        }

        @Test
        fun `request CAN`() = runTest {
            val changePinCoordinator = ChangePinCoordinator(
                mockContext,
                mockCanCoordinator,
                mockNavigator,
                mockEidInteractionManager,
                mockChangePinStateMachine,
                mockCanStateMachine,
                mockCoroutineContextProvider,
                mockIssueTrackerManager
            )
            changePinCoordinator.startPinChange(false, false)

            val eIdFlow = MutableStateFlow(EidInteractionEvent.CanRequested())
            every { mockEidInteractionManager.eidFlow } returns eIdFlow

            every { mockCanCoordinator.stateFlow } returns MutableStateFlow(SubCoordinatorState.FINISHED)

            val newState = ChangePinStateMachine.State.StartIdCardInteraction(false, false, "12345", "000000")
            stateFlow.value = Pair(ChangePinStateMachine.Event.Invalidate, newState)
            advanceUntilIdle()

            verify { mockChangePinStateMachine.transition(ChangePinStateMachine.Event.FrameworkRequestsCan) }
        }

        @Test
        fun `request PUK`() = runTest {
            val changePinCoordinator = ChangePinCoordinator(
                mockContext,
                mockCanCoordinator,
                mockNavigator,
                mockEidInteractionManager,
                mockChangePinStateMachine,
                mockCanStateMachine,
                mockCoroutineContextProvider,
                mockIssueTrackerManager
            )
            changePinCoordinator.startPinChange(false, false)

            val eIdFlow = MutableStateFlow(EidInteractionEvent.PukRequested)
            every { mockEidInteractionManager.eidFlow } returns eIdFlow

            val newState = ChangePinStateMachine.State.StartIdCardInteraction(false, false, "12345", "000000")
            stateFlow.value = Pair(ChangePinStateMachine.Event.Invalidate, newState)
            advanceUntilIdle()

            verify { mockChangePinStateMachine.transition(ChangePinStateMachine.Event.Error(EidInteractionException.CardBlocked)) }
            verify { mockIssueTrackerManager.captureMessage("${EidInteractionException.CardBlocked}") }
        }

        @Test
        fun `card deactivated`() = runTest {
            val changePinCoordinator = ChangePinCoordinator(
                mockContext,
                mockCanCoordinator,
                mockNavigator,
                mockEidInteractionManager,
                mockChangePinStateMachine,
                mockCanStateMachine,
                mockCoroutineContextProvider,
                mockIssueTrackerManager
            )
            changePinCoordinator.startPinChange(false, false)

            val eIdFlow = MutableStateFlow(EidInteractionEvent.Error(EidInteractionException.CardDeactivated))
            every { mockEidInteractionManager.eidFlow } returns eIdFlow

            val newState = ChangePinStateMachine.State.StartIdCardInteraction(false, false, "12345", "000000")
            stateFlow.value = Pair(ChangePinStateMachine.Event.Invalidate, newState)
            advanceUntilIdle()

            verify { mockChangePinStateMachine.transition(ChangePinStateMachine.Event.Error(EidInteractionException.CardDeactivated)) }
            verify { mockIssueTrackerManager.captureMessage("${EidInteractionException.CardDeactivated}") }
        }

        @Test
        fun `process failed`() = runTest {
            val changePinCoordinator = ChangePinCoordinator(
                mockContext,
                mockCanCoordinator,
                mockNavigator,
                mockEidInteractionManager,
                mockChangePinStateMachine,
                mockCanStateMachine,
                mockCoroutineContextProvider,
                mockIssueTrackerManager
            )
            changePinCoordinator.startPinChange(false, false)

            val exception = EidInteractionException.ProcessFailed()
            val capturedExceptionSlot = slot<Exception>()
            val eIdFlow = MutableStateFlow(EidInteractionEvent.Error(exception))
            every { mockEidInteractionManager.eidFlow } returns eIdFlow

            val newState = ChangePinStateMachine.State.StartIdCardInteraction(false, false, "12345", "000000")
            stateFlow.value = Pair(ChangePinStateMachine.Event.Invalidate, newState)
            advanceUntilIdle()

            verify { mockChangePinStateMachine.transition(ChangePinStateMachine.Event.Error(exception)) }
            verify { mockIssueTrackerManager.capture(capture(capturedExceptionSlot)) }
            Assertions.assertEquals(exception.redacted?.message, capturedExceptionSlot.captured.message)
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun `start PIN management`(identificationPending: Boolean) = runTest {
        val changePinCoordinator = ChangePinCoordinator(
            mockContext,
            mockCanCoordinator,
            mockNavigator,
            mockEidInteractionManager,
            mockChangePinStateMachine,
            mockCanStateMachine,
            mockCoroutineContextProvider,
            mockIssueTrackerManager
        )

        changePinCoordinator.startPinChange(identificationPending, true)

        Assertions.assertEquals(SubCoordinatorState.ACTIVE, changePinCoordinator.stateFlow.value)
        verify { mockChangePinStateMachine.transition(ChangePinStateMachine.Event.StartPinChange(identificationPending, true)) }
        verify { mockCanStateMachine.transition(CanStateMachine.Event.Invalidate) }
    }

    @Test
    fun `old PIN entered`() = runTest {
        val changePinCoordinator = ChangePinCoordinator(
            mockContext,
            mockCanCoordinator,
            mockNavigator,
            mockEidInteractionManager,
            mockChangePinStateMachine,
            mockCanStateMachine,
            mockCoroutineContextProvider,
            mockIssueTrackerManager
        )

        val oldPin = "123456"
        changePinCoordinator.onOldPinEntered(oldPin)

        verify { mockChangePinStateMachine.transition(ChangePinStateMachine.Event.EnterOldPin(oldPin)) }
    }

    @Test
    fun `personal PIN intro finished`() = runTest {
        val changePinCoordinator = ChangePinCoordinator(
            mockContext,
            mockCanCoordinator,
            mockNavigator,
            mockEidInteractionManager,
            mockChangePinStateMachine,
            mockCanStateMachine,
            mockCoroutineContextProvider,
            mockIssueTrackerManager
        )

        val oldPin = "123456"
        changePinCoordinator.onPersonalPinIntroFinished()

        verify { mockChangePinStateMachine.transition(ChangePinStateMachine.Event.ConfirmNewPinIntro) }
    }

    @Test
    fun `confirm matching PIN`() = runTest {
        val changePinCoordinator = ChangePinCoordinator(
            mockContext,
            mockCanCoordinator,
            mockNavigator,
            mockEidInteractionManager,
            mockChangePinStateMachine,
            mockCanStateMachine,
            mockCoroutineContextProvider,
            mockIssueTrackerManager
        )

        val newPin = "000000"
        val matching = changePinCoordinator.confirmNewPin(newPin)

        verify { mockChangePinStateMachine.transition(ChangePinStateMachine.Event.ConfirmNewPin(newPin)) }
        Assertions.assertTrue(matching)
    }

    @Test
    fun `confirm PIN not matching`() = runTest {
        val changePinCoordinator = ChangePinCoordinator(
            mockContext,
            mockCanCoordinator,
            mockNavigator,
            mockEidInteractionManager,
            mockChangePinStateMachine,
            mockCanStateMachine,
            mockCoroutineContextProvider,
            mockIssueTrackerManager
        )

        val newPin = "000000"
        every { mockChangePinStateMachine.transition(ChangePinStateMachine.Event.ConfirmNewPin(newPin)) } throws ChangePinStateMachine.Error.PinConfirmationFailed
        val matching = changePinCoordinator.confirmNewPin(newPin)

        verify { mockChangePinStateMachine.transition(ChangePinStateMachine.Event.ConfirmNewPin(newPin)) }
        Assertions.assertFalse(matching)
    }

    @Test
    fun `confirm PIN mismatch error`() = runTest {
        val changePinCoordinator = ChangePinCoordinator(
            mockContext,
            mockCanCoordinator,
            mockNavigator,
            mockEidInteractionManager,
            mockChangePinStateMachine,
            mockCanStateMachine,
            mockCoroutineContextProvider,
            mockIssueTrackerManager
        )

        changePinCoordinator.onConfirmPinMismatchError()

        verify { mockChangePinStateMachine.transition(ChangePinStateMachine.Event.RetryNewPinConfirmation) }
    }

    @Test
    fun `on back`() = runTest {
        val changePinCoordinator = ChangePinCoordinator(
            mockContext,
            mockCanCoordinator,
            mockNavigator,
            mockEidInteractionManager,
            mockChangePinStateMachine,
            mockCanStateMachine,
            mockCoroutineContextProvider,
            mockIssueTrackerManager
        )

        changePinCoordinator.onBack()

        verify { mockChangePinStateMachine.transition(ChangePinStateMachine.Event.Back) }
    }

    @Test
    fun `cancel PIN management`() = runTest {
        val changePinCoordinator = ChangePinCoordinator(
            mockContext,
            mockCanCoordinator,
            mockNavigator,
            mockEidInteractionManager,
            mockChangePinStateMachine,
            mockCanStateMachine,
            mockCoroutineContextProvider,
            mockIssueTrackerManager
        )

        changePinCoordinator.cancelPinManagement()

        Assertions.assertEquals(SubCoordinatorState.CANCELLED, changePinCoordinator.stateFlow.value)
        verify { mockChangePinStateMachine.transition(ChangePinStateMachine.Event.Invalidate) }
        verify { mockEidInteractionManager.cancelTask() }
    }

    @Test
    fun `confirm card unreadable`() = runTest {
        val changePinCoordinator = ChangePinCoordinator(
            mockContext,
            mockCanCoordinator,
            mockNavigator,
            mockEidInteractionManager,
            mockChangePinStateMachine,
            mockCanStateMachine,
            mockCoroutineContextProvider,
            mockIssueTrackerManager
        )

        changePinCoordinator.confirmCardUnreadableError()

        verify { mockChangePinStateMachine.transition(ChangePinStateMachine.Event.ProceedAfterError) }
    }

    @Nested
    inner class CanEventHandling {
        @ParameterizedTest
        @ValueSource(booleans = [true, false])
        fun cancelled(shortFlow: Boolean) = runTest {
            val changePinCoordinator = ChangePinCoordinator(
                mockContext,
                mockCanCoordinator,
                mockNavigator,
                mockEidInteractionManager,
                mockChangePinStateMachine,
                mockCanStateMachine,
                mockCoroutineContextProvider,
                mockIssueTrackerManager
            )
            changePinCoordinator.startPinChange(false, false)

            val oldPin = "123456"
            val newPin = "000000"

            every { mockCanCoordinator.stateFlow } returns MutableStateFlow(SubCoordinatorState.FINISHED)
            every { mockCanCoordinator.startPinChangeCanFlow(false, oldPin, newPin, shortFlow) } returns MutableStateFlow(SubCoordinatorState.CANCELLED)

            val state = ChangePinStateMachine.State.CanRequested(false, true, oldPin, newPin, shortFlow)
            stateFlow.value = Pair(ChangePinStateMachine.Event.Invalidate, state)
            advanceUntilIdle()

            Assertions.assertEquals(SubCoordinatorState.CANCELLED, changePinCoordinator.stateFlow.value)
            verify { mockChangePinStateMachine.transition(ChangePinStateMachine.Event.Invalidate) }
            verify { mockEidInteractionManager.cancelTask() }
        }

        @ParameterizedTest
        @ValueSource(booleans = [true, false])
        fun skipped(shortFlow: Boolean) = runTest {
            val changePinCoordinator = ChangePinCoordinator(
                mockContext,
                mockCanCoordinator,
                mockNavigator,
                mockEidInteractionManager,
                mockChangePinStateMachine,
                mockCanStateMachine,
                mockCoroutineContextProvider,
                mockIssueTrackerManager
            )
            changePinCoordinator.startPinChange(false, false)

            val oldPin = "123456"
            val newPin = "000000"

            every { mockCanCoordinator.stateFlow } returns MutableStateFlow(SubCoordinatorState.FINISHED)
            every { mockCanCoordinator.startPinChangeCanFlow(false, oldPin, newPin, shortFlow) } returns MutableStateFlow(SubCoordinatorState.SKIPPED)

            val state = ChangePinStateMachine.State.CanRequested(false, true, oldPin, newPin, shortFlow)
            stateFlow.value = Pair(ChangePinStateMachine.Event.Invalidate, state)
            advanceUntilIdle()

            Assertions.assertEquals(SubCoordinatorState.SKIPPED, changePinCoordinator.stateFlow.value)
            verify { mockChangePinStateMachine.transition(ChangePinStateMachine.Event.Invalidate) }
            verify { mockEidInteractionManager.cancelTask() }
        }
    }
}
