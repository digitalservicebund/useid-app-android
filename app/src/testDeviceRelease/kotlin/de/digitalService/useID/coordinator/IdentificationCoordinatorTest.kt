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
    lateinit var mockIdCardManager: IdCardManager

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
    private val testUrl = "bundesident://127.0.0.1/eID-Client?tokenURL="

    private val pinCallback: PinCallback = mockk()
    private val request: EidAuthenticationRequest = mockk()
    private val attributeConfirmationCallback: AttributeConfirmationCallback = mockk()

    private val stateFlow: MutableStateFlow<Pair<IdentificationStateMachine.Event, IdentificationStateMachine.State>> = MutableStateFlow(Pair(
        IdentificationStateMachine.Event.Invalidate, IdentificationStateMachine.State.Invalid))

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
        mockkStatic("android.net.Uri")
        every { Uri.encode(any()) } answers { value }
        every { Uri.decode(any()) } answers { value }
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Nested
    inner class IdentificationStateChangeHandling {
        private fun testTransition(event: IdentificationStateMachine.Event, state: IdentificationStateMachine.State, testScope: TestScope) {
            val identificationCoordinator = IdentificationCoordinator(
                mockContext,
                mockCanCoordinator,
                mockNavigator,
                mockIdCardManager,
                mockStorageManager,
                mockTrackerManager,
                mockIdentificationStateMachine,
                mockCanStateMachine,
                mockCoroutineContextProvider
            )

            stateFlow.value = Pair(event, state)
            testScope.advanceUntilIdle()
        }

        @ParameterizedTest
        @SealedClassesSource(names = [] , mode = SealedClassesSource.Mode.EXCLUDE, factoryClass = IdentificationStateFactory::class)
        fun back(state: IdentificationStateMachine.State) = runTest {
            testTransition(IdentificationStateMachine.Event.Back, state, this)

            verify { mockNavigator.pop() }
        }

        @Test
        fun `backing down`() = runTest {
            testTransition(IdentificationStateMachine.Event.Back, IdentificationStateMachine.State.Invalid, this)

            verify { mockNavigator.pop() }
            verify { mockIdCardManager.cancelTask() }
            verify { mockIdentificationStateMachine.transition(IdentificationStateMachine.Event.Invalidate) }
        }

        @Test
        fun `start identification`() = runTest {
            val state = IdentificationStateMachine.State.StartIdentification(false, testTokenUrl)

            val identificationCoordinator = IdentificationCoordinator(
                mockContext,
                mockCanCoordinator,
                mockNavigator,
                mockIdCardManager,
                mockStorageManager,
                mockTrackerManager,
                mockIdentificationStateMachine,
                mockCanStateMachine,
                mockCoroutineContextProvider
            )

            every { mockIdCardManager.eidFlow } returns flowOf(EidInteractionEvent.Idle)

            stateFlow.value = Pair(IdentificationStateMachine.Event.Invalidate, state)
            advanceUntilIdle()

            verify { mockIdCardManager.cancelTask() }
            verify { mockIdCardManager.identify(mockContext, testTokenUrl) }
        }

        @ParameterizedTest
        @ValueSource(booleans = [true, false])
        fun `fetching metadata`(backingDownAllowed: Boolean) = runTest {
            val state = IdentificationStateMachine.State.FetchingMetadata(backingDownAllowed, testTokenUrl)
            testTransition(IdentificationStateMachine.Event.Invalidate, state, this)

            verify { mockNavigator.popUpToOrNavigate(any(), false) }

            Assertions.assertEquals(IdentificationFetchMetadataDestination(backingDownAllowed).route, navigationPopUpToOrNavigateDestinationSlot.captured.route)
        }

        @ParameterizedTest
        @ValueSource(booleans = [true, false])
        fun `fetching metadata failed`(backingDownAllowed: Boolean) = runTest {
            val state = IdentificationStateMachine.State.FetchingMetadataFailed(backingDownAllowed, testTokenUrl)
            testTransition(IdentificationStateMachine.Event.Invalidate, state, this)

            verify { mockNavigator.navigate(IdentificationOtherErrorDestination) }
        }

        @ParameterizedTest
        @ValueSource(booleans = [true, false])
        fun `request attribute confirmation`(backingDownAllowed: Boolean) = runTest {
            mockkStatic("android.util.Base64")
            every { Base64.encodeToString(any(), any()) } returns "serializedBase64"

            val request = EidAuthenticationRequest("issuer", "issuerUrl", "subject", "subjectUrl", "validity", AuthenticationTerms.Text(""), null, mapOf())
            val state = IdentificationStateMachine.State.RequestAttributeConfirmation(backingDownAllowed, request, attributeConfirmationCallback)
            testTransition(IdentificationStateMachine.Event.Invalidate, state, this)

            verify { mockNavigator.navigatePopping(any()) }

            Assertions.assertEquals(IdentificationAttributeConsentDestination(request, backingDownAllowed).route, destinationPoppingSlot.captured.route)
        }

        @ParameterizedTest
        @ValueSource(booleans = [true, false])
        fun `confirm attributes`(backingDownAllowed: Boolean) = runTest {
            val readAttributes: Map<IdCardAttribute, Boolean> = mapOf(Pair(IdCardAttribute.DG01, true), Pair(IdCardAttribute.DG02, false))
            every { request.readAttributes } returns readAttributes

            every { attributeConfirmationCallback(any()) } returns Unit

            val state = IdentificationStateMachine.State.SubmitAttributeConfirmation(backingDownAllowed, request, attributeConfirmationCallback)
            testTransition(IdentificationStateMachine.Event.Invalidate, state, this)

            verify { attributeConfirmationCallback(readAttributes.filterValues { it }) }
        }

        @ParameterizedTest
        @ValueSource(booleans = [true, false])
        fun `input PIN`(backingDownAllowed: Boolean) = runTest {
            val state = IdentificationStateMachine.State.PinInput(backingDownAllowed, request, pinCallback)
            testTransition(IdentificationStateMachine.Event.Invalidate, state, this)

            verify { mockNavigator.navigate(any()) }

            Assertions.assertEquals(IdentificationPersonalPinDestination(false).route, destinationSlot.captured.route)
        }

        @Test
        fun `input PIN retrying`() = runTest {
            val state = IdentificationStateMachine.State.PinInputRetry(pinCallback)
            testTransition(IdentificationStateMachine.Event.Invalidate, state, this)

            verify { mockNavigator.navigate(any()) }

            Assertions.assertEquals(IdentificationPersonalPinDestination(true).route, destinationSlot.captured.route)
        }

        @ParameterizedTest
        @ValueSource(booleans = [true, false])
        fun `revisit attributes`(backingDownAllowed: Boolean) = runTest {
            val state = IdentificationStateMachine.State.RevisitAttributes(backingDownAllowed, request, pinCallback)
            testTransition(IdentificationStateMachine.Event.Invalidate, state, this)

            verify { mockNavigator.pop() }
        }

        @Test
        fun `PIN entered`() = runTest {
            val pin = "123456"
            val state = IdentificationStateMachine.State.PinEntered(pin, false, pinCallback)
            testTransition(IdentificationStateMachine.Event.Invalidate, state, this)

            verify { pinCallback(pin) }
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
        fun `waiting for card attachment`() = runTest {
            val state = IdentificationStateMachine.State.WaitingForCardAttachment(null)
            testTransition(IdentificationStateMachine.Event.Invalidate, state, this)

            verify { mockNavigator.popUpToOrNavigate(IdentificationScanDestination, false) }
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

            verify { mockIdCardManager.cancelTask() }
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
            mockIdCardManager,
            mockStorageManager,
            mockTrackerManager,
            mockIdentificationStateMachine,
            mockCanStateMachine,
            mockCoroutineContextProvider
        )

        mockkStatic(Uri::class)

        val mockUriBuilder = mockk<Uri.Builder>()

        mockkConstructor(Uri.Builder::class)

        every {
            anyConstructed<Uri.Builder>()
                .scheme("http")
                .encodedAuthority("127.0.0.1:24727")
                .appendPath("eID-Client")
                .appendQueryParameter("tcTokenURL", testTokenUrl)
        } returns mockUriBuilder

        val normalizedUri = mockk<Uri>()

        every { mockUriBuilder.build() } returns normalizedUri
        every { normalizedUri.toString() } returns testUrl

        Assertions.assertEquals(SubCoordinatorState.FINISHED, identificationCoordinator.stateFlow.value)

        val tcTokenUrl = "tcTokenUrl"
        identificationCoordinator.startIdentificationProcess(testTokenUrl, setupSkipped)

        Assertions.assertEquals(SubCoordinatorState.ACTIVE, identificationCoordinator.stateFlow.value)

        verify { mockIdentificationStateMachine.transition(IdentificationStateMachine.Event.Initialize(setupSkipped, testUrl)) }
        verify { mockCanStateMachine.transition(CanStateMachine.Event.Invalidate) }
    }

    @Test
    fun `confirm attributes`() = runTest {
        val identificationCoordinator = IdentificationCoordinator(
            mockContext,
            mockCanCoordinator,
            mockNavigator,
            mockIdCardManager,
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
            mockIdCardManager,
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
            mockIdCardManager,
            mockStorageManager,
            mockTrackerManager,
            mockIdentificationStateMachine,
            mockCanStateMachine,
            mockCoroutineContextProvider
        )

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
            mockIdCardManager,
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
            mockIdCardManager,
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
            mockIdCardManager,
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
}
