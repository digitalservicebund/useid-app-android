package de.digitalService.useID.stateMachines

import de.digitalService.useID.analytics.IssueTrackerManagerType
import de.digitalService.useID.flows.SetupStateMachine
import de.digitalService.useID.util.SetupStateFactory
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

@OptIn(ExperimentalCoroutinesApi::class)
class SetupStateMachineTest {

    private val issueTrackerManager = mockk<IssueTrackerManagerType>(relaxUnitFun = true)
    private inline fun <reified NewState : SetupStateMachine.State> transition(initialState: SetupStateMachine.State, event: SetupStateMachine.Event, testScope: TestScope): NewState {
        val stateMachine = SetupStateMachine(initialState, issueTrackerManager)
        Assertions.assertEquals(stateMachine.state.value.second, initialState)

        stateMachine.transition(event)
        testScope.advanceUntilIdle()

        return stateMachine.state.value.second as? NewState ?: Assertions.fail("Unexpected new state.")
    }

    @Test
    fun `offer setup from invalid state with url`() = runTest {
        val tcTokenUrl = "tcTokenUrl"

        val event = SetupStateMachine.Event.OfferSetup(tcTokenUrl)
        val oldState = SetupStateMachine.State.Invalid
        val newState: SetupStateMachine.State.Intro = transition(oldState, event, this)

        Assertions.assertEquals(tcTokenUrl, newState.tcTokenUrl)
    }

    @Test
    fun `offer setup from invalid state without url`() = runTest {
        val event = SetupStateMachine.Event.OfferSetup(null)
        val oldState = SetupStateMachine.State.Invalid
        val newState: SetupStateMachine.State.Intro = transition(oldState, event, this)

        Assertions.assertNull(newState.tcTokenUrl)
    }

    @Test
    fun `skip setup in intro with url`() = runTest {
        val tcTokenUrl = "tcTokenUrl"

        val event = SetupStateMachine.Event.SkipSetup
        val oldState = SetupStateMachine.State.Intro(tcTokenUrl)
        val newState: SetupStateMachine.State.SkippingToIdentRequested = transition(oldState, event, this)

        Assertions.assertEquals(tcTokenUrl, newState.tcTokenUrl)
    }

    @Test
    fun `skip setup in intro without url`() = runTest {
        val event = SetupStateMachine.Event.SkipSetup
        val oldState = SetupStateMachine.State.Intro(null)
        val newState: SetupStateMachine.State.AlreadySetUpConfirmation = transition(oldState, event, this)
    }

    @Test
    fun `confirm already set up`() = runTest {
        val event = SetupStateMachine.Event.ConfirmAlreadySetUp
        val oldState = SetupStateMachine.State.AlreadySetUpConfirmation
        val newState: SetupStateMachine.State.SetupFinished = transition(oldState, event, this)
    }

    @Test
    fun `skip setup after intro`() = runTest {
        val tcTokenUrl = "tcTokenUrl"

        val event = SetupStateMachine.Event.SkipSetup
        val oldState = SetupStateMachine.State.SkippingToIdentRequested(tcTokenUrl)
        val newState: SetupStateMachine.State.SkippingToIdentRequested = transition(oldState, event, this)

        Assertions.assertEquals(tcTokenUrl, newState.tcTokenUrl)
    }

    @Test
    fun `start setup with url`() = runTest {
        val tcTokenUrl = "tcTokenUrl"

        val event = SetupStateMachine.Event.StartSetup
        val oldState = SetupStateMachine.State.Intro(tcTokenUrl)
        val newState: SetupStateMachine.State.StartSetup = transition(oldState, event, this)

        Assertions.assertEquals(tcTokenUrl, newState.tcTokenUrl)
    }

    @Test
    fun `start setup with url after previously skipped setup`() = runTest {
        val tcTokenUrl = "tcTokenUrl"

        val event = SetupStateMachine.Event.StartSetup
        val oldState = SetupStateMachine.State.SkippingToIdentRequested(tcTokenUrl)
        val newState: SetupStateMachine.State.StartSetup = transition(oldState, event, this)

        Assertions.assertEquals(tcTokenUrl, newState.tcTokenUrl)
    }

    @Test
    fun `start setup without url`() = runTest {
        val event = SetupStateMachine.Event.StartSetup
        val oldState = SetupStateMachine.State.Intro(null)
        val newState: SetupStateMachine.State.StartSetup = transition(oldState, event, this)

        Assertions.assertNull(newState.tcTokenUrl)
    }

    @Test
    fun `PIN reset with url`() = runTest {
        val tcTokenUrl = "tcTokenUrl"

        val event = SetupStateMachine.Event.ResetPin
        val oldState = SetupStateMachine.State.StartSetup(tcTokenUrl)
        val newState: SetupStateMachine.State.PinReset = transition(oldState, event, this)

        Assertions.assertEquals(tcTokenUrl, newState.tcTokenUrl)
    }

    @Test
    fun `PIN reset without url`() = runTest {
        val event = SetupStateMachine.Event.ResetPin
        val oldState = SetupStateMachine.State.StartSetup(null)
        val newState: SetupStateMachine.State.PinReset = transition(oldState, event, this)

        Assertions.assertNull(newState.tcTokenUrl)
    }

    @Test
    fun `start PIN management with url`() = runTest {
        val tcTokenUrl = "tcTokenUrl"

        val event = SetupStateMachine.Event.StartPinManagement
        val oldState = SetupStateMachine.State.StartSetup(tcTokenUrl)
        val newState: SetupStateMachine.State.PinManagement = transition(oldState, event, this)

        Assertions.assertEquals(tcTokenUrl, newState.tcTokenUrl)
    }

    @Test
    fun `start PIN management without url`() = runTest {
        val event = SetupStateMachine.Event.StartPinManagement
        val oldState = SetupStateMachine.State.StartSetup(null)
        val newState: SetupStateMachine.State.PinManagement = transition(oldState, event, this)

        Assertions.assertNull(newState.tcTokenUrl)
    }

    @Test
    fun `finish PIN management with url`() = runTest {
        val tcTokenUrl = "tcTokenUrl"

        val event = SetupStateMachine.Event.FinishPinManagement
        val oldState = SetupStateMachine.State.PinManagement(tcTokenUrl)
        val newState: SetupStateMachine.State.PinManagementFinished = transition(oldState, event, this)

        Assertions.assertEquals(tcTokenUrl, newState.tcTokenUrl)
    }

    @Test
    fun `finish PIN management without url`() = runTest {
        val event = SetupStateMachine.Event.FinishPinManagement
        val oldState = SetupStateMachine.State.PinManagement(null)
        val newState: SetupStateMachine.State.PinManagementFinished = transition(oldState, event, this)

        Assertions.assertNull(newState.tcTokenUrl)
    }

    @Test
    fun `confirm finish with url`() = runTest {
        val tcTokenUrl = "tcTokenUrl"

        val event = SetupStateMachine.Event.ConfirmFinish
        val oldState = SetupStateMachine.State.PinManagementFinished(tcTokenUrl)
        val newState: SetupStateMachine.State.IdentAfterFinishedSetupRequested = transition(oldState, event, this)

        Assertions.assertEquals(tcTokenUrl, newState.tcTokenUrl)
    }

    @Test
    fun `confirm finish without url`() = runTest {
        val event = SetupStateMachine.Event.ConfirmFinish
        val oldState = SetupStateMachine.State.PinManagementFinished(null)
        val newState: SetupStateMachine.State.SetupFinished = transition(oldState, event, this)
    }

    @Test
    fun `subsequent flow backed down with url`() = runTest {
        val tcTokenUrl = "tcTokenUrl"

        val event = SetupStateMachine.Event.SubsequentFlowBackedDown
        val oldState = SetupStateMachine.State.PinManagement(tcTokenUrl)
        val newState: SetupStateMachine.State.StartSetup = transition(oldState, event, this)

        Assertions.assertEquals(tcTokenUrl, newState.tcTokenUrl)
    }

    @Test
    fun `subsequent flow backed down without url`() = runTest {
        val event = SetupStateMachine.Event.SubsequentFlowBackedDown
        val oldState = SetupStateMachine.State.PinManagement(null)
        val newState: SetupStateMachine.State.StartSetup = transition(oldState, event, this)

        Assertions.assertNull(newState.tcTokenUrl)
    }

    @Nested
    inner class Back {
        @Test
        fun `from start setup with url`() = runTest {
            val tcTokenUrl = "tcTokenUrl"

            val event = SetupStateMachine.Event.Back
            val oldState = SetupStateMachine.State.StartSetup(tcTokenUrl)
            val newState: SetupStateMachine.State.Intro = transition(oldState, event, this)

            Assertions.assertEquals(tcTokenUrl, newState.tcTokenUrl)
        }

        @Test
        fun `from start setup without url`() = runTest {
            val event = SetupStateMachine.Event.Back
            val oldState = SetupStateMachine.State.StartSetup(null)
            val newState: SetupStateMachine.State.Intro = transition(oldState, event, this)

            Assertions.assertNull(newState.tcTokenUrl)
        }

        @Test
        fun `from pin reset with url`() = runTest {
            val tcTokenUrl = "tcTokenUrl"

            val event = SetupStateMachine.Event.Back
            val oldState = SetupStateMachine.State.PinReset(tcTokenUrl)
            val newState: SetupStateMachine.State.StartSetup = transition(oldState, event, this)

            Assertions.assertEquals(tcTokenUrl, newState.tcTokenUrl)
        }

        @Test
        fun `from pin reset without url`() = runTest {
            val event = SetupStateMachine.Event.Back
            val oldState = SetupStateMachine.State.PinReset(null)
            val newState: SetupStateMachine.State.StartSetup = transition(oldState, event, this)

            Assertions.assertNull(newState.tcTokenUrl)
        }
    }

    @Test
    fun `from start setup with url`() = runTest {
        val tcTokenUrl = "tcTokenUrl"

        val event = SetupStateMachine.Event.Back
        val oldState = SetupStateMachine.State.StartSetup(tcTokenUrl)
        val newState: SetupStateMachine.State.Intro = transition(oldState, event, this)

        Assertions.assertEquals(tcTokenUrl, newState.tcTokenUrl)
    }

    @ParameterizedTest
    @SealedClassesSource(names = [], mode = SealedClassesSource.Mode.EXCLUDE, factoryClass = SetupStateFactory::class)
    fun invalidate(oldState: SetupStateMachine.State) = runTest {
        val event = SetupStateMachine.Event.Invalidate

        val stateMachine = SetupStateMachine(oldState, issueTrackerManager)
        Assertions.assertEquals(stateMachine.state.value.second, oldState)

        stateMachine.transition(event)
        Assertions.assertEquals(SetupStateMachine.State.Invalid, stateMachine.state.value.second)
    }

    @Nested
    inner class InvalidTransitions {
        @ParameterizedTest
        @SealedClassesSource(names = ["Invalid"], mode = SealedClassesSource.Mode.EXCLUDE, factoryClass = SetupStateFactory::class)
        fun `offer setup`(oldState: SetupStateMachine.State) = runTest {
            val event = SetupStateMachine.Event.OfferSetup(null)

            val stateMachine = SetupStateMachine(oldState, issueTrackerManager)
            Assertions.assertEquals(stateMachine.state.value.second, oldState)

            Assertions.assertThrows(IllegalArgumentException::class.java) { stateMachine.transition(event) }
        }

        @ParameterizedTest
        @SealedClassesSource(names = ["Intro", "SkippingToIdentRequested"], mode = SealedClassesSource.Mode.EXCLUDE, factoryClass = SetupStateFactory::class)
        fun `skip setup`(oldState: SetupStateMachine.State) = runTest {
            val event = SetupStateMachine.Event.SkipSetup

            val stateMachine = SetupStateMachine(oldState, issueTrackerManager)
            Assertions.assertEquals(stateMachine.state.value.second, oldState)

            Assertions.assertThrows(IllegalArgumentException::class.java) { stateMachine.transition(event) }
        }

        @ParameterizedTest
        @SealedClassesSource(names = ["AlreadySetUpConfirmation"], mode = SealedClassesSource.Mode.EXCLUDE, factoryClass = SetupStateFactory::class)
        fun `confirm already set up`(oldState: SetupStateMachine.State) = runTest {
            val event = SetupStateMachine.Event.ConfirmAlreadySetUp

            val stateMachine = SetupStateMachine(oldState, issueTrackerManager)
            Assertions.assertEquals(stateMachine.state.value.second, oldState)

            Assertions.assertThrows(IllegalArgumentException::class.java) { stateMachine.transition(event) }
        }

        @ParameterizedTest
        @SealedClassesSource(names = ["Intro", "SkippingToIdentRequested"], mode = SealedClassesSource.Mode.EXCLUDE, factoryClass = SetupStateFactory::class)
        fun `start setup`(oldState: SetupStateMachine.State) = runTest {
            val event = SetupStateMachine.Event.StartSetup

            val stateMachine = SetupStateMachine(oldState, issueTrackerManager)
            Assertions.assertEquals(stateMachine.state.value.second, oldState)

            Assertions.assertThrows(IllegalArgumentException::class.java) { stateMachine.transition(event) }
        }

        @ParameterizedTest
        @SealedClassesSource(names = ["StartSetup"], mode = SealedClassesSource.Mode.EXCLUDE, factoryClass = SetupStateFactory::class)
        fun `PIN reset`(oldState: SetupStateMachine.State) = runTest {
            val event = SetupStateMachine.Event.ResetPin

            val stateMachine = SetupStateMachine(oldState, issueTrackerManager)
            Assertions.assertEquals(stateMachine.state.value.second, oldState)

            Assertions.assertThrows(IllegalArgumentException::class.java) { stateMachine.transition(event) }
        }

        @ParameterizedTest
        @SealedClassesSource(names = ["PinManagement", "PinManagementFinished"], mode = SealedClassesSource.Mode.EXCLUDE, factoryClass = SetupStateFactory::class)
        fun `finish PIN management`(oldState: SetupStateMachine.State) = runTest {
            val event = SetupStateMachine.Event.FinishPinManagement

            val stateMachine = SetupStateMachine(oldState, issueTrackerManager)
            Assertions.assertEquals(stateMachine.state.value.second, oldState)

            Assertions.assertThrows(IllegalArgumentException::class.java) { stateMachine.transition(event) }
        }

        @ParameterizedTest
        @SealedClassesSource(names = ["PinManagementFinished"], mode = SealedClassesSource.Mode.EXCLUDE, factoryClass = SetupStateFactory::class)
        fun `confirm finish`(oldState: SetupStateMachine.State) = runTest {
            val event = SetupStateMachine.Event.ConfirmFinish

            val stateMachine = SetupStateMachine(oldState, issueTrackerManager)
            Assertions.assertEquals(stateMachine.state.value.second, oldState)

            Assertions.assertThrows(IllegalArgumentException::class.java) { stateMachine.transition(event) }
        }

        @ParameterizedTest
        @SealedClassesSource(names = ["PinManagement", "PinManagementFinished"], mode = SealedClassesSource.Mode.EXCLUDE, factoryClass = SetupStateFactory::class)
        fun `subsequent flow backed down`(oldState: SetupStateMachine.State) = runTest {
            val event = SetupStateMachine.Event.SubsequentFlowBackedDown

            val stateMachine = SetupStateMachine(oldState, issueTrackerManager)
            Assertions.assertEquals(stateMachine.state.value.second, oldState)

            Assertions.assertThrows(IllegalArgumentException::class.java) { stateMachine.transition(event) }
        }

        @ParameterizedTest
        @SealedClassesSource(names = ["StartSetup", "PinReset", "AlreadySetUpConfirmation"], mode = SealedClassesSource.Mode.EXCLUDE, factoryClass = SetupStateFactory::class)
        fun back(oldState: SetupStateMachine.State) = runTest {
            val event = SetupStateMachine.Event.Back

            val stateMachine = SetupStateMachine(oldState, issueTrackerManager)
            Assertions.assertEquals(stateMachine.state.value.second, oldState)

            Assertions.assertThrows(IllegalArgumentException::class.java) { stateMachine.transition(event) }
        }
    }
}
