package de.digitalService.useID.stateMachines

import de.digitalService.useID.analytics.IssueTrackerManagerType
import de.digitalService.useID.flows.*
import de.digitalService.useID.util.CanIdentStateFactory
import de.digitalService.useID.util.CanPinManagementStateFactory
import de.jodamob.junit5.SealedClassesSource
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

@OptIn(ExperimentalCoroutinesApi::class)
class CanStateMachineTest {
    private val issueTrackerManager = mockk<IssueTrackerManagerType>(relaxUnitFun = true)

    @OptIn(ExperimentalCoroutinesApi::class)
    private inline fun <reified NewState : CanStateMachine.State> transition(initialState: CanStateMachine.State, event: CanStateMachine.Event, testScope: TestScope): NewState {
        val stateMachine = CanStateMachine(initialState, issueTrackerManager)
        Assertions.assertEquals(stateMachine.state.value.second, initialState)

        stateMachine.transition(event)
        testScope.advanceUntilIdle()

        return stateMachine.state.value.second as? NewState ?: Assertions.fail("Unexpected new state.")
    }

    @Nested
    @DisplayName("Pin management flow")
    inner class PinManagement {

        @ParameterizedTest
        @ValueSource(booleans = [true, false])
        fun `initialize with long flow from invalid state`(identificationPending: Boolean) = runTest {
            val oldPin = "12345"
            val newPin = "000000"
            val event = CanStateMachine.Event.FrameworkRequestsCanForPinChange(identificationPending, oldPin, newPin, false)

            val newState: CanStateMachine.State.ChangePin.Intro = transition(CanStateMachine.State.Invalid, event, this)

            Assertions.assertEquals(newState.oldPin, oldPin)
            Assertions.assertEquals(newState.newPin, newPin)
            Assertions.assertEquals(newState.identificationPending, identificationPending)
        }

        @ParameterizedTest
        @ValueSource(booleans = [true, false])
        fun `initialize with short flow from invalid state`(identificationPending: Boolean) = runTest {
            val oldPin = "12345"
            val newPin = "000000"
            val event = CanStateMachine.Event.FrameworkRequestsCanForPinChange(identificationPending, oldPin, newPin, true)

            val newState: CanStateMachine.State.ChangePin.CanIntro = transition(CanStateMachine.State.Invalid, event, this)

            Assertions.assertEquals(newState.oldPin, oldPin)
            Assertions.assertEquals(newState.newPin, newPin)
            Assertions.assertEquals(newState.identificationPending, identificationPending)
        }

        @ParameterizedTest
        @ValueSource(booleans = [true, false])
        fun `re-initialize from can-and-pin-entered state`(identificationPending: Boolean) = runTest {
            val oldPin = "12345"
            val newPin = "000000"

            val event = CanStateMachine.Event.FrameworkRequestsCanForPinChange(identificationPending, "999999", "888888", false)

            val oldState = CanStateMachine.State.ChangePin.CanAndPinEntered(identificationPending, oldPin, "654321", newPin)
            val newState: CanStateMachine.State.ChangePin.CanInputRetry = transition(oldState, event, this)

            Assertions.assertEquals(newState.oldPin, oldPin)
            Assertions.assertEquals(newState.newPin, newPin)
            Assertions.assertEquals(newState.identificationPending, identificationPending)
        }

        @ParameterizedTest
        @ValueSource(booleans = [true, false])
        fun `agree to third attempt from intro`(identificationPending: Boolean) = runTest {
            val event = CanStateMachine.Event.AgreeToThirdAttempt

            val oldPin = "12345"
            val newPin = "000000"
            val oldState = CanStateMachine.State.ChangePin.Intro(identificationPending, oldPin, newPin)
            val newState: CanStateMachine.State.ChangePin.CanIntro = transition(oldState, event, this)

            Assertions.assertEquals(newState.oldPin, oldPin)
            Assertions.assertEquals(newState.newPin, newPin)
            Assertions.assertEquals(newState.identificationPending, identificationPending)
        }

        @ParameterizedTest
        @ValueSource(booleans = [true, false])
        fun `deny third attempt from intro`(identificationPending: Boolean) = runTest {
            val event = CanStateMachine.Event.DenyThirdAttempt

            val oldPin = "12345"
            val newPin = "000000"
            val oldState = CanStateMachine.State.ChangePin.Intro(identificationPending, oldPin, newPin)
            val newState: CanStateMachine.State.ChangePin.IdAlreadySetup = transition(oldState, event, this)

            Assertions.assertEquals(newState.oldPin, oldPin)
            Assertions.assertEquals(newState.newPin, newPin)
        }

        @ParameterizedTest
        @ValueSource(booleans = [true, false])
        fun `reset PIN from already-setup state`(identificationPending: Boolean) = runTest {
            val event = CanStateMachine.Event.ResetPin

            val oldPin = "12345"
            val newPin = "000000"
            val oldState = CanStateMachine.State.ChangePin.IdAlreadySetup(identificationPending, oldPin, newPin)
            val newState: CanStateMachine.State.ChangePin.PinReset = transition(oldState, event, this)

            Assertions.assertEquals(newState.oldPin, oldPin)
            Assertions.assertEquals(newState.newPin, newPin)
            Assertions.assertEquals(newState.identificationPending, identificationPending)
        }

        @ParameterizedTest
        @ValueSource(booleans = [true, false])
        fun `confirm CAN intro with pending identification`(shortFlow: Boolean) = runTest {
            val event = CanStateMachine.Event.ConfirmCanIntro

            val oldPin = "12345"
            val newPin = "000000"
            val oldState = CanStateMachine.State.ChangePin.CanIntro(true, oldPin, newPin, shortFlow)
            val newState: CanStateMachine.State.ChangePin.CanInput = transition(oldState, event, this)

            Assertions.assertEquals(newState.oldPin, oldPin)
            Assertions.assertEquals(newState.newPin, newPin)
            Assertions.assertEquals(newState.shortFlow, shortFlow)
            Assertions.assertTrue(newState.identificationPending)
        }

        @ParameterizedTest
        @ValueSource(booleans = [true, false])
        fun `confirm CAN intro without pending identification`(shortFlow: Boolean) = runTest {
            val event = CanStateMachine.Event.ConfirmCanIntro

            val oldPin = "12345"
            val newPin = "000000"
            val oldState = CanStateMachine.State.ChangePin.CanIntro(false, oldPin, newPin, shortFlow)
            val newState: CanStateMachine.State.ChangePin.CanInput = transition(oldState, event, this)

            Assertions.assertEquals(newState.oldPin, oldPin)
            Assertions.assertEquals(newState.newPin, newPin)
            Assertions.assertEquals(newState.shortFlow, shortFlow)
            Assertions.assertFalse(newState.identificationPending)
        }

        @ParameterizedTest
        @ValueSource(booleans = [true, false])
        fun `from CAN input in short flow`(identificationPending: Boolean) = runTest {
            val can = "654321"
            val event = CanStateMachine.Event.EnterCan(can)

            val oldPin = "12345"
            val newPin = "000000"
            val oldState = CanStateMachine.State.ChangePin.CanInput(identificationPending, oldPin, newPin, true)
            val newState: CanStateMachine.State.ChangePin.CanAndPinEntered = transition(oldState, event, this)

            Assertions.assertEquals(newState.oldPin, oldPin)
            Assertions.assertEquals(newState.newPin, newPin)
            Assertions.assertEquals(newState.can, can)
            Assertions.assertEquals(newState.identificationPending, identificationPending)
        }

        @ParameterizedTest
        @ValueSource(booleans = [true, false])
        fun `from CAN input in long flow`(identificationPending: Boolean) = runTest {
            val can = "654321"
            val event = CanStateMachine.Event.EnterCan(can)

            val oldPin = "12345"
            val newPin = "000000"
            val oldState = CanStateMachine.State.ChangePin.CanInput(identificationPending, oldPin, newPin, false)
            val newState: CanStateMachine.State.ChangePin.PinInput = transition(oldState, event, this)

            Assertions.assertEquals(newState.oldPin, oldPin)
            Assertions.assertEquals(newState.can, can)
            Assertions.assertEquals(newState.newPin, newPin)
            Assertions.assertEquals(newState.identificationPending, identificationPending)
        }

        @ParameterizedTest
        @ValueSource(booleans = [true, false])
        fun `from CAN input when retrying`(identificationPending: Boolean) = runTest {
            val can = "654321"
            val event = CanStateMachine.Event.EnterCan(can)

            val oldPin = "12345"
            val newPin = "000000"
            val oldState = CanStateMachine.State.ChangePin.CanInputRetry(identificationPending, oldPin, newPin)
            val newState: CanStateMachine.State.ChangePin.CanAndPinEntered = transition(oldState, event, this)

            Assertions.assertEquals(newState.oldPin, oldPin)
            Assertions.assertEquals(newState.newPin, newPin)
            Assertions.assertEquals(newState.can, can)
            Assertions.assertEquals(newState.identificationPending, identificationPending)
        }

        @ParameterizedTest
        @ValueSource(booleans = [true, false])
        fun `from PIN input`(identificationPending: Boolean) = runTest {
            val oldPin = "12345"
            val event = CanStateMachine.Event.EnterPin(oldPin)

            val newPin = "000000"
            val can = "654321"
            val oldState = CanStateMachine.State.ChangePin.PinInput(identificationPending, oldPin, can, newPin)
            val newState: CanStateMachine.State.ChangePin.CanAndPinEntered = transition(oldState, event, this)

            Assertions.assertEquals(newState.oldPin, oldPin)
            Assertions.assertEquals(newState.newPin, newPin)
            Assertions.assertEquals(newState.can, can)
            Assertions.assertEquals(newState.identificationPending, identificationPending)
        }

        @Nested
        inner class Back {
            @ParameterizedTest
            @ValueSource(booleans = [true, false])
            fun `from already-setup`(identificationPending: Boolean) = runTest {
                val event = CanStateMachine.Event.Back

                val oldPin = "12345"
                val newPin = "000000"
                val oldState = CanStateMachine.State.ChangePin.IdAlreadySetup(identificationPending, oldPin, newPin)
                val newState: CanStateMachine.State.ChangePin.Intro = transition(oldState, event, this)

                Assertions.assertEquals(newState.oldPin, oldPin)
                Assertions.assertEquals(newState.newPin, newPin)
                Assertions.assertEquals(newState.identificationPending, identificationPending)
            }

            @ParameterizedTest
            @ValueSource(booleans = [true, false])
            fun `from PIN reset`(identificationPending: Boolean) = runTest {
                val event = CanStateMachine.Event.Back

                val oldPin = "12345"
                val newPin = "000000"
                val oldState = CanStateMachine.State.ChangePin.PinReset(identificationPending, oldPin, newPin)
                val newState: CanStateMachine.State.ChangePin.IdAlreadySetup = transition(oldState, event, this)

                Assertions.assertEquals(newState.oldPin, oldPin)
                Assertions.assertEquals(newState.newPin, newPin)
                Assertions.assertEquals(newState.identificationPending, identificationPending)
            }

            @ParameterizedTest
            @ValueSource(booleans = [true, false])
            fun `from CAN intro with long flow`(identificationPending: Boolean) = runTest {
                val event = CanStateMachine.Event.Back

                val oldPin = "12345"
                val newPin = "000000"
                val oldState = CanStateMachine.State.ChangePin.CanIntro(identificationPending, oldPin, newPin, false)
                val newState: CanStateMachine.State.ChangePin.Intro = transition(oldState, event, this)

                Assertions.assertEquals(newState.newPin, newPin)
                Assertions.assertEquals(newState.oldPin, oldPin)
                Assertions.assertEquals(newState.identificationPending, identificationPending)
            }

            @ParameterizedTest
            @ValueSource(booleans = [true, false])
            fun `invalid transition from CAN intro in short flow`(identificationPending: Boolean) = runTest {
                val event = CanStateMachine.Event.Back

                val oldPin = "12345"
                val newPin = "000000"
                val oldState = CanStateMachine.State.ChangePin.CanIntro(identificationPending, oldPin, newPin, true)

                val stateMachine = CanStateMachine(oldState, issueTrackerManager)
                Assertions.assertThrows(IllegalArgumentException::class.java) { stateMachine.transition(event) }
            }

            @ParameterizedTest
            @ValueSource(booleans = [true, false])
            fun `from CAN input with pending identification`(shortFlow: Boolean) = runTest {
                val event = CanStateMachine.Event.Back

                val oldPin = "12345"
                val newPin = "000000"
                val oldState = CanStateMachine.State.ChangePin.CanInput(true, oldPin, newPin, shortFlow)
                val newState: CanStateMachine.State.ChangePin.CanIntro = transition(oldState, event, this)

                Assertions.assertEquals(newState.oldPin, oldPin)
                Assertions.assertEquals(newState.newPin, newPin)
                Assertions.assertEquals(newState.shortFlow, shortFlow)
                Assertions.assertTrue(newState.identificationPending)
            }

            @ParameterizedTest
            @ValueSource(booleans = [true, false])
            fun `from CAN input without pending identification`(shortFlow: Boolean) = runTest {
                val event = CanStateMachine.Event.Back

                val oldPin = "12345"
                val newPin = "000000"
                val oldState = CanStateMachine.State.ChangePin.CanInput(false, oldPin, newPin, shortFlow)
                val newState: CanStateMachine.State.ChangePin.CanIntro = transition(oldState, event, this)

                Assertions.assertEquals(newState.oldPin, oldPin)
                Assertions.assertEquals(newState.newPin, newPin)
                Assertions.assertEquals(newState.shortFlow, shortFlow)
                Assertions.assertFalse(newState.identificationPending)
            }

            @ParameterizedTest
            @ValueSource(booleans = [true, false])
            fun `from CAN input retrying`(identificationPending: Boolean) = runTest {
                val event = CanStateMachine.Event.Back

                val oldPin = "12345"
                val newPin = "000000"
                val oldState = CanStateMachine.State.ChangePin.CanInputRetry(identificationPending, oldPin, newPin)
                val newState: CanStateMachine.State.ChangePin.CanIntro = transition(oldState, event, this)

                Assertions.assertEquals(newState.oldPin, oldPin)
                Assertions.assertEquals(newState.newPin, newPin)
                Assertions.assertTrue(newState.shortFlow)
                Assertions.assertEquals(newState.identificationPending, identificationPending)
            }

            @ParameterizedTest
            @ValueSource(booleans = [true, false])
            fun `from PIN input`(identificationPending: Boolean) = runTest {
                val event = CanStateMachine.Event.Back

                val oldPin = "12345"
                val newPin = "000000"
                val oldState = CanStateMachine.State.ChangePin.PinInput(identificationPending, oldPin, "000000", newPin)
                val newState: CanStateMachine.State.ChangePin.CanInput = transition(oldState, event, this)

                Assertions.assertEquals(newState.oldPin, oldPin)
                Assertions.assertEquals(newState.newPin, newPin)
                Assertions.assertFalse(newState.shortFlow)
                Assertions.assertEquals(newState.identificationPending, identificationPending)
            }
        }
    }

    @Nested
    @DisplayName("Ident flow")
    inner class Ident {
        @Nested
        inner class Initialize {
            @Test
            fun `initialize with long flow from invalid state`() = runTest {
                val event = CanStateMachine.Event.FrameworkRequestsCanForIdent(null)

                val newState: CanStateMachine.State.Ident.Intro = transition(CanStateMachine.State.Invalid, event, this)

                Assertions.assertNull(newState.pin)
            }

            @Test
            fun `initialize with short flow from invalid state`() = runTest {
                val pin = "123456"
                val event = CanStateMachine.Event.FrameworkRequestsCanForIdent(pin)

                val newState: CanStateMachine.State.Ident.CanIntro = transition(CanStateMachine.State.Invalid, event, this)

                Assertions.assertEquals(newState.pin, pin)
            }

            @ParameterizedTest
            @ValueSource(booleans = [true, false])
            fun `re-initialize from can-and-pin-entered state`(shortFlow: Boolean) = runTest {
                val pin = "123456"

                val event = CanStateMachine.Event.FrameworkRequestsCanForIdent(pin)

                val oldState = CanStateMachine.State.Ident.CanAndPinEntered("654321", pin, shortFlow)
                val newState: CanStateMachine.State.Ident.CanInputRetry = transition(oldState, event, this)

                Assertions.assertEquals(newState.pin, pin)
            }

            @Test
            fun `agree to third attempt`() = runTest {
                val pin = "123456"

                val event = CanStateMachine.Event.AgreeToThirdAttempt

                val oldState = CanStateMachine.State.Ident.Intro(pin)
                val newState: CanStateMachine.State.Ident.CanIntro = transition(oldState, event, this)

                Assertions.assertEquals(newState.pin, pin)
            }

            @Test
            fun `reset PIN`() = runTest {
                val pin = "123456"

                val event = CanStateMachine.Event.ResetPin

                val oldState = CanStateMachine.State.Ident.Intro(pin)
                val newState: CanStateMachine.State.Ident.PinReset = transition(oldState, event, this)

                Assertions.assertEquals(newState.pin, pin)
            }

            @ParameterizedTest
            @ValueSource(booleans = [true, false])
            fun `confirm CAN intro`(shortFlow: Boolean) = runTest {
                val pin = "123456"

                val event = CanStateMachine.Event.ConfirmCanIntro

                val oldState = CanStateMachine.State.Ident.CanIntro(pin, shortFlow)
                val newState: CanStateMachine.State.Ident.CanInput = transition(oldState, event, this)

                Assertions.assertEquals(newState.pin, pin)
            }

            @ParameterizedTest
            @ValueSource(booleans = [true, false])
            fun `enter CAN in first attempt with PIN`(shortFlow: Boolean) = runTest {
                val pin = "123456"
                val can = "654321"

                val event = CanStateMachine.Event.EnterCan(can)

                val oldState = CanStateMachine.State.Ident.CanInput(pin, shortFlow)
                val newState: CanStateMachine.State.Ident.CanAndPinEntered = transition(oldState, event, this)

                Assertions.assertEquals(newState.pin, pin)
                Assertions.assertEquals(newState.can, can)
            }

            @ParameterizedTest
            @ValueSource(booleans = [true, false])
            fun `enter CAN in first attempt without PIN`(shortFlow: Boolean) = runTest {
                val can = "654321"

                val event = CanStateMachine.Event.EnterCan(can)

                val oldState = CanStateMachine.State.Ident.CanInput(null, shortFlow)
                val newState: CanStateMachine.State.Ident.PinInput = transition(oldState, event, this)

                Assertions.assertEquals(newState.can, can)
            }

            @ParameterizedTest
            @ValueSource(booleans = [true, false])
            fun `enter CAN when retrying`(shortFlow: Boolean) = runTest {
                val pin = "123456"
                val can = "654321"

                val event = CanStateMachine.Event.EnterCan(can)

                val oldState = CanStateMachine.State.Ident.CanInputRetry(pin, shortFlow)
                val newState: CanStateMachine.State.Ident.CanAndPinEntered = transition(oldState, event, this)

                Assertions.assertEquals(newState.pin, pin)
                Assertions.assertEquals(newState.can, can)
            }

            @ParameterizedTest
            @ValueSource(booleans = [true, false])
            fun `enter PIN`(shortFlow: Boolean) = runTest {
                val pin = "123456"
                val can = "654321"

                val event = CanStateMachine.Event.EnterPin(pin)

                val oldState = CanStateMachine.State.Ident.PinInput(can, shortFlow)
                val newState: CanStateMachine.State.Ident.CanAndPinEntered = transition(oldState, event, this)

                Assertions.assertEquals(newState.pin, pin)
                Assertions.assertEquals(newState.can, can)
            }

            @ParameterizedTest
            @SealedClassesSource(names = ["IdAlreadySetup", "PinReset", "CanIntro", "CanInput", "CanInputRetry", "PinInput"], mode = SealedClassesSource.Mode.EXCLUDE, factoryClass = CanPinManagementStateFactory::class)
            fun back(oldState: CanStateMachine.State.ChangePin) = runTest {
                val event = CanStateMachine.Event.Back

                val stateMachine = CanStateMachine(oldState, issueTrackerManager)
                Assertions.assertEquals(stateMachine.state.value.second, oldState)

                Assertions.assertThrows(IllegalArgumentException::class.java) { stateMachine.transition(event) }
            }
        }

        @Nested
        inner class InvalidTransitions {
            @Nested
            @DisplayName("starting in PIN management state")
            inner class PinManagement {
                @ParameterizedTest
                @SealedClassesSource(names = ["Invalid", "CanAndPinEntered"], mode = SealedClassesSource.Mode.EXCLUDE, factoryClass = CanPinManagementStateFactory::class)
                fun `initialize with pin management callback`(oldState: CanStateMachine.State.ChangePin) = runTest {
                    val event = CanStateMachine.Event.FrameworkRequestsCanForPinChange(false, "999999", "888888", false)

                    val stateMachine = CanStateMachine(oldState, issueTrackerManager)
                    Assertions.assertEquals(stateMachine.state.value.second, oldState)

                    Assertions.assertThrows(IllegalArgumentException::class.java) { stateMachine.transition(event) }
                }

                @ParameterizedTest
                @SealedClassesSource(names = ["Invalid"], mode = SealedClassesSource.Mode.EXCLUDE, factoryClass = CanPinManagementStateFactory::class)
                fun `initialize with pin callback`(oldState: CanStateMachine.State.ChangePin) = runTest {
                    val event = CanStateMachine.Event.FrameworkRequestsCanForIdent(null)

                    val stateMachine = CanStateMachine(oldState, issueTrackerManager)
                    Assertions.assertEquals(stateMachine.state.value.second, oldState)

                    Assertions.assertThrows(IllegalArgumentException::class.java) { stateMachine.transition(event) }
                }

                @ParameterizedTest
                @SealedClassesSource(names = ["Intro"], mode = SealedClassesSource.Mode.EXCLUDE, factoryClass = CanPinManagementStateFactory::class)
                fun `agree to third attempt`(oldState: CanStateMachine.State.ChangePin) = runTest {
                    val event = CanStateMachine.Event.AgreeToThirdAttempt

                    val stateMachine = CanStateMachine(oldState, issueTrackerManager)
                    Assertions.assertEquals(stateMachine.state.value.second, oldState)

                    Assertions.assertThrows(IllegalArgumentException::class.java) { stateMachine.transition(event) }
                }

                @ParameterizedTest
                @SealedClassesSource(names = ["Intro"], mode = SealedClassesSource.Mode.EXCLUDE, factoryClass = CanPinManagementStateFactory::class)
                fun `deny third attempt`(oldState: CanStateMachine.State.ChangePin) = runTest {
                    val event = CanStateMachine.Event.DenyThirdAttempt

                    val stateMachine = CanStateMachine(oldState, issueTrackerManager)
                    Assertions.assertEquals(stateMachine.state.value.second, oldState)

                    Assertions.assertThrows(IllegalArgumentException::class.java) { stateMachine.transition(event) }
                }

                @ParameterizedTest
                @SealedClassesSource(names = ["IdAlreadySetup"], mode = SealedClassesSource.Mode.EXCLUDE, factoryClass = CanPinManagementStateFactory::class)
                fun `reset PIN`(oldState: CanStateMachine.State.ChangePin) = runTest {
                    val event = CanStateMachine.Event.ResetPin

                    val stateMachine = CanStateMachine(oldState, issueTrackerManager)
                    Assertions.assertEquals(stateMachine.state.value.second, oldState)

                    Assertions.assertThrows(IllegalArgumentException::class.java) { stateMachine.transition(event) }
                }

                @ParameterizedTest
                @SealedClassesSource(names = ["CanIntro"], mode = SealedClassesSource.Mode.EXCLUDE, factoryClass = CanPinManagementStateFactory::class)
                fun `confirm CAN intro`(oldState: CanStateMachine.State.ChangePin) = runTest {
                    val event = CanStateMachine.Event.ConfirmCanIntro

                    val stateMachine = CanStateMachine(oldState, issueTrackerManager)
                    Assertions.assertEquals(stateMachine.state.value.second, oldState)

                    Assertions.assertThrows(IllegalArgumentException::class.java) { stateMachine.transition(event) }
                }

                @ParameterizedTest
                @SealedClassesSource(names = ["CanInput", "CanInputRetry"], mode = SealedClassesSource.Mode.EXCLUDE, factoryClass = CanPinManagementStateFactory::class)
                fun `enter CAN`(oldState: CanStateMachine.State.ChangePin) = runTest {
                    val event = CanStateMachine.Event.EnterCan("")

                    val stateMachine = CanStateMachine(oldState, issueTrackerManager)
                    Assertions.assertEquals(stateMachine.state.value.second, oldState)

                    Assertions.assertThrows(IllegalArgumentException::class.java) { stateMachine.transition(event) }
                }

                @ParameterizedTest
                @SealedClassesSource(names = ["PinInput"], mode = SealedClassesSource.Mode.EXCLUDE, factoryClass = CanPinManagementStateFactory::class)
                fun `enter PIN`(oldState: CanStateMachine.State.ChangePin) = runTest {
                    val event = CanStateMachine.Event.EnterPin("")

                    val stateMachine = CanStateMachine(oldState, issueTrackerManager)
                    Assertions.assertEquals(stateMachine.state.value.second, oldState)

                    Assertions.assertThrows(IllegalArgumentException::class.java) { stateMachine.transition(event) }
                }
            }

            @Nested
            @DisplayName("starting in ident state")
            inner class Ident {
                @ParameterizedTest
                @SealedClassesSource(names = ["Invalid"], mode = SealedClassesSource.Mode.EXCLUDE, factoryClass = CanIdentStateFactory::class)
                fun `initialize with pin management callback`(oldState: CanStateMachine.State.Ident) = runTest {
                    val event = CanStateMachine.Event.FrameworkRequestsCanForPinChange(false, "999999", "888888", false)

                    val stateMachine = CanStateMachine(oldState, issueTrackerManager)
                    Assertions.assertEquals(stateMachine.state.value.second, oldState)

                    Assertions.assertThrows(IllegalArgumentException::class.java) { stateMachine.transition(event) }
                }

                @ParameterizedTest
                @SealedClassesSource(names = ["Invalid", "CanAndPinEntered"], mode = SealedClassesSource.Mode.EXCLUDE, factoryClass = CanIdentStateFactory::class)
                fun `initialize with pin callback`(oldState: CanStateMachine.State.Ident) = runTest {
                    val event = CanStateMachine.Event.FrameworkRequestsCanForIdent(null)

                    val stateMachine = CanStateMachine(oldState, issueTrackerManager)
                    Assertions.assertEquals(stateMachine.state.value.second, oldState)

                    Assertions.assertThrows(IllegalArgumentException::class.java) { stateMachine.transition(event) }
                }

                @ParameterizedTest
                @SealedClassesSource(names = ["Intro"], mode = SealedClassesSource.Mode.EXCLUDE, factoryClass = CanIdentStateFactory::class)
                fun `agree to third attempt`(oldState: CanStateMachine.State.Ident) = runTest {
                    val event = CanStateMachine.Event.AgreeToThirdAttempt

                    val stateMachine = CanStateMachine(oldState, issueTrackerManager)
                    Assertions.assertEquals(stateMachine.state.value.second, oldState)

                    Assertions.assertThrows(IllegalArgumentException::class.java) { stateMachine.transition(event) }
                }

                @ParameterizedTest
                @SealedClassesSource(names = [], mode = SealedClassesSource.Mode.EXCLUDE, factoryClass = CanIdentStateFactory::class)
                fun `deny third attempt`(oldState: CanStateMachine.State.Ident) = runTest {
                    val event = CanStateMachine.Event.DenyThirdAttempt

                    val stateMachine = CanStateMachine(oldState, issueTrackerManager)
                    Assertions.assertEquals(stateMachine.state.value.second, oldState)

                    Assertions.assertThrows(IllegalArgumentException::class.java) { stateMachine.transition(event) }
                }

                @ParameterizedTest
                @SealedClassesSource(names = ["Intro"], mode = SealedClassesSource.Mode.EXCLUDE, factoryClass = CanIdentStateFactory::class)
                fun `reset PIN`(oldState: CanStateMachine.State.Ident) = runTest {
                    val event = CanStateMachine.Event.ResetPin

                    val stateMachine = CanStateMachine(oldState, issueTrackerManager)
                    Assertions.assertEquals(stateMachine.state.value.second, oldState)

                    Assertions.assertThrows(IllegalArgumentException::class.java) { stateMachine.transition(event) }
                }

                @ParameterizedTest
                @SealedClassesSource(names = ["CanIntro"], mode = SealedClassesSource.Mode.EXCLUDE, factoryClass = CanIdentStateFactory::class)
                fun `confirm CAN intro`(oldState: CanStateMachine.State.Ident) = runTest {
                    val event = CanStateMachine.Event.ConfirmCanIntro

                    val stateMachine = CanStateMachine(oldState, issueTrackerManager)
                    Assertions.assertEquals(stateMachine.state.value.second, oldState)

                    Assertions.assertThrows(IllegalArgumentException::class.java) { stateMachine.transition(event) }
                }

                @ParameterizedTest
                @SealedClassesSource(names = ["CanInput", "CanInputRetry"], mode = SealedClassesSource.Mode.EXCLUDE, factoryClass = CanIdentStateFactory::class)
                fun `enter CAN`(oldState: CanStateMachine.State.Ident) = runTest {
                    val event = CanStateMachine.Event.EnterCan("")

                    val stateMachine = CanStateMachine(oldState, issueTrackerManager)
                    Assertions.assertEquals(stateMachine.state.value.second, oldState)

                    Assertions.assertThrows(IllegalArgumentException::class.java) { stateMachine.transition(event) }
                }

                @ParameterizedTest
                @SealedClassesSource(names = ["PinInput"], mode = SealedClassesSource.Mode.EXCLUDE, factoryClass = CanIdentStateFactory::class)
                fun `enter PIN`(oldState: CanStateMachine.State.Ident) = runTest {
                    val event = CanStateMachine.Event.EnterPin("")

                    val stateMachine = CanStateMachine(oldState, issueTrackerManager)
                    Assertions.assertEquals(stateMachine.state.value.second, oldState)

                    Assertions.assertThrows(IllegalArgumentException::class.java) { stateMachine.transition(event) }
                }

                @ParameterizedTest
                @SealedClassesSource(names = ["PinReset", "CanIntro", "CanInput", "CanInputRetry", "PinInput"], mode = SealedClassesSource.Mode.EXCLUDE, factoryClass = CanIdentStateFactory::class)
                fun back(oldState: CanStateMachine.State.Ident) = runTest {
                    val event = CanStateMachine.Event.Back

                    val stateMachine = CanStateMachine(oldState, issueTrackerManager)
                    Assertions.assertEquals(stateMachine.state.value.second, oldState)

                    Assertions.assertThrows(IllegalArgumentException::class.java) { stateMachine.transition(event) }
                }
            }
        }

        @ParameterizedTest
        @SealedClassesSource(names = [], mode = SealedClassesSource.Mode.EXCLUDE, factoryClass = CanPinManagementStateFactory::class)
        fun `invalidate from pin management state`(oldState: CanStateMachine.State.ChangePin) = runTest {
            val event = CanStateMachine.Event.Invalidate

            val stateMachine = CanStateMachine(oldState, issueTrackerManager)
            Assertions.assertEquals(stateMachine.state.value.second, oldState)

            stateMachine.transition(event)
            Assertions.assertEquals(CanStateMachine.State.Invalid, stateMachine.state.value.second)
        }

        @ParameterizedTest
        @SealedClassesSource(names = [], mode = SealedClassesSource.Mode.EXCLUDE, factoryClass = CanIdentStateFactory::class)
        fun `invalidate from ident state`(oldState: CanStateMachine.State.Ident) = runTest {
            val event = CanStateMachine.Event.Invalidate

            val stateMachine = CanStateMachine(oldState, issueTrackerManager)
            Assertions.assertEquals(stateMachine.state.value.second, oldState)

            stateMachine.transition(event)
            Assertions.assertEquals(CanStateMachine.State.Invalid, stateMachine.state.value.second)
        }
    }
}
