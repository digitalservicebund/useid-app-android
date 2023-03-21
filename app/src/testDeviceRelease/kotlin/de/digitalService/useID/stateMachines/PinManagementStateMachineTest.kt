package de.digitalService.useID.stateMachines

import de.digitalService.useID.analytics.IssueTrackerManagerType
import de.digitalService.useID.flows.PinManagementCallback
import de.digitalService.useID.flows.PinManagementStateMachine
import de.digitalService.useID.idCardInterface.IdCardInteractionException
import de.digitalService.useID.util.PinManagementStateFactory
import de.jodamob.junit5.DefaultTypeFactory
import de.jodamob.junit5.SealedClassesSource
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Nested
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.openecard.mobile.activation.ActivationResultCode
import kotlin.reflect.KClass

@OptIn(ExperimentalCoroutinesApi::class)
class PinManagementStateMachineTest {
    val pinManagementCallback: PinManagementCallback = mockk()

    private val issueTrackerManager = mockk<IssueTrackerManagerType>(relaxUnitFun = true)

    private inline fun <reified NewState: PinManagementStateMachine.State> transition(initialState: PinManagementStateMachine.State, event: PinManagementStateMachine.Event, testScope: TestScope): NewState {
        val stateMachine = PinManagementStateMachine(initialState, issueTrackerManager)
        Assertions.assertEquals(stateMachine.state.value.second, initialState)

        stateMachine.transition(event)
        testScope.advanceUntilIdle()

        return stateMachine.state.value.second as? NewState ?: Assertions.fail("Unexpected new state.")
    }

    @ParameterizedTest
    @ValueSource(booleans = [false, true])
    fun `start PIN management with transport PIN from invalid state`(identificationPending: Boolean) = runTest {
        val transportPin = true

        val event = PinManagementStateMachine.Event.StartPinManagement(identificationPending, transportPin)

        val newState: PinManagementStateMachine.State.OldTransportPinInput = transition(PinManagementStateMachine.State.Invalid, event, this)

        Assertions.assertEquals(newState.identificationPending, identificationPending)
    }

    @ParameterizedTest
    @ValueSource(booleans = [false, true])
    fun `enter old transport PIN from input`(identificationPending: Boolean) = runTest {
        val oldPin = "12345"

        val event = PinManagementStateMachine.Event.EnterOldPin(oldPin)
        val oldState = PinManagementStateMachine.State.OldTransportPinInput(identificationPending)
        val newState: PinManagementStateMachine.State.NewPinIntro = transition(oldState, event, this)

        Assertions.assertEquals(newState.identificationPending, identificationPending)
        Assertions.assertEquals(newState.oldPin, oldPin)
        Assertions.assertTrue(newState.transportPin)
    }

    @ParameterizedTest
    @ValueSource(booleans = [false, true])
    fun `re-enter old transport PIN from retry input`(identificationPending: Boolean) = runTest {
        val oldPin = "12345"
        val newPin = "000000"

        val event = PinManagementStateMachine.Event.EnterOldPin(oldPin)
        val oldState = PinManagementStateMachine.State.OldTransportPinRetry(identificationPending, newPin, pinManagementCallback)
        val newState: PinManagementStateMachine.State.FrameworkReadyForPinManagement = transition(oldState, event, this)

        Assertions.assertEquals(newState.identificationPending, identificationPending)
        Assertions.assertEquals(newState.oldPin, oldPin)
        Assertions.assertEquals(newState.newPin, newPin)
        Assertions.assertTrue(newState.transportPin)
    }

    @ParameterizedTest
    @ValueSource(booleans = [false, true])
    fun `confirm new PIN intro in first attempt`(identificationPending: Boolean) = runTest {
        val oldPin = "12345"

        val event = PinManagementStateMachine.Event.ConfirmNewPinIntro
        val oldState = PinManagementStateMachine.State.NewPinIntro(identificationPending, true, oldPin)
        val newState: PinManagementStateMachine.State.NewPinInput = transition(oldState, event, this)

        Assertions.assertEquals(newState.identificationPending, identificationPending)
        Assertions.assertEquals(newState.oldPin, oldPin)
        Assertions.assertTrue(newState.transportPin)
    }

    @ParameterizedTest
    @ValueSource(booleans = [false, true])
    fun `retry new PIN after mismatch`(identificationPending: Boolean) = runTest {
        val oldPin = "12345"
        val newPin = "000000"

        val event = PinManagementStateMachine.Event.RetryNewPinConfirmation
        val oldState = PinManagementStateMachine.State.NewPinConfirmation(identificationPending, true, oldPin, newPin)
        val newState: PinManagementStateMachine.State.NewPinInput = transition(oldState, event, this)

        Assertions.assertEquals(newState.identificationPending, identificationPending)
        Assertions.assertEquals(newState.oldPin, oldPin)
        Assertions.assertTrue(newState.transportPin)
    }

    @ParameterizedTest
    @ValueSource(booleans = [false, true])
    fun `enter new PIN`(identificationPending: Boolean) = runTest {
        val oldPin = "12345"
        val newPin = "000000"

        val event = PinManagementStateMachine.Event.EnterNewPin(newPin)
        val oldState = PinManagementStateMachine.State.NewPinInput(identificationPending, true, oldPin)
        val newState: PinManagementStateMachine.State.NewPinConfirmation = transition(oldState, event, this)

        Assertions.assertEquals(newState.identificationPending, identificationPending)
        Assertions.assertEquals(newState.oldPin, oldPin)
        Assertions.assertEquals(newState.newPin, newPin)
        Assertions.assertTrue(newState.transportPin)
    }

    @ParameterizedTest
    @ValueSource(booleans = [false, true])
    fun `confirm new PIN matching`(identificationPending: Boolean) = runTest {
        val oldPin = "12345"
        val newPin = "000000"

        val event = PinManagementStateMachine.Event.ConfirmNewPin(newPin)
        val oldState = PinManagementStateMachine.State.NewPinConfirmation(identificationPending, true, oldPin, newPin)
        val newState: PinManagementStateMachine.State.ReadyForScan = transition(oldState, event, this)

        Assertions.assertEquals(newState.identificationPending, identificationPending)
        Assertions.assertEquals(newState.oldPin, oldPin)
        Assertions.assertEquals(newState.newPin, newPin)
        Assertions.assertTrue(newState.transportPin)
    }

    @ParameterizedTest
    @ValueSource(booleans = [false, true])
    fun `confirm new PIN mismatch`(identificationPending: Boolean) = runTest {
        val oldPin = "12345"
        val newPin = "000000"

        val event = PinManagementStateMachine.Event.ConfirmNewPin(newPin)
        val oldState = PinManagementStateMachine.State.NewPinConfirmation(identificationPending, true, oldPin, "555555")
        Assertions.assertThrows(PinManagementStateMachine.Error.PinConfirmationFailed::class.java) { transition(oldState, event, this) as? PinManagementStateMachine.State.ReadyForScan}
    }

    @ParameterizedTest
    @ValueSource(booleans = [false, true])
    fun `request card insertion first time`(identificationPending: Boolean) = runTest {
        val oldPin = "12345"
        val newPin = "000000"

        val event = PinManagementStateMachine.Event.RequestCardInsertion
        val oldState = PinManagementStateMachine.State.ReadyForScan(identificationPending, true, oldPin, newPin)
        val newState: PinManagementStateMachine.State.WaitingForFirstCardAttachment = transition(oldState, event, this)

        Assertions.assertEquals(newState.identificationPending, identificationPending)
        Assertions.assertEquals(newState.oldPin, oldPin)
        Assertions.assertEquals(newState.newPin, newPin)
        Assertions.assertTrue(newState.transportPin)
    }

    @ParameterizedTest
    @ValueSource(booleans = [false, true])
    fun `request card insertion second time`(identificationPending: Boolean) = runTest {
        val oldPin = "12345"
        val newPin = "000000"

        val event = PinManagementStateMachine.Event.RequestCardInsertion
        val oldState = PinManagementStateMachine.State.FrameworkReadyForPinManagement(identificationPending, true, oldPin, newPin, pinManagementCallback)
        val newState: PinManagementStateMachine.State.WaitingForCardReAttachment = transition(oldState, event, this)

        Assertions.assertEquals(newState.identificationPending, identificationPending)
        Assertions.assertEquals(newState.oldPin, oldPin)
        Assertions.assertEquals(newState.newPin, newPin)
        Assertions.assertTrue(newState.transportPin)
    }

    @ParameterizedTest
    @ValueSource(booleans = [false, true])
    fun `request card insertion after requesting CAN`(identificationPending: Boolean) = runTest {
        val oldPin = "12345"
        val newPin = "000000"

        val event = PinManagementStateMachine.Event.RequestCardInsertion
        val oldState = PinManagementStateMachine.State.CanRequested(identificationPending, true, oldPin, newPin, false)
        val newState: PinManagementStateMachine.State.WaitingForCardReAttachment = transition(oldState, event, this)

        Assertions.assertEquals(newState.identificationPending, identificationPending)
        Assertions.assertEquals(newState.oldPin, oldPin)
        Assertions.assertEquals(newState.newPin, newPin)
        Assertions.assertTrue(newState.transportPin)
    }

    @ParameterizedTest
    @ValueSource(booleans = [false, true])
    fun `framework requests new PIN for first time`(identificationPending: Boolean) = runTest {
        val oldPin = "12345"
        val newPin = "000000"

        val event = PinManagementStateMachine.Event.FrameworkRequestsChangedPin(pinManagementCallback)
        val oldState = PinManagementStateMachine.State.WaitingForFirstCardAttachment(identificationPending, true, oldPin, newPin)
        val newState: PinManagementStateMachine.State.FrameworkReadyForPinManagement = transition(oldState, event, this)

        Assertions.assertEquals(newState.identificationPending, identificationPending)
        Assertions.assertEquals(newState.oldPin, oldPin)
        Assertions.assertEquals(newState.newPin, newPin)
        Assertions.assertEquals(newState.callback, pinManagementCallback)
        Assertions.assertTrue(newState.transportPin)
    }

    @ParameterizedTest
    @ValueSource(booleans = [false, true])
    fun `framework requests new PIN for second time`(identificationPending: Boolean) = runTest {
        val oldPin = "12345"
        val newPin = "000000"

        val newCallback: PinManagementCallback = mockk()
        val event = PinManagementStateMachine.Event.FrameworkRequestsChangedPin(newCallback)
        val oldState = PinManagementStateMachine.State.FrameworkReadyForPinManagement(identificationPending, true, oldPin, newPin, pinManagementCallback)
        val newState: PinManagementStateMachine.State.OldTransportPinRetry = transition(oldState, event, this)

        Assertions.assertEquals(newState.identificationPending, identificationPending)
        Assertions.assertEquals(newState.newPin, newPin)
        Assertions.assertEquals(newState.callback, newCallback)
    }

    @ParameterizedTest
    @ValueSource(booleans = [false, true])
    fun `framework requests CAN on first attempt`(identificationPending: Boolean) = runTest {
        val oldPin = "12345"
        val newPin = "000000"

        val event = PinManagementStateMachine.Event.FrameworkRequestsCan
        val oldState = PinManagementStateMachine.State.WaitingForFirstCardAttachment(identificationPending, true, oldPin, newPin)
        val newState: PinManagementStateMachine.State.CanRequested = transition(oldState, event, this)

        Assertions.assertEquals(newState.identificationPending, identificationPending)
        Assertions.assertEquals(newState.oldPin, oldPin)
        Assertions.assertEquals(newState.newPin, newPin)
        Assertions.assertTrue(newState.shortFlow)
    }

    @ParameterizedTest
    @ValueSource(booleans = [false, true])
    fun `framework requests CAN on second attempt`(identificationPending: Boolean) = runTest {
        val oldPin = "12345"
        val newPin = "000000"

        val event = PinManagementStateMachine.Event.FrameworkRequestsCan
        val oldState = PinManagementStateMachine.State.WaitingForCardReAttachment(identificationPending, true, oldPin, newPin)
        val newState: PinManagementStateMachine.State.CanRequested = transition(oldState, event, this)

        Assertions.assertEquals(newState.identificationPending, identificationPending)
        Assertions.assertEquals(newState.oldPin, oldPin)
        Assertions.assertEquals(newState.newPin, newPin)
        Assertions.assertFalse(newState.shortFlow)
    }

    @ParameterizedTest
    @ValueSource(booleans = [false, true])
    fun `framework requests CAN on second attempt with card already attached`(identificationPending: Boolean) = runTest {
        val oldPin = "12345"
        val newPin = "000000"

        val event = PinManagementStateMachine.Event.FrameworkRequestsCan
        val oldState = PinManagementStateMachine.State.FrameworkReadyForPinManagement(identificationPending, true, oldPin, newPin, pinManagementCallback)
        val newState: PinManagementStateMachine.State.CanRequested = transition(oldState, event, this)

        Assertions.assertEquals(newState.identificationPending, identificationPending)
        Assertions.assertEquals(newState.oldPin, oldPin)
        Assertions.assertEquals(newState.newPin, newPin)
        Assertions.assertFalse(newState.shortFlow)
    }

    @ParameterizedTest
    @ValueSource(booleans = [false, true])
    fun `finish after PIN callback`(identificationPending: Boolean) = runTest {
        val oldPin = "12345"
        val newPin = "000000"

        val event = PinManagementStateMachine.Event.Finish
        val oldState = PinManagementStateMachine.State.FrameworkReadyForPinManagement(identificationPending, true, oldPin, newPin, pinManagementCallback)
        val newState: PinManagementStateMachine.State.Finished = transition(oldState, event, this)
    }

    @ParameterizedTest
    @ValueSource(booleans = [false, true])
    fun `finish after first card attachment request`(identificationPending: Boolean) = runTest {
        val oldPin = "12345"
        val newPin = "000000"

        val event = PinManagementStateMachine.Event.Finish
        val oldState = PinManagementStateMachine.State.WaitingForFirstCardAttachment(identificationPending, true, oldPin, newPin)
        val newState: PinManagementStateMachine.State.Finished = transition(oldState, event, this)
    }

    @ParameterizedTest
    @ValueSource(booleans = [false, true])
    fun `finish after second card attachment request`(identificationPending: Boolean) = runTest {
        val oldPin = "12345"
        val newPin = "000000"

        val event = PinManagementStateMachine.Event.Finish
        val oldState = PinManagementStateMachine.State.WaitingForCardReAttachment(identificationPending, true, oldPin, newPin)
        val newState: PinManagementStateMachine.State.Finished = transition(oldState, event, this)
    }

    @ParameterizedTest
    @ValueSource(booleans = [false, true])
    fun `finish after CAN request`(identificationPending: Boolean) = runTest {
        val oldPin = "12345"
        val newPin = "000000"

        val event = PinManagementStateMachine.Event.Finish
        val oldState = PinManagementStateMachine.State.CanRequested(identificationPending, true, oldPin, newPin, false)
        val newState: PinManagementStateMachine.State.Finished = transition(oldState, event, this)
    }

    @ParameterizedTest
    @ValueSource(booleans = [false, true])
    fun `retry after error in first scan`(identificationPending: Boolean) = runTest {
        val oldPin = "12345"
        val newPin = "000000"

        val event = PinManagementStateMachine.Event.ProceedAfterError
        val oldState = PinManagementStateMachine.State.ProcessFailed(identificationPending, true, oldPin, newPin, true)
        val newState: PinManagementStateMachine.State.ReadyForScan = transition(oldState, event, this)

        Assertions.assertEquals(newState.identificationPending, identificationPending)
        Assertions.assertEquals(newState.oldPin, oldPin)
        Assertions.assertEquals(newState.newPin, newPin)
        Assertions.assertTrue(newState.transportPin)
    }

    @ParameterizedTest
    @ValueSource(booleans = [false, true])
    fun `retry after error in second scan`(identificationPending: Boolean) = runTest {
        val oldPin = "12345"
        val newPin = "000000"

        val event = PinManagementStateMachine.Event.ProceedAfterError
        val oldState = PinManagementStateMachine.State.ProcessFailed(identificationPending, true, oldPin, newPin, false)
        val newState: PinManagementStateMachine.State.Cancelled = transition(oldState, event, this)
    }

    @Nested
    inner class Error {
        @ParameterizedTest
        @ValueSource(booleans = [false, true])
        fun `card deactivated after framework requested changed PIN`(identificationPending: Boolean) = runTest {
            val oldPin = "12345"
            val newPin = "000000"

            val event = PinManagementStateMachine.Event.Error(IdCardInteractionException.CardDeactivated)
            val oldState = PinManagementStateMachine.State.FrameworkReadyForPinManagement(identificationPending, true, oldPin, newPin, pinManagementCallback)
            val newState: PinManagementStateMachine.State.CardDeactivated = transition(oldState, event, this)
        }

        @ParameterizedTest
        @ValueSource(booleans = [false, true])
        fun `card blocked after framework requested changed PIN`(identificationPending: Boolean) = runTest {
            val oldPin = "12345"
            val newPin = "000000"

            val event = PinManagementStateMachine.Event.Error(IdCardInteractionException.CardBlocked)
            val oldState = PinManagementStateMachine.State.FrameworkReadyForPinManagement(identificationPending, true, oldPin, newPin, pinManagementCallback)
            val newState: PinManagementStateMachine.State.CardBlocked = transition(oldState, event, this)
        }

        @ParameterizedTest
        @ValueSource(booleans = [false, true])
        fun `process failed after framework requested changed PIN`(identificationPending: Boolean) = runTest {
            val oldPin = "12345"
            val newPin = "000000"

            val event = PinManagementStateMachine.Event.Error(IdCardInteractionException.ProcessFailed(ActivationResultCode.INTERRUPTED, null, null))
            val oldState = PinManagementStateMachine.State.FrameworkReadyForPinManagement(identificationPending, true, oldPin, newPin, pinManagementCallback)
            val newState: PinManagementStateMachine.State.ProcessFailed = transition(oldState, event, this)

            Assertions.assertEquals(newState.identificationPending, identificationPending)
            Assertions.assertEquals(newState.oldPin, oldPin)
            Assertions.assertEquals(newState.newPin, newPin)
            Assertions.assertTrue(newState.firstScan)
        }

        @ParameterizedTest
        @ValueSource(booleans = [false, true])
        fun `card deactivated after framework requested card attachment`(identificationPending: Boolean) = runTest {
            val oldPin = "12345"
            val newPin = "000000"

            val event = PinManagementStateMachine.Event.Error(IdCardInteractionException.CardDeactivated)
            val oldState = PinManagementStateMachine.State.WaitingForFirstCardAttachment(identificationPending, true, oldPin, newPin)
            val newState: PinManagementStateMachine.State.CardDeactivated = transition(oldState, event, this)
        }

        @ParameterizedTest
        @ValueSource(booleans = [false, true])
        fun `card blocked after framework requested card attachment`(identificationPending: Boolean) = runTest {
            val oldPin = "12345"
            val newPin = "000000"

            val event = PinManagementStateMachine.Event.Error(IdCardInteractionException.CardBlocked)
            val oldState = PinManagementStateMachine.State.WaitingForFirstCardAttachment(identificationPending, true, oldPin, newPin)
            val newState: PinManagementStateMachine.State.CardBlocked = transition(oldState, event, this)
        }

        @ParameterizedTest
        @ValueSource(booleans = [false, true])
        fun `process failed after framework requested card attachment`(identificationPending: Boolean) = runTest {
            val oldPin = "12345"
            val newPin = "000000"

            val event = PinManagementStateMachine.Event.Error(IdCardInteractionException.ProcessFailed(ActivationResultCode.INTERRUPTED, null, null))
            val oldState = PinManagementStateMachine.State.WaitingForFirstCardAttachment(identificationPending, true, oldPin, newPin)
            val newState: PinManagementStateMachine.State.ProcessFailed = transition(oldState, event, this)

            Assertions.assertEquals(newState.identificationPending, identificationPending)
            Assertions.assertEquals(newState.oldPin, oldPin)
            Assertions.assertEquals(newState.newPin, newPin)
            Assertions.assertTrue(newState.firstScan)
        }

        @ParameterizedTest
        @ValueSource(booleans = [false, true])
        fun `card deactivated after second scan`(identificationPending: Boolean) = runTest {
            val oldPin = "12345"
            val newPin = "000000"

            val event = PinManagementStateMachine.Event.Error(IdCardInteractionException.CardDeactivated)
            val oldState = PinManagementStateMachine.State.WaitingForCardReAttachment(identificationPending, true, oldPin, newPin)
            val newState: PinManagementStateMachine.State.CardDeactivated = transition(oldState, event, this)
        }

        @ParameterizedTest
        @ValueSource(booleans = [false, true])
        fun `card blocked after second scan`(identificationPending: Boolean) = runTest {
            val oldPin = "12345"
            val newPin = "000000"

            val event = PinManagementStateMachine.Event.Error(IdCardInteractionException.CardBlocked)
            val oldState = PinManagementStateMachine.State.WaitingForCardReAttachment(identificationPending, true, oldPin, newPin)
            val newState: PinManagementStateMachine.State.CardBlocked = transition(oldState, event, this)
        }

        @ParameterizedTest
        @ValueSource(booleans = [false, true])
        fun `process failed after second scan`(identificationPending: Boolean) = runTest {
            val oldPin = "12345"
            val newPin = "000000"

            val event = PinManagementStateMachine.Event.Error(IdCardInteractionException.ProcessFailed(ActivationResultCode.INTERRUPTED, null, null))
            val oldState = PinManagementStateMachine.State.WaitingForCardReAttachment(identificationPending, true, oldPin, newPin)
            val newState: PinManagementStateMachine.State.ProcessFailed = transition(oldState, event, this)

            Assertions.assertEquals(newState.identificationPending, identificationPending)
            Assertions.assertEquals(newState.oldPin, oldPin)
            Assertions.assertEquals(newState.newPin, newPin)
            Assertions.assertFalse(newState.firstScan)
        }
    }

    @Nested
    inner class Back {
        @ParameterizedTest
        @ValueSource(booleans = [false, true])
        fun `from transport PIN input`(identificationPending: Boolean) = runTest {
            val event = PinManagementStateMachine.Event.Back
            val oldState = PinManagementStateMachine.State.OldTransportPinInput(identificationPending)
            val newState: PinManagementStateMachine.State.Invalid = transition(oldState, event, this)
        }

        @ParameterizedTest
        @ValueSource(booleans = [false, true])
        fun `from new PIN intro`(identificationPending: Boolean) = runTest {
            val oldPin = "123456"

            val event = PinManagementStateMachine.Event.Back
            val oldState = PinManagementStateMachine.State.NewPinIntro(identificationPending, true, oldPin)
            val newState: PinManagementStateMachine.State.OldTransportPinInput = transition(oldState, event, this)

            Assertions.assertEquals(identificationPending, newState.identificationPending)
        }

        @ParameterizedTest
        @ValueSource(booleans = [false, true])
        fun `from new PIN input`(identificationPending: Boolean) = runTest {
            val oldPin = "123456"

            val event = PinManagementStateMachine.Event.Back
            val oldState = PinManagementStateMachine.State.NewPinInput(identificationPending, true, oldPin)
            val newState: PinManagementStateMachine.State.NewPinIntro = transition(oldState, event, this)

            Assertions.assertEquals(identificationPending, newState.identificationPending)
            Assertions.assertEquals(oldPin, newState.oldPin)
            Assertions.assertTrue(newState.transportPin)
        }

        @ParameterizedTest
        @ValueSource(booleans = [false, true])
        fun `from new PIN confirmation`(identificationPending: Boolean) = runTest {
            val oldPin = "123456"
            val newPin = "000000"

            val event = PinManagementStateMachine.Event.Back
            val oldState = PinManagementStateMachine.State.NewPinConfirmation(identificationPending, true, oldPin, newPin)
            val newState: PinManagementStateMachine.State.NewPinInput = transition(oldState, event, this)

            Assertions.assertEquals(identificationPending, newState.identificationPending)
            Assertions.assertEquals(oldPin, newState.oldPin)
            Assertions.assertTrue(newState.transportPin)
        }

        @ParameterizedTest
        @ValueSource(booleans = [false, true])
        fun `from scan screen`(identificationPending: Boolean) = runTest {
            val oldPin = "123456"
            val newPin = "000000"

            val event = PinManagementStateMachine.Event.Back
            val oldState = PinManagementStateMachine.State.WaitingForFirstCardAttachment(identificationPending, true, oldPin, newPin)
            val newState: PinManagementStateMachine.State.NewPinInput = transition(oldState, event, this)

            Assertions.assertEquals(identificationPending, newState.identificationPending)
            Assertions.assertEquals(oldPin, newState.oldPin)
            Assertions.assertTrue(newState.transportPin)
        }
    }

    @ParameterizedTest
    @SealedClassesSource(names = [] , mode = SealedClassesSource.Mode.EXCLUDE, factoryClass = PinManagementStateFactory::class)
    fun invalidate(oldState: PinManagementStateMachine.State) = runTest {
        val event = PinManagementStateMachine.Event.Invalidate

        val stateMachine = PinManagementStateMachine(oldState, issueTrackerManager)
        Assertions.assertEquals(stateMachine.state.value.second, oldState)

        stateMachine.transition(event)
        Assertions.assertEquals(PinManagementStateMachine.State.Invalid, stateMachine.state.value.second)
    }

    @Nested
    inner class InvalidTransitions {
        @ParameterizedTest
        @SealedClassesSource(names = ["Invalid"] , mode = SealedClassesSource.Mode.EXCLUDE, factoryClass = PinManagementStateFactory::class)
        fun `start PIN management`(oldState: PinManagementStateMachine.State) = runTest {
            val event = PinManagementStateMachine.Event.StartPinManagement(false, true)

            val stateMachine = PinManagementStateMachine(oldState, issueTrackerManager)
            Assertions.assertEquals(stateMachine.state.value.second, oldState)

            Assertions.assertThrows(IllegalArgumentException::class.java) { stateMachine.transition(event) }
        }

        @ParameterizedTest
        @SealedClassesSource(names = ["OldTransportPinInput", "OldPersonalPinInput", "OldTransportPinRetry", "OldPersonalPinRetry"] , mode = SealedClassesSource.Mode.EXCLUDE, factoryClass = PinManagementStateFactory::class)
        fun `enter old PIN`(oldState: PinManagementStateMachine.State) = runTest {
            val event = PinManagementStateMachine.Event.EnterOldPin("")

            val stateMachine = PinManagementStateMachine(oldState, issueTrackerManager)
            Assertions.assertEquals(stateMachine.state.value.second, oldState)

            Assertions.assertThrows(IllegalArgumentException::class.java) { stateMachine.transition(event) }
        }

        @ParameterizedTest
        @SealedClassesSource(names = ["NewPinIntro"] , mode = SealedClassesSource.Mode.EXCLUDE, factoryClass = PinManagementStateFactory::class)
        fun `confirm PIN intro`(oldState: PinManagementStateMachine.State) = runTest {
            val event = PinManagementStateMachine.Event.ConfirmNewPinIntro

            val stateMachine = PinManagementStateMachine(oldState, issueTrackerManager)
            Assertions.assertEquals(stateMachine.state.value.second, oldState)

            Assertions.assertThrows(IllegalArgumentException::class.java) { stateMachine.transition(event) }
        }

        @ParameterizedTest
        @SealedClassesSource(names = ["NewPinConfirmation"] , mode = SealedClassesSource.Mode.EXCLUDE, factoryClass = PinManagementStateFactory::class)
        fun `retry PIN confirmation after mismatch`(oldState: PinManagementStateMachine.State) = runTest {
            val event = PinManagementStateMachine.Event.RetryNewPinConfirmation

            val stateMachine = PinManagementStateMachine(oldState, issueTrackerManager)
            Assertions.assertEquals(stateMachine.state.value.second, oldState)

            Assertions.assertThrows(IllegalArgumentException::class.java) { stateMachine.transition(event) }
        }

        @ParameterizedTest
        @SealedClassesSource(names = ["NewPinInput"] , mode = SealedClassesSource.Mode.EXCLUDE, factoryClass = PinManagementStateFactory::class)
        fun `enter new PIN`(oldState: PinManagementStateMachine.State) = runTest {
            val event = PinManagementStateMachine.Event.EnterNewPin("")

            val stateMachine = PinManagementStateMachine(oldState, issueTrackerManager)
            Assertions.assertEquals(stateMachine.state.value.second, oldState)

            Assertions.assertThrows(IllegalArgumentException::class.java) { stateMachine.transition(event) }
        }

        @ParameterizedTest
        @SealedClassesSource(names = ["NewPinConfirmation"] , mode = SealedClassesSource.Mode.EXCLUDE, factoryClass = PinManagementStateFactory::class)
        fun `confirm new PIN`(oldState: PinManagementStateMachine.State) = runTest {
            val event = PinManagementStateMachine.Event.ConfirmNewPin("")

            val stateMachine = PinManagementStateMachine(oldState, issueTrackerManager)
            Assertions.assertEquals(stateMachine.state.value.second, oldState)

            Assertions.assertThrows(IllegalArgumentException::class.java) { stateMachine.transition(event) }
        }

        @ParameterizedTest
        @SealedClassesSource(names = ["ReadyForScan", "FrameworkReadyForPinManagement", "CanRequested"] , mode = SealedClassesSource.Mode.EXCLUDE, factoryClass = PinManagementStateFactory::class)
        fun `request card insertion`(oldState: PinManagementStateMachine.State) = runTest {
            val event = PinManagementStateMachine.Event.RequestCardInsertion

            val stateMachine = PinManagementStateMachine(oldState, issueTrackerManager)
            Assertions.assertEquals(stateMachine.state.value.second, oldState)

            Assertions.assertThrows(IllegalArgumentException::class.java) { stateMachine.transition(event) }
        }

        @ParameterizedTest
        @SealedClassesSource(names = ["WaitingForFirstCardAttachment", "FrameworkReadyForPinManagement"] , mode = SealedClassesSource.Mode.EXCLUDE, factoryClass = PinManagementStateFactory::class)
        fun `framework requests changed PIN`(oldState: PinManagementStateMachine.State) = runTest {
            val event = PinManagementStateMachine.Event.FrameworkRequestsChangedPin(pinManagementCallback)

            val stateMachine = PinManagementStateMachine(oldState, issueTrackerManager)
            Assertions.assertEquals(stateMachine.state.value.second, oldState)

            Assertions.assertThrows(IllegalArgumentException::class.java) { stateMachine.transition(event) }
        }

        @ParameterizedTest
        @SealedClassesSource(names = ["WaitingForFirstCardAttachment", "WaitingForCardReAttachment", "FrameworkReadyForPinManagement"] , mode = SealedClassesSource.Mode.EXCLUDE, factoryClass = PinManagementStateFactory::class)
        fun `framework requests CAN`(oldState: PinManagementStateMachine.State) = runTest {
            val event = PinManagementStateMachine.Event.FrameworkRequestsCan

            val stateMachine = PinManagementStateMachine(oldState, issueTrackerManager)
            Assertions.assertEquals(stateMachine.state.value.second, oldState)

            Assertions.assertThrows(IllegalArgumentException::class.java) { stateMachine.transition(event) }
        }

        @ParameterizedTest
        @SealedClassesSource(names = ["FrameworkReadyForPinManagement", "WaitingForFirstCardAttachment", "WaitingForCardReAttachment", "CanRequested"] , mode = SealedClassesSource.Mode.EXCLUDE, factoryClass = PinManagementStateFactory::class)
        fun finish(oldState: PinManagementStateMachine.State) = runTest {
            val event = PinManagementStateMachine.Event.Finish

            val stateMachine = PinManagementStateMachine(oldState, issueTrackerManager)
            Assertions.assertEquals(stateMachine.state.value.second, oldState)

            Assertions.assertThrows(IllegalArgumentException::class.java) { stateMachine.transition(event) }
        }

        @ParameterizedTest
        @SealedClassesSource(names = ["ProcessFailed"] , mode = SealedClassesSource.Mode.EXCLUDE, factoryClass = PinManagementStateFactory::class)
        fun `proceed after error`(oldState: PinManagementStateMachine.State) = runTest {
            val event = PinManagementStateMachine.Event.ProceedAfterError

            val stateMachine = PinManagementStateMachine(oldState, issueTrackerManager)
            Assertions.assertEquals(stateMachine.state.value.second, oldState)

            Assertions.assertThrows(IllegalArgumentException::class.java) { stateMachine.transition(event) }
        }

        @ParameterizedTest
        @SealedClassesSource(names = ["FrameworkReadyForPinManagement", "WaitingForFirstCardAttachment", "WaitingForCardReAttachment"] , mode = SealedClassesSource.Mode.EXCLUDE, factoryClass = PinManagementStateFactory::class)
        fun `card deactivated`(oldState: PinManagementStateMachine.State) = runTest {
            val event = PinManagementStateMachine.Event.Error(IdCardInteractionException.CardDeactivated)

            val stateMachine = PinManagementStateMachine(oldState, issueTrackerManager)
            Assertions.assertEquals(stateMachine.state.value.second, oldState)

            Assertions.assertThrows(IllegalArgumentException::class.java) { stateMachine.transition(event) }
        }

        @ParameterizedTest
        @SealedClassesSource(names = ["FrameworkReadyForPinManagement", "WaitingForFirstCardAttachment", "WaitingForCardReAttachment"] , mode = SealedClassesSource.Mode.EXCLUDE, factoryClass = PinManagementStateFactory::class)
        fun `card blocked`(oldState: PinManagementStateMachine.State) = runTest {
            val event = PinManagementStateMachine.Event.Error(IdCardInteractionException.CardBlocked)

            val stateMachine = PinManagementStateMachine(oldState, issueTrackerManager)
            Assertions.assertEquals(stateMachine.state.value.second, oldState)

            Assertions.assertThrows(IllegalArgumentException::class.java) { stateMachine.transition(event) }
        }


        @ParameterizedTest
        @SealedClassesSource(names = ["FrameworkReadyForPinManagement", "WaitingForFirstCardAttachment", "WaitingForCardReAttachment"] , mode = SealedClassesSource.Mode.EXCLUDE, factoryClass = PinManagementStateFactory::class)
        fun `process failed`(oldState: PinManagementStateMachine.State) = runTest {
            val event = PinManagementStateMachine.Event.Error(IdCardInteractionException.ProcessFailed(ActivationResultCode.INTERRUPTED, null, null))

            val stateMachine = PinManagementStateMachine(oldState, issueTrackerManager)
            Assertions.assertEquals(stateMachine.state.value.second, oldState)

            Assertions.assertThrows(IllegalArgumentException::class.java) { stateMachine.transition(event) }
        }


        @ParameterizedTest
        @SealedClassesSource(names = ["OldTransportPinInput", "OldPersonalPinInput", "NewPinIntro", "NewPinInput", "NewPinConfirmation", "WaitingForFirstCardAttachment"] , mode = SealedClassesSource.Mode.EXCLUDE, factoryClass = PinManagementStateFactory::class)
        fun back(oldState: PinManagementStateMachine.State) = runTest {
            val event = PinManagementStateMachine.Event.Back

            val stateMachine = PinManagementStateMachine(oldState, issueTrackerManager)
            Assertions.assertEquals(stateMachine.state.value.second, oldState)

            Assertions.assertThrows(IllegalArgumentException::class.java) { stateMachine.transition(event) }
        }
    }
}
