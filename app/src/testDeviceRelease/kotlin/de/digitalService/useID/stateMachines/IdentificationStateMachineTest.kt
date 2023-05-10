package de.digitalService.useID.stateMachines

import android.net.Uri
import de.digitalService.useID.analytics.IssueTrackerManagerType
import de.digitalService.useID.flows.IdentificationStateMachine
import de.digitalService.useID.idCardInterface.AuthenticationRequest
import de.digitalService.useID.idCardInterface.CertificateDescription
import de.digitalService.useID.idCardInterface.EidInteractionException
import de.digitalService.useID.util.IdentificationStateFactory
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

@OptIn(ExperimentalCoroutinesApi::class)
class IdentificationStateMachineTest {
    private val request: AuthenticationRequest = mockk()
    val certificateDescription = mockk<CertificateDescription>()

    private val issueTrackerManager = mockk<IssueTrackerManagerType>(relaxUnitFun = true)

    private inline fun <reified NewState : IdentificationStateMachine.State> transition(initialState: IdentificationStateMachine.State, event: IdentificationStateMachine.Event, testScope: TestScope): NewState {
        val stateMachine = IdentificationStateMachine(initialState, issueTrackerManager)
        Assertions.assertEquals(stateMachine.state.value.second, initialState)

        stateMachine.transition(event)
        testScope.advanceUntilIdle()

        return stateMachine.state.value.second as? NewState ?: Assertions.fail("Unexpected new state.")
    }

    @ParameterizedTest
    @SealedClassesSource(names = [], mode = SealedClassesSource.Mode.EXCLUDE, factoryClass = IdentificationStateFactory::class)
    fun `initialize backing down allowed`(oldState: IdentificationStateMachine.State) = runTest {
        val tcTokenUrl = mockk<Uri>()

        val event = IdentificationStateMachine.Event.Initialize(true, tcTokenUrl)
        val newState: IdentificationStateMachine.State.StartIdentification = transition(oldState, event, this)

        Assertions.assertTrue(newState.backingDownAllowed)
        Assertions.assertEquals(tcTokenUrl, newState.tcTokenUrl)
    }

    @ParameterizedTest
    @SealedClassesSource(names = [], mode = SealedClassesSource.Mode.EXCLUDE, factoryClass = IdentificationStateFactory::class)
    fun `initialize backing down not allowed`(oldState: IdentificationStateMachine.State) = runTest {
        val tcTokenUrl = mockk<Uri>()

        val event = IdentificationStateMachine.Event.Initialize(false, tcTokenUrl)
        val newState: IdentificationStateMachine.State.StartIdentification = transition(oldState, event, this)

        Assertions.assertFalse(newState.backingDownAllowed)
        Assertions.assertEquals(tcTokenUrl, newState.tcTokenUrl)
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun `fetching metadata`(backingDownAllowed: Boolean) = runTest {
        val tcTokenUrl = mockk<Uri>()

        val event = IdentificationStateMachine.Event.StartedFetchingMetadata
        val oldState = IdentificationStateMachine.State.StartIdentification(backingDownAllowed, tcTokenUrl)
        val newState: IdentificationStateMachine.State.FetchingMetadata = transition(oldState, event, this)

        Assertions.assertEquals(backingDownAllowed, newState.backingDownAllowed)
        Assertions.assertEquals(tcTokenUrl, newState.tcTokenUrl)
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun `request attribute confirmation`(backingDownAllowed: Boolean) = runTest {
        val tcTokenUrl = mockk<Uri>()

        val event = IdentificationStateMachine.Event.FrameworkRequestsAttributeConfirmation(request)
        val oldState = IdentificationStateMachine.State.FetchingMetadata(backingDownAllowed, tcTokenUrl)
        val newState: IdentificationStateMachine.State.RequestCertificate = transition(oldState, event, this)

        Assertions.assertEquals(backingDownAllowed, newState.backingDownAllowed)
        Assertions.assertEquals(request, newState.request)
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun `certificate description received`(backingDownAllowed: Boolean) = runTest {
        val event = IdentificationStateMachine.Event.CertificateDescriptionReceived(certificateDescription)
        val oldState = IdentificationStateMachine.State.RequestCertificate(backingDownAllowed, request)
        val newState: IdentificationStateMachine.State.CertificateDescriptionReceived = transition(oldState, event, this)

        Assertions.assertEquals(backingDownAllowed, newState.backingDownAllowed)
        Assertions.assertEquals(certificateDescription, newState.certificateDescription)
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun `confirm attributes`(backingDownAllowed: Boolean) = runTest {
        val event = IdentificationStateMachine.Event.ConfirmAttributes
        val oldState = IdentificationStateMachine.State.CertificateDescriptionReceived(backingDownAllowed, request, certificateDescription)
        val newState: IdentificationStateMachine.State.PinInput = transition(oldState, event, this)

        Assertions.assertEquals(backingDownAllowed, newState.backingDownAllowed)
        Assertions.assertEquals(request, newState.authenticationRequest)
    }

    @Test
    fun `framework requests PIN first attempt from PIN entered`() = runTest {
        val pin = "123456"
        val event = IdentificationStateMachine.Event.FrameworkRequestsPin(true)
        val oldState = IdentificationStateMachine.State.PinEntered(pin, true)
        val newState: IdentificationStateMachine.State.PinRequested = transition(oldState, event, this)

        Assertions.assertEquals(pin, newState.pin)
    }

    @Test
    fun `framework requests PIN first attempt from PIN requested`() = runTest {
        val pin = "123456"
        val event = IdentificationStateMachine.Event.FrameworkRequestsPin(true)
        val oldState = IdentificationStateMachine.State.PinRequested(pin)
        val newState: IdentificationStateMachine.State.PinRequested = transition(oldState, event, this)

        Assertions.assertEquals(pin, newState.pin)
    }

    @Test
    fun `framework requests PIN later again`() = runTest {
        val pin = "123456"
        val event = IdentificationStateMachine.Event.FrameworkRequestsPin(false)
        val oldState = IdentificationStateMachine.State.PinRequested(pin)
        val newState: IdentificationStateMachine.State.PinInputRetry = transition(oldState, event, this)
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun `enter PIN first time`(backingDownAllowed: Boolean) = runTest {
        val pin = "123456"

        val event = IdentificationStateMachine.Event.EnterPin(pin)
        val oldState = IdentificationStateMachine.State.PinInput(backingDownAllowed, request, certificateDescription)
        val newState: IdentificationStateMachine.State.PinEntered = transition(oldState, event, this)

        Assertions.assertEquals(pin, newState.pin)
        Assertions.assertTrue(newState.firstTime)
    }

    @Test
    fun `enter PIN another time`() = runTest {
        val pin = "123456"

        val event = IdentificationStateMachine.Event.EnterPin(pin)
        val oldState = IdentificationStateMachine.State.PinInputRetry
        val newState: IdentificationStateMachine.State.PinEntered = transition(oldState, event, this)

        Assertions.assertEquals(pin, newState.pin)
        Assertions.assertFalse(newState.firstTime)
    }

    @Test
    fun `framework requests CAN short flow from PIN entered`() = runTest {
        val pin = "123456"

        val event = IdentificationStateMachine.Event.FrameworkRequestsCan
        val oldState = IdentificationStateMachine.State.PinEntered(pin, true)
        val newState: IdentificationStateMachine.State.CanRequested = transition(oldState, event, this)

        Assertions.assertEquals(pin, newState.pin)
    }

    @Test
    fun `framework requests CAN short flow from PIN requested`() = runTest {
        val pin = "123456"

        val event = IdentificationStateMachine.Event.FrameworkRequestsCan
        val oldState = IdentificationStateMachine.State.PinRequested(pin)
        val newState: IdentificationStateMachine.State.CanRequested = transition(oldState, event, this)

        Assertions.assertEquals(pin, newState.pin)
    }

    @Test
    fun `framework requests CAN long flow`() = runTest {
        val pin = "123456"

        val event = IdentificationStateMachine.Event.FrameworkRequestsCan
        val oldState = IdentificationStateMachine.State.PinEntered(pin, false)
        val newState: IdentificationStateMachine.State.CanRequested = transition(oldState, event, this)

        Assertions.assertNull(newState.pin)
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun `finish after happy path from PIN entered`(firstTime: Boolean) = runTest {
        val pin = "123456"
        val redirectUrl = "redirectUrl"

        val event = IdentificationStateMachine.Event.Finish(redirectUrl)
        val oldState = IdentificationStateMachine.State.PinEntered(pin, firstTime)
        val newState: IdentificationStateMachine.State.Finished = transition(oldState, event, this)

        Assertions.assertEquals(redirectUrl, newState.redirectUrl)
    }

    @Test
    fun `finish after happy path from PIN requested`() = runTest {
        val pin = "123456"
        val redirectUrl = "redirectUrl"

        val event = IdentificationStateMachine.Event.Finish(redirectUrl)
        val oldState = IdentificationStateMachine.State.PinRequested(pin)
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
            val tcTokenUrl = mockk<Uri>()
            val redirectUrl = "redirectUrl"

            val event = IdentificationStateMachine.Event.Error(EidInteractionException.ProcessFailed(redirectUrl))
            val oldState = IdentificationStateMachine.State.FetchingMetadata(backingDownAllowed, tcTokenUrl)
            val newState: IdentificationStateMachine.State.FetchingMetadataFailed = transition(oldState, event, this)

            Assertions.assertEquals(tcTokenUrl, newState.tcTokenUrl)
            Assertions.assertEquals(backingDownAllowed, newState.backingDownAllowed)
        }

        @ParameterizedTest
        @SealedClassesSource(names = ["FetchingMetadata"], mode = SealedClassesSource.Mode.EXCLUDE, factoryClass = IdentificationStateFactory::class)
        fun `card deactivated`(oldState: IdentificationStateMachine.State) = runTest {
            val event = IdentificationStateMachine.Event.Error(EidInteractionException.CardDeactivated)
            val newState: IdentificationStateMachine.State.CardDeactivated = transition(oldState, event, this)
        }

        @ParameterizedTest
        @SealedClassesSource(names = ["FetchingMetadata"], mode = SealedClassesSource.Mode.EXCLUDE, factoryClass = IdentificationStateFactory::class)
        fun `card blocked`(oldState: IdentificationStateMachine.State) = runTest {
            val event = IdentificationStateMachine.Event.Error(EidInteractionException.CardBlocked)
            val newState: IdentificationStateMachine.State.CardBlocked = transition(oldState, event, this)
        }

        @ParameterizedTest
        @SealedClassesSource(names = ["FetchingMetadata", "CardBlocked", "CardDeactivated"], mode = SealedClassesSource.Mode.EXCLUDE, factoryClass = IdentificationStateFactory::class)
        fun `process failed`(oldState: IdentificationStateMachine.State) = runTest {
            val redirectUrl = "redirectUrl"

            val event = IdentificationStateMachine.Event.Error(EidInteractionException.ProcessFailed(redirectUrl))
            val newState: IdentificationStateMachine.State.CardUnreadable = transition(oldState, event, this)

            Assertions.assertEquals(redirectUrl, newState.redirectUrl)
        }

        @Test
        fun `process failed after card blocked`() = runTest {
            val redirectUrl = "redirectUrl"

            val event = IdentificationStateMachine.Event.Error(EidInteractionException.ProcessFailed(redirectUrl))
            val oldState = IdentificationStateMachine.State.CardBlocked
            val newState: IdentificationStateMachine.State.CardBlocked = transition(oldState, event, this)
        }

        @Test
        fun `process failed after card deactivated`() = runTest {
            val redirectUrl = "redirectUrl"

            val event = IdentificationStateMachine.Event.Error(EidInteractionException.ProcessFailed(redirectUrl))
            val oldState = IdentificationStateMachine.State.CardBlocked
            val newState: IdentificationStateMachine.State.CardBlocked = transition(oldState, event, this)
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun `retry after error`(backingDownAllowed: Boolean) = runTest {
        val tcTokenUrl = mockk<Uri>()

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
            val tcTokenUrl = mockk<Uri>()

            val event = IdentificationStateMachine.Event.Back
            val oldState = IdentificationStateMachine.State.FetchingMetadata(backingDownAllowed, tcTokenUrl)
            val newState: IdentificationStateMachine.State.Invalid = transition(oldState, event, this)
        }

        @ParameterizedTest
        @ValueSource(booleans = [true, false])
        fun `from request certificate`(backingDownAllowed: Boolean) = runTest {
            val event = IdentificationStateMachine.Event.Back
            val oldState = IdentificationStateMachine.State.RequestCertificate(backingDownAllowed, request)
            val newState: IdentificationStateMachine.State.Invalid = transition(oldState, event, this)
        }

        @ParameterizedTest
        @ValueSource(booleans = [true, false])
        fun `from certificate received`(backingDownAllowed: Boolean) = runTest {
            val event = IdentificationStateMachine.Event.Back
            val oldState = IdentificationStateMachine.State.CertificateDescriptionReceived(backingDownAllowed, request, certificateDescription)
            val newState: IdentificationStateMachine.State.Invalid = transition(oldState, event, this)
        }

        @ParameterizedTest
        @ValueSource(booleans = [true, false])
        fun `from PIN input`(backingDownAllowed: Boolean) = runTest {
            val event = IdentificationStateMachine.Event.Back
            val oldState = IdentificationStateMachine.State.PinInput(backingDownAllowed, request, certificateDescription)
            val newState: IdentificationStateMachine.State.CertificateDescriptionReceived = transition(oldState, event, this)

            Assertions.assertEquals(backingDownAllowed, newState.backingDownAllowed)
            Assertions.assertEquals(request, newState.authenticationRequest)
            Assertions.assertEquals(certificateDescription, newState.certificateDescription)
        }
    }

    @ParameterizedTest
    @SealedClassesSource(names = [], mode = SealedClassesSource.Mode.EXCLUDE, factoryClass = IdentificationStateFactory::class)
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
        @SealedClassesSource(names = ["StartIdentification"], mode = SealedClassesSource.Mode.EXCLUDE, factoryClass = IdentificationStateFactory::class)
        fun `started fetching metadata`(oldState: IdentificationStateMachine.State) = runTest {
            val event = IdentificationStateMachine.Event.StartedFetchingMetadata
            val stateMachine = IdentificationStateMachine(oldState, issueTrackerManager)
            Assertions.assertEquals(stateMachine.state.value.second, oldState)

            Assertions.assertThrows(IllegalArgumentException::class.java) { stateMachine.transition(event) }
        }

        @ParameterizedTest
        @SealedClassesSource(names = ["FetchingMetadata"], mode = SealedClassesSource.Mode.EXCLUDE, factoryClass = IdentificationStateFactory::class)
        fun `framework requests attribute confirmation`(oldState: IdentificationStateMachine.State) = runTest {
            val event = IdentificationStateMachine.Event.FrameworkRequestsAttributeConfirmation(request)
            val stateMachine = IdentificationStateMachine(oldState, issueTrackerManager)
            Assertions.assertEquals(stateMachine.state.value.second, oldState)

            Assertions.assertThrows(IllegalArgumentException::class.java) { stateMachine.transition(event) }
        }

        @ParameterizedTest
        @SealedClassesSource(names = ["CertificateDescriptionReceived"], mode = SealedClassesSource.Mode.EXCLUDE, factoryClass = IdentificationStateFactory::class)
        fun `confirm attributes`(oldState: IdentificationStateMachine.State) = runTest {
            val event = IdentificationStateMachine.Event.ConfirmAttributes
            val stateMachine = IdentificationStateMachine(oldState, issueTrackerManager)
            Assertions.assertEquals(stateMachine.state.value.second, oldState)

            Assertions.assertThrows(IllegalArgumentException::class.java) { stateMachine.transition(event) }
        }

        @ParameterizedTest
        @SealedClassesSource(names = ["PinRequested", "PinEntered"], mode = SealedClassesSource.Mode.EXCLUDE, factoryClass = IdentificationStateFactory::class)
        fun `framework requests PIN first attempt`(oldState: IdentificationStateMachine.State) = runTest {
            val event = IdentificationStateMachine.Event.FrameworkRequestsPin(true)
            val stateMachine = IdentificationStateMachine(oldState, issueTrackerManager)
            Assertions.assertEquals(stateMachine.state.value.second, oldState)

            Assertions.assertThrows(IllegalArgumentException::class.java) { stateMachine.transition(event) }
        }

        @ParameterizedTest
        @SealedClassesSource(names = ["PinRequested", "PinEntered"], mode = SealedClassesSource.Mode.EXCLUDE, factoryClass = IdentificationStateFactory::class)
        fun `framework requests PIN later attempt`(oldState: IdentificationStateMachine.State) = runTest {
            val event = IdentificationStateMachine.Event.FrameworkRequestsPin(false)
            val stateMachine = IdentificationStateMachine(oldState, issueTrackerManager)
            Assertions.assertEquals(stateMachine.state.value.second, oldState)

            Assertions.assertThrows(IllegalArgumentException::class.java) { stateMachine.transition(event) }
        }

        @ParameterizedTest
        @SealedClassesSource(names = ["PinInput", "PinInputRetry"], mode = SealedClassesSource.Mode.EXCLUDE, factoryClass = IdentificationStateFactory::class)
        fun `enter PIN`(oldState: IdentificationStateMachine.State) = runTest {
            val event = IdentificationStateMachine.Event.EnterPin("")
            val stateMachine = IdentificationStateMachine(oldState, issueTrackerManager)
            Assertions.assertEquals(stateMachine.state.value.second, oldState)

            Assertions.assertThrows(IllegalArgumentException::class.java) { stateMachine.transition(event) }
        }

        @ParameterizedTest
        @SealedClassesSource(names = ["PinEntered", "PinRequested"], mode = SealedClassesSource.Mode.EXCLUDE, factoryClass = IdentificationStateFactory::class)
        fun `framework requests CAN`(oldState: IdentificationStateMachine.State) = runTest {
            val event = IdentificationStateMachine.Event.FrameworkRequestsCan
            val stateMachine = IdentificationStateMachine(oldState, issueTrackerManager)
            Assertions.assertEquals(stateMachine.state.value.second, oldState)

            Assertions.assertThrows(IllegalArgumentException::class.java) { stateMachine.transition(event) }
        }

        @ParameterizedTest
        @SealedClassesSource(names = ["PinEntered", "CanRequested", "PinRequested"], mode = SealedClassesSource.Mode.EXCLUDE, factoryClass = IdentificationStateFactory::class)
        fun finish(oldState: IdentificationStateMachine.State) = runTest {
            val event = IdentificationStateMachine.Event.Finish("")
            val stateMachine = IdentificationStateMachine(oldState, issueTrackerManager)
            Assertions.assertEquals(stateMachine.state.value.second, oldState)

            Assertions.assertThrows(IllegalArgumentException::class.java) { stateMachine.transition(event) }
        }

        @ParameterizedTest
        @SealedClassesSource(names = ["FetchingMetadataFailed"], mode = SealedClassesSource.Mode.EXCLUDE, factoryClass = IdentificationStateFactory::class)
        fun `retry after error`(oldState: IdentificationStateMachine.State) = runTest {
            val event = IdentificationStateMachine.Event.RetryAfterError
            val stateMachine = IdentificationStateMachine(oldState, issueTrackerManager)
            Assertions.assertEquals(stateMachine.state.value.second, oldState)

            Assertions.assertThrows(IllegalArgumentException::class.java) { stateMachine.transition(event) }
        }

        @ParameterizedTest
        @SealedClassesSource(names = ["FetchingMetadata", "RequestCertificate", "CertificateDescriptionReceived", "PinInput"], mode = SealedClassesSource.Mode.EXCLUDE, factoryClass = IdentificationStateFactory::class)
        fun back(oldState: IdentificationStateMachine.State) = runTest {
            val event = IdentificationStateMachine.Event.Back
            val stateMachine = IdentificationStateMachine(oldState, issueTrackerManager)
            Assertions.assertEquals(stateMachine.state.value.second, oldState)

            Assertions.assertThrows(IllegalArgumentException::class.java) { stateMachine.transition(event) }
        }
    }
}
