package de.digitalService.useID.stateMachines

import de.digitalService.useID.analytics.IssueTrackerManagerType
import de.digitalService.useID.flows.ChangePinStateMachine
import de.digitalService.useID.idCardInterface.IdCardInteractionException
import de.digitalService.useID.util.PinManagementStateFactory
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

@OptIn(ExperimentalCoroutinesApi::class)
class ChangePinStateMachineTest {
    private val issueTrackerManager = mockk<IssueTrackerManagerType>(relaxUnitFun = true)

    private inline fun <reified NewState : ChangePinStateMachine.State> transition(initialState: ChangePinStateMachine.State, event: ChangePinStateMachine.Event, testScope: TestScope): NewState {
        val stateMachine = ChangePinStateMachine(initialState, issueTrackerManager)
        Assertions.assertEquals(stateMachine.state.value.second, initialState)

        stateMachine.transition(event)
        testScope.advanceUntilIdle()

        return stateMachine.state.value.second as? NewState ?: Assertions.fail("Unexpected new state.")
    }

    @ParameterizedTest
    @ValueSource(booleans = [false, true])
    fun `start PIN management with transport PIN from invalid state`(identificationPending: Boolean) = runTest {
        val transportPin = true

        val event = ChangePinStateMachine.Event.StartPinChange(identificationPending, transportPin)

        val newState: ChangePinStateMachine.State.OldTransportPinInput = transition(ChangePinStateMachine.State.Invalid, event, this)

        Assertions.assertEquals(newState.identificationPending, identificationPending)
    }

    @ParameterizedTest
    @ValueSource(booleans = [false, true])
    fun `enter old transport PIN from input`(identificationPending: Boolean) = runTest {
        val oldPin = "12345"

        val event = ChangePinStateMachine.Event.EnterOldPin(oldPin)
        val oldState = ChangePinStateMachine.State.OldTransportPinInput(identificationPending)
        val newState: ChangePinStateMachine.State.NewPinIntro = transition(oldState, event, this)

        Assertions.assertEquals(newState.identificationPending, identificationPending)
        Assertions.assertEquals(newState.oldPin, oldPin)
        Assertions.assertTrue(newState.transportPin)
    }

    @ParameterizedTest
    @ValueSource(booleans = [false, true])
    fun `re-enter old transport PIN from retry input`(identificationPending: Boolean) = runTest {
        val oldPin = "12345"
        val newPin = "000000"

        val event = ChangePinStateMachine.Event.EnterOldPin(oldPin)
        val oldState = ChangePinStateMachine.State.OldTransportPinRetry(identificationPending, newPin)
        val newState: ChangePinStateMachine.State.ReadyForSubsequentScan = transition(oldState, event, this)

        Assertions.assertEquals(newState.identificationPending, identificationPending)
        Assertions.assertEquals(newState.oldPin, oldPin)
        Assertions.assertEquals(newState.newPin, newPin)
        Assertions.assertTrue(newState.transportPin)
    }

    @ParameterizedTest
    @ValueSource(booleans = [false, true])
    fun `confirm new PIN intro in first attempt`(identificationPending: Boolean) = runTest {
        val oldPin = "12345"

        val event = ChangePinStateMachine.Event.ConfirmNewPinIntro
        val oldState = ChangePinStateMachine.State.NewPinIntro(identificationPending, true, oldPin)
        val newState: ChangePinStateMachine.State.NewPinInput = transition(oldState, event, this)

        Assertions.assertEquals(newState.identificationPending, identificationPending)
        Assertions.assertEquals(newState.oldPin, oldPin)
        Assertions.assertTrue(newState.transportPin)
    }

    @ParameterizedTest
    @ValueSource(booleans = [false, true])
    fun `retry new PIN after mismatch`(identificationPending: Boolean) = runTest {
        val oldPin = "12345"
        val newPin = "000000"

        val event = ChangePinStateMachine.Event.RetryNewPinConfirmation
        val oldState = ChangePinStateMachine.State.NewPinConfirmation(identificationPending, true, oldPin, newPin)
        val newState: ChangePinStateMachine.State.NewPinInput = transition(oldState, event, this)

        Assertions.assertEquals(newState.identificationPending, identificationPending)
        Assertions.assertEquals(newState.oldPin, oldPin)
        Assertions.assertTrue(newState.transportPin)
    }

    @ParameterizedTest
    @ValueSource(booleans = [false, true])
    fun `enter new PIN`(identificationPending: Boolean) = runTest {
        val oldPin = "12345"
        val newPin = "000000"

        val event = ChangePinStateMachine.Event.EnterNewPin(newPin)
        val oldState = ChangePinStateMachine.State.NewPinInput(identificationPending, true, oldPin)
        val newState: ChangePinStateMachine.State.NewPinConfirmation = transition(oldState, event, this)

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

        val event = ChangePinStateMachine.Event.ConfirmNewPin(newPin)
        val oldState = ChangePinStateMachine.State.NewPinConfirmation(identificationPending, true, oldPin, newPin)
        val newState: ChangePinStateMachine.State.StartIdCardInteraction = transition(oldState, event, this)

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

        val event = ChangePinStateMachine.Event.ConfirmNewPin(newPin)
        val oldState = ChangePinStateMachine.State.NewPinConfirmation(identificationPending, true, oldPin, "555555")
        Assertions.assertThrows(ChangePinStateMachine.Error.PinConfirmationFailed::class.java) { transition(oldState, event, this) as? ChangePinStateMachine.State.ReadyForSubsequentScan }
    }

    @ParameterizedTest
    @ValueSource(booleans = [false, true])
    fun `framework requests PIN after fist scan`(identificationPending: Boolean) = runTest {
        val oldPin = "12345"
        val newPin = "000000"

        val event = ChangePinStateMachine.Event.FrameworkRequestsPin
        val oldState = ChangePinStateMachine.State.StartIdCardInteraction(identificationPending, true, oldPin, newPin)
        val newState: ChangePinStateMachine.State.FrameworkReadyForPinInput = transition(oldState, event, this)

        Assertions.assertEquals(newState.identificationPending, identificationPending)
        Assertions.assertEquals(newState.oldPin, oldPin)
        Assertions.assertEquals(newState.newPin, newPin)
        Assertions.assertTrue(newState.transportPin)
    }

    @ParameterizedTest
    @ValueSource(booleans = [false, true])
    fun `framework requests PIN again`(identificationPending: Boolean) = runTest {
        val oldPin = "12345"
        val newPin = "000000"

        val event = ChangePinStateMachine.Event.FrameworkRequestsPin
        val oldState = ChangePinStateMachine.State.FrameworkReadyForPinInput(identificationPending, true, oldPin, newPin)
        val newState: ChangePinStateMachine.State.OldTransportPinRetry = transition(oldState, event, this)

        Assertions.assertEquals(newState.identificationPending, identificationPending)
        Assertions.assertEquals(newState.newPin, newPin)
    }

    @ParameterizedTest
    @ValueSource(booleans = [false, true])
    fun `framework requests PIN after subsequent scan`(identificationPending: Boolean) = runTest {
        val oldPin = "12345"
        val newPin = "000000"

        val event = ChangePinStateMachine.Event.FrameworkRequestsPin
        val oldState = ChangePinStateMachine.State.ReadyForSubsequentScan(identificationPending, true, oldPin, newPin)
        val newState: ChangePinStateMachine.State.FrameworkReadyForPinInput = transition(oldState, event, this)

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

        val event = ChangePinStateMachine.Event.FrameworkRequestsNewPin
        val oldState = ChangePinStateMachine.State.FrameworkReadyForPinInput(identificationPending, true, oldPin, newPin)
        val newState: ChangePinStateMachine.State.FrameworkReadyForNewPinInput = transition(oldState, event, this)

        Assertions.assertEquals(newState.identificationPending, identificationPending)
        Assertions.assertEquals(newState.oldPin, oldPin)
        Assertions.assertEquals(newState.newPin, newPin)
        Assertions.assertTrue(newState.transportPin)
    }

    @ParameterizedTest
    @ValueSource(booleans = [false, true])
    fun `framework requests new PIN for second time`(identificationPending: Boolean) = runTest {
        val oldPin = "12345"
        val newPin = "000000"

        val event = ChangePinStateMachine.Event.FrameworkRequestsNewPin
        val oldState = ChangePinStateMachine.State.ReadyForSubsequentScan(identificationPending, true, oldPin, newPin)
        val newState: ChangePinStateMachine.State.FrameworkReadyForNewPinInput = transition(oldState, event, this)

        Assertions.assertEquals(newState.identificationPending, identificationPending)
        Assertions.assertEquals(newState.newPin, newPin)
    }

    @ParameterizedTest
    @ValueSource(booleans = [false, true])
    fun `framework requests CAN on first attempt`(identificationPending: Boolean) = runTest {
        val oldPin = "12345"
        val newPin = "000000"

        val event = ChangePinStateMachine.Event.FrameworkRequestsCan
        val oldState = ChangePinStateMachine.State.StartIdCardInteraction(identificationPending, true, oldPin, newPin)
        val newState: ChangePinStateMachine.State.CanRequested = transition(oldState, event, this)

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

        val event = ChangePinStateMachine.Event.FrameworkRequestsCan
        val oldState = ChangePinStateMachine.State.ReadyForSubsequentScan(identificationPending, true, oldPin, newPin)
        val newState: ChangePinStateMachine.State.CanRequested = transition(oldState, event, this)

        Assertions.assertEquals(newState.identificationPending, identificationPending)
        Assertions.assertEquals(newState.oldPin, oldPin)
        Assertions.assertEquals(newState.newPin, newPin)
        Assertions.assertFalse(newState.shortFlow)
    }

    @ParameterizedTest
    @ValueSource(booleans = [false, true])
    fun `framework requests CAN with card already attached`(identificationPending: Boolean) = runTest {
        val oldPin = "12345"
        val newPin = "000000"

        val event = ChangePinStateMachine.Event.FrameworkRequestsCan
        val oldState = ChangePinStateMachine.State.FrameworkReadyForPinInput(identificationPending, true, oldPin, newPin)
        val newState: ChangePinStateMachine.State.CanRequested = transition(oldState, event, this)

        Assertions.assertEquals(newState.identificationPending, identificationPending)
        Assertions.assertEquals(newState.oldPin, oldPin)
        Assertions.assertEquals(newState.newPin, newPin)
        Assertions.assertTrue(newState.shortFlow)
    }

    @ParameterizedTest
    @ValueSource(booleans = [false, true])
    fun `finish after first scan`(identificationPending: Boolean) = runTest {
        val oldPin = "12345"
        val newPin = "000000"

        val event = ChangePinStateMachine.Event.Finish
        val oldState = ChangePinStateMachine.State.StartIdCardInteraction(identificationPending, true, oldPin, newPin)
        val newState: ChangePinStateMachine.State.Finished = transition(oldState, event, this)
    }

    @ParameterizedTest
    @ValueSource(booleans = [false, true])
    fun `finish after subsequent scan`(identificationPending: Boolean) = runTest {
        val oldPin = "12345"
        val newPin = "000000"

        val event = ChangePinStateMachine.Event.Finish
        val oldState = ChangePinStateMachine.State.ReadyForSubsequentScan(identificationPending, true, oldPin, newPin)
        val newState: ChangePinStateMachine.State.Finished = transition(oldState, event, this)
    }

    @ParameterizedTest
    @ValueSource(booleans = [false, true])
    fun `finish after CAN request`(identificationPending: Boolean) = runTest {
        val oldPin = "12345"
        val newPin = "000000"

        val event = ChangePinStateMachine.Event.Finish
        val oldState = ChangePinStateMachine.State.CanRequested(identificationPending, true, oldPin, newPin, false)
        val newState: ChangePinStateMachine.State.Finished = transition(oldState, event, this)
    }

    @ParameterizedTest
    @ValueSource(booleans = [false, true])
    fun `finish after new PIN input`(identificationPending: Boolean) = runTest {
        val oldPin = "12345"
        val newPin = "000000"

        val event = ChangePinStateMachine.Event.Finish
        val oldState = ChangePinStateMachine.State.FrameworkReadyForNewPinInput(identificationPending, true, oldPin, newPin)
        val newState: ChangePinStateMachine.State.Finished = transition(oldState, event, this)
    }

    @ParameterizedTest
    @ValueSource(booleans = [false, true])
    fun `retry after error in first scan`(identificationPending: Boolean) = runTest {
        val oldPin = "12345"
        val newPin = "000000"

        val event = ChangePinStateMachine.Event.ProceedAfterError
        val oldState = ChangePinStateMachine.State.ProcessFailed(identificationPending, true, oldPin, newPin, true)
        val newState: ChangePinStateMachine.State.StartIdCardInteraction = transition(oldState, event, this)

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

        val event = ChangePinStateMachine.Event.ProceedAfterError
        val oldState = ChangePinStateMachine.State.ProcessFailed(identificationPending, true, oldPin, newPin, false)
        val newState: ChangePinStateMachine.State.Cancelled = transition(oldState, event, this)
    }

    @Nested
    inner class Error {
        @ParameterizedTest
        @ValueSource(booleans = [false, true])
        fun `card deactivated after framework ready for PIN input`(identificationPending: Boolean) = runTest {
            val oldPin = "12345"
            val newPin = "000000"

            val event = ChangePinStateMachine.Event.Error(IdCardInteractionException.CardDeactivated)
            val oldState = ChangePinStateMachine.State.FrameworkReadyForPinInput(identificationPending, true, oldPin, newPin)
            val newState: ChangePinStateMachine.State.CardDeactivated = transition(oldState, event, this)
        }

        @ParameterizedTest
        @ValueSource(booleans = [false, true])
        fun `card blocked after framework ready for PIN input`(identificationPending: Boolean) = runTest {
            val oldPin = "12345"
            val newPin = "000000"

            val event = ChangePinStateMachine.Event.Error(IdCardInteractionException.CardBlocked)
            val oldState = ChangePinStateMachine.State.FrameworkReadyForPinInput(identificationPending, true, oldPin, newPin)
            val newState: ChangePinStateMachine.State.CardBlocked = transition(oldState, event, this)
        }

        @ParameterizedTest
        @ValueSource(booleans = [false, true])
        fun `process failed after framework ready for PIN input`(identificationPending: Boolean) = runTest {
            val oldPin = "12345"
            val newPin = "000000"

            val event = ChangePinStateMachine.Event.Error(IdCardInteractionException.ProcessFailed())
            val oldState = ChangePinStateMachine.State.FrameworkReadyForPinInput(identificationPending, true, oldPin, newPin)
            val newState: ChangePinStateMachine.State.ProcessFailed = transition(oldState, event, this)

            Assertions.assertEquals(newState.identificationPending, identificationPending)
            Assertions.assertEquals(newState.oldPin, oldPin)
            Assertions.assertEquals(newState.newPin, newPin)
            Assertions.assertTrue(newState.firstScan)
        }

        @ParameterizedTest
        @ValueSource(booleans = [false, true])
        fun `card deactivated after first scan`(identificationPending: Boolean) = runTest {
            val oldPin = "12345"
            val newPin = "000000"

            val event = ChangePinStateMachine.Event.Error(IdCardInteractionException.CardDeactivated)
            val oldState = ChangePinStateMachine.State.StartIdCardInteraction(identificationPending, true, oldPin, newPin)
            val newState: ChangePinStateMachine.State.CardDeactivated = transition(oldState, event, this)
        }

        @ParameterizedTest
        @ValueSource(booleans = [false, true])
        fun `card blocked after first scan`(identificationPending: Boolean) = runTest {
            val oldPin = "12345"
            val newPin = "000000"

            val event = ChangePinStateMachine.Event.Error(IdCardInteractionException.CardBlocked)
            val oldState = ChangePinStateMachine.State.StartIdCardInteraction(identificationPending, true, oldPin, newPin)
            val newState: ChangePinStateMachine.State.CardBlocked = transition(oldState, event, this)
        }

        @ParameterizedTest
        @ValueSource(booleans = [false, true])
        fun `process failed after first scan`(identificationPending: Boolean) = runTest {
            val oldPin = "12345"
            val newPin = "000000"

            val event = ChangePinStateMachine.Event.Error(IdCardInteractionException.ProcessFailed())
            val oldState = ChangePinStateMachine.State.StartIdCardInteraction(identificationPending, true, oldPin, newPin)
            val newState: ChangePinStateMachine.State.ProcessFailed = transition(oldState, event, this)

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

            val event = ChangePinStateMachine.Event.Error(IdCardInteractionException.CardDeactivated)
            val oldState = ChangePinStateMachine.State.ReadyForSubsequentScan(identificationPending, true, oldPin, newPin)
            val newState: ChangePinStateMachine.State.CardDeactivated = transition(oldState, event, this)
        }

        @ParameterizedTest
        @ValueSource(booleans = [false, true])
        fun `card blocked after second scan`(identificationPending: Boolean) = runTest {
            val oldPin = "12345"
            val newPin = "000000"

            val event = ChangePinStateMachine.Event.Error(IdCardInteractionException.CardBlocked)
            val oldState = ChangePinStateMachine.State.ReadyForSubsequentScan(identificationPending, true, oldPin, newPin)
            val newState: ChangePinStateMachine.State.CardBlocked = transition(oldState, event, this)
        }

        @ParameterizedTest
        @ValueSource(booleans = [false, true])
        fun `process failed after second scan`(identificationPending: Boolean) = runTest {
            val oldPin = "12345"
            val newPin = "000000"

            val event = ChangePinStateMachine.Event.Error(IdCardInteractionException.ProcessFailed())
            val oldState = ChangePinStateMachine.State.ReadyForSubsequentScan(identificationPending, true, oldPin, newPin)
            val newState: ChangePinStateMachine.State.ProcessFailed = transition(oldState, event, this)

            Assertions.assertEquals(newState.identificationPending, identificationPending)
            Assertions.assertEquals(newState.oldPin, oldPin)
            Assertions.assertEquals(newState.newPin, newPin)
            Assertions.assertFalse(newState.firstScan)
        }

        @ParameterizedTest
        @ValueSource(booleans = [false, true])
        fun `process failed after CAN requested`(identificationPending: Boolean) = runTest {
            val oldPin = "12345"
            val newPin = "000000"

            val event = ChangePinStateMachine.Event.Error(IdCardInteractionException.ProcessFailed())
            val oldState = ChangePinStateMachine.State.CanRequested(identificationPending, true, oldPin, newPin, true)
            val newState: ChangePinStateMachine.State.ProcessFailed = transition(oldState, event, this)

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
            val event = ChangePinStateMachine.Event.Back
            val oldState = ChangePinStateMachine.State.OldTransportPinInput(identificationPending)
            val newState: ChangePinStateMachine.State.Invalid = transition(oldState, event, this)
        }

        @ParameterizedTest
        @ValueSource(booleans = [false, true])
        fun `from new PIN intro`(identificationPending: Boolean) = runTest {
            val oldPin = "123456"

            val event = ChangePinStateMachine.Event.Back
            val oldState = ChangePinStateMachine.State.NewPinIntro(identificationPending, true, oldPin)
            val newState: ChangePinStateMachine.State.OldTransportPinInput = transition(oldState, event, this)

            Assertions.assertEquals(identificationPending, newState.identificationPending)
        }

        @ParameterizedTest
        @ValueSource(booleans = [false, true])
        fun `from new PIN input`(identificationPending: Boolean) = runTest {
            val oldPin = "123456"

            val event = ChangePinStateMachine.Event.Back
            val oldState = ChangePinStateMachine.State.NewPinInput(identificationPending, true, oldPin)
            val newState: ChangePinStateMachine.State.NewPinIntro = transition(oldState, event, this)

            Assertions.assertEquals(identificationPending, newState.identificationPending)
            Assertions.assertEquals(oldPin, newState.oldPin)
            Assertions.assertTrue(newState.transportPin)
        }

        @ParameterizedTest
        @ValueSource(booleans = [false, true])
        fun `from new PIN confirmation`(identificationPending: Boolean) = runTest {
            val oldPin = "123456"
            val newPin = "000000"

            val event = ChangePinStateMachine.Event.Back
            val oldState = ChangePinStateMachine.State.NewPinConfirmation(identificationPending, true, oldPin, newPin)
            val newState: ChangePinStateMachine.State.NewPinInput = transition(oldState, event, this)

            Assertions.assertEquals(identificationPending, newState.identificationPending)
            Assertions.assertEquals(oldPin, newState.oldPin)
            Assertions.assertTrue(newState.transportPin)
        }

        @ParameterizedTest
        @ValueSource(booleans = [false, true])
        fun `from scan screen`(identificationPending: Boolean) = runTest {
            val oldPin = "123456"
            val newPin = "000000"

            val event = ChangePinStateMachine.Event.Back
            val oldState = ChangePinStateMachine.State.StartIdCardInteraction(identificationPending, true, oldPin, newPin)
            val newState: ChangePinStateMachine.State.NewPinInput = transition(oldState, event, this)

            Assertions.assertEquals(identificationPending, newState.identificationPending)
            Assertions.assertEquals(oldPin, newState.oldPin)
            Assertions.assertTrue(newState.transportPin)
        }
    }

    @ParameterizedTest
    @SealedClassesSource(names = [], mode = SealedClassesSource.Mode.EXCLUDE, factoryClass = PinManagementStateFactory::class)
    fun invalidate(oldState: ChangePinStateMachine.State) = runTest {
        val event = ChangePinStateMachine.Event.Invalidate

        val stateMachine = ChangePinStateMachine(oldState, issueTrackerManager)
        Assertions.assertEquals(stateMachine.state.value.second, oldState)

        stateMachine.transition(event)
        Assertions.assertEquals(ChangePinStateMachine.State.Invalid, stateMachine.state.value.second)
    }

    @Nested
    inner class InvalidTransitions {
        @ParameterizedTest
        @SealedClassesSource(names = ["Invalid"], mode = SealedClassesSource.Mode.EXCLUDE, factoryClass = PinManagementStateFactory::class)
        fun `start PIN change`(oldState: ChangePinStateMachine.State) = runTest {
            val event = ChangePinStateMachine.Event.StartPinChange(false, true)

            val stateMachine = ChangePinStateMachine(oldState, issueTrackerManager)
            Assertions.assertEquals(stateMachine.state.value.second, oldState)

            Assertions.assertThrows(IllegalArgumentException::class.java) { stateMachine.transition(event) }
        }

        @ParameterizedTest
        @SealedClassesSource(names = ["OldTransportPinInput", "OldPersonalPinInput", "OldTransportPinRetry", "OldPersonalPinRetry"], mode = SealedClassesSource.Mode.EXCLUDE, factoryClass = PinManagementStateFactory::class)
        fun `enter old PIN`(oldState: ChangePinStateMachine.State) = runTest {
            val event = ChangePinStateMachine.Event.EnterOldPin("")

            val stateMachine = ChangePinStateMachine(oldState, issueTrackerManager)
            Assertions.assertEquals(stateMachine.state.value.second, oldState)

            Assertions.assertThrows(IllegalArgumentException::class.java) { stateMachine.transition(event) }
        }

        @ParameterizedTest
        @SealedClassesSource(names = ["NewPinIntro"], mode = SealedClassesSource.Mode.EXCLUDE, factoryClass = PinManagementStateFactory::class)
        fun `confirm PIN intro`(oldState: ChangePinStateMachine.State) = runTest {
            val event = ChangePinStateMachine.Event.ConfirmNewPinIntro

            val stateMachine = ChangePinStateMachine(oldState, issueTrackerManager)
            Assertions.assertEquals(stateMachine.state.value.second, oldState)

            Assertions.assertThrows(IllegalArgumentException::class.java) { stateMachine.transition(event) }
        }

        @ParameterizedTest
        @SealedClassesSource(names = ["NewPinConfirmation"], mode = SealedClassesSource.Mode.EXCLUDE, factoryClass = PinManagementStateFactory::class)
        fun `retry PIN confirmation after mismatch`(oldState: ChangePinStateMachine.State) = runTest {
            val event = ChangePinStateMachine.Event.RetryNewPinConfirmation

            val stateMachine = ChangePinStateMachine(oldState, issueTrackerManager)
            Assertions.assertEquals(stateMachine.state.value.second, oldState)

            Assertions.assertThrows(IllegalArgumentException::class.java) { stateMachine.transition(event) }
        }

        @ParameterizedTest
        @SealedClassesSource(names = ["NewPinInput"], mode = SealedClassesSource.Mode.EXCLUDE, factoryClass = PinManagementStateFactory::class)
        fun `enter new PIN`(oldState: ChangePinStateMachine.State) = runTest {
            val event = ChangePinStateMachine.Event.EnterNewPin("")

            val stateMachine = ChangePinStateMachine(oldState, issueTrackerManager)
            Assertions.assertEquals(stateMachine.state.value.second, oldState)

            Assertions.assertThrows(IllegalArgumentException::class.java) { stateMachine.transition(event) }
        }

        @ParameterizedTest
        @SealedClassesSource(names = ["NewPinConfirmation"], mode = SealedClassesSource.Mode.EXCLUDE, factoryClass = PinManagementStateFactory::class)
        fun `confirm new PIN`(oldState: ChangePinStateMachine.State) = runTest {
            val event = ChangePinStateMachine.Event.ConfirmNewPin("")

            val stateMachine = ChangePinStateMachine(oldState, issueTrackerManager)
            Assertions.assertEquals(stateMachine.state.value.second, oldState)

            Assertions.assertThrows(IllegalArgumentException::class.java) { stateMachine.transition(event) }
        }

        @ParameterizedTest
        @SealedClassesSource(names = ["StartIdCardInteraction", "FrameworkReadyForPinInput", "ReadyForSubsequentScan"], mode = SealedClassesSource.Mode.EXCLUDE, factoryClass = PinManagementStateFactory::class)
        fun `framework requests PIN`(oldState: ChangePinStateMachine.State) = runTest {
            val event = ChangePinStateMachine.Event.FrameworkRequestsNewPin

            val stateMachine = ChangePinStateMachine(oldState, issueTrackerManager)
            Assertions.assertEquals(stateMachine.state.value.second, oldState)

            Assertions.assertThrows(IllegalArgumentException::class.java) { stateMachine.transition(event) }
        }

        @ParameterizedTest
        @SealedClassesSource(names = ["FrameworkReadyForPinInput", "ReadyForSubsequentScan"], mode = SealedClassesSource.Mode.EXCLUDE, factoryClass = PinManagementStateFactory::class)
        fun `framework requests changed PIN`(oldState: ChangePinStateMachine.State) = runTest {
            val event = ChangePinStateMachine.Event.FrameworkRequestsNewPin

            val stateMachine = ChangePinStateMachine(oldState, issueTrackerManager)
            Assertions.assertEquals(stateMachine.state.value.second, oldState)

            Assertions.assertThrows(IllegalArgumentException::class.java) { stateMachine.transition(event) }
        }

        @ParameterizedTest
        @SealedClassesSource(names = ["StartIdCardInteraction", "ReadyForSubsequentScan", "FrameworkReadyForPinInput"], mode = SealedClassesSource.Mode.EXCLUDE, factoryClass = PinManagementStateFactory::class)
        fun `framework requests CAN`(oldState: ChangePinStateMachine.State) = runTest {
            val event = ChangePinStateMachine.Event.FrameworkRequestsCan

            val stateMachine = ChangePinStateMachine(oldState, issueTrackerManager)
            Assertions.assertEquals(stateMachine.state.value.second, oldState)

            Assertions.assertThrows(IllegalArgumentException::class.java) { stateMachine.transition(event) }
        }

        @ParameterizedTest
        @SealedClassesSource(names = ["StartIdCardInteraction", "ReadyForSubsequentScan", "CanRequested", "FrameworkReadyForNewPinInput"], mode = SealedClassesSource.Mode.EXCLUDE, factoryClass = PinManagementStateFactory::class)
        fun finish(oldState: ChangePinStateMachine.State) = runTest {
            val event = ChangePinStateMachine.Event.Finish

            val stateMachine = ChangePinStateMachine(oldState, issueTrackerManager)
            Assertions.assertEquals(stateMachine.state.value.second, oldState)

            Assertions.assertThrows(IllegalArgumentException::class.java) { stateMachine.transition(event) }
        }

        @ParameterizedTest
        @SealedClassesSource(names = ["ProcessFailed"], mode = SealedClassesSource.Mode.EXCLUDE, factoryClass = PinManagementStateFactory::class)
        fun `proceed after error`(oldState: ChangePinStateMachine.State) = runTest {
            val event = ChangePinStateMachine.Event.ProceedAfterError

            val stateMachine = ChangePinStateMachine(oldState, issueTrackerManager)
            Assertions.assertEquals(stateMachine.state.value.second, oldState)

            Assertions.assertThrows(IllegalArgumentException::class.java) { stateMachine.transition(event) }
        }

        @ParameterizedTest
        @SealedClassesSource(names = ["FrameworkReadyForPinInput", "StartIdCardInteraction", "ReadyForSubsequentScan", "CanRequested"], mode = SealedClassesSource.Mode.EXCLUDE, factoryClass = PinManagementStateFactory::class)
        fun `card deactivated`(oldState: ChangePinStateMachine.State) = runTest {
            val event = ChangePinStateMachine.Event.Error(IdCardInteractionException.CardDeactivated)

            val stateMachine = ChangePinStateMachine(oldState, issueTrackerManager)
            Assertions.assertEquals(stateMachine.state.value.second, oldState)

            Assertions.assertThrows(IllegalArgumentException::class.java) { stateMachine.transition(event) }
        }

        @ParameterizedTest
        @SealedClassesSource(names = ["FrameworkReadyForPinInput", "StartIdCardInteraction", "ReadyForSubsequentScan", "CanRequested"], mode = SealedClassesSource.Mode.EXCLUDE, factoryClass = PinManagementStateFactory::class)
        fun `card blocked`(oldState: ChangePinStateMachine.State) = runTest {
            val event = ChangePinStateMachine.Event.Error(IdCardInteractionException.CardBlocked)

            val stateMachine = ChangePinStateMachine(oldState, issueTrackerManager)
            Assertions.assertEquals(stateMachine.state.value.second, oldState)

            Assertions.assertThrows(IllegalArgumentException::class.java) { stateMachine.transition(event) }
        }


        @ParameterizedTest
        @SealedClassesSource(names = ["FrameworkReadyForPinInput", "StartIdCardInteraction", "ReadyForSubsequentScan", "CanRequested"], mode = SealedClassesSource.Mode.EXCLUDE, factoryClass = PinManagementStateFactory::class)
        fun `process failed`(oldState: ChangePinStateMachine.State) = runTest {
            val event = ChangePinStateMachine.Event.Error(IdCardInteractionException.ProcessFailed())

            val stateMachine = ChangePinStateMachine(oldState, issueTrackerManager)
            Assertions.assertEquals(stateMachine.state.value.second, oldState)

            Assertions.assertThrows(IllegalArgumentException::class.java) { stateMachine.transition(event) }
        }


        @ParameterizedTest
        @SealedClassesSource(names = ["OldTransportPinInput", "OldPersonalPinInput", "NewPinIntro", "NewPinInput", "NewPinConfirmation", "StartIdCardInteraction"], mode = SealedClassesSource.Mode.EXCLUDE, factoryClass = PinManagementStateFactory::class)
        fun back(oldState: ChangePinStateMachine.State) = runTest {
            val event = ChangePinStateMachine.Event.Back

            val stateMachine = ChangePinStateMachine(oldState, issueTrackerManager)
            Assertions.assertEquals(stateMachine.state.value.second, oldState)

            Assertions.assertThrows(IllegalArgumentException::class.java) { stateMachine.transition(event) }
        }
    }
}
