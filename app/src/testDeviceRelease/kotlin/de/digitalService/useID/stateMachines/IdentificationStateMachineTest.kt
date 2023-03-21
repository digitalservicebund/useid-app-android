package de.digitalService.useID.stateMachines

import de.digitalService.useID.analytics.IssueTrackerManagerType
import de.digitalService.useID.flows.AttributeConfirmationCallback
import de.digitalService.useID.flows.IdentificationStateMachine
import de.digitalService.useID.flows.PinCallback
import de.digitalService.useID.flows.SetupStateMachine
import de.digitalService.useID.idCardInterface.EidAuthenticationRequest
import de.digitalService.useID.idCardInterface.IdCardInteractionException
import de.digitalService.useID.util.IdentificationStateFactory
import de.jodamob.junit5.DefaultTypeFactory
import de.jodamob.junit5.SealedClassesSource
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.openecard.mobile.activation.ActivationResultCode
import kotlin.reflect.KClass

@OptIn(ExperimentalCoroutinesApi::class)
class IdentificationStateMachineTest {
    private val pinCallback: PinCallback = mockk()
    private val request: EidAuthenticationRequest = mockk()
    private val attributeConfirmationCallback: AttributeConfirmationCallback = mockk()

    private val issueTrackerManager = mockk<IssueTrackerManagerType>(relaxUnitFun = true)

    private inline fun <reified NewState: IdentificationStateMachine.State> transition(initialState: IdentificationStateMachine.State, event: IdentificationStateMachine.Event, testScope: TestScope): NewState {
        val stateMachine = IdentificationStateMachine(initialState, issueTrackerManager)
        Assertions.assertEquals(stateMachine.state.value.second, initialState)

        stateMachine.transition(event)
        testScope.advanceUntilIdle()

        return stateMachine.state.value.second as? NewState ?: Assertions.fail("Unexpected new state.")
    }

    @ParameterizedTest
    @SealedClassesSource(names = [] , mode = SealedClassesSource.Mode.EXCLUDE, factoryClass = IdentificationStateFactory::class)
    fun `initialize backing down allowed`(oldState: IdentificationStateMachine.State) = runTest {
        val tcTokenUrl = "tcTokenUrl"

        val event = IdentificationStateMachine.Event.Initialize(true, tcTokenUrl)
        val newState: IdentificationStateMachine.State.StartIdentification = transition(oldState, event, this)

        Assertions.assertTrue(newState.backingDownAllowed)
        Assertions.assertEquals(tcTokenUrl, newState.tcTokenUrl)
    }

    @ParameterizedTest
    @SealedClassesSource(names = [] , mode = SealedClassesSource.Mode.EXCLUDE, factoryClass = IdentificationStateFactory::class)
    fun `initialize backing down not allowed`(oldState: IdentificationStateMachine.State) = runTest {
        val tcTokenUrl = "tcTokenUrl"

        val event = IdentificationStateMachine.Event.Initialize(false, tcTokenUrl)
        val newState: IdentificationStateMachine.State.StartIdentification = transition(oldState, event, this)

        Assertions.assertFalse(newState.backingDownAllowed)
        Assertions.assertEquals(tcTokenUrl, newState.tcTokenUrl)
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun `fetching metadata`(backingDownAllowed: Boolean) = runTest {
        val tcTokenUrl = "tcTokenUrl"

        val event = IdentificationStateMachine.Event.StartedFetchingMetadata
        val oldState = IdentificationStateMachine.State.StartIdentification(backingDownAllowed, tcTokenUrl)
        val newState: IdentificationStateMachine.State.FetchingMetadata = transition(oldState, event, this)

        Assertions.assertEquals(backingDownAllowed, newState.backingDownAllowed)
        Assertions.assertEquals(tcTokenUrl, newState.tcTokenUrl)
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun `request attribute confirmation`(backingDownAllowed: Boolean) = runTest {
        val tcTokenUrl = "tcTokenUrl"

        val event = IdentificationStateMachine.Event.FrameworkRequestsAttributeConfirmation(request, attributeConfirmationCallback)
        val oldState = IdentificationStateMachine.State.FetchingMetadata(backingDownAllowed, tcTokenUrl)
        val newState: IdentificationStateMachine.State.RequestAttributeConfirmation = transition(oldState, event, this)

        Assertions.assertEquals(backingDownAllowed, newState.backingDownAllowed)
        Assertions.assertEquals(request, newState.request)
        Assertions.assertEquals(attributeConfirmationCallback, newState.confirmationCallback)
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun `confirm attributes first time`(backingDownAllowed: Boolean) = runTest {
        val event = IdentificationStateMachine.Event.ConfirmAttributes
        val oldState = IdentificationStateMachine.State.RequestAttributeConfirmation(backingDownAllowed, request, attributeConfirmationCallback)
        val newState: IdentificationStateMachine.State.SubmitAttributeConfirmation = transition(oldState, event, this)

        Assertions.assertEquals(backingDownAllowed, newState.backingDownAllowed)
        Assertions.assertEquals(request, newState.request)
        Assertions.assertEquals(attributeConfirmationCallback, newState.confirmationCallback)
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun `confirm attributes again`(backingDownAllowed: Boolean) = runTest {
        val event = IdentificationStateMachine.Event.ConfirmAttributes
        val oldState = IdentificationStateMachine.State.RevisitAttributes(backingDownAllowed, request, pinCallback)
        val newState: IdentificationStateMachine.State.PinInput = transition(oldState, event, this)

        Assertions.assertEquals(backingDownAllowed, newState.backingDownAllowed)
        Assertions.assertEquals(request, newState.request)
        Assertions.assertEquals(pinCallback, newState.callback)
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun `framework requests PIN first time`(backingDownAllowed: Boolean) = runTest {
        val event = IdentificationStateMachine.Event.FrameworkRequestsPin(pinCallback)
        val oldState = IdentificationStateMachine.State.SubmitAttributeConfirmation(backingDownAllowed, request, attributeConfirmationCallback)
        val newState: IdentificationStateMachine.State.PinInput = transition(oldState, event, this)

        Assertions.assertEquals(backingDownAllowed, newState.backingDownAllowed)
        Assertions.assertEquals(request, newState.request)
        Assertions.assertEquals(pinCallback, newState.callback)
    }

    @Test
    fun `framework requests PIN later again`() = runTest {
        val event = IdentificationStateMachine.Event.FrameworkRequestsPin(pinCallback)
        val oldState = IdentificationStateMachine.State.WaitingForCardAttachment(null)
        val newState: IdentificationStateMachine.State.PinInputRetry = transition(oldState, event, this)

        Assertions.assertEquals(pinCallback, newState.callback)
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun `enter PIN first attempt`(backingDownAllowed: Boolean) = runTest {
        val pin = "123456"

        val event = IdentificationStateMachine.Event.EnterPin(pin)
        val oldState = IdentificationStateMachine.State.PinInput(backingDownAllowed, request, pinCallback)
        val newState: IdentificationStateMachine.State.PinEntered = transition(oldState, event, this)

        Assertions.assertEquals(pin, newState.pin)
        Assertions.assertFalse(newState.secondTime)
        Assertions.assertEquals(pinCallback, newState.callback)
    }

    @Test
    fun `enter PIN second attempt`() = runTest {
        val pin = "123456"

        val event = IdentificationStateMachine.Event.EnterPin(pin)
        val oldState = IdentificationStateMachine.State.PinInputRetry(pinCallback)
        val newState: IdentificationStateMachine.State.PinEntered = transition(oldState, event, this)

        Assertions.assertEquals(pin, newState.pin)
        Assertions.assertTrue(newState.secondTime)
        Assertions.assertEquals(pinCallback, newState.callback)
    }

    @Test
    fun `framework requests CAN short flow`() = runTest {
        val pin = "123456"

        val event = IdentificationStateMachine.Event.FrameworkRequestsCan
        val oldState = IdentificationStateMachine.State.WaitingForCardAttachment(pin)
        val newState: IdentificationStateMachine.State.CanRequested = transition(oldState, event, this)

        Assertions.assertEquals(pin, newState.pin)
    }

    @Test
    fun `framework requests CAN long flow`() = runTest {
        val event = IdentificationStateMachine.Event.FrameworkRequestsCan
        val oldState = IdentificationStateMachine.State.WaitingForCardAttachment(null)
        val newState: IdentificationStateMachine.State.CanRequested = transition(oldState, event, this)

        Assertions.assertNull(newState.pin)
    }

    @Test
    fun `requests card insertion after PIN entry first attempt`() = runTest {
        val pin = "123456"

        val event = IdentificationStateMachine.Event.RequestCardInsertion
        val oldState = IdentificationStateMachine.State.PinEntered(pin, false, pinCallback)
        val newState: IdentificationStateMachine.State.WaitingForCardAttachment = transition(oldState, event, this)

        Assertions.assertEquals(pin, newState.pin)
    }

    @Test
    fun `requests card insertion after PIN entry second attempt`() = runTest {
        val pin = "123456"

        val event = IdentificationStateMachine.Event.RequestCardInsertion
        val oldState = IdentificationStateMachine.State.PinEntered(pin, true, pinCallback)
        val newState: IdentificationStateMachine.State.WaitingForCardAttachment = transition(oldState, event, this)

        Assertions.assertNull(newState.pin)
    }

    @Test
    fun `requests card insertion after CAN request with PIN`() = runTest {
        val pin = "123456"

        val event = IdentificationStateMachine.Event.RequestCardInsertion
        val oldState = IdentificationStateMachine.State.CanRequested(pin)
        val newState: IdentificationStateMachine.State.WaitingForCardAttachment = transition(oldState, event, this)

        Assertions.assertEquals(pin, newState.pin)
    }

    @Test
    fun `requests card insertion after CAN request without PIN`() = runTest {
        val event = IdentificationStateMachine.Event.RequestCardInsertion
        val oldState = IdentificationStateMachine.State.CanRequested(null)
        val newState: IdentificationStateMachine.State.WaitingForCardAttachment = transition(oldState, event, this)

        Assertions.assertNull(newState.pin)
    }

    @Test
    fun `finish after happy path`() = runTest {
        val pin = "123456"
        val redirectUrl = "redirectUrl"

        val event = IdentificationStateMachine.Event.Finish(redirectUrl)
        val oldState = IdentificationStateMachine.State.WaitingForCardAttachment(pin)
        val newState: IdentificationStateMachine.State.Finished = transition(oldState, event, this)

        Assertions.assertEquals(redirectUrl, newState.redirectUrl)
    }

    @Test
    fun `finish after CAN flow`() = runTest {
        val pin = "123456"
        val redirectUrl = "redirectUrl"

        val event = IdentificationStateMachine.Event.Finish(redirectUrl)
        val oldState = IdentificationStateMachine.State.CanRequested(pin)
        val newState: IdentificationStateMachine.State.Finished = transition(oldState, event, this)

        Assertions.assertEquals(redirectUrl, newState.redirectUrl)
    }

    @Nested
    inner class Error {
        @ParameterizedTest
        @ValueSource(booleans = [true, false])
        fun `fetching metadata failed`(backingDownAllowed: Boolean) = runTest {
            val tcTokenUrl = "tcTokenUrl"
            val redirectUrl = "redirectUrl"

            val event = IdentificationStateMachine.Event.Error(IdCardInteractionException.ProcessFailed(ActivationResultCode.INTERRUPTED, redirectUrl, null))
            val oldState = IdentificationStateMachine.State.FetchingMetadata(backingDownAllowed, tcTokenUrl)
            val newState: IdentificationStateMachine.State.FetchingMetadataFailed = transition(oldState, event, this)

            Assertions.assertEquals(tcTokenUrl, newState.tcTokenUrl)
            Assertions.assertEquals(backingDownAllowed, newState.backingDownAllowed)
        }

        @Test
        fun `card deactivated`() = runTest {
                val event = IdentificationStateMachine.Event.Error(IdCardInteractionException.CardDeactivated)
                val oldState = IdentificationStateMachine.State.WaitingForCardAttachment(null)
                val newState: IdentificationStateMachine.State.CardDeactivated = transition(oldState, event, this)
        }

        @Test
        fun `card blocked`() = runTest {
            val event = IdentificationStateMachine.Event.Error(IdCardInteractionException.CardBlocked)
            val oldState = IdentificationStateMachine.State.WaitingForCardAttachment(null)
            val newState: IdentificationStateMachine.State.CardBlocked = transition(oldState, event, this)
        }

        @Test
        fun `process failed`() = runTest {
            val redirectUrl = "redirectUrl"

            val event = IdentificationStateMachine.Event.Error(IdCardInteractionException.ProcessFailed(ActivationResultCode.INTERRUPTED, redirectUrl, null))
            val oldState = IdentificationStateMachine.State.WaitingForCardAttachment(null)
            val newState: IdentificationStateMachine.State.CardUnreadable = transition(oldState, event, this)

            Assertions.assertEquals(redirectUrl, newState.redirectUrl)
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun `retry after error`(backingDownAllowed: Boolean) = runTest {
        val tcTokenUrl = "tcTokenUrl"

        val event = IdentificationStateMachine.Event.RetryAfterError
        val oldState = IdentificationStateMachine.State.FetchingMetadataFailed(backingDownAllowed, tcTokenUrl)
        val newState: IdentificationStateMachine.State.StartIdentification = transition(oldState, event, this)

        Assertions.assertEquals(tcTokenUrl, newState.tcTokenUrl)
        Assertions.assertEquals(backingDownAllowed, newState.backingDownAllowed)
    }

    @Nested
    inner class Back {
        @ParameterizedTest
        @ValueSource(booleans = [true, false])
        fun `from fetching metadata`(backingDownAllowed: Boolean) = runTest {
            val tcTokenUrl = "tcTokenUrl"

            val event = IdentificationStateMachine.Event.Back
            val oldState = IdentificationStateMachine.State.FetchingMetadata(backingDownAllowed, tcTokenUrl)
            val newState: IdentificationStateMachine.State.Invalid = transition(oldState, event, this)
        }

        @ParameterizedTest
        @ValueSource(booleans = [true, false])
        fun `from attribute confirmation request`(backingDownAllowed: Boolean) = runTest {
            val tcTokenUrl = "tcTokenUrl"

            val event = IdentificationStateMachine.Event.Back
            val oldState = IdentificationStateMachine.State.RequestAttributeConfirmation(backingDownAllowed, request, attributeConfirmationCallback)
            val newState: IdentificationStateMachine.State.Invalid = transition(oldState, event, this)
        }

        @ParameterizedTest
        @ValueSource(booleans = [true, false])
        fun `from PIN input`(backingDownAllowed: Boolean) = runTest {
            val event = IdentificationStateMachine.Event.Back
            val oldState = IdentificationStateMachine.State.PinInput(backingDownAllowed, request, pinCallback)
            val newState: IdentificationStateMachine.State.RevisitAttributes = transition(oldState, event, this)

            Assertions.assertEquals(backingDownAllowed, newState.backingDownAllowed)
            Assertions.assertEquals(request, newState.request)
            Assertions.assertEquals(pinCallback, newState.pinCallback)
        }
    }

    @ParameterizedTest
    @SealedClassesSource(names = [] , mode = SealedClassesSource.Mode.EXCLUDE, factoryClass = IdentificationStateFactory::class)
    fun invalidate(oldState: IdentificationStateMachine.State) = runTest {

        val event = IdentificationStateMachine.Event.Invalidate
        val stateMachine = IdentificationStateMachine(oldState, issueTrackerManager)
        Assertions.assertEquals(stateMachine.state.value.second, oldState)

        stateMachine.transition(event)
        Assertions.assertEquals(IdentificationStateMachine.State.Invalid, stateMachine.state.value.second)
    }

    @Nested
    inner class InvalidTransitions {
        @ParameterizedTest
        @SealedClassesSource(names = ["StartIdentification"] , mode = SealedClassesSource.Mode.EXCLUDE, factoryClass = IdentificationStateFactory::class)
        fun `started fetching metadata`(oldState: IdentificationStateMachine.State) = runTest {
            val event = IdentificationStateMachine.Event.StartedFetchingMetadata
            val stateMachine = IdentificationStateMachine(oldState, issueTrackerManager)
            Assertions.assertEquals(stateMachine.state.value.second, oldState)

            Assertions.assertThrows(IllegalArgumentException::class.java) { stateMachine.transition(event) }
        }

        @ParameterizedTest
        @SealedClassesSource(names = ["FetchingMetadata"] , mode = SealedClassesSource.Mode.EXCLUDE, factoryClass = IdentificationStateFactory::class)
        fun `framework requests attribute confirmation`(oldState: IdentificationStateMachine.State) = runTest {
            val event = IdentificationStateMachine.Event.FrameworkRequestsAttributeConfirmation(request, attributeConfirmationCallback)
            val stateMachine = IdentificationStateMachine(oldState, issueTrackerManager)
            Assertions.assertEquals(stateMachine.state.value.second, oldState)

            Assertions.assertThrows(IllegalArgumentException::class.java) { stateMachine.transition(event) }
        }

        @ParameterizedTest
        @SealedClassesSource(names = ["RequestAttributeConfirmation", "RevisitAttributes"] , mode = SealedClassesSource.Mode.EXCLUDE, factoryClass = IdentificationStateFactory::class)
        fun `confirm attributes`(oldState: IdentificationStateMachine.State) = runTest {
            val event = IdentificationStateMachine.Event.ConfirmAttributes
            val stateMachine = IdentificationStateMachine(oldState, issueTrackerManager)
            Assertions.assertEquals(stateMachine.state.value.second, oldState)

            Assertions.assertThrows(IllegalArgumentException::class.java) { stateMachine.transition(event) }
        }

        @ParameterizedTest
        @SealedClassesSource(names = ["SubmitAttributeConfirmation", "WaitingForCardAttachment"] , mode = SealedClassesSource.Mode.EXCLUDE, factoryClass = IdentificationStateFactory::class)
        fun `framework requests PIN`(oldState: IdentificationStateMachine.State) = runTest {
            val event = IdentificationStateMachine.Event.FrameworkRequestsPin(pinCallback)
            val stateMachine = IdentificationStateMachine(oldState, issueTrackerManager)
            Assertions.assertEquals(stateMachine.state.value.second, oldState)

            Assertions.assertThrows(IllegalArgumentException::class.java) { stateMachine.transition(event) }
        }

        @ParameterizedTest
        @SealedClassesSource(names = ["PinInput", "PinInputRetry"] , mode = SealedClassesSource.Mode.EXCLUDE, factoryClass = IdentificationStateFactory::class)
        fun `enter PIN`(oldState: IdentificationStateMachine.State) = runTest {
            val event = IdentificationStateMachine.Event.EnterPin("")
            val stateMachine = IdentificationStateMachine(oldState, issueTrackerManager)
            Assertions.assertEquals(stateMachine.state.value.second, oldState)

            Assertions.assertThrows(IllegalArgumentException::class.java) { stateMachine.transition(event) }
        }

        @ParameterizedTest
        @SealedClassesSource(names = ["WaitingForCardAttachment"] , mode = SealedClassesSource.Mode.EXCLUDE, factoryClass = IdentificationStateFactory::class)
        fun `framework requests CAN`(oldState: IdentificationStateMachine.State) = runTest {
            val event = IdentificationStateMachine.Event.FrameworkRequestsCan
            val stateMachine = IdentificationStateMachine(oldState, issueTrackerManager)
            Assertions.assertEquals(stateMachine.state.value.second, oldState)

            Assertions.assertThrows(IllegalArgumentException::class.java) { stateMachine.transition(event) }
        }

        @ParameterizedTest
        @SealedClassesSource(names = ["PinEntered", "CanRequested"] , mode = SealedClassesSource.Mode.EXCLUDE, factoryClass = IdentificationStateFactory::class)
        fun `request card insertion`(oldState: IdentificationStateMachine.State) = runTest {
            val event = IdentificationStateMachine.Event.RequestCardInsertion
            val stateMachine = IdentificationStateMachine(oldState, issueTrackerManager)
            Assertions.assertEquals(stateMachine.state.value.second, oldState)

            Assertions.assertThrows(IllegalArgumentException::class.java) { stateMachine.transition(event) }
        }

        @ParameterizedTest
        @SealedClassesSource(names = ["WaitingForCardAttachment", "CanRequested"] , mode = SealedClassesSource.Mode.EXCLUDE, factoryClass = IdentificationStateFactory::class)
        fun finish(oldState: IdentificationStateMachine.State) = runTest {
            val event = IdentificationStateMachine.Event.Finish("")
            val stateMachine = IdentificationStateMachine(oldState, issueTrackerManager)
            Assertions.assertEquals(stateMachine.state.value.second, oldState)

            Assertions.assertThrows(IllegalArgumentException::class.java) { stateMachine.transition(event) }
        }

        @ParameterizedTest
        @SealedClassesSource(names = ["FetchingMetadata", "WaitingForCardAttachment", "CanRequested"] , mode = SealedClassesSource.Mode.EXCLUDE, factoryClass = IdentificationStateFactory::class)
        fun error(oldState: IdentificationStateMachine.State) = runTest {
            val event = IdentificationStateMachine.Event.Error(IdCardInteractionException.CardDeactivated)
            val stateMachine = IdentificationStateMachine(oldState, issueTrackerManager)
            Assertions.assertEquals(stateMachine.state.value.second, oldState)

            Assertions.assertThrows(IllegalArgumentException::class.java) { stateMachine.transition(event) }
        }

        @ParameterizedTest
        @SealedClassesSource(names = ["FetchingMetadataFailed"] , mode = SealedClassesSource.Mode.EXCLUDE, factoryClass = IdentificationStateFactory::class)
        fun `retry after error`(oldState: IdentificationStateMachine.State) = runTest {
            val event = IdentificationStateMachine.Event.RetryAfterError
            val stateMachine = IdentificationStateMachine(oldState, issueTrackerManager)
            Assertions.assertEquals(stateMachine.state.value.second, oldState)

            Assertions.assertThrows(IllegalArgumentException::class.java) { stateMachine.transition(event) }
        }

        @ParameterizedTest
        @SealedClassesSource(names = ["FetchingMetadata", "RequestAttributeConfirmation", "PinInput"] , mode = SealedClassesSource.Mode.EXCLUDE, factoryClass = IdentificationStateFactory::class)
        fun back(oldState: IdentificationStateMachine.State) = runTest {
            val event = IdentificationStateMachine.Event.Back
            val stateMachine = IdentificationStateMachine(oldState, issueTrackerManager)
            Assertions.assertEquals(stateMachine.state.value.second, oldState)

            Assertions.assertThrows(IllegalArgumentException::class.java) { stateMachine.transition(event) }
        }
    }
}
