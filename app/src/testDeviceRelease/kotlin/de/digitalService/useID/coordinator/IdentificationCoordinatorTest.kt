package de.digitalService.useID.coordinator

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Base64
import com.ramcosta.composedestinations.spec.Direction
import de.digitalService.useID.StorageManager
import de.digitalService.useID.analytics.TrackerManagerType
import de.digitalService.useID.flows.*
import de.digitalService.useID.idCardInterface.*
import de.digitalService.useID.ui.coordinators.CanCoordinator
import de.digitalService.useID.ui.coordinators.IdentificationCoordinator
import de.digitalService.useID.ui.coordinators.SubCoordinatorState
import de.digitalService.useID.ui.navigation.Navigator
import de.digitalService.useID.ui.screens.destinations.*
import de.digitalService.useID.util.CoroutineContextProvider
import de.digitalService.useID.util.IdentificationStateFactory
import de.jodamob.junit5.SealedClassesSource
import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MockKExtension::class)
class IdentificationCoordinatorTest {

    @MockK(relaxUnitFun = true)
    lateinit var mockContext: Context

    @MockK(relaxUnitFun = true)
    lateinit var mockCanCoordinator: CanCoordinator

    @MockK(relaxUnitFun = true)
    lateinit var mockNavigator: Navigator

    @MockK(relaxUnitFun = true)
    lateinit var mockEidInteractionManager: EidInteractionManager

    @MockK(relaxUnitFun = true)
    lateinit var mockStorageManager: StorageManager

    @MockK(relaxUnitFun = true)
    lateinit var mockIdentificationStateMachine: IdentificationStateMachine

    @MockK(relaxUnitFun = true)
    lateinit var mockCanStateMachine: CanStateMachine

    @MockK(relaxUnitFun = true)
    lateinit var mockTrackerManager: TrackerManagerType

    @MockK(relaxUnitFun = true)
    lateinit var mockCoroutineContextProvider: CoroutineContextProvider

    @OptIn(ExperimentalCoroutinesApi::class)
    val dispatcher = StandardTestDispatcher()

    private val destinationSlot = slot<Direction>()
    private val destinationPoppingSlot = slot<Direction>()
    private val navigationPopUpToOrNavigateDestinationSlot = slot<Direction>()

    private val testTokenUrl = "https://token"
    private val testTokenUri = mockk<Uri>()

    private val request: AuthenticationRequest = mockk()
    val certificateDescription: CertificateDescription = mockk()

    private val stateFlow: MutableStateFlow<Pair<IdentificationStateMachine.Event, IdentificationStateMachine.State>> = MutableStateFlow(
        Pair(
            IdentificationStateMachine.Event.Invalidate, IdentificationStateMachine.State.Invalid
        )
    )

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(dispatcher)

        every { mockNavigator.navigate(capture(destinationSlot)) } returns Unit
        every { mockNavigator.navigatePopping(capture(destinationPoppingSlot)) } returns Unit
        every { mockNavigator.popUpToOrNavigate(capture(navigationPopUpToOrNavigateDestinationSlot), any()) } returns Unit

        every { mockCoroutineContextProvider.Default } returns dispatcher
        every { mockCoroutineContextProvider.IO } returns dispatcher

        every { mockIdentificationStateMachine.state } returns stateFlow

        // For supporting destinations with String nav arguments
        mockkStatic(Uri::class)
        every { Uri.encode(any()) } answers { value }
        every { Uri.decode(any()) } answers { value }

        every { Uri.parse(testTokenUrl) } returns testTokenUri
    }

    @AfterEach
    fun tearDown() {
        clearStaticMockk(Uri::class)
        Dispatchers.resetMain()
    }

    @Test
    fun singleStateObservation() = runTest {
        val identificationCoordinator = IdentificationCoordinator(
            mockContext,
            mockCanCoordinator,
            mockNavigator,
            mockEidInteractionManager,
            mockStorageManager,
            mockTrackerManager,
            mockIdentificationStateMachine,
            mockCanStateMachine,
            mockCoroutineContextProvider
        )

        identificationCoordinator.startIdentificationProcess(testTokenUrl, false)
        advanceUntilIdle()

        verify(exactly = 1) { mockIdentificationStateMachine.state }

        identificationCoordinator.startIdentificationProcess(testTokenUrl, false)
        advanceUntilIdle()

        verify(exactly = 1) { mockIdentificationStateMachine.state }
    }

    @Nested
    inner class IdentificationStateChangeHandling {
        private fun testTransition(event: IdentificationStateMachine.Event, state: IdentificationStateMachine.State, testScope: TestScope) {
            val identificationCoordinator = IdentificationCoordinator(
                mockContext,
                mockCanCoordinator,
                mockNavigator,
                mockEidInteractionManager,
                mockStorageManager,
                mockTrackerManager,
                mockIdentificationStateMachine,
                mockCanStateMachine,
                mockCoroutineContextProvider
            )
            identificationCoordinator.startIdentificationProcess(testTokenUrl, false)

            stateFlow.value = Pair(event, state)
            testScope.advanceUntilIdle()
        }

        @ParameterizedTest
        @SealedClassesSource(names = [], mode = SealedClassesSource.Mode.EXCLUDE, factoryClass = IdentificationStateFactory::class)
        fun back(state: IdentificationStateMachine.State) = runTest {
            testTransition(IdentificationStateMachine.Event.Back, state, this)

            verify { mockNavigator.pop() }
        }

        @Test
        fun `backing down`() = runTest {
            testTransition(IdentificationStateMachine.Event.Back, IdentificationStateMachine.State.Invalid, this)

            verify { mockNavigator.pop() }
            verify { mockEidInteractionManager.cancelTask() }
            verify { mockIdentificationStateMachine.transition(IdentificationStateMachine.Event.Invalidate) }
        }

        @Test
        fun `start identification`() = runTest {
            val state = IdentificationStateMachine.State.StartIdentification(false, testTokenUri)

            val identificationCoordinator = IdentificationCoordinator(
                mockContext,
                mockCanCoordinator,
                mockNavigator,
                mockEidInteractionManager,
                mockStorageManager,
                mockTrackerManager,
                mockIdentificationStateMachine,
                mockCanStateMachine,
                mockCoroutineContextProvider
            )
            identificationCoordinator.startIdentificationProcess(testTokenUrl, false)

            every { mockEidInteractionManager.eidFlow } returns flowOf(EidInteractionEvent.Idle)

            stateFlow.value = Pair(IdentificationStateMachine.Event.Invalidate, state)
            advanceUntilIdle()

            verify { mockEidInteractionManager.identify(mockContext, testTokenUri) }
        }

        @ParameterizedTest
        @ValueSource(booleans = [true, false])
        fun `fetching metadata`(backingDownAllowed: Boolean) = runTest {
            val state = IdentificationStateMachine.State.FetchingMetadata(backingDownAllowed, testTokenUri)
            testTransition(IdentificationStateMachine.Event.Invalidate, state, this)

            verify { mockNavigator.popUpToOrNavigate(any(), false) }

            Assertions.assertEquals(IdentificationFetchMetadataDestination(backingDownAllowed).route, navigationPopUpToOrNavigateDestinationSlot.captured.route)
        }

        @ParameterizedTest
        @ValueSource(booleans = [true, false])
        fun `fetching metadata failed`(backingDownAllowed: Boolean) = runTest {
            val state = IdentificationStateMachine.State.FetchingMetadataFailed(backingDownAllowed, testTokenUri)
            testTransition(IdentificationStateMachine.Event.Invalidate, state, this)

            verify { mockNavigator.navigate(IdentificationOtherErrorDestination) }
        }

        @ParameterizedTest
        @ValueSource(booleans = [true, false])
        fun `request certificate`(backingDownAllowed: Boolean) = runTest {
            val state = IdentificationStateMachine.State.RequestCertificate(false, request)
            testTransition(IdentificationStateMachine.Event.Invalidate, state, this)

            verify { mockEidInteractionManager.getCertificate() }
        }

        @ParameterizedTest
        @ValueSource(booleans = [true, false])
        fun `request certificate description received`(backingDownAllowed: Boolean) = runTest {
            mockkStatic("android.util.Base64")
            every { Base64.encodeToString(any(), any()) } returns "serializedBase64"

            val request = AuthenticationRequest(emptyList(), "")
            val certificateDescription = CertificateDescription("", "", "", "", "", "")
            val state = IdentificationStateMachine.State.CertificateDescriptionReceived(backingDownAllowed, request, certificateDescription)
            testTransition(IdentificationStateMachine.Event.Invalidate, state, this)

            verify { mockNavigator.navigatePopping(any()) }

            Assertions.assertEquals(IdentificationAttributeConsentDestination(IdentificationAttributes(request.requiredAttributes, certificateDescription), backingDownAllowed).route, destinationPoppingSlot.captured.route)
        }

        @ParameterizedTest
        @ValueSource(booleans = [true, false])
        fun `input PIN`(backingDownAllowed: Boolean) = runTest {
            val state = IdentificationStateMachine.State.PinInput(backingDownAllowed, request, certificateDescription)
            testTransition(IdentificationStateMachine.Event.Invalidate, state, this)

            verify { mockNavigator.navigate(any()) }

            Assertions.assertEquals(IdentificationPersonalPinDestination(false).route, destinationSlot.captured.route)
        }

        @Test
        fun `input PIN retrying`() = runTest {
            val state = IdentificationStateMachine.State.PinInputRetry
            testTransition(IdentificationStateMachine.Event.Invalidate, state, this)

            verify { mockNavigator.navigate(any()) }

            Assertions.assertEquals(IdentificationPersonalPinDestination(true).route, destinationSlot.captured.route)
        }

        @Test
        fun `PIN entered first time`() = runTest {
            val pin = "123456"
            val state = IdentificationStateMachine.State.PinEntered(pin, true)
            testTransition(IdentificationStateMachine.Event.Invalidate, state, this)

            verify { mockNavigator.popUpToOrNavigate(IdentificationScanDestination, false) }
            verify { mockEidInteractionManager.acceptAccessRights() }
        }

        @Test
        fun `PIN entered not first time`() = runTest {
            val pin = "123456"
            val state = IdentificationStateMachine.State.PinEntered(pin, false)
            testTransition(IdentificationStateMachine.Event.Invalidate, state, this)

            verify { mockNavigator.popUpToOrNavigate(IdentificationScanDestination, false) }
            verify { mockEidInteractionManager.providePin(pin) }
        }

        @Test
        fun `CAN requested short flow`() = runTest {
            val pin = "123456"
            val state = IdentificationStateMachine.State.CanRequested(pin)

            every { mockCanCoordinator.stateFlow } returns MutableStateFlow(SubCoordinatorState.FINISHED)

            testTransition(IdentificationStateMachine.Event.Invalidate, state, this)

            verify { mockCanCoordinator.startIdentCanFlow(pin) }
        }

        @Test
        fun `CAN requested long flow`() = runTest {
            val state = IdentificationStateMachine.State.CanRequested(null)

            every { mockCanCoordinator.stateFlow } returns MutableStateFlow(SubCoordinatorState.FINISHED)

            testTransition(IdentificationStateMachine.Event.Invalidate, state, this)

            verify { mockCanCoordinator.startIdentCanFlow(null) }
        }

        @Test
        fun `CAN requested short flow with CAN flow already active`() = runTest {
            val pin = "123456"
            val state = IdentificationStateMachine.State.CanRequested(pin)

            every { mockCanCoordinator.stateFlow } returns MutableStateFlow(SubCoordinatorState.ACTIVE)

            testTransition(IdentificationStateMachine.Event.Invalidate, state, this)

            verify(exactly = 0) { mockCanCoordinator.startIdentCanFlow(pin) }
        }

        @Test
        fun `CAN requested long flow with CAN flow already active`() = runTest {
            val state = IdentificationStateMachine.State.CanRequested(null)

            every { mockCanCoordinator.stateFlow } returns MutableStateFlow(SubCoordinatorState.ACTIVE)

            testTransition(IdentificationStateMachine.Event.Invalidate, state, this)

            verify(exactly = 0) { mockCanCoordinator.startIdentCanFlow(null) }
        }

        @Test
        fun `PIN requested`() = runTest {
            val pin = "123456"
            val state = IdentificationStateMachine.State.PinRequested(pin)
            testTransition(IdentificationStateMachine.Event.Invalidate, state, this)

            verify { mockEidInteractionManager.providePin(pin) }
        }

        @Test
        fun finished() = runTest {
            val redirectUrl = "redirectUrl"
            val state = IdentificationStateMachine.State.Finished(redirectUrl)

            val redirectUri: Uri = mockk()
            every { Uri.parse(redirectUrl) } returns redirectUri

            mockkStatic(Intent::class)
            mockkConstructor(Intent::class)

            val intent: Intent = mockk()
            every { constructedWith<Intent>(EqMatcher("android.intent.action.VIEW"), OfTypeMatcher<Uri>(Uri::class)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) } returns intent

            testTransition(IdentificationStateMachine.Event.Invalidate, state, this)

            verify { mockNavigator.popToRoot() }
            verify { mockTrackerManager.trackEvent("identification", "success", "") }

            verify { mockEidInteractionManager.cancelTask() }
            verify { mockIdentificationStateMachine.transition(IdentificationStateMachine.Event.Invalidate) }
        }

        @Test
        fun `card deactivated`() = runTest {
            val state = IdentificationStateMachine.State.CardDeactivated
            testTransition(IdentificationStateMachine.Event.Invalidate, state, this)

            verify { mockNavigator.navigate(IdentificationCardDeactivatedDestination) }
        }

        @Test
        fun `card blocked`() = runTest {
            val state = IdentificationStateMachine.State.CardBlocked
            testTransition(IdentificationStateMachine.Event.Invalidate, state, this)

            verify { mockNavigator.navigate(IdentificationCardBlockedDestination) }
        }

        @Test
        fun `card unreadable with redirect`() = runTest {
            val redirectUrl = "redirectUrl"
            val state = IdentificationStateMachine.State.CardUnreadable(redirectUrl)
            testTransition(IdentificationStateMachine.Event.Invalidate, state, this)

            verify { mockNavigator.navigate(any()) }

            Assertions.assertEquals(IdentificationCardUnreadableDestination(true, redirectUrl).route, destinationSlot.captured.route)
        }

        @Test
        fun `card unreadable without redirect`() = runTest {
            val state = IdentificationStateMachine.State.CardUnreadable(null)
            testTransition(IdentificationStateMachine.Event.Invalidate, state, this)

            verify { mockNavigator.navigate(any()) }

            Assertions.assertEquals(IdentificationCardUnreadableDestination(true, null).route, destinationSlot.captured.route)
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun `start identification process`(setupSkipped: Boolean) = runTest {
        val identificationCoordinator = IdentificationCoordinator(
            mockContext,
            mockCanCoordinator,
            mockNavigator,
            mockEidInteractionManager,
            mockStorageManager,
            mockTrackerManager,
            mockIdentificationStateMachine,
            mockCanStateMachine,
            mockCoroutineContextProvider
        )

        Assertions.assertEquals(SubCoordinatorState.FINISHED, identificationCoordinator.stateFlow.value)

        identificationCoordinator.startIdentificationProcess(testTokenUrl, setupSkipped)

        Assertions.assertEquals(SubCoordinatorState.ACTIVE, identificationCoordinator.stateFlow.value)

        verify { mockIdentificationStateMachine.transition(IdentificationStateMachine.Event.Initialize(setupSkipped, testTokenUri)) }
        verify { mockCanStateMachine.transition(CanStateMachine.Event.Invalidate) }
    }

    @Test
    fun `confirm attributes`() = runTest {
        val identificationCoordinator = IdentificationCoordinator(
            mockContext,
            mockCanCoordinator,
            mockNavigator,
            mockEidInteractionManager,
            mockStorageManager,
            mockTrackerManager,
            mockIdentificationStateMachine,
            mockCanStateMachine,
            mockCoroutineContextProvider
        )

        identificationCoordinator.confirmAttributesForIdentification()

        verify { mockIdentificationStateMachine.transition(IdentificationStateMachine.Event.ConfirmAttributes) }
    }

    @Test
    fun `PIN entered`() = runTest {
        val identificationCoordinator = IdentificationCoordinator(
            mockContext,
            mockCanCoordinator,
            mockNavigator,
            mockEidInteractionManager,
            mockStorageManager,
            mockTrackerManager,
            mockIdentificationStateMachine,
            mockCanStateMachine,
            mockCoroutineContextProvider
        )

        val pin = "123456"
        identificationCoordinator.setPin(pin)

        verify { mockIdentificationStateMachine.transition(IdentificationStateMachine.Event.EnterPin(pin)) }
    }

    @Test
    fun `handle cancelled CAN flow`() = runTest {
        val identificationCoordinator = IdentificationCoordinator(
            mockContext,
            mockCanCoordinator,
            mockNavigator,
            mockEidInteractionManager,
            mockStorageManager,
            mockTrackerManager,
            mockIdentificationStateMachine,
            mockCanStateMachine,
            mockCoroutineContextProvider
        )
        identificationCoordinator.startIdentificationProcess(testTokenUrl, false)

        val canStateFlow = MutableStateFlow(SubCoordinatorState.CANCELLED)
        every { mockCanCoordinator.stateFlow } returns canStateFlow
        every { mockCanCoordinator.startIdentCanFlow(null) } returns canStateFlow

        stateFlow.value = Pair(IdentificationStateMachine.Event.Invalidate, IdentificationStateMachine.State.CanRequested(null))
        advanceUntilIdle()

        verify { mockNavigator.popToRoot() }
        verify { mockIdentificationStateMachine.transition(IdentificationStateMachine.Event.Invalidate) }
        Assertions.assertEquals(SubCoordinatorState.CANCELLED, identificationCoordinator.stateFlow.value)
    }

    @Test
    fun `on back`() = runTest {
        val identificationCoordinator = IdentificationCoordinator(
            mockContext,
            mockCanCoordinator,
            mockNavigator,
            mockEidInteractionManager,
            mockStorageManager,
            mockTrackerManager,
            mockIdentificationStateMachine,
            mockCanStateMachine,
            mockCoroutineContextProvider
        )

        identificationCoordinator.onBack()

        verify { mockIdentificationStateMachine.transition(IdentificationStateMachine.Event.Back) }
    }

    @Test
    fun `retry identification`() = runTest {
        val identificationCoordinator = IdentificationCoordinator(
            mockContext,
            mockCanCoordinator,
            mockNavigator,
            mockEidInteractionManager,
            mockStorageManager,
            mockTrackerManager,
            mockIdentificationStateMachine,
            mockCanStateMachine,
            mockCoroutineContextProvider
        )

        identificationCoordinator.retryIdentification()

        verify { mockIdentificationStateMachine.transition(IdentificationStateMachine.Event.RetryAfterError) }
    }

    @Test
    fun `cancel identification`() = runTest {
        val identificationCoordinator = IdentificationCoordinator(
            mockContext,
            mockCanCoordinator,
            mockNavigator,
            mockEidInteractionManager,
            mockStorageManager,
            mockTrackerManager,
            mockIdentificationStateMachine,
            mockCanStateMachine,
            mockCoroutineContextProvider
        )

        identificationCoordinator.cancelIdentification()

        verify { mockNavigator.popToRoot() }
        verify { mockIdentificationStateMachine.transition(IdentificationStateMachine.Event.Invalidate) }
        Assertions.assertEquals(SubCoordinatorState.CANCELLED, identificationCoordinator.stateFlow.value)
    }

    @Nested
    inner class IdentificationEidEvent {
        @Test
        fun `handling authentication started`() = runTest {
            every { mockIdentificationStateMachine.state } returns MutableStateFlow(Pair(IdentificationStateMachine.Event.Initialize(false, testTokenUri), IdentificationStateMachine.State.StartIdentification(false, testTokenUri)))

            val eIdFlow: MutableStateFlow<EidInteractionEvent> = MutableStateFlow(EidInteractionEvent.Idle)
            every { mockEidInteractionManager.eidFlow } returns eIdFlow

            val identificationCoordinator = IdentificationCoordinator(mockContext, mockCanCoordinator, mockNavigator, mockEidInteractionManager, mockStorageManager, mockTrackerManager, mockIdentificationStateMachine, mockCanStateMachine, mockCoroutineContextProvider)
            advanceUntilIdle()

            identificationCoordinator.startIdentificationProcess(testTokenUrl, false)
            advanceUntilIdle()

            eIdFlow.value = EidInteractionEvent.AuthenticationStarted
            advanceUntilIdle()

            verify { mockIdentificationStateMachine.transition(IdentificationStateMachine.Event.StartedFetchingMetadata) }
        }

        @Test
        fun `handling certificate description received`() = runTest {
            every { mockIdentificationStateMachine.state } returns MutableStateFlow(Pair(IdentificationStateMachine.Event.Initialize(false, testTokenUri), IdentificationStateMachine.State.StartIdentification(false, testTokenUri)))

            val eIdFlow: MutableStateFlow<EidInteractionEvent> = MutableStateFlow(EidInteractionEvent.Idle)
            every { mockEidInteractionManager.eidFlow } returns eIdFlow

            val identificationCoordinator = IdentificationCoordinator(mockContext, mockCanCoordinator, mockNavigator, mockEidInteractionManager, mockStorageManager, mockTrackerManager, mockIdentificationStateMachine, mockCanStateMachine, mockCoroutineContextProvider)
            advanceUntilIdle()

            val certificateDescription = mockk<CertificateDescription>()
            identificationCoordinator.startIdentificationProcess(testTokenUrl, false)
            advanceUntilIdle()

            eIdFlow.value = EidInteractionEvent.CertificateDescriptionReceived(certificateDescription)
            advanceUntilIdle()

            verify { mockIdentificationStateMachine.transition(IdentificationStateMachine.Event.CertificateDescriptionReceived(certificateDescription)) }
        }

        @Test
        fun `handling authentication request confirmation requested`() = runTest {
            every { mockIdentificationStateMachine.state } returns MutableStateFlow(Pair(IdentificationStateMachine.Event.Initialize(false, testTokenUri), IdentificationStateMachine.State.StartIdentification(false, testTokenUri)))

            val eIdFlow: MutableStateFlow<EidInteractionEvent> = MutableStateFlow(EidInteractionEvent.Idle)
            every { mockEidInteractionManager.eidFlow } returns eIdFlow

            val identificationCoordinator = IdentificationCoordinator(mockContext, mockCanCoordinator, mockNavigator, mockEidInteractionManager, mockStorageManager, mockTrackerManager, mockIdentificationStateMachine, mockCanStateMachine, mockCoroutineContextProvider)
            advanceUntilIdle()

            val request = mockk<AuthenticationRequest>()
            identificationCoordinator.startIdentificationProcess(testTokenUrl, false)
            advanceUntilIdle()

            eIdFlow.value = EidInteractionEvent.AuthenticationRequestConfirmationRequested(request)
            advanceUntilIdle()

            verify { mockIdentificationStateMachine.transition(IdentificationStateMachine.Event.FrameworkRequestsAttributeConfirmation(request)) }
        }

        @Test
        fun `handling PIN requested`() = runTest {
            every { mockIdentificationStateMachine.state } returns MutableStateFlow(Pair(IdentificationStateMachine.Event.Initialize(false, testTokenUri), IdentificationStateMachine.State.StartIdentification(false, testTokenUri)))

            val eIdFlow: MutableStateFlow<EidInteractionEvent> = MutableStateFlow(EidInteractionEvent.Idle)
            every { mockEidInteractionManager.eidFlow } returns eIdFlow

            every { mockCanCoordinator.stateFlow } returns MutableStateFlow(SubCoordinatorState.FINISHED)

            val identificationCoordinator = IdentificationCoordinator(mockContext, mockCanCoordinator, mockNavigator, mockEidInteractionManager, mockStorageManager, mockTrackerManager, mockIdentificationStateMachine, mockCanStateMachine, mockCoroutineContextProvider)
            advanceUntilIdle()

            identificationCoordinator.startIdentificationProcess(testTokenUrl, false)
            advanceUntilIdle()

            eIdFlow.value = EidInteractionEvent.PinRequested(3)
            advanceUntilIdle()

            verify { mockIdentificationStateMachine.transition(IdentificationStateMachine.Event.FrameworkRequestsPin(true)) }
        }

        @Test
        fun `not handling PIN requested when in CAN flow`() = runTest {
            every { mockIdentificationStateMachine.state } returns MutableStateFlow(Pair(IdentificationStateMachine.Event.Initialize(false, testTokenUri), IdentificationStateMachine.State.StartIdentification(false, testTokenUri)))

            val eIdFlow: MutableStateFlow<EidInteractionEvent> = MutableStateFlow(EidInteractionEvent.Idle)
            every { mockEidInteractionManager.eidFlow } returns eIdFlow

            every { mockCanCoordinator.stateFlow } returns MutableStateFlow(SubCoordinatorState.ACTIVE)

            val identificationCoordinator = IdentificationCoordinator(mockContext, mockCanCoordinator, mockNavigator, mockEidInteractionManager, mockStorageManager, mockTrackerManager, mockIdentificationStateMachine, mockCanStateMachine, mockCoroutineContextProvider)
            advanceUntilIdle()

            identificationCoordinator.startIdentificationProcess(testTokenUrl, false)
            advanceUntilIdle()

            eIdFlow.value = EidInteractionEvent.PinRequested(3)
            advanceUntilIdle()

            verify(exactly = 0) { mockIdentificationStateMachine.transition(IdentificationStateMachine.Event.FrameworkRequestsPin(true)) }
        }

        @Test
        fun `handling authentication succeeded with redirect`() = runTest {
            every { mockIdentificationStateMachine.state } returns MutableStateFlow(Pair(IdentificationStateMachine.Event.Initialize(false, testTokenUri), IdentificationStateMachine.State.StartIdentification(false, testTokenUri)))

            val eIdFlow: MutableStateFlow<EidInteractionEvent> = MutableStateFlow(EidInteractionEvent.Idle)
            every { mockEidInteractionManager.eidFlow } returns eIdFlow

            val identificationCoordinator = IdentificationCoordinator(mockContext, mockCanCoordinator, mockNavigator, mockEidInteractionManager, mockStorageManager, mockTrackerManager, mockIdentificationStateMachine, mockCanStateMachine, mockCoroutineContextProvider)
            advanceUntilIdle()

            val redirectUrl = ""
            identificationCoordinator.startIdentificationProcess(testTokenUrl, false)
            advanceUntilIdle()

            eIdFlow.value = EidInteractionEvent.AuthenticationSucceededWithRedirect(redirectUrl)
            advanceUntilIdle()

            verify { mockIdentificationStateMachine.transition(IdentificationStateMachine.Event.Finish(redirectUrl)) }
        }

        @Test
        fun `handling CAN requested`() = runTest {
            every { mockIdentificationStateMachine.state } returns MutableStateFlow(Pair(IdentificationStateMachine.Event.Initialize(false, testTokenUri), IdentificationStateMachine.State.StartIdentification(false, testTokenUri)))

            val eIdFlow: MutableStateFlow<EidInteractionEvent> = MutableStateFlow(EidInteractionEvent.Idle)
            every { mockEidInteractionManager.eidFlow } returns eIdFlow

            every { mockCanCoordinator.stateFlow } returns MutableStateFlow(SubCoordinatorState.FINISHED)

            val identificationCoordinator = IdentificationCoordinator(mockContext, mockCanCoordinator, mockNavigator, mockEidInteractionManager, mockStorageManager, mockTrackerManager, mockIdentificationStateMachine, mockCanStateMachine, mockCoroutineContextProvider)
            advanceUntilIdle()

            identificationCoordinator.startIdentificationProcess(testTokenUrl, false)
            advanceUntilIdle()

            eIdFlow.value = EidInteractionEvent.CanRequested()
            advanceUntilIdle()

            verify { mockIdentificationStateMachine.transition(IdentificationStateMachine.Event.FrameworkRequestsCan) }
        }

        @Test
        fun `not handling CAN requested when in CAN flow`() = runTest {
            every { mockIdentificationStateMachine.state } returns MutableStateFlow(Pair(IdentificationStateMachine.Event.Initialize(false, testTokenUri), IdentificationStateMachine.State.StartIdentification(false, testTokenUri)))

            val eIdFlow: MutableStateFlow<EidInteractionEvent> = MutableStateFlow(EidInteractionEvent.Idle)
            every { mockEidInteractionManager.eidFlow } returns eIdFlow

            every { mockCanCoordinator.stateFlow } returns MutableStateFlow(SubCoordinatorState.ACTIVE)

            val identificationCoordinator = IdentificationCoordinator(mockContext, mockCanCoordinator, mockNavigator, mockEidInteractionManager, mockStorageManager, mockTrackerManager, mockIdentificationStateMachine, mockCanStateMachine, mockCoroutineContextProvider)
            advanceUntilIdle()

            identificationCoordinator.startIdentificationProcess(testTokenUrl, false)
            advanceUntilIdle()

            eIdFlow.value = EidInteractionEvent.CanRequested()
            advanceUntilIdle()

            verify(exactly = 0) { mockIdentificationStateMachine.transition(IdentificationStateMachine.Event.FrameworkRequestsCan) }
        }

        @Test
        fun `handling PUK requested`() = runTest {
            every { mockIdentificationStateMachine.state } returns MutableStateFlow(Pair(IdentificationStateMachine.Event.Initialize(false, testTokenUri), IdentificationStateMachine.State.StartIdentification(false, testTokenUri)))

            val eIdFlow: MutableStateFlow<EidInteractionEvent> = MutableStateFlow(EidInteractionEvent.Idle)
            every { mockEidInteractionManager.eidFlow } returns eIdFlow

            val identificationCoordinator = IdentificationCoordinator(mockContext, mockCanCoordinator, mockNavigator, mockEidInteractionManager, mockStorageManager, mockTrackerManager, mockIdentificationStateMachine, mockCanStateMachine, mockCoroutineContextProvider)
            advanceUntilIdle()

            identificationCoordinator.startIdentificationProcess(testTokenUrl, false)
            advanceUntilIdle()

            eIdFlow.value = EidInteractionEvent.PukRequested
            advanceUntilIdle()

            verify { mockIdentificationStateMachine.transition(IdentificationStateMachine.Event.Error(IdCardInteractionException.CardBlocked)) }
        }

        @Test
        fun `handling error`() = runTest {
            every { mockIdentificationStateMachine.state } returns MutableStateFlow(Pair(IdentificationStateMachine.Event.Initialize(false, testTokenUri), IdentificationStateMachine.State.StartIdentification(false, testTokenUri)))

            val eIdFlow: MutableStateFlow<EidInteractionEvent> = MutableStateFlow(EidInteractionEvent.Idle)
            every { mockEidInteractionManager.eidFlow } returns eIdFlow

            val identificationCoordinator = IdentificationCoordinator(mockContext, mockCanCoordinator, mockNavigator, mockEidInteractionManager, mockStorageManager, mockTrackerManager, mockIdentificationStateMachine, mockCanStateMachine, mockCoroutineContextProvider)
            advanceUntilIdle()

            val exception = IdCardInteractionException.FrameworkError("message")
            identificationCoordinator.startIdentificationProcess(testTokenUrl, false)
            advanceUntilIdle()

            eIdFlow.value = EidInteractionEvent.Error(exception)
            advanceUntilIdle()

            verify { mockIdentificationStateMachine.transition(IdentificationStateMachine.Event.Error(exception)) }
        }
    }
}
