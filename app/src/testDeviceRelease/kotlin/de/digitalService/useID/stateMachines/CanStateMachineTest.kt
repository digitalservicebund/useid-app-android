package de.digitalService.useID.stateMachines

import de.digitalService.useID.flows.CanStateMachine
import de.digitalService.useID.flows.PinCallback
import de.digitalService.useID.flows.PinManagementCallback
import de.jodamob.junit5.DefaultTypeFactory
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
import kotlin.reflect.KClass

@OptIn(ExperimentalCoroutinesApi::class)
class CanStateMachineTest {
    private val pinManagementCallback: PinManagementCallback = mockk()
    private val pinCallback: PinCallback = mockk()

    @OptIn(ExperimentalCoroutinesApi::class)
    private inline fun <reified NewState: CanStateMachine.State> transition(initialState: CanStateMachine.State, event: CanStateMachine.Event, testScope: TestScope): NewState {
        val stateMachine = CanStateMachine(initialState)
        Assertions.assertEquals(stateMachine.state.value.second, initialState)

        stateMachine.transition(event)
        testScope.advanceUntilIdle()

        return stateMachine.state.value.second as? NewState ?: Assertions.fail("Unexpected new state.")
    }

    @Nested
    @DisplayName("Pin management flow")
    inner class PinManagement {

        @Test
        fun `initialize with long flow from invalid state`() = runTest {
            val oldPin = "12345"
            val event = CanStateMachine.Event.InitializeCanForPinManagement(oldPin, null, pinManagementCallback)

            val newState: CanStateMachine.State.PinManagement.Intro = transition(CanStateMachine.State.Invalid, event, this)

            Assertions.assertEquals(newState.oldPin, oldPin)
            Assertions.assertNull(newState.newPin)
            Assertions.assertEquals(newState.callback, pinManagementCallback)
        }

        @Test
        fun `initialize with short flow from invalid state`() = runTest {
            val oldPin = "12345"
            val newPin = "000000"
            val event = CanStateMachine.Event.InitializeCanForPinManagement(oldPin, newPin, pinManagementCallback)

            val newState: CanStateMachine.State.PinManagement.CanIntro = transition(CanStateMachine.State.Invalid, event, this)

            Assertions.assertEquals(newState.oldPin, oldPin)
            Assertions.assertEquals(newState.newPin, newPin)
            Assertions.assertEquals(newState.callback, pinManagementCallback)
        }

        @Test
        fun `re-initialize from can-and-pin-entered state`() = runTest {
            val oldPin = "12345"
            val newPin = "000000"

            val newCallback: PinManagementCallback = mockk()
            val event = CanStateMachine.Event.InitializeCanForPinManagement("999999", "888888", newCallback)

            val oldState = CanStateMachine.State.PinManagement.CanAndPinEntered(oldPin, pinManagementCallback, "654321", newPin)
            val newState: CanStateMachine.State.PinManagement.CanInputRetry = transition(oldState, event, this)

            Assertions.assertEquals(newState.oldPin, oldPin)
            Assertions.assertEquals(newState.newPin, newPin)
            Assertions.assertEquals(newState.callback, newCallback)
        }

        @Test
        fun `agree to third attempt from intro`() = runTest {
            val event = CanStateMachine.Event.AgreeToThirdAttempt

            val oldPin = "12345"
            val newPin = "000000"
            val oldState = CanStateMachine.State.PinManagement.Intro(oldPin, pinManagementCallback, newPin)
            val newState: CanStateMachine.State.PinManagement.CanIntro = transition(oldState, event, this)

            Assertions.assertEquals(newState.oldPin, oldPin)
            Assertions.assertEquals(newState.newPin, newPin)
            Assertions.assertEquals(newState.callback, pinManagementCallback)
        }

        @Test
        fun `deny third attempt from intro`() = runTest {
            val event = CanStateMachine.Event.DenyThirdAttempt

            val oldPin = "12345"
            val newPin = "000000"
            val oldState = CanStateMachine.State.PinManagement.Intro(oldPin, pinManagementCallback, newPin)
            val newState: CanStateMachine.State.PinManagement.IdAlreadySetup = transition(oldState, event, this)

            Assertions.assertEquals(newState.oldPin, oldPin)
            Assertions.assertEquals(newState.newPin, newPin)
            Assertions.assertEquals(newState.callback, pinManagementCallback)
        }

        @Test
        fun `reset PIN from already-setup state`() = runTest {
            val event = CanStateMachine.Event.ResetPin

            val oldPin = "12345"
            val newPin = "000000"
            val oldState = CanStateMachine.State.PinManagement.IdAlreadySetup(oldPin, pinManagementCallback, newPin)
            val newState: CanStateMachine.State.PinManagement.PinReset = transition(oldState, event, this)

            Assertions.assertEquals(newState.oldPin, oldPin)
            Assertions.assertEquals(newState.newPin, newPin)
            Assertions.assertEquals(newState.callback, pinManagementCallback)
        }

        @Test
        fun `confirm CAN intro`() = runTest {
            val event = CanStateMachine.Event.ConfirmCanIntro

            val oldPin = "12345"
            val newPin = "000000"
            val oldState = CanStateMachine.State.PinManagement.CanIntro(oldPin, pinManagementCallback, newPin)
            val newState: CanStateMachine.State.PinManagement.CanInput = transition(oldState, event, this)

            Assertions.assertEquals(newState.oldPin, oldPin)
            Assertions.assertEquals(newState.newPin, newPin)
            Assertions.assertEquals(newState.callback, pinManagementCallback)
        }

        @Test
        fun `from CAN input having new PIN`() = runTest {
            val can = "654321"
            val event = CanStateMachine.Event.EnterCan(can)

            val oldPin = "12345"
            val newPin = "000000"
            val oldState = CanStateMachine.State.PinManagement.CanInput(oldPin, pinManagementCallback, newPin)
            val newState: CanStateMachine.State.PinManagement.CanAndPinEntered = transition(oldState, event, this)

            Assertions.assertEquals(newState.oldPin, oldPin)
            Assertions.assertEquals(newState.newPin, newPin)
            Assertions.assertEquals(newState.can, can)
            Assertions.assertEquals(newState.callback, pinManagementCallback)
        }

        @Test
        fun `from CAN input not having new PIN`() = runTest {
            val can = "654321"
            val event = CanStateMachine.Event.EnterCan(can)

            val oldPin = "12345"
            val oldState = CanStateMachine.State.PinManagement.CanInput(oldPin, pinManagementCallback, null)
            val newState: CanStateMachine.State.PinManagement.PinInput = transition(oldState, event, this)

            Assertions.assertEquals(newState.oldPin, oldPin)
            Assertions.assertEquals(newState.can, can)
            Assertions.assertEquals(newState.callback, pinManagementCallback)
        }

        @Test
        fun `from CAN input when retrying`() = runTest {
            val can = "654321"
            val event = CanStateMachine.Event.EnterCan(can)

            val oldPin = "12345"
            val newPin = "000000"
            val oldState = CanStateMachine.State.PinManagement.CanInputRetry(oldPin, pinManagementCallback, newPin)
            val newState: CanStateMachine.State.PinManagement.CanAndPinEntered = transition(oldState, event, this)

            Assertions.assertEquals(newState.oldPin, oldPin)
            Assertions.assertEquals(newState.newPin, newPin)
            Assertions.assertEquals(newState.can, can)
            Assertions.assertEquals(newState.callback, pinManagementCallback)
        }

        @Test
        fun `from PIN input`() = runTest {
            val newPin = "000000"
            val event = CanStateMachine.Event.EnterPin(newPin)

            val oldPin = "12345"
            val can = "654321"
            val oldState = CanStateMachine.State.PinManagement.PinInput(oldPin, pinManagementCallback, can)
            val newState: CanStateMachine.State.PinManagement.CanAndPinEntered = transition(oldState, event, this)

            Assertions.assertEquals(newState.oldPin, oldPin)
            Assertions.assertEquals(newState.newPin, newPin)
            Assertions.assertEquals(newState.can, can)
            Assertions.assertEquals(newState.callback, pinManagementCallback)
        }

        @Nested
        inner class Back {
            @Test
            fun `from already-setup`() = runTest {
                val event = CanStateMachine.Event.Back

                val oldPin = "12345"
                val newPin = "000000"
                val oldState = CanStateMachine.State.PinManagement.IdAlreadySetup(oldPin, pinManagementCallback, newPin)
                val newState: CanStateMachine.State.PinManagement.Intro = transition(oldState, event, this)

                Assertions.assertEquals(newState.oldPin, oldPin)
                Assertions.assertEquals(newState.newPin, newPin)
                Assertions.assertEquals(newState.callback, pinManagementCallback)
            }

            @Test
            fun `from PIN reset`() = runTest {
                val event = CanStateMachine.Event.Back

                val oldPin = "12345"
                val newPin = "000000"
                val oldState = CanStateMachine.State.PinManagement.PinReset(oldPin, pinManagementCallback, newPin)
                val newState: CanStateMachine.State.PinManagement.IdAlreadySetup = transition(oldState, event, this)

                Assertions.assertEquals(newState.oldPin, oldPin)
                Assertions.assertEquals(newState.newPin, newPin)
                Assertions.assertEquals(newState.callback, pinManagementCallback)
            }

            @Test
            fun `from CAN intro without new PIN`() = runTest {
                val event = CanStateMachine.Event.Back

                val oldPin = "12345"
                val oldState = CanStateMachine.State.PinManagement.CanIntro(oldPin, pinManagementCallback, null)
                val newState: CanStateMachine.State.PinManagement.Intro = transition(oldState, event, this)

                Assertions.assertEquals(newState.oldPin, oldPin)
                Assertions.assertNull(newState.newPin)
                Assertions.assertEquals(newState.callback, pinManagementCallback)
            }

            @Test
            fun `invalid transition from CAN intro with new PIN`() = runTest {
                val event = CanStateMachine.Event.Back

                val oldPin = "12345"
                val newPin = "000000"
                val oldState = CanStateMachine.State.PinManagement.CanIntro(oldPin, pinManagementCallback, newPin)

                val stateMachine = CanStateMachine(oldState)
                Assertions.assertThrows(IllegalArgumentException::class.java) { stateMachine.transition(event) }
            }

            @Test
            fun `from CAN input`() = runTest {
                val event = CanStateMachine.Event.Back

                val oldPin = "12345"
                val newPin = "000000"
                val oldState = CanStateMachine.State.PinManagement.CanInput(oldPin, pinManagementCallback, newPin)
                val newState: CanStateMachine.State.PinManagement.CanIntro = transition(oldState, event, this)

                Assertions.assertEquals(newState.oldPin, oldPin)
                Assertions.assertEquals(newState.newPin, newPin)
                Assertions.assertEquals(newState.callback, pinManagementCallback)
            }

            @Test
            fun `from CAN input retrying`() = runTest {
                val event = CanStateMachine.Event.Back

                val oldPin = "12345"
                val newPin = "000000"
                val oldState = CanStateMachine.State.PinManagement.CanInputRetry(oldPin, pinManagementCallback, newPin)
                val newState: CanStateMachine.State.PinManagement.CanIntro = transition(oldState, event, this)

                Assertions.assertEquals(newState.oldPin, oldPin)
                Assertions.assertEquals(newState.newPin, newPin)
                Assertions.assertEquals(newState.callback, pinManagementCallback)
            }

            @Test
            fun `from PIN input`() = runTest {
                val event = CanStateMachine.Event.Back

                val oldPin = "12345"
                val newPin = "000000"
                val oldState = CanStateMachine.State.PinManagement.PinInput(oldPin, pinManagementCallback, newPin)
                val newState: CanStateMachine.State.PinManagement.CanInput = transition(oldState, event, this)

                Assertions.assertEquals(newState.oldPin, oldPin)
                Assertions.assertNull(newState.newPin)
                Assertions.assertEquals(newState.callback, pinManagementCallback)
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
                val event = CanStateMachine.Event.InitializeCanForIdent(null, pinCallback)

                val newState: CanStateMachine.State.Ident.Intro = transition(CanStateMachine.State.Invalid, event, this)

                Assertions.assertNull(newState.pin)
                Assertions.assertEquals(newState.callback, pinCallback)
            }

            @Test
            fun `initialize with short flow from invalid state`() = runTest {
                val pin = "123456"
                val event = CanStateMachine.Event.InitializeCanForIdent(pin, pinCallback)

                val newState: CanStateMachine.State.Ident.CanIntro = transition(CanStateMachine.State.Invalid, event, this)

                Assertions.assertEquals(newState.pin, pin)
                Assertions.assertEquals(newState.callback, pinCallback)
            }

            @Test
            fun `re-initialize from can-and-pin-entered state`() = runTest {
                val pin = "123456"

                val newPinCallback: PinCallback = mockk()
                val event = CanStateMachine.Event.InitializeCanForIdent(pin, newPinCallback)

                val oldState = CanStateMachine.State.Ident.CanAndPinEntered(pinCallback, "654321", pin)
                val newState: CanStateMachine.State.Ident.CanInputRetry = transition(oldState, event, this)

                Assertions.assertEquals(newState.pin, pin)
                Assertions.assertEquals(newState.callback, newPinCallback)
            }

            @Test
            fun `agree to third attempt`() = runTest {
                val pin = "123456"

                val event = CanStateMachine.Event.AgreeToThirdAttempt

                val oldState = CanStateMachine.State.Ident.Intro(pinCallback, pin)
                val newState: CanStateMachine.State.Ident.CanIntro = transition(oldState, event, this)

                Assertions.assertEquals(newState.pin, pin)
                Assertions.assertEquals(newState.callback, pinCallback)
            }

            @Test
            fun `reset PIN`() = runTest {
                val pin = "123456"

                val event = CanStateMachine.Event.ResetPin

                val oldState = CanStateMachine.State.Ident.Intro(pinCallback, pin)
                val newState: CanStateMachine.State.Ident.PinReset = transition(oldState, event, this)

                Assertions.assertEquals(newState.pin, pin)
                Assertions.assertEquals(newState.callback, pinCallback)
            }

            @Test
            fun `confirm CAN intro`() = runTest {
                val pin = "123456"

                val event = CanStateMachine.Event.ConfirmCanIntro

                val oldState = CanStateMachine.State.Ident.CanIntro(pinCallback, pin)
                val newState: CanStateMachine.State.Ident.CanInput = transition(oldState, event, this)

                Assertions.assertEquals(newState.pin, pin)
                Assertions.assertEquals(newState.callback, pinCallback)
            }

            @Test
            fun `enter CAN in first attempt with PIN`() = runTest {
                val pin = "123456"
                val can = "654321"

                val event = CanStateMachine.Event.EnterCan(can)

                val oldState = CanStateMachine.State.Ident.CanInput(pinCallback, pin)
                val newState: CanStateMachine.State.Ident.CanAndPinEntered = transition(oldState, event, this)

                Assertions.assertEquals(newState.pin, pin)
                Assertions.assertEquals(newState.can, can)
                Assertions.assertEquals(newState.callback, pinCallback)
            }

            @Test
            fun `enter CAN in first attempt without PIN`() = runTest {
                val can = "654321"

                val event = CanStateMachine.Event.EnterCan(can)

                val oldState = CanStateMachine.State.Ident.CanInput(pinCallback, null)
                val newState: CanStateMachine.State.Ident.PinInput = transition(oldState, event, this)

                Assertions.assertEquals(newState.can, can)
                Assertions.assertEquals(newState.callback, pinCallback)
            }

            @Test
            fun `enter CAN when retrying`() = runTest {
                val pin = "123456"
                val can = "654321"

                val event = CanStateMachine.Event.EnterCan(can)

                val oldState = CanStateMachine.State.Ident.CanInputRetry(pinCallback, pin)
                val newState: CanStateMachine.State.Ident.CanAndPinEntered = transition(oldState, event, this)

                Assertions.assertEquals(newState.pin, pin)
                Assertions.assertEquals(newState.can, can)
                Assertions.assertEquals(newState.callback, pinCallback)
            }

            @Test
            fun `enter PIN`() = runTest {
                val pin = "123456"
                val can = "654321"

                val event = CanStateMachine.Event.EnterPin(pin)

                val oldState = CanStateMachine.State.Ident.PinInput(pinCallback, can)
                val newState: CanStateMachine.State.Ident.CanAndPinEntered = transition(oldState, event, this)

                Assertions.assertEquals(newState.pin, pin)
                Assertions.assertEquals(newState.can, can)
                Assertions.assertEquals(newState.callback, pinCallback)
            }

            @ParameterizedTest
            @SealedClassesSource(names = ["IdAlreadySetup", "PinReset", "CanIntro", "CanInput", "CanInputRetry", "PinInput"] , mode = SealedClassesSource.Mode.EXCLUDE, factoryClass = CanPinManagementStateFactory::class)
            fun back(oldState: CanStateMachine.State.PinManagement) = runTest {
                val event = CanStateMachine.Event.Back

                val stateMachine = CanStateMachine(oldState)
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
                @SealedClassesSource(names = ["Invalid", "CanAndPinEntered"] , mode = SealedClassesSource.Mode.EXCLUDE, factoryClass = CanPinManagementStateFactory::class)
                fun `initialize with pin management callback`(oldState: CanStateMachine.State.PinManagement) = runTest {
                    val event = CanStateMachine.Event.InitializeCanForPinManagement("999999", "888888", pinManagementCallback)

                    val stateMachine = CanStateMachine(oldState)
                    Assertions.assertEquals(stateMachine.state.value.second, oldState)

                    Assertions.assertThrows(IllegalArgumentException::class.java) { stateMachine.transition(event) }
                }

                @ParameterizedTest
                @SealedClassesSource(names = ["Invalid"] , mode = SealedClassesSource.Mode.EXCLUDE, factoryClass = CanPinManagementStateFactory::class)
                fun `initialize with pin callback`(oldState: CanStateMachine.State.PinManagement) = runTest {
                    val event = CanStateMachine.Event.InitializeCanForIdent(null, pinCallback)

                    val stateMachine = CanStateMachine(oldState)
                    Assertions.assertEquals(stateMachine.state.value.second, oldState)

                    Assertions.assertThrows(IllegalArgumentException::class.java) { stateMachine.transition(event) }
                }

                @ParameterizedTest
                @SealedClassesSource(names = ["Intro"] , mode = SealedClassesSource.Mode.EXCLUDE, factoryClass = CanPinManagementStateFactory::class)
                fun `agree to third attempt`(oldState: CanStateMachine.State.PinManagement) = runTest {
                    val event = CanStateMachine.Event.AgreeToThirdAttempt

                    val stateMachine = CanStateMachine(oldState)
                    Assertions.assertEquals(stateMachine.state.value.second, oldState)

                    Assertions.assertThrows(IllegalArgumentException::class.java) { stateMachine.transition(event) }
                }

                @ParameterizedTest
                @SealedClassesSource(names = ["Intro"] , mode = SealedClassesSource.Mode.EXCLUDE, factoryClass = CanPinManagementStateFactory::class)
                fun `deny third attempt`(oldState: CanStateMachine.State.PinManagement) = runTest {
                    val event = CanStateMachine.Event.DenyThirdAttempt

                    val stateMachine = CanStateMachine(oldState)
                    Assertions.assertEquals(stateMachine.state.value.second, oldState)

                    Assertions.assertThrows(IllegalArgumentException::class.java) { stateMachine.transition(event) }
                }

                @ParameterizedTest
                @SealedClassesSource(names = ["IdAlreadySetup"] , mode = SealedClassesSource.Mode.EXCLUDE, factoryClass = CanPinManagementStateFactory::class)
                fun `reset PIN`(oldState: CanStateMachine.State.PinManagement) = runTest {
                    val event = CanStateMachine.Event.ResetPin

                    val stateMachine = CanStateMachine(oldState)
                    Assertions.assertEquals(stateMachine.state.value.second, oldState)

                    Assertions.assertThrows(IllegalArgumentException::class.java) { stateMachine.transition(event) }
                }

                @ParameterizedTest
                @SealedClassesSource(names = ["CanIntro"] , mode = SealedClassesSource.Mode.EXCLUDE, factoryClass = CanPinManagementStateFactory::class)
                fun `confirm CAN intro`(oldState: CanStateMachine.State.PinManagement) = runTest {
                    val event = CanStateMachine.Event.ConfirmCanIntro

                    val stateMachine = CanStateMachine(oldState)
                    Assertions.assertEquals(stateMachine.state.value.second, oldState)

                    Assertions.assertThrows(IllegalArgumentException::class.java) { stateMachine.transition(event) }
                }

                @ParameterizedTest
                @SealedClassesSource(names = ["CanInput", "CanInputRetry"] , mode = SealedClassesSource.Mode.EXCLUDE, factoryClass = CanPinManagementStateFactory::class)
                fun `enter CAN`(oldState: CanStateMachine.State.PinManagement) = runTest {
                    val event = CanStateMachine.Event.EnterCan("")

                    val stateMachine = CanStateMachine(oldState)
                    Assertions.assertEquals(stateMachine.state.value.second, oldState)

                    Assertions.assertThrows(IllegalArgumentException::class.java) { stateMachine.transition(event) }
                }

                @ParameterizedTest
                @SealedClassesSource(names = ["PinInput"] , mode = SealedClassesSource.Mode.EXCLUDE, factoryClass = CanPinManagementStateFactory::class)
                fun `enter PIN`(oldState: CanStateMachine.State.PinManagement) = runTest {
                    val event = CanStateMachine.Event.EnterPin("")

                    val stateMachine = CanStateMachine(oldState)
                    Assertions.assertEquals(stateMachine.state.value.second, oldState)

                    Assertions.assertThrows(IllegalArgumentException::class.java) { stateMachine.transition(event) }
                }
            }

            @Nested
            @DisplayName("starting in ident state")
            inner class Ident {
                @ParameterizedTest
                @SealedClassesSource(names = ["Invalid"] , mode = SealedClassesSource.Mode.EXCLUDE, factoryClass = CanIdentStateFactory::class)
                fun `initialize with pin management callback`(oldState: CanStateMachine.State.Ident) = runTest {
                    val event = CanStateMachine.Event.InitializeCanForPinManagement("999999", "888888", pinManagementCallback)

                    val stateMachine = CanStateMachine(oldState)
                    Assertions.assertEquals(stateMachine.state.value.second, oldState)

                    Assertions.assertThrows(IllegalArgumentException::class.java) { stateMachine.transition(event) }
                }

                @ParameterizedTest
                @SealedClassesSource(names = ["Invalid", "CanAndPinEntered"] , mode = SealedClassesSource.Mode.EXCLUDE, factoryClass = CanIdentStateFactory::class)
                fun `initialize with pin callback`(oldState: CanStateMachine.State.Ident) = runTest {
                    val event = CanStateMachine.Event.InitializeCanForIdent(null, pinCallback)

                    val stateMachine = CanStateMachine(oldState)
                    Assertions.assertEquals(stateMachine.state.value.second, oldState)

                    Assertions.assertThrows(IllegalArgumentException::class.java) { stateMachine.transition(event) }
                }

                @ParameterizedTest
                @SealedClassesSource(names = ["Intro"] , mode = SealedClassesSource.Mode.EXCLUDE, factoryClass = CanIdentStateFactory::class)
                fun `agree to third attempt`(oldState: CanStateMachine.State.Ident) = runTest {
                    val event = CanStateMachine.Event.AgreeToThirdAttempt

                    val stateMachine = CanStateMachine(oldState)
                    Assertions.assertEquals(stateMachine.state.value.second, oldState)

                    Assertions.assertThrows(IllegalArgumentException::class.java) { stateMachine.transition(event) }
                }

                @ParameterizedTest
                @SealedClassesSource(names = [] , mode = SealedClassesSource.Mode.EXCLUDE, factoryClass = CanIdentStateFactory::class)
                fun `deny third attempt`(oldState: CanStateMachine.State.Ident) = runTest {
                    val event = CanStateMachine.Event.DenyThirdAttempt

                    val stateMachine = CanStateMachine(oldState)
                    Assertions.assertEquals(stateMachine.state.value.second, oldState)

                    Assertions.assertThrows(IllegalArgumentException::class.java) { stateMachine.transition(event) }
                }

                @ParameterizedTest
                @SealedClassesSource(names = ["Intro"] , mode = SealedClassesSource.Mode.EXCLUDE, factoryClass = CanIdentStateFactory::class)
                fun `reset PIN`(oldState: CanStateMachine.State.Ident) = runTest {
                    val event = CanStateMachine.Event.ResetPin

                    val stateMachine = CanStateMachine(oldState)
                    Assertions.assertEquals(stateMachine.state.value.second, oldState)

                    Assertions.assertThrows(IllegalArgumentException::class.java) { stateMachine.transition(event) }
                }

                @ParameterizedTest
                @SealedClassesSource(names = ["CanIntro"] , mode = SealedClassesSource.Mode.EXCLUDE, factoryClass = CanIdentStateFactory::class)
                fun `confirm CAN intro`(oldState: CanStateMachine.State.Ident) = runTest {
                    val event = CanStateMachine.Event.ConfirmCanIntro

                    val stateMachine = CanStateMachine(oldState)
                    Assertions.assertEquals(stateMachine.state.value.second, oldState)

                    Assertions.assertThrows(IllegalArgumentException::class.java) { stateMachine.transition(event) }
                }

                @ParameterizedTest
                @SealedClassesSource(names = ["CanInput", "CanInputRetry"] , mode = SealedClassesSource.Mode.EXCLUDE, factoryClass = CanIdentStateFactory::class)
                fun `enter CAN`(oldState: CanStateMachine.State.Ident) = runTest {
                    val event = CanStateMachine.Event.EnterCan("")

                    val stateMachine = CanStateMachine(oldState)
                    Assertions.assertEquals(stateMachine.state.value.second, oldState)

                    Assertions.assertThrows(IllegalArgumentException::class.java) { stateMachine.transition(event) }
                }

                @ParameterizedTest
                @SealedClassesSource(names = ["PinInput"] , mode = SealedClassesSource.Mode.EXCLUDE, factoryClass = CanIdentStateFactory::class)
                fun `enter PIN`(oldState: CanStateMachine.State.Ident) = runTest {
                    val event = CanStateMachine.Event.EnterPin("")

                    val stateMachine = CanStateMachine(oldState)
                    Assertions.assertEquals(stateMachine.state.value.second, oldState)

                    Assertions.assertThrows(IllegalArgumentException::class.java) { stateMachine.transition(event) }
                }

                @ParameterizedTest
                @SealedClassesSource(names = ["PinReset", "CanIntro", "CanInput", "CanInputRetry", "PinInput"] , mode = SealedClassesSource.Mode.EXCLUDE, factoryClass = CanIdentStateFactory::class)
                fun back(oldState: CanStateMachine.State.Ident) = runTest {
                    val event = CanStateMachine.Event.Back

                    val stateMachine = CanStateMachine(oldState)
                    Assertions.assertEquals(stateMachine.state.value.second, oldState)

                    Assertions.assertThrows(IllegalArgumentException::class.java) { stateMachine.transition(event) }
                }
            }
        }

        @ParameterizedTest
        @SealedClassesSource(names = [] , mode = SealedClassesSource.Mode.EXCLUDE, factoryClass = CanPinManagementStateFactory::class)
        fun `invalidate from pin management state`(oldState: CanStateMachine.State.PinManagement) = runTest {
            val event = CanStateMachine.Event.Invalidate

            val stateMachine = CanStateMachine(oldState)
            Assertions.assertEquals(stateMachine.state.value.second, oldState)

            stateMachine.transition(event)
            Assertions.assertEquals(CanStateMachine.State.Invalid, stateMachine.state.value.second)
        }

        @ParameterizedTest
        @SealedClassesSource(names = [] , mode = SealedClassesSource.Mode.EXCLUDE, factoryClass = CanIdentStateFactory::class)
        fun `invalidate from ident state`(oldState: CanStateMachine.State.Ident) = runTest {
            val event = CanStateMachine.Event.Invalidate

            val stateMachine = CanStateMachine(oldState)
            Assertions.assertEquals(stateMachine.state.value.second, oldState)

            stateMachine.transition(event)
            Assertions.assertEquals(CanStateMachine.State.Invalid, stateMachine.state.value.second)
        }
    }

    class CanPinManagementStateFactory: DefaultTypeFactory() {
        override fun create(what: KClass<*>): CanStateMachine.State {
            return when (what) {
                CanStateMachine.State.Invalid::class -> CanStateMachine.State.Invalid

                CanStateMachine.State.PinManagement.Intro::class -> CanStateMachine.State.PinManagement.Intro("", {_, _, _ -> }, null)
                CanStateMachine.State.PinManagement.IdAlreadySetup::class -> CanStateMachine.State.PinManagement.IdAlreadySetup("", {_, _, _ -> }, null)
                CanStateMachine.State.PinManagement.PinReset::class -> CanStateMachine.State.PinManagement.PinReset("", {_, _, _ -> }, null)
                CanStateMachine.State.PinManagement.CanIntro::class -> CanStateMachine.State.PinManagement.CanIntro("", {_, _, _ -> }, null)
                CanStateMachine.State.PinManagement.CanInput::class -> CanStateMachine.State.PinManagement.CanInput("", {_, _, _ -> }, null)
                CanStateMachine.State.PinManagement.CanInputRetry::class -> CanStateMachine.State.PinManagement.CanInputRetry("", {_, _, _ -> }, "")
                CanStateMachine.State.PinManagement.PinInput::class -> CanStateMachine.State.PinManagement.PinInput("", {_, _, _ -> }, "")
                CanStateMachine.State.PinManagement.CanAndPinEntered::class -> CanStateMachine.State.PinManagement.CanAndPinEntered("", {_, _, _ -> }, "", "")

                else -> throw IllegalArgumentException()
            }
        }
    }

    class CanIdentStateFactory: DefaultTypeFactory() {
        override fun create(what: KClass<*>): CanStateMachine.State {
            return when (what) {
                CanStateMachine.State.Invalid::class -> CanStateMachine.State.Invalid

                CanStateMachine.State.Ident.Intro::class -> CanStateMachine.State.Ident.Intro({_, _ -> }, null)
                CanStateMachine.State.Ident.PinReset::class -> CanStateMachine.State.Ident.PinReset({_, _ -> }, null)
                CanStateMachine.State.Ident.CanIntro::class -> CanStateMachine.State.Ident.CanIntro({_, _ -> }, null)
                CanStateMachine.State.Ident.CanIntroWithoutFlowIntro::class -> CanStateMachine.State.Ident.CanIntroWithoutFlowIntro({_, _ -> }, null)
                CanStateMachine.State.Ident.CanInput::class -> CanStateMachine.State.Ident.CanInput({_, _ -> }, null)
                CanStateMachine.State.Ident.CanInputRetry::class -> CanStateMachine.State.Ident.CanInputRetry({_, _ -> }, "")
                CanStateMachine.State.Ident.PinInput::class -> CanStateMachine.State.Ident.PinInput({_, _ -> }, "")
                CanStateMachine.State.Ident.CanAndPinEntered::class -> CanStateMachine.State.Ident.CanAndPinEntered({_, _ -> }, "", "")

                else -> throw IllegalArgumentException()
            }
        }
    }
}
