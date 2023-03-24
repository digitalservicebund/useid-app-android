package de.digitalService.useID.coordinator

import android.net.Uri
import com.ramcosta.composedestinations.spec.Direction
import de.digitalService.useID.StorageManager
import de.digitalService.useID.flows.SetupStateMachine
import de.digitalService.useID.ui.coordinators.*
import de.digitalService.useID.ui.navigation.Navigator
import de.digitalService.useID.ui.screens.destinations.*
import de.digitalService.useID.util.CoroutineContextProvider
import de.digitalService.useID.util.SetupStateFactory
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

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MockKExtension::class)
class SetupCoordinatorTest {

    @MockK(relaxUnitFun = true)
    lateinit var mockNavigator: Navigator

    @MockK(relaxUnitFun = true)
    lateinit var mockPinManagementCoordinator: PinManagementCoordinator

    @MockK(relaxUnitFun = true)
    lateinit var mockIdentificationCoordinator: IdentificationCoordinator

    @MockK(relaxUnitFun = true)
    lateinit var mockStorageManager: StorageManager

    @MockK(relaxUnitFun = true)
    lateinit var mockSetupStateMachine: SetupStateMachine

    @MockK(relaxUnitFun = true)
    lateinit var mockCoroutineContextProvider: CoroutineContextProvider

    @OptIn(ExperimentalCoroutinesApi::class)
    val dispatcher = StandardTestDispatcher()

    private val navigationDestinationSlot = slot<Direction>()
    private val navigationPoppingDestinationSlot = slot<Direction>()

    val stateFlow: MutableStateFlow<Pair<SetupStateMachine.Event, SetupStateMachine.State>> = MutableStateFlow(
        Pair(SetupStateMachine.Event.Invalidate, SetupStateMachine.State.Invalid)
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    @BeforeEach
    fun beforeEach() {
        Dispatchers.setMain(dispatcher)

        every { mockNavigator.navigate(capture(navigationDestinationSlot)) } returns Unit
        every { mockNavigator.navigatePopping(capture(navigationPoppingDestinationSlot)) } returns Unit

        every { mockCoroutineContextProvider.Default } returns dispatcher
        every { mockCoroutineContextProvider.IO } returns dispatcher

        // For supporting destinations with String nav arguments
        mockkStatic("android.net.Uri")
        every { Uri.encode(any()) } answers { value }
        every { Uri.decode(any()) } answers { value }

        every { mockSetupStateMachine.state } returns stateFlow
    }

    @Test
    fun singleStateObservation() = runTest {
        val setupCoordinator = SetupCoordinator(
            navigator = mockNavigator,
            pinManagementCoordinator = mockPinManagementCoordinator,
            identificationCoordinator = mockIdentificationCoordinator,
            storageManager = mockStorageManager,
            flowStateMachine = mockSetupStateMachine,
            coroutineContextProvider = mockCoroutineContextProvider
        )

        setupCoordinator.showSetupIntro(null)
        advanceUntilIdle()

        verify(exactly = 1) { mockSetupStateMachine.state }

        setupCoordinator.showSetupIntro(null)
        advanceUntilIdle()

        verify(exactly = 1) { mockSetupStateMachine.state }
    }

    @Nested
    inner class SetupStateChangeHandling {
        private fun testTransition(event: SetupStateMachine.Event, state: SetupStateMachine.State, testScope: TestScope) {
            val setupCoordinator = SetupCoordinator(
                navigator = mockNavigator,
                pinManagementCoordinator = mockPinManagementCoordinator,
                identificationCoordinator = mockIdentificationCoordinator,
                storageManager = mockStorageManager,
                flowStateMachine = mockSetupStateMachine,
                coroutineContextProvider = mockCoroutineContextProvider
            )
            setupCoordinator.showSetupIntro(null)

            stateFlow.value = Pair(event, state)
            testScope.advanceUntilIdle()
        }

        @ParameterizedTest
        @SealedClassesSource(names = [], mode = SealedClassesSource.Mode.EXCLUDE, factoryClass = SetupStateFactory::class)
        fun back(state: SetupStateMachine.State) = runTest {
            testTransition(SetupStateMachine.Event.Back, state, this)

            verify { mockNavigator.pop() }
        }

        @ParameterizedTest
        @SealedClassesSource(names = [], mode = SealedClassesSource.Mode.EXCLUDE, factoryClass = SetupStateFactory::class)
        fun `subsequent flow backed down`(state: SetupStateMachine.State) = runTest {
            testTransition(SetupStateMachine.Event.SubsequentFlowBackedDown, state, this)

            verify(exactly = 0) { mockNavigator.pop() }
        }

        @Test
        fun `intro with ident`() = runTest {
            val newState = SetupStateMachine.State.Intro("tcTokenUrl")

            testTransition(SetupStateMachine.Event.Invalidate, newState, this)

            verify { mockNavigator.navigate(any()) }

            Assertions.assertEquals(SetupIntroDestination(true).route, navigationDestinationSlot.captured.route)
        }

        @Test
        fun `intro without ident`() = runTest {
            val newState = SetupStateMachine.State.Intro(null)

            testTransition(SetupStateMachine.Event.Invalidate, newState, this)

            verify { mockNavigator.navigate(any()) }

            Assertions.assertEquals(SetupIntroDestination(false).route, navigationDestinationSlot.captured.route)
        }

        @Test
        fun `PIN management with ident`() = runTest {
            val newState = SetupStateMachine.State.PinManagement("tcTokenUrl")

            testTransition(SetupStateMachine.Event.Invalidate, newState, this)
            advanceUntilIdle()

            verify { mockPinManagementCoordinator.startPinManagement(true, true) }
        }

        @Test
        fun `PIN management without ident`() = runTest {
            val newState = SetupStateMachine.State.PinManagement(null)

            testTransition(SetupStateMachine.Event.Invalidate, newState, this)
            advanceUntilIdle()

            verify { mockPinManagementCoordinator.startPinManagement(false, true) }
        }

        @Test
        fun `skipping to ident`() = runTest {
            val tcTokenUrl = "tcTokenUrl"
            val newState = SetupStateMachine.State.SkippingToIdentRequested(tcTokenUrl)

            testTransition(SetupStateMachine.Event.Invalidate, newState, this)

            verify { mockIdentificationCoordinator.startIdentificationProcess(tcTokenUrl, true) }
        }

        @Test
        fun `start setup`() = runTest {
            val newState = SetupStateMachine.State.StartSetup(null)

            testTransition(SetupStateMachine.Event.Invalidate, newState, this)

            verify { mockNavigator.navigate(SetupPinLetterDestination) }
        }

        @Test
        fun `PIN reset`() = runTest {
            val newState = SetupStateMachine.State.PinReset(null)

            testTransition(SetupStateMachine.Event.Invalidate, newState, this)

            verify { mockNavigator.navigate(SetupResetPersonalPinDestination) }
        }

        @Test
        fun `PIN management finished with ident`() = runTest {
            val tcTokenUrl = "tcTokenUrl"
            val newState = SetupStateMachine.State.PinManagementFinished(tcTokenUrl)

            testTransition(SetupStateMachine.Event.Invalidate, newState, this)

            verify { mockStorageManager.setIsNotFirstTimeUser() }
            verify { mockNavigator.navigate(any()) }

            Assertions.assertEquals(SetupFinishDestination(true).route, navigationDestinationSlot.captured.route)
        }

        @Test
        fun `PIN management finished without ident`() = runTest {
            val newState = SetupStateMachine.State.PinManagementFinished(null)

            testTransition(SetupStateMachine.Event.Invalidate, newState, this)

            verify { mockStorageManager.setIsNotFirstTimeUser() }
            verify { mockNavigator.navigate(any()) }

            Assertions.assertEquals(SetupFinishDestination(false).route, navigationDestinationSlot.captured.route)
        }

        @Test
        fun `setup finished`() = runTest {
            val newState = SetupStateMachine.State.SetupFinished

            val stateFlow: MutableStateFlow<Pair<SetupStateMachine.Event, SetupStateMachine.State>> = MutableStateFlow(
                Pair(SetupStateMachine.Event.Invalidate, SetupStateMachine.State.Invalid)
            )
            every { mockSetupStateMachine.state } returns stateFlow

            val setupCoordinator = SetupCoordinator(
                navigator = mockNavigator,
                pinManagementCoordinator = mockPinManagementCoordinator,
                identificationCoordinator = mockIdentificationCoordinator,
                storageManager = mockStorageManager,
                flowStateMachine = mockSetupStateMachine,
                coroutineContextProvider = mockCoroutineContextProvider
            )
            setupCoordinator.showSetupIntro(null)

            stateFlow.value = Pair(SetupStateMachine.Event.Invalidate, newState)
            advanceUntilIdle()

            verify { mockNavigator.popToRoot() }
            Assertions.assertEquals(SubCoordinatorState.FINISHED, setupCoordinator.stateFlow.value)

            verify { mockSetupStateMachine.transition(SetupStateMachine.Event.Invalidate) }
        }

        @Test
        fun `ident after finished setup`() = runTest {
            val tcTokenUrl = "tcTokenUrl"
            val newState = SetupStateMachine.State.IdentAfterFinishedSetupRequested(tcTokenUrl)

            testTransition(SetupStateMachine.Event.Invalidate, newState, this)

            verify { mockIdentificationCoordinator.startIdentificationProcess(tcTokenUrl, false) }
        }

        @Test
        fun `already set up`() = runTest {
            val newState = SetupStateMachine.State.AlreadySetUpConfirmation

            testTransition(SetupStateMachine.Event.Invalidate, newState, this)

            verify { mockNavigator.navigate(AlreadySetupConfirmationDestination) }
        }
    }

    @Test
    fun `show setup intro with ident`() {
        val setupCoordinator = SetupCoordinator(
            navigator = mockNavigator,
            pinManagementCoordinator = mockPinManagementCoordinator,
            identificationCoordinator = mockIdentificationCoordinator,
            storageManager = mockStorageManager,
            flowStateMachine = mockSetupStateMachine,
            coroutineContextProvider = mockCoroutineContextProvider
        )

        Assertions.assertEquals(SubCoordinatorState.FINISHED, setupCoordinator.stateFlow.value)

        val tcTokenUrl = "tcTokenUrl"
        setupCoordinator.showSetupIntro(tcTokenUrl)

        Assertions.assertEquals(SubCoordinatorState.ACTIVE, setupCoordinator.stateFlow.value)
        verify { mockSetupStateMachine.transition(SetupStateMachine.Event.OfferSetup(tcTokenUrl)) }
    }

    @Test
    fun `start setup ID card`() {
        val setupCoordinator = SetupCoordinator(
            navigator = mockNavigator,
            pinManagementCoordinator = mockPinManagementCoordinator,
            identificationCoordinator = mockIdentificationCoordinator,
            storageManager = mockStorageManager,
            flowStateMachine = mockSetupStateMachine,
            coroutineContextProvider = mockCoroutineContextProvider
        )

        setupCoordinator.startSetupIdCard()

        verify { mockSetupStateMachine.transition(SetupStateMachine.Event.StartSetup) }
    }

    @Test
    fun `start PIN management`() {
        val setupCoordinator = SetupCoordinator(
            navigator = mockNavigator,
            pinManagementCoordinator = mockPinManagementCoordinator,
            identificationCoordinator = mockIdentificationCoordinator,
            storageManager = mockStorageManager,
            flowStateMachine = mockSetupStateMachine,
            coroutineContextProvider = mockCoroutineContextProvider
        )

        setupCoordinator.setupWithPinLetter()

        verify { mockSetupStateMachine.transition(SetupStateMachine.Event.StartPinManagement) }
    }

    @Test
    fun `handle PIN management cancelled`() = runTest {
        val setupCoordinator = SetupCoordinator(
            navigator = mockNavigator,
            pinManagementCoordinator = mockPinManagementCoordinator,
            identificationCoordinator = mockIdentificationCoordinator,
            storageManager = mockStorageManager,
            flowStateMachine = mockSetupStateMachine,
            coroutineContextProvider = mockCoroutineContextProvider
        )
        setupCoordinator.showSetupIntro(null)

        val subCoordinatorStateFlow = MutableStateFlow(SubCoordinatorState.CANCELLED)
        every { mockPinManagementCoordinator.startPinManagement(any(), any()) } returns subCoordinatorStateFlow

        val newState = SetupStateMachine.State.PinManagement(null)
        stateFlow.value = Pair(SetupStateMachine.Event.Invalidate, newState)
        advanceUntilIdle()

        verify { mockNavigator.popToRoot() }
        Assertions.assertEquals(SubCoordinatorState.CANCELLED, setupCoordinator.stateFlow.value)

        verify { mockSetupStateMachine.transition(SetupStateMachine.Event.Invalidate) }
    }

    @Test
    fun `handle PIN management backed down`() = runTest {
        val setupCoordinator = SetupCoordinator(
            navigator = mockNavigator,
            pinManagementCoordinator = mockPinManagementCoordinator,
            identificationCoordinator = mockIdentificationCoordinator,
            storageManager = mockStorageManager,
            flowStateMachine = mockSetupStateMachine,
            coroutineContextProvider = mockCoroutineContextProvider
        )
        setupCoordinator.showSetupIntro(null)

        val subCoordinatorStateFlow = MutableStateFlow(SubCoordinatorState.BACKED_DOWN)
        every { mockPinManagementCoordinator.startPinManagement(any(), any()) } returns subCoordinatorStateFlow

        val newState = SetupStateMachine.State.PinManagement(null)
        stateFlow.value = Pair(SetupStateMachine.Event.Invalidate, newState)
        advanceUntilIdle()

        verify { mockSetupStateMachine.transition(SetupStateMachine.Event.SubsequentFlowBackedDown) }
    }

    @Test
    fun `handle PIN management finished`() = runTest {
        val setupCoordinator = SetupCoordinator(
            navigator = mockNavigator,
            pinManagementCoordinator = mockPinManagementCoordinator,
            identificationCoordinator = mockIdentificationCoordinator,
            storageManager = mockStorageManager,
            flowStateMachine = mockSetupStateMachine,
            coroutineContextProvider = mockCoroutineContextProvider
        )
        setupCoordinator.showSetupIntro(null)

        val subCoordinatorStateFlow = MutableStateFlow(SubCoordinatorState.FINISHED)
        every { mockPinManagementCoordinator.startPinManagement(any(), any()) } returns subCoordinatorStateFlow

        val newState = SetupStateMachine.State.PinManagement(null)
        stateFlow.value = Pair(SetupStateMachine.Event.Invalidate, newState)
        advanceUntilIdle()

        verify { mockSetupStateMachine.transition(SetupStateMachine.Event.FinishPinManagement) }
    }

    @Test
    fun `handle PIN management skipped`() = runTest {
        val setupCoordinator = SetupCoordinator(
            navigator = mockNavigator,
            pinManagementCoordinator = mockPinManagementCoordinator,
            identificationCoordinator = mockIdentificationCoordinator,
            storageManager = mockStorageManager,
            flowStateMachine = mockSetupStateMachine,
            coroutineContextProvider = mockCoroutineContextProvider
        )
        setupCoordinator.showSetupIntro(null)

        val subCoordinatorStateFlow = MutableStateFlow(SubCoordinatorState.SKIPPED)
        every { mockPinManagementCoordinator.startPinManagement(any(), any()) } returns subCoordinatorStateFlow

        val newState = SetupStateMachine.State.PinManagement(null)
        stateFlow.value = Pair(SetupStateMachine.Event.Invalidate, newState)
        advanceUntilIdle()

        verify { mockNavigator.popToRoot() }
        Assertions.assertEquals(SubCoordinatorState.FINISHED, setupCoordinator.stateFlow.value)

        verify { mockSetupStateMachine.transition(SetupStateMachine.Event.Invalidate) }
    }

    @Test
    fun `setup without PIN letter`() {
        val setupCoordinator = SetupCoordinator(
            navigator = mockNavigator,
            pinManagementCoordinator = mockPinManagementCoordinator,
            identificationCoordinator = mockIdentificationCoordinator,
            storageManager = mockStorageManager,
            flowStateMachine = mockSetupStateMachine,
            coroutineContextProvider = mockCoroutineContextProvider
        )

        setupCoordinator.setupWithoutPinLetter()

        verify { mockSetupStateMachine.transition(SetupStateMachine.Event.ResetPin) }
    }

    @Test
    fun `on back`() {
        val setupCoordinator = SetupCoordinator(
            navigator = mockNavigator,
            pinManagementCoordinator = mockPinManagementCoordinator,
            identificationCoordinator = mockIdentificationCoordinator,
            storageManager = mockStorageManager,
            flowStateMachine = mockSetupStateMachine,
            coroutineContextProvider = mockCoroutineContextProvider
        )

        setupCoordinator.onBackClicked()

        verify { mockSetupStateMachine.transition(SetupStateMachine.Event.Back) }
    }

    @Test
    fun `skip setup`() {
        val setupCoordinator = SetupCoordinator(
            navigator = mockNavigator,
            pinManagementCoordinator = mockPinManagementCoordinator,
            identificationCoordinator = mockIdentificationCoordinator,
            storageManager = mockStorageManager,
            flowStateMachine = mockSetupStateMachine,
            coroutineContextProvider = mockCoroutineContextProvider
        )

        setupCoordinator.skipSetup()

        verify { mockSetupStateMachine.transition(SetupStateMachine.Event.SkipSetup) }
    }

    @Test
    fun `confirm already set up`() {
        val setupCoordinator = SetupCoordinator(
            navigator = mockNavigator,
            pinManagementCoordinator = mockPinManagementCoordinator,
            identificationCoordinator = mockIdentificationCoordinator,
            storageManager = mockStorageManager,
            flowStateMachine = mockSetupStateMachine,
            coroutineContextProvider = mockCoroutineContextProvider
        )

        setupCoordinator.confirmAlreadySetUp()

        verify { mockSetupStateMachine.transition(SetupStateMachine.Event.ConfirmAlreadySetUp) }
    }

    @Test
    fun `confirmed finished setup`() {
        val setupCoordinator = SetupCoordinator(
            navigator = mockNavigator,
            pinManagementCoordinator = mockPinManagementCoordinator,
            identificationCoordinator = mockIdentificationCoordinator,
            storageManager = mockStorageManager,
            flowStateMachine = mockSetupStateMachine,
            coroutineContextProvider = mockCoroutineContextProvider
        )

        setupCoordinator.onSetupFinishConfirmed()

        verify { mockSetupStateMachine.transition(SetupStateMachine.Event.ConfirmFinish) }
    }

    @Test
    fun `cancel setup`() {
        val setupCoordinator = SetupCoordinator(
            navigator = mockNavigator,
            pinManagementCoordinator = mockPinManagementCoordinator,
            identificationCoordinator = mockIdentificationCoordinator,
            storageManager = mockStorageManager,
            flowStateMachine = mockSetupStateMachine,
            coroutineContextProvider = mockCoroutineContextProvider
        )

        setupCoordinator.cancelSetup()

        verify { mockNavigator.popToRoot() }
        Assertions.assertEquals(SubCoordinatorState.CANCELLED, setupCoordinator.stateFlow.value)

        verify { mockSetupStateMachine.transition(SetupStateMachine.Event.Invalidate) }
    }
}
