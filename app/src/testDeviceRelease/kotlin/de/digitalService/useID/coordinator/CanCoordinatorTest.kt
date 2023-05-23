package de.digitalService.useID.coordinator

import android.net.Uri
import com.ramcosta.composedestinations.spec.Direction
import de.digitalService.useID.flows.*
import de.digitalService.useID.idCardInterface.*
import de.digitalService.useID.ui.coordinators.CanCoordinator
import de.digitalService.useID.ui.coordinators.SubCoordinatorState
import de.digitalService.useID.ui.navigation.Navigator
import de.digitalService.useID.ui.screens.destinations.*
import de.digitalService.useID.util.CanIdentStateFactory
import de.digitalService.useID.util.CanPinManagementStateFactory
import de.digitalService.useID.util.CoroutineContextProvider
import de.digitalService.useID.util.EidInteractionEventTypeFactory
import de.jodamob.junit5.SealedClassesSource
import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MockKExtension::class)
class CanCoordinatorTest {

    @MockK(relaxUnitFun = true)
    lateinit var mockAppNavigator: Navigator

    @MockK(relaxUnitFun = true)
    lateinit var mockEidInteractionManager: EidInteractionManager

    @MockK(relaxUnitFun = true)
    lateinit var mockCanStateMachine: CanStateMachine

    @MockK(relaxUnitFun = true)
    lateinit var mockCoroutineContextProvider: CoroutineContextProvider

    private val dispatcher = StandardTestDispatcher()

    private val navigationDestinationSlots = mutableListOf<Direction>()

    @OptIn(ExperimentalCoroutinesApi::class)
    @BeforeEach
    fun setup() {
        Dispatchers.setMain(dispatcher)

        navigationDestinationSlots.clear()
        every { mockAppNavigator.navigate(capture(navigationDestinationSlots)) } returns Unit

        every { mockCoroutineContextProvider.Default } returns dispatcher
        every { mockCoroutineContextProvider.IO } returns dispatcher

        // For supporting destinations with String nav arguments
        mockkStatic("android.net.Uri")
        every { Uri.encode(any()) } answers { value }
        every { Uri.decode(any()) } answers { value }
    }

    @Test
    fun singleStateObservation() = runTest {
        val stateFlow: MutableStateFlow<Pair<CanStateMachine.Event, CanStateMachine.State>> = MutableStateFlow(Pair(CanStateMachine.Event.Invalidate, CanStateMachine.State.Invalid))
        every { mockCanStateMachine.state } returns stateFlow
        every { mockEidInteractionManager.eidFlow } returns flowOf()

        val canCoordinator = CanCoordinator(mockAppNavigator, mockEidInteractionManager, mockCanStateMachine, mockCoroutineContextProvider)

        canCoordinator.startIdentCanFlow(null)
        advanceUntilIdle()

        verify(exactly = 1) { mockCanStateMachine.state }

        canCoordinator.startIdentCanFlow(null)
        advanceUntilIdle()

        verify(exactly = 1) { mockCanStateMachine.state }
    }

    @Nested
    inner class CanStateChangeHandling {
        private fun testTransition(event: CanStateMachine.Event, state: CanStateMachine.State, testScope: TestScope) {
            val stateFlow: MutableStateFlow<Pair<CanStateMachine.Event, CanStateMachine.State>> = MutableStateFlow(Pair(CanStateMachine.Event.Invalidate, CanStateMachine.State.Invalid))
            every { mockCanStateMachine.state } returns stateFlow

            val canCoordinator = CanCoordinator(mockAppNavigator, mockEidInteractionManager, mockCanStateMachine, mockCoroutineContextProvider)
            canCoordinator.startIdentCanFlow(null)

            stateFlow.value = Pair(event, state)
            testScope.advanceUntilIdle()
        }

        @Nested
        inner class PinManagement {

            @BeforeEach
            fun setup() {
                every { mockEidInteractionManager.eidFlow } returns flowOf()
            }

            @ParameterizedTest
            @SealedClassesSource(names = [] , mode = SealedClassesSource.Mode.EXCLUDE, factoryClass = CanPinManagementStateFactory::class)
            fun back(state: CanStateMachine.State.ChangePin) = runTest {
                testTransition(CanStateMachine.Event.Back, state, this)

                verify { mockAppNavigator.pop() }
            }

            @ParameterizedTest
            @ValueSource(booleans = [true, false])
            fun intro(identificationPending: Boolean) = runTest {
                val oldPin = "123456"
                val newPin = "000000"
                val newState = CanStateMachine.State.ChangePin.Intro(identificationPending, oldPin, newPin)

                testTransition(CanStateMachine.Event.Invalidate, newState, this)

                verify { mockAppNavigator.navigate(any()) }

                Assertions.assertEquals(SetupCanConfirmTransportPinDestination(oldPin, identificationPending).route, navigationDestinationSlots.last().route)
            }

            @ParameterizedTest
            @ValueSource(booleans = [true, false])
            fun `ID already setup`(identificationPending: Boolean) = runTest {
                val oldPin = "123456"
                val newPin = "000000"
                val newState = CanStateMachine.State.ChangePin.IdAlreadySetup(identificationPending, oldPin, newPin)

                testTransition(CanStateMachine.Event.Invalidate, newState, this)

                verify { mockAppNavigator.navigate(any()) }

                Assertions.assertEquals(SetupCanAlreadySetupDestination(identificationPending).route, navigationDestinationSlots.last().route)
            }

            @ParameterizedTest
            @ValueSource(booleans = [true, false])
            fun `PIN Reset`(identificationPending: Boolean) = runTest {
                val oldPin = "123456"
                val newPin = "000000"
                val newState = CanStateMachine.State.ChangePin.PinReset(identificationPending, oldPin, newPin)

                testTransition(CanStateMachine.Event.Invalidate, newState, this)

                verify { mockAppNavigator.navigate(CanResetPersonalPinDestination) }
            }

            @ParameterizedTest
            @ValueSource(booleans = [true, false])
            fun `CAN intro with pending identification`(shortFlow: Boolean) = runTest {
                val oldPin = "123456"
                val newPin = "000000"
                val newState = CanStateMachine.State.ChangePin.CanIntro(true, oldPin, newPin, shortFlow)

                testTransition(CanStateMachine.Event.Invalidate, newState, this)

                verify { mockAppNavigator.navigate(any()) }

                Assertions.assertEquals(SetupCanIntroDestination(!shortFlow, true).route, navigationDestinationSlots.last().route)
            }

            @ParameterizedTest
            @ValueSource(booleans = [true, false])
            fun `CAN intro without pending identification`(shortFlow: Boolean) = runTest {
                val oldPin = "123456"
                val newPin = "000000"
                val newState = CanStateMachine.State.ChangePin.CanIntro(false, oldPin, newPin, shortFlow)

                testTransition(CanStateMachine.Event.Invalidate, newState, this)

                verify { mockAppNavigator.navigate(any()) }

                Assertions.assertEquals(SetupCanIntroDestination(!shortFlow, false).route, navigationDestinationSlots.last().route)
            }

            @ParameterizedTest
            @ValueSource(booleans = [true, false])
            fun `CAN input`(identificationPending: Boolean) = runTest {
                val oldPin = "123456"
                val newPin = "000000"
                val newState = CanStateMachine.State.ChangePin.CanInput(identificationPending, oldPin, newPin, false)

                testTransition(CanStateMachine.Event.Invalidate, newState, this)

                verify { mockAppNavigator.navigate(any()) }

                Assertions.assertEquals(CanInputDestination(false).route, navigationDestinationSlots.last().route)
            }

            @ParameterizedTest
            @ValueSource(booleans = [true, false])
            fun `CAN input retrying`(identificationPending: Boolean) = runTest {
                val oldPin = "123456"
                val newPin = "000000"
                val newState = CanStateMachine.State.ChangePin.CanInputRetry(identificationPending, oldPin, newPin)

                testTransition(CanStateMachine.Event.Invalidate, newState, this)

                verify(exactly = 2) { mockAppNavigator.navigate(any()) }

                Assertions.assertEquals(SetupCanIntroDestination(false, identificationPending).route, navigationDestinationSlots.first().route)
                Assertions.assertEquals(CanInputDestination(true).route, navigationDestinationSlots.last().route)
            }

            @ParameterizedTest
            @ValueSource(booleans = [true, false])
            fun `PIN input`(identificationPending: Boolean) = runTest {
                val oldPin = "123456"
                val newPin = "000000"
                val newState = CanStateMachine.State.ChangePin.PinInput(identificationPending, oldPin,"654321", newPin)

                testTransition(CanStateMachine.Event.Invalidate, newState, this)

                verify { mockAppNavigator.navigate(any()) }

                Assertions.assertEquals(SetupCanTransportPinDestination(identificationPending).route, navigationDestinationSlots.last().route)
            }

            @ParameterizedTest
            @ValueSource(booleans = [true, false])
            fun `CAN and PIN entered`(identificationPending: Boolean) = runTest {
                val oldPin = "123456"
                val newPin = "000000"
                val can = "654321"
                val destinationSlot = slot<Direction>()

                val newState = CanStateMachine.State.ChangePin.CanAndPinEntered(identificationPending, oldPin, can, newPin)

                testTransition(CanStateMachine.Event.Invalidate, newState, this)

                verify { mockAppNavigator.popUpToOrNavigate(capture(destinationSlot), true) }
                verify { mockEidInteractionManager.provideCan(can) }

                Assertions.assertEquals(SetupScanDestination(false,  identificationPending).route, destinationSlot.captured.route)
            }

            @ParameterizedTest
            @ValueSource(booleans = [true, false])
            fun `framework ready for PIN input`(identificationPending: Boolean) = runTest {
                val pin = "123456"
                val newPin = "000000"

                val newState = CanStateMachine.State.ChangePin.FrameworkReadyForPinInput(identificationPending, pin, newPin)

                testTransition(CanStateMachine.Event.Invalidate, newState, this)

                verify { mockEidInteractionManager.providePin(pin) }
            }

            @ParameterizedTest
            @ValueSource(booleans = [true, false])
            fun `framework ready for new PIN input`(identificationPending: Boolean) = runTest {
                val pin = "123456"
                val newPin = "000000"

                val newState = CanStateMachine.State.ChangePin.FrameworkReadyForNewPinInput(identificationPending, pin, newPin)

                testTransition(CanStateMachine.Event.Invalidate, newState, this)

                verify { mockEidInteractionManager.provideNewPin(newPin) }
            }
        }


        @Nested
        inner class Ident {
            @ParameterizedTest
            @SealedClassesSource(names = [] , mode = SealedClassesSource.Mode.EXCLUDE, factoryClass = CanIdentStateFactory::class)
            fun back(state: CanStateMachine.State.Ident) = runTest {
                every { mockEidInteractionManager.eidFlow } returns flowOf()

                testTransition(CanStateMachine.Event.Back, state, this)

                verify { mockAppNavigator.pop() }
            }

            @Test
            fun intro() = runTest {
                every { mockEidInteractionManager.eidFlow } returns flowOf()

                val newState = CanStateMachine.State.Ident.Intro(null)

                testTransition(CanStateMachine.Event.Invalidate, newState, this)

                verify { mockAppNavigator.navigate(IdentificationCanPinForgottenDestination) }
            }

            @ParameterizedTest
            @ValueSource(booleans = [true, false])
            fun `CAN intro without PIN`(shortFlow: Boolean) = runTest {
                every { mockEidInteractionManager.eidFlow } returns flowOf()

                val newState = CanStateMachine.State.Ident.CanIntro( null, shortFlow)

                testTransition(CanStateMachine.Event.Invalidate, newState, this)

                verify { mockAppNavigator.navigate(any()) }
                Assertions.assertEquals(IdentificationCanIntroDestination(!shortFlow).route, navigationDestinationSlots.last().route)
            }

            @ParameterizedTest
            @ValueSource(booleans = [true, false])
            fun `CAN intro with PIN`(shortFlow: Boolean) = runTest {
                every { mockEidInteractionManager.eidFlow } returns flowOf()

                val newState = CanStateMachine.State.Ident.CanIntro( "123456", shortFlow)

                testTransition(CanStateMachine.Event.Invalidate, newState, this)

                verify { mockAppNavigator.navigate(any()) }
                Assertions.assertEquals(IdentificationCanIntroDestination(!shortFlow).route, navigationDestinationSlots.last().route)
            }

            @Test
            fun `CAN input`() = runTest {
                every { mockEidInteractionManager.eidFlow } returns flowOf()

                val newState = CanStateMachine.State.Ident.CanInput(null, true)

                testTransition(CanStateMachine.Event.Invalidate, newState, this)

                verify { mockAppNavigator.navigate(any()) }
                Assertions.assertEquals(CanInputDestination(false).route, navigationDestinationSlots.last().route)
            }

            @Test
            fun `PIN Reset`() = runTest {
                every { mockEidInteractionManager.eidFlow } returns flowOf()

                val newState = CanStateMachine.State.Ident.PinReset(null)

                testTransition(CanStateMachine.Event.Invalidate, newState, this)

                verify { mockAppNavigator.navigate(CanResetPersonalPinDestination) }
            }

            @ParameterizedTest
            @ValueSource(booleans = [true, false])
            fun `CAN input retrying`(shortFlow: Boolean) = runTest {
                every { mockEidInteractionManager.eidFlow } returns flowOf()

                val newState = CanStateMachine.State.Ident.CanInputRetry("123456", shortFlow)

                testTransition(CanStateMachine.Event.Invalidate, newState, this)

                verify { mockAppNavigator.navigate(any()) }
                Assertions.assertEquals(IdentificationCanIntroDestination(false).route, navigationDestinationSlots.first().route)
                Assertions.assertEquals(CanInputDestination(true).route, navigationDestinationSlots.last().route)
            }

            @Test
            fun `PIN input`() = runTest {
                every { mockEidInteractionManager.eidFlow } returns flowOf()

                val newState = CanStateMachine.State.Ident.PinInput( "654321", false)

                testTransition(CanStateMachine.Event.Invalidate, newState, this)

                verify { mockAppNavigator.navigate(IdentificationCanPinInputDestination) }
            }

            @Test
            fun `CAN and PIN entered`() = runTest {
                every { mockEidInteractionManager.eidFlow } returns flowOf()

                val pin = "123456"
                val can = "654321"

                val newState = CanStateMachine.State.Ident.CanAndPinEntered(can, pin, false)

                testTransition(CanStateMachine.Event.Invalidate, newState, this)

                verify { mockAppNavigator.popUpToOrNavigate(IdentificationScanDestination, true) }
                verify { mockEidInteractionManager.provideCan(can) }
            }

            @Test
            fun `framework ready for PIN input`() = runTest {
                every { mockEidInteractionManager.eidFlow } returns flowOf()

                val pin = "123456"

                val newState = CanStateMachine.State.Ident.FrameworkReadyForPinInput(pin)

                testTransition(CanStateMachine.Event.Invalidate, newState, this)

                verify { mockEidInteractionManager.providePin(pin) }
            }
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun `start flow in PIN management`(shortFlow: Boolean) = runTest {
        every { mockCanStateMachine.state } returns MutableStateFlow(Pair(CanStateMachine.Event.Invalidate, CanStateMachine.State.Invalid))
        every { mockEidInteractionManager.eidFlow } returns flowOf()

        val canCoordinator = CanCoordinator(mockAppNavigator, mockEidInteractionManager, mockCanStateMachine, mockCoroutineContextProvider)
        advanceUntilIdle()

        Assertions.assertEquals(SubCoordinatorState.FINISHED, canCoordinator.stateFlow.value)

        val oldPin = "123456"
        val newPin = "000000"
        val returnedStateFlow = canCoordinator.startPinChangeCanFlow(false, oldPin, newPin, shortFlow)

        Assertions.assertEquals(returnedStateFlow, canCoordinator.stateFlow)
        Assertions.assertEquals(SubCoordinatorState.ACTIVE, canCoordinator.stateFlow.value)

        advanceUntilIdle()

        verify { mockEidInteractionManager.eidFlow }
    }

    @Nested
    inner class ChangePinEidEvents {
        @ParameterizedTest
        @ValueSource(booleans = [true, false])
        fun `handling requesting CAN and PIN with pending identification`(shortFlow: Boolean) = runTest {
            every { mockCanStateMachine.state } returns MutableStateFlow(Pair(CanStateMachine.Event.Invalidate, CanStateMachine.State.Invalid))

            val eIdFlow: MutableStateFlow<EidInteractionEvent> = MutableStateFlow(EidInteractionEvent.Idle)
            every { mockEidInteractionManager.eidFlow } returns eIdFlow

            val canCoordinator = CanCoordinator(mockAppNavigator, mockEidInteractionManager, mockCanStateMachine, mockCoroutineContextProvider)
            advanceUntilIdle()

            val oldPin = "123456"
            val newPin = "000000"
            canCoordinator.startPinChangeCanFlow(true, oldPin, newPin, shortFlow)
            advanceUntilIdle()

            eIdFlow.value = EidInteractionEvent.CanRequested()
            advanceUntilIdle()

            verify { mockCanStateMachine.transition(CanStateMachine.Event.FrameworkRequestsCanForPinChange(true, oldPin, newPin, shortFlow)) }

            eIdFlow.value = EidInteractionEvent.PinRequested(1)
            advanceUntilIdle()

            verify { mockCanStateMachine.transition(CanStateMachine.Event.FrameworkRequestsPinForPinChange(true, oldPin, newPin, shortFlow)) }
        }

        @ParameterizedTest
        @ValueSource(booleans = [true, false])
        fun `handling requesting CAN and PIN without pending identification`(shortFlow: Boolean) = runTest {
            every { mockCanStateMachine.state } returns MutableStateFlow(Pair(CanStateMachine.Event.Invalidate, CanStateMachine.State.Invalid))

            val eIdFlow: MutableStateFlow<EidInteractionEvent> = MutableStateFlow(EidInteractionEvent.Idle)
            every { mockEidInteractionManager.eidFlow } returns eIdFlow

            val canCoordinator = CanCoordinator(mockAppNavigator, mockEidInteractionManager, mockCanStateMachine, mockCoroutineContextProvider)
            advanceUntilIdle()

            val oldPin = "123456"
            val newPin = "000000"
            canCoordinator.startPinChangeCanFlow(false, oldPin, newPin, shortFlow)
            advanceUntilIdle()

            eIdFlow.value = EidInteractionEvent.CanRequested()
            advanceUntilIdle()

            verify { mockCanStateMachine.transition(CanStateMachine.Event.FrameworkRequestsCanForPinChange(false, oldPin, newPin, shortFlow)) }

            eIdFlow.value = EidInteractionEvent.PinRequested(1)
            advanceUntilIdle()

            verify { mockCanStateMachine.transition(CanStateMachine.Event.FrameworkRequestsPinForPinChange(false, oldPin, newPin, shortFlow)) }
        }

        @ParameterizedTest
        @SealedClassesSource(names = ["PinChangeSucceeded", "Error", "PukRequested"], factoryClass = EidInteractionEventTypeFactory::class)
        fun `handling finishing events`(event: EidInteractionEvent) = runTest {
            every { mockCanStateMachine.state } returns MutableStateFlow(Pair(CanStateMachine.Event.Invalidate, CanStateMachine.State.Invalid))

            val eIdFlow: MutableStateFlow<EidInteractionEvent> = MutableStateFlow(EidInteractionEvent.Idle)
            every { mockEidInteractionManager.eidFlow } returns eIdFlow

            val canCoordinator = CanCoordinator(mockAppNavigator, mockEidInteractionManager, mockCanStateMachine, mockCoroutineContextProvider)
            advanceUntilIdle()

            canCoordinator.startPinChangeCanFlow(false, "123456", "000000", false)
            advanceUntilIdle()

            Assertions.assertEquals(SubCoordinatorState.ACTIVE, canCoordinator.stateFlow.value)

            eIdFlow.value = event
            advanceUntilIdle()

            verify { mockCanStateMachine.transition(CanStateMachine.Event.Invalidate) }
            Assertions.assertEquals(SubCoordinatorState.FINISHED, canCoordinator.stateFlow.value)
        }
    }

    @Test
    fun `start flow in ident`() = runTest {
        every { mockCanStateMachine.state } returns MutableStateFlow(Pair(CanStateMachine.Event.Invalidate, CanStateMachine.State.Invalid))
        every { mockEidInteractionManager.eidFlow } returns flowOf()

        val canCoordinator = CanCoordinator(mockAppNavigator, mockEidInteractionManager, mockCanStateMachine, mockCoroutineContextProvider)
        advanceUntilIdle()

        Assertions.assertEquals(SubCoordinatorState.FINISHED, canCoordinator.stateFlow.value)

        val returnedStateFlow = canCoordinator.startIdentCanFlow(null)

        Assertions.assertEquals(returnedStateFlow, canCoordinator.stateFlow)
        Assertions.assertEquals(SubCoordinatorState.ACTIVE, canCoordinator.stateFlow.value)

        advanceUntilIdle()

        verify { mockEidInteractionManager.eidFlow }
    }

    @Nested
    inner class IdentEidEvents {
        @Test
        fun `handling requesting CAN in long flow`() = runTest {
            every { mockCanStateMachine.state } returns MutableStateFlow(Pair(CanStateMachine.Event.Invalidate, CanStateMachine.State.Invalid))

            val eIdFlow: MutableStateFlow<EidInteractionEvent> = MutableStateFlow(EidInteractionEvent.Idle)
            every { mockEidInteractionManager.eidFlow } returns eIdFlow

            val canCoordinator = CanCoordinator(mockAppNavigator, mockEidInteractionManager, mockCanStateMachine, mockCoroutineContextProvider)
            advanceUntilIdle()

            canCoordinator.startIdentCanFlow(null)
            advanceUntilIdle()

            eIdFlow.value = EidInteractionEvent.CanRequested()
            advanceUntilIdle()

            verify { mockCanStateMachine.transition(CanStateMachine.Event.FrameworkRequestsCanForIdent(null)) }
        }

        @Test
        fun `handling requesting PIN in long flow`() = runTest {
            every { mockCanStateMachine.state } returns MutableStateFlow(Pair(CanStateMachine.Event.Invalidate, CanStateMachine.State.Invalid))

            val eIdFlow: MutableStateFlow<EidInteractionEvent> = MutableStateFlow(EidInteractionEvent.Idle)
            every { mockEidInteractionManager.eidFlow } returns eIdFlow

            val canCoordinator = CanCoordinator(mockAppNavigator, mockEidInteractionManager, mockCanStateMachine, mockCoroutineContextProvider)
            advanceUntilIdle()

            canCoordinator.startIdentCanFlow(null)
            advanceUntilIdle()

            eIdFlow.value = EidInteractionEvent.PinRequested(3)
            advanceUntilIdle()

            verify { mockCanStateMachine.transition(CanStateMachine.Event.FrameworkRequestsPinForIdent(null)) }
        }

        @Test
        fun `handling requesting CAN in short flow`() = runTest {
            every { mockCanStateMachine.state } returns MutableStateFlow(Pair(CanStateMachine.Event.Invalidate, CanStateMachine.State.Invalid))

            val eIdFlow: MutableStateFlow<EidInteractionEvent> = MutableStateFlow(EidInteractionEvent.Idle)
            every { mockEidInteractionManager.eidFlow } returns eIdFlow

            val canCoordinator = CanCoordinator(mockAppNavigator, mockEidInteractionManager, mockCanStateMachine, mockCoroutineContextProvider)
            advanceUntilIdle()

            val pin = "123456"
            canCoordinator.startIdentCanFlow(pin)
            advanceUntilIdle()

            eIdFlow.value = EidInteractionEvent.CanRequested()
            advanceUntilIdle()

            verify { mockCanStateMachine.transition(CanStateMachine.Event.FrameworkRequestsCanForIdent(pin)) }
        }

        @Test
        fun `handling requesting PIN in short flow`() = runTest {
            every { mockCanStateMachine.state } returns MutableStateFlow(Pair(CanStateMachine.Event.Invalidate, CanStateMachine.State.Invalid))

            val eIdFlow: MutableStateFlow<EidInteractionEvent> = MutableStateFlow(EidInteractionEvent.Idle)
            every { mockEidInteractionManager.eidFlow } returns eIdFlow

            val canCoordinator = CanCoordinator(mockAppNavigator, mockEidInteractionManager, mockCanStateMachine, mockCoroutineContextProvider)
            advanceUntilIdle()

            val pin = "123456"
            canCoordinator.startIdentCanFlow(pin)
            advanceUntilIdle()

            eIdFlow.value = EidInteractionEvent.PinRequested(3)
            advanceUntilIdle()

            verify { mockCanStateMachine.transition(CanStateMachine.Event.FrameworkRequestsPinForIdent(pin)) }
        }

        @ParameterizedTest
        @SealedClassesSource(names = ["AuthenticationSucceededWithRedirect", "Error", "PukRequested"], factoryClass = EidInteractionEventTypeFactory::class)
        fun `handling finishing events`(event: EidInteractionEvent) = runTest {
            every { mockCanStateMachine.state } returns MutableStateFlow(Pair(CanStateMachine.Event.Invalidate, CanStateMachine.State.Invalid))

            val eIdFlow: MutableStateFlow<EidInteractionEvent> = MutableStateFlow(EidInteractionEvent.Idle)
            every { mockEidInteractionManager.eidFlow } returns eIdFlow

            val canCoordinator = CanCoordinator(mockAppNavigator, mockEidInteractionManager, mockCanStateMachine, mockCoroutineContextProvider)
            advanceUntilIdle()

            canCoordinator.startIdentCanFlow(null)
            advanceUntilIdle()

            Assertions.assertEquals(SubCoordinatorState.ACTIVE, canCoordinator.stateFlow.value)

            eIdFlow.value = event
            advanceUntilIdle()

            verify { mockCanStateMachine.transition(CanStateMachine.Event.Invalidate) }
            Assertions.assertEquals(SubCoordinatorState.FINISHED, canCoordinator.stateFlow.value)
        }
    }

    @Test
    fun onResetPin() {
        val canCoordinator = CanCoordinator(mockAppNavigator, mockEidInteractionManager, mockCanStateMachine, mockCoroutineContextProvider)
        canCoordinator.onResetPin()

        verify { mockCanStateMachine.transition(CanStateMachine.Event.ResetPin) }
    }

    @Test
    fun onConfirmedPinInput() {
        val canCoordinator = CanCoordinator(mockAppNavigator, mockEidInteractionManager, mockCanStateMachine, mockCoroutineContextProvider)
        canCoordinator.onConfirmedPinInput()

        verify { mockCanStateMachine.transition(CanStateMachine.Event.DenyThirdAttempt) }
    }

    @Test
    fun proceedWithThirdAttempt() {
        val canCoordinator = CanCoordinator(mockAppNavigator, mockEidInteractionManager, mockCanStateMachine, mockCoroutineContextProvider)
        canCoordinator.proceedWithThirdAttempt()

        verify { mockCanStateMachine.transition(CanStateMachine.Event.AgreeToThirdAttempt) }
    }

    @Test
    fun finishIntro() {
        val canCoordinator = CanCoordinator(mockAppNavigator, mockEidInteractionManager, mockCanStateMachine, mockCoroutineContextProvider)
        canCoordinator.finishIntro()

        verify { mockCanStateMachine.transition(CanStateMachine.Event.ConfirmCanIntro) }
    }

    @Test
    fun onCanEntered() {
        val canCoordinator = CanCoordinator(mockAppNavigator, mockEidInteractionManager, mockCanStateMachine, mockCoroutineContextProvider)
        val can = "654321"
        canCoordinator.onCanEntered(can)

        verify { mockCanStateMachine.transition(CanStateMachine.Event.EnterCan(can)) }
    }

    @Test
    fun onPinEntered() {
        val canCoordinator = CanCoordinator(mockAppNavigator, mockEidInteractionManager, mockCanStateMachine, mockCoroutineContextProvider)
        val pin = "123456"
        canCoordinator.onPinEntered(pin)

        verify { mockCanStateMachine.transition(CanStateMachine.Event.EnterPin(pin)) }
    }

    @Test
    fun onBack() {
        val canCoordinator = CanCoordinator(mockAppNavigator, mockEidInteractionManager, mockCanStateMachine, mockCoroutineContextProvider)
        canCoordinator.onBack()

        verify { mockCanStateMachine.transition(CanStateMachine.Event.Back) }
    }

    @Test
    fun cancelFlow() = runTest {
        every { mockCanStateMachine.state } returns MutableStateFlow(Pair(CanStateMachine.Event.Invalidate, CanStateMachine.State.Invalid))
        every { mockEidInteractionManager.eidFlow } returns flowOf()

        val canCoordinator = CanCoordinator(mockAppNavigator, mockEidInteractionManager, mockCanStateMachine, mockCoroutineContextProvider)
        advanceUntilIdle()

        canCoordinator.startPinChangeCanFlow(false, "123456", "000000", false)
        advanceUntilIdle()

        Assertions.assertEquals(SubCoordinatorState.ACTIVE, canCoordinator.stateFlow.value)

        canCoordinator.cancelCanFlow()

        verify { mockCanStateMachine.transition(CanStateMachine.Event.Invalidate) }
        Assertions.assertEquals(SubCoordinatorState.CANCELLED, canCoordinator.stateFlow.value)
    }

    @Test
    fun skipFlow() = runTest {
        every { mockCanStateMachine.state } returns MutableStateFlow(Pair(CanStateMachine.Event.Invalidate, CanStateMachine.State.Invalid))
        every { mockEidInteractionManager.eidFlow } returns flowOf()

        val canCoordinator = CanCoordinator(mockAppNavigator, mockEidInteractionManager, mockCanStateMachine, mockCoroutineContextProvider)
        advanceUntilIdle()

        canCoordinator.startPinChangeCanFlow(false, "123456", "000000", false)
        advanceUntilIdle()

        Assertions.assertEquals(SubCoordinatorState.ACTIVE, canCoordinator.stateFlow.value)

        canCoordinator.skipCanFlow()

        verify { mockCanStateMachine.transition(CanStateMachine.Event.Invalidate) }
        Assertions.assertEquals(SubCoordinatorState.SKIPPED, canCoordinator.stateFlow.value)
    }
}
