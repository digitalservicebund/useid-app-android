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
    lateinit var mockIdCardManager: IdCardManager

    @MockK(relaxUnitFun = true)
    lateinit var mockCanStateMachine: CanStateMachine

    @MockK(relaxUnitFun = true)
    lateinit var mockCoroutineContextProvider: CoroutineContextProvider

    @OptIn(ExperimentalCoroutinesApi::class)
    val dispatcher = StandardTestDispatcher()

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
        every { mockIdCardManager.eidFlow } returns flowOf()

        val canCoordinator = CanCoordinator(mockAppNavigator, mockIdCardManager, mockCanStateMachine, mockCoroutineContextProvider)

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

            val canCoordinator = CanCoordinator(mockAppNavigator, mockIdCardManager, mockCanStateMachine, mockCoroutineContextProvider)
            canCoordinator.startIdentCanFlow(null)

            stateFlow.value = Pair(event, state)
            testScope.advanceUntilIdle()
        }

        @Nested
        inner class PinManagement {
            @ParameterizedTest
            @SealedClassesSource(names = [] , mode = SealedClassesSource.Mode.EXCLUDE, factoryClass = CanPinManagementStateFactory::class)
            fun back(state: CanStateMachine.State.PinManagement) = runTest {
                testTransition(CanStateMachine.Event.Back, state, this)

                verify { mockAppNavigator.pop() }
            }

            @ParameterizedTest
            @ValueSource(booleans = [true, false])
            fun intro(identificationPending: Boolean) = runTest {
                val oldPin = "123456"
                val newPin = "000000"
                val newState = CanStateMachine.State.PinManagement.Intro(identificationPending, {_, _, _ -> }, oldPin, newPin)

                testTransition(CanStateMachine.Event.Invalidate, newState, this)

                verify { mockAppNavigator.navigate(any()) }

                Assertions.assertEquals(SetupCanConfirmTransportPinDestination(oldPin, identificationPending).route, navigationDestinationSlots.last().route)
            }

            @ParameterizedTest
            @ValueSource(booleans = [true, false])
            fun `ID already setup`(identificationPending: Boolean) = runTest {
                val oldPin = "123456"
                val newPin = "000000"
                val newState = CanStateMachine.State.PinManagement.IdAlreadySetup(identificationPending, {_, _, _ -> }, oldPin, newPin)

                testTransition(CanStateMachine.Event.Invalidate, newState, this)

                verify { mockAppNavigator.navigate(any()) }

                Assertions.assertEquals(SetupCanAlreadySetupDestination(identificationPending).route, navigationDestinationSlots.last().route)
            }

            @ParameterizedTest
            @ValueSource(booleans = [true, false])
            fun `PIN Reset`(identificationPending: Boolean) = runTest {
                val oldPin = "123456"
                val newPin = "000000"
                val newState = CanStateMachine.State.PinManagement.PinReset(identificationPending, {_, _, _ -> }, oldPin, newPin)

                testTransition(CanStateMachine.Event.Invalidate, newState, this)

                verify { mockAppNavigator.navigate(CanResetPersonalPinDestination) }
            }

            @ParameterizedTest
            @ValueSource(booleans = [true, false])
            fun `CAN intro with pending identification`(shortFlow: Boolean) = runTest {
                val oldPin = "123456"
                val newPin = "000000"
                val newState = CanStateMachine.State.PinManagement.CanIntro(true, {_, _, _ -> }, oldPin, newPin, shortFlow)

                testTransition(CanStateMachine.Event.Invalidate, newState, this)

                verify { mockAppNavigator.navigate(any()) }

                Assertions.assertEquals(SetupCanIntroDestination(!shortFlow, true).route, navigationDestinationSlots.last().route)
            }

            @ParameterizedTest
            @ValueSource(booleans = [true, false])
            fun `CAN intro without pending identification`(shortFlow: Boolean) = runTest {
                val oldPin = "123456"
                val newPin = "000000"
                val newState = CanStateMachine.State.PinManagement.CanIntro(false, {_, _, _ -> }, oldPin, newPin, shortFlow)

                testTransition(CanStateMachine.Event.Invalidate, newState, this)

                verify { mockAppNavigator.navigate(any()) }

                Assertions.assertEquals(SetupCanIntroDestination(!shortFlow, false).route, navigationDestinationSlots.last().route)
            }

            @ParameterizedTest
            @ValueSource(booleans = [true, false])
            fun `CAN input`(identificationPending: Boolean) = runTest {
                val oldPin = "123456"
                val newPin = "000000"
                val newState = CanStateMachine.State.PinManagement.CanInput(identificationPending, {_, _, _ -> }, oldPin, newPin, false)

                testTransition(CanStateMachine.Event.Invalidate, newState, this)

                verify { mockAppNavigator.navigate(any()) }

                Assertions.assertEquals(CanInputDestination(false).route, navigationDestinationSlots.last().route)
            }

            @ParameterizedTest
            @ValueSource(booleans = [true, false])
            fun `CAN input retrying`(identificationPending: Boolean) = runTest {
                val oldPin = "123456"
                val newPin = "000000"
                val newState = CanStateMachine.State.PinManagement.CanInputRetry(identificationPending, {_, _, _ -> }, oldPin, newPin)

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
                val newState = CanStateMachine.State.PinManagement.PinInput(identificationPending, {_, _, _ -> }, oldPin,"654321", newPin)

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
                val callback: PinManagementCanCallback = mockk()

                val newState = CanStateMachine.State.PinManagement.CanAndPinEntered(identificationPending, callback, oldPin, can, newPin)

                testTransition(CanStateMachine.Event.Invalidate, newState, this)

                verify { callback(oldPin, can, newPin) }
            }
        }


        @Nested
        inner class Ident {
            @ParameterizedTest
            @SealedClassesSource(names = [] , mode = SealedClassesSource.Mode.EXCLUDE, factoryClass = CanIdentStateFactory::class)
            fun back(state: CanStateMachine.State.Ident) = runTest {
                testTransition(CanStateMachine.Event.Back, state, this)

                verify { mockAppNavigator.pop() }
            }

            @Test
            fun intro() = runTest {
                val newState = CanStateMachine.State.Ident.Intro({_, _ -> }, null)

                testTransition(CanStateMachine.Event.Invalidate, newState, this)

                verify { mockAppNavigator.navigate(IdentificationCanPinForgottenDestination) }
            }

            @Test
            fun `CAN intro without PIN`() = runTest {
                val newState = CanStateMachine.State.Ident.CanIntro({_, _ -> }, null)

                testTransition(CanStateMachine.Event.Invalidate, newState, this)

                verify { mockAppNavigator.navigate(any()) }
                Assertions.assertEquals(IdentificationCanIntroDestination(true).route, navigationDestinationSlots.last().route)
            }

            @Test
            fun `CAN intro with PIN`() = runTest {
                val newState = CanStateMachine.State.Ident.CanIntro({_, _ -> }, "123456")

                testTransition(CanStateMachine.Event.Invalidate, newState, this)

                verify { mockAppNavigator.navigate(any()) }
                Assertions.assertEquals(IdentificationCanIntroDestination(false).route, navigationDestinationSlots.last().route)
            }

            @Test
            fun `CAN intro short flow`() = runTest {
                val newState = CanStateMachine.State.Ident.CanIntroWithoutFlowIntro({_, _ -> }, "123456")

                testTransition(CanStateMachine.Event.Invalidate, newState, this)

                verify { mockAppNavigator.navigate(any()) }
                Assertions.assertEquals(IdentificationCanIntroDestination(false).route, navigationDestinationSlots.last().route)
            }

            @Test
            fun `CAN input`() = runTest {
                val newState = CanStateMachine.State.Ident.CanInput({_, _ -> }, null)

                testTransition(CanStateMachine.Event.Invalidate, newState, this)

                verify { mockAppNavigator.navigate(any()) }
                Assertions.assertEquals(CanInputDestination(false).route, navigationDestinationSlots.last().route)
            }

            @Test
            fun `PIN Reset`() = runTest {
                val newState = CanStateMachine.State.Ident.PinReset({_, _ -> }, null)

                testTransition(CanStateMachine.Event.Invalidate, newState, this)

                verify { mockAppNavigator.navigate(CanResetPersonalPinDestination) }
            }

            @Test
            fun `CAN input retrying`() = runTest {
                val newState = CanStateMachine.State.Ident.CanInputRetry({_, _ -> }, "123456")

                testTransition(CanStateMachine.Event.Invalidate, newState, this)

                verify { mockAppNavigator.navigate(any()) }
                Assertions.assertEquals(IdentificationCanPinForgottenDestination.route, navigationDestinationSlots[0].route)
                Assertions.assertEquals(IdentificationCanIntroDestination(true).route, navigationDestinationSlots[1].route)
                Assertions.assertEquals(CanInputDestination(true).route, navigationDestinationSlots[2].route)
            }

            @Test
            fun `PIN input`() = runTest {
                val newState = CanStateMachine.State.Ident.PinInput({_, _ -> }, "654321")

                testTransition(CanStateMachine.Event.Invalidate, newState, this)

                verify { mockAppNavigator.navigate(IdentificationCanPinInputDestination) }
            }

            @Test
            fun `CAN and PIN entered`() = runTest {
                val pin = "123456"
                val can = "654321"
                val callback: PinCanCallback = mockk()

                val newState = CanStateMachine.State.Ident.CanAndPinEntered(callback, can, pin)

                testTransition(CanStateMachine.Event.Invalidate, newState, this)

                verify { callback(pin, can) }
            }
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun `start flow in PIN management`(shortFlow: Boolean) = runTest {
        every { mockCanStateMachine.state } returns MutableStateFlow(Pair(CanStateMachine.Event.Invalidate, CanStateMachine.State.Invalid))
        every { mockIdCardManager.eidFlow } returns flowOf()

        val canCoordinator = CanCoordinator(mockAppNavigator, mockIdCardManager, mockCanStateMachine, mockCoroutineContextProvider)
        advanceUntilIdle()

        Assertions.assertEquals(SubCoordinatorState.FINISHED, canCoordinator.stateFlow.value)

        val oldPin = "123456"
        val newPin = "000000"
        val returnedStateFlow = canCoordinator.startPinManagementCanFlow(false, oldPin, newPin, shortFlow)

        Assertions.assertEquals(returnedStateFlow, canCoordinator.stateFlow)
        Assertions.assertEquals(SubCoordinatorState.ACTIVE, canCoordinator.stateFlow.value)

        advanceUntilIdle()

        verify { mockIdCardManager.eidFlow }
    }

    @Nested
    inner class PinManagementEidEvents {
        @ParameterizedTest
        @ValueSource(booleans = [true, false])
        fun `handling requesting CAN and PIN with pending identification`(shortFlow: Boolean) = runTest {
            every { mockCanStateMachine.state } returns MutableStateFlow(Pair(CanStateMachine.Event.Invalidate, CanStateMachine.State.Invalid))

            val eIdFlow: MutableStateFlow<EidInteractionEvent> = MutableStateFlow(EidInteractionEvent.Idle)
            every { mockIdCardManager.eidFlow } returns eIdFlow

            val canCoordinator = CanCoordinator(mockAppNavigator, mockIdCardManager, mockCanStateMachine, mockCoroutineContextProvider)
            advanceUntilIdle()

            val oldPin = "123456"
            val newPin = "000000"
            canCoordinator.startPinManagementCanFlow(true, oldPin, newPin, shortFlow)
            advanceUntilIdle()

            val pinManagementCallback: PinManagementCanCallback = mockk()
            eIdFlow.value = EidInteractionEvent.RequestCanAndChangedPin(pinManagementCallback)
            advanceUntilIdle()

            verify { mockCanStateMachine.transition(CanStateMachine.Event.FrameworkRequestsCanForPinManagement(true, oldPin, newPin, shortFlow, pinManagementCallback)) }

            eIdFlow.value = EidInteractionEvent.RequestCanAndChangedPin(pinManagementCallback)
            advanceUntilIdle()

            verify(exactly = 2) { mockCanStateMachine.transition(CanStateMachine.Event.FrameworkRequestsCanForPinManagement(true, oldPin, newPin, shortFlow, pinManagementCallback)) }
        }

        @ParameterizedTest
        @ValueSource(booleans = [true, false])
        fun `handling requesting CAN and PIN without pending identification`(shortFlow: Boolean) = runTest {
            every { mockCanStateMachine.state } returns MutableStateFlow(Pair(CanStateMachine.Event.Invalidate, CanStateMachine.State.Invalid))

            val eIdFlow: MutableStateFlow<EidInteractionEvent> = MutableStateFlow(EidInteractionEvent.Idle)
            every { mockIdCardManager.eidFlow } returns eIdFlow

            val canCoordinator = CanCoordinator(mockAppNavigator, mockIdCardManager, mockCanStateMachine, mockCoroutineContextProvider)
            advanceUntilIdle()

            val oldPin = "123456"
            val newPin = "000000"
            canCoordinator.startPinManagementCanFlow(false, oldPin, newPin, shortFlow)
            advanceUntilIdle()

            val pinManagementCallback: PinManagementCanCallback = mockk()
            eIdFlow.value = EidInteractionEvent.RequestCanAndChangedPin(pinManagementCallback)
            advanceUntilIdle()

            verify { mockCanStateMachine.transition(CanStateMachine.Event.FrameworkRequestsCanForPinManagement(false, oldPin, newPin, shortFlow, pinManagementCallback)) }

            eIdFlow.value = EidInteractionEvent.RequestCanAndChangedPin(pinManagementCallback)
            advanceUntilIdle()

            verify(exactly = 2) { mockCanStateMachine.transition(CanStateMachine.Event.FrameworkRequestsCanForPinManagement(false, oldPin, newPin, shortFlow, pinManagementCallback)) }
        }

        @ParameterizedTest
        @SealedClassesSource(names = ["AuthenticationSuccessful", "ProcessCompletedSuccessfullyWithoutResult", "ProcessCompletedSuccessfullyWithRedirect", "Error"], factoryClass = EidInteractionEventTypeFactory::class)
        fun `handling finishing events`(event: EidInteractionEvent) = runTest {
            every { mockCanStateMachine.state } returns MutableStateFlow(Pair(CanStateMachine.Event.Invalidate, CanStateMachine.State.Invalid))

            val eIdFlow: MutableStateFlow<EidInteractionEvent> = MutableStateFlow(EidInteractionEvent.Idle)
            every { mockIdCardManager.eidFlow } returns eIdFlow

            val canCoordinator = CanCoordinator(mockAppNavigator, mockIdCardManager, mockCanStateMachine, mockCoroutineContextProvider)
            advanceUntilIdle()

            canCoordinator.startPinManagementCanFlow(false, "123456", "000000", false)
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
        every { mockIdCardManager.eidFlow } returns flowOf()

        val canCoordinator = CanCoordinator(mockAppNavigator, mockIdCardManager, mockCanStateMachine, mockCoroutineContextProvider)
        advanceUntilIdle()

        Assertions.assertEquals(SubCoordinatorState.FINISHED, canCoordinator.stateFlow.value)

        val returnedStateFlow = canCoordinator.startIdentCanFlow(null)

        Assertions.assertEquals(returnedStateFlow, canCoordinator.stateFlow)
        Assertions.assertEquals(SubCoordinatorState.ACTIVE, canCoordinator.stateFlow.value)

        advanceUntilIdle()

        verify { mockIdCardManager.eidFlow }
    }

    @Nested
    inner class IdentEidEvents {
        @Test
        fun `handling requesting CAN in long flow`() = runTest {
            every { mockCanStateMachine.state } returns MutableStateFlow(Pair(CanStateMachine.Event.Invalidate, CanStateMachine.State.Invalid))

            val eIdFlow: MutableStateFlow<EidInteractionEvent> = MutableStateFlow(EidInteractionEvent.Idle)
            every { mockIdCardManager.eidFlow } returns eIdFlow

            val canCoordinator = CanCoordinator(mockAppNavigator, mockIdCardManager, mockCanStateMachine, mockCoroutineContextProvider)
            advanceUntilIdle()

            canCoordinator.startIdentCanFlow(null)
            advanceUntilIdle()

            val pinCallback: PinCanCallback = mockk()
            eIdFlow.value = EidInteractionEvent.RequestPinAndCan(pinCallback)
            advanceUntilIdle()

            verify { mockCanStateMachine.transition(CanStateMachine.Event.FrameworkRequestsCanForIdent(null, pinCallback)) }

            eIdFlow.value = EidInteractionEvent.RequestPinAndCan(pinCallback)
            advanceUntilIdle()

            verify(exactly = 2) { mockCanStateMachine.transition(CanStateMachine.Event.FrameworkRequestsCanForIdent(null, pinCallback)) }
        }

        @Test
        fun `handling requesting CAN in short flow`() = runTest {
            every { mockCanStateMachine.state } returns MutableStateFlow(Pair(CanStateMachine.Event.Invalidate, CanStateMachine.State.Invalid))

            val eIdFlow: MutableStateFlow<EidInteractionEvent> = MutableStateFlow(EidInteractionEvent.Idle)
            every { mockIdCardManager.eidFlow } returns eIdFlow

            val canCoordinator = CanCoordinator(mockAppNavigator, mockIdCardManager, mockCanStateMachine, mockCoroutineContextProvider)
            advanceUntilIdle()

            val pin = "123456"
            canCoordinator.startIdentCanFlow(pin)
            advanceUntilIdle()

            val pinCallback: PinCanCallback = mockk()
            eIdFlow.value = EidInteractionEvent.RequestPinAndCan(pinCallback)
            advanceUntilIdle()

            verify { mockCanStateMachine.transition(CanStateMachine.Event.FrameworkRequestsCanForIdent(pin, pinCallback)) }

            eIdFlow.value = EidInteractionEvent.RequestPinAndCan(pinCallback)
            advanceUntilIdle()

            verify(exactly = 2) { mockCanStateMachine.transition(CanStateMachine.Event.FrameworkRequestsCanForIdent(pin, pinCallback)) }
        }

        @ParameterizedTest
        @SealedClassesSource(names = ["AuthenticationSuccessful", "ProcessCompletedSuccessfullyWithoutResult", "ProcessCompletedSuccessfullyWithRedirect", "Error"], factoryClass = EidInteractionEventTypeFactory::class)
        fun `handling finishing events`(event: EidInteractionEvent) = runTest {
            every { mockCanStateMachine.state } returns MutableStateFlow(Pair(CanStateMachine.Event.Invalidate, CanStateMachine.State.Invalid))

            val eIdFlow: MutableStateFlow<EidInteractionEvent> = MutableStateFlow(EidInteractionEvent.Idle)
            every { mockIdCardManager.eidFlow } returns eIdFlow

            val canCoordinator = CanCoordinator(mockAppNavigator, mockIdCardManager, mockCanStateMachine, mockCoroutineContextProvider)
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
        val canCoordinator = CanCoordinator(mockAppNavigator, mockIdCardManager, mockCanStateMachine, mockCoroutineContextProvider)
        canCoordinator.onResetPin()

        verify { mockCanStateMachine.transition(CanStateMachine.Event.ResetPin) }
    }

    @Test
    fun onConfirmedPinInput() {
        val canCoordinator = CanCoordinator(mockAppNavigator, mockIdCardManager, mockCanStateMachine, mockCoroutineContextProvider)
        canCoordinator.onConfirmedPinInput()

        verify { mockCanStateMachine.transition(CanStateMachine.Event.DenyThirdAttempt) }
    }

    @Test
    fun proceedWithThirdAttempt() {
        val canCoordinator = CanCoordinator(mockAppNavigator, mockIdCardManager, mockCanStateMachine, mockCoroutineContextProvider)
        canCoordinator.proceedWithThirdAttempt()

        verify { mockCanStateMachine.transition(CanStateMachine.Event.AgreeToThirdAttempt) }
    }

    @Test
    fun finishIntro() {
        val canCoordinator = CanCoordinator(mockAppNavigator, mockIdCardManager, mockCanStateMachine, mockCoroutineContextProvider)
        canCoordinator.finishIntro()

        verify { mockCanStateMachine.transition(CanStateMachine.Event.ConfirmCanIntro) }
    }

    @Test
    fun onCanEntered() {
        val canCoordinator = CanCoordinator(mockAppNavigator, mockIdCardManager, mockCanStateMachine, mockCoroutineContextProvider)
        val can = "654321"
        canCoordinator.onCanEntered(can)

        verify { mockCanStateMachine.transition(CanStateMachine.Event.EnterCan(can)) }
    }

    @Test
    fun onPinEntered() {
        val canCoordinator = CanCoordinator(mockAppNavigator, mockIdCardManager, mockCanStateMachine, mockCoroutineContextProvider)
        val pin = "123456"
        canCoordinator.onPinEntered(pin)

        verify { mockCanStateMachine.transition(CanStateMachine.Event.EnterPin(pin)) }
    }

    @Test
    fun onBack() {
        val canCoordinator = CanCoordinator(mockAppNavigator, mockIdCardManager, mockCanStateMachine, mockCoroutineContextProvider)
        canCoordinator.onBack()

        verify { mockCanStateMachine.transition(CanStateMachine.Event.Back) }
    }

    @Test
    fun cancelFlow() = runTest {
        val canCoordinator = CanCoordinator(mockAppNavigator, mockIdCardManager, mockCanStateMachine, mockCoroutineContextProvider)
        advanceUntilIdle()

        canCoordinator.startPinManagementCanFlow(false, "123456", "000000", false)
        advanceUntilIdle()

        Assertions.assertEquals(SubCoordinatorState.ACTIVE, canCoordinator.stateFlow.value)

        canCoordinator.cancelCanFlow()

        verify { mockCanStateMachine.transition(CanStateMachine.Event.Invalidate) }
        Assertions.assertEquals(SubCoordinatorState.CANCELLED, canCoordinator.stateFlow.value)
    }

    @Test
    fun skipFlow() = runTest {
        val canCoordinator = CanCoordinator(mockAppNavigator, mockIdCardManager, mockCanStateMachine, mockCoroutineContextProvider)
        advanceUntilIdle()

        canCoordinator.startPinManagementCanFlow(false, "123456", "000000", false)
        advanceUntilIdle()

        Assertions.assertEquals(SubCoordinatorState.ACTIVE, canCoordinator.stateFlow.value)

        canCoordinator.skipCanFlow()

        verify { mockCanStateMachine.transition(CanStateMachine.Event.Invalidate) }
        Assertions.assertEquals(SubCoordinatorState.SKIPPED, canCoordinator.stateFlow.value)
    }
}
