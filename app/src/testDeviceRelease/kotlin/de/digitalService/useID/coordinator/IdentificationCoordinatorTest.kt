package de.digitalService.useID.coordinator

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Base64
import com.ramcosta.composedestinations.spec.Direction
import de.digitalService.useID.StorageManager
import de.digitalService.useID.analytics.IssueTrackerManagerType
import de.digitalService.useID.analytics.TrackerManagerType
import de.digitalService.useID.idCardInterface.*
import de.digitalService.useID.ui.coordinators.CanCoordinator
import de.digitalService.useID.ui.coordinators.IdentificationCoordinator
import de.digitalService.useID.ui.coordinators.SubCoordinatorState
import de.digitalService.useID.ui.navigation.Navigator
import de.digitalService.useID.ui.screens.destinations.*
import de.digitalService.useID.util.CoroutineContextProvider
import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.openecard.mobile.activation.ActivationResultCode

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
    lateinit var mockTrackerManager: TrackerManagerType

    @MockK(relaxUnitFun = true)
    lateinit var mockIssueTrackerManager: IssueTrackerManagerType

    @MockK(relaxUnitFun = true)
    lateinit var mockCoroutineContextProvider: CoroutineContextProvider

    @OptIn(ExperimentalCoroutinesApi::class)
    val dispatcher = StandardTestDispatcher()

    private val destinationSlot = slot<Direction>()
    private val destinationPoppingSlot = slot<Direction>()

    @OptIn(ExperimentalCoroutinesApi::class)
    @BeforeEach
    fun setup() {
        Dispatchers.setMain(dispatcher)

        mockkStatic(Uri::class)

        val mockUriBuilder = mockk<Uri.Builder>()

        mockkConstructor(Uri.Builder::class)

        every {
            anyConstructed<Uri.Builder>()
                .scheme("http")
                .encodedAuthority("127.0.0.1:24727")
                .appendPath("eID-Client")
                .appendQueryParameter("tcTokenURL", testTokenURL)
        } returns mockUriBuilder

        val mockedUri = mockk<Uri>()

        every { mockUriBuilder.build() } returns mockedUri

        every { mockedUri.toString() } returns testURL

        every { mockNavigator.navigate(capture(destinationSlot)) } returns Unit
        every { mockNavigator.navigatePopping(capture(destinationPoppingSlot)) } returns Unit
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private val testTokenURL = "https://token"
    private val testURL = "bundesident://127.0.0.1/eID-Client?tokenURL="

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun successfulIdentificationWithSetup() = runTest {

        // SETUP
        every { mockCoroutineContextProvider.IO } returns dispatcher

        val idCardManagerFlow = MutableStateFlow<EidInteractionEvent>(EidInteractionEvent.Idle)
        every { mockIdCardManager.eidFlow } returns idCardManagerFlow

        val mockIntent = mockkClass(Intent::class)
        mockkConstructor(Intent::class)
        every { anyConstructed<Intent>().addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) } returns mockIntent

        val identificationCoordinator = IdentificationCoordinator(
            canCoordinator = mockCanCoordinator,
            context = mockContext,
            navigator = mockNavigator,
            idCardManager = mockIdCardManager,
            storageManager = mockStorageManager,
            trackerManager = mockTrackerManager,
            issueTrackerManager = mockIssueTrackerManager,
            coroutineContextProvider = mockCoroutineContextProvider
        )

        var scanInProgress = false
        val scanJob = identificationCoordinator.scanInProgress
            .onEach { scanInProgress = it }
            .launchIn(CoroutineScope(dispatcher))

        val pinCallback = mockk<(String) -> Unit>()

        val testRequestAuthenticationRequestConfirmation =
            EidInteractionEvent.RequestAuthenticationRequestConfirmation(
                EidAuthenticationRequest(
                    "",
                    "",
                    "subject",
                    "",
                    "",
                    AuthenticationTerms.Text(""),
                    "",
                    mapOf()
                )
            ) {}

        val testRedirectUrl = "testRedirectUrl"
        mockkStatic("android.util.Base64")
        mockkStatic("android.net.Uri")
        every { Base64.encodeToString(any(), any()) } returns testRedirectUrl
        every { Uri.encode(any()) } returns testRedirectUrl
        every { Uri.decode(any()) } returns testRedirectUrl
        every { Uri.parse(testRedirectUrl) } returns Uri.EMPTY

        // START IDENTIFICATION PROCESS
        val setupSkipped = false
        identificationCoordinator.startIdentificationProcess(testTokenURL, setupSkipped)
        advanceUntilIdle()
        Assertions.assertEquals(
            SubCoordinatorState.Active,
            identificationCoordinator.stateFlow.value
        )
        verify(exactly = 1) { mockIdCardManager.cancelTask() }
        verify(exactly = 1) { mockIdCardManager.identify(mockContext, testURL) }

        // SEND AUTHENTIFICATION STARTED EVENT
        idCardManagerFlow.value = EidInteractionEvent.AuthenticationStarted
        advanceUntilIdle()
        var navigationParameter = destinationSlot.captured
        Assertions.assertEquals(
            IdentificationFetchMetadataDestination(setupSkipped).route,
            navigationParameter.route
        )

        // SEND TEST REQUEST CONFIRMATION
        idCardManagerFlow.value = testRequestAuthenticationRequestConfirmation
        advanceUntilIdle()
        navigationParameter = destinationPoppingSlot.captured
        Assertions.assertEquals(
            IdentificationAttributeConsentDestination(
                testRequestAuthenticationRequestConfirmation.request,
                setupSkipped
            ).route,
            navigationParameter.route
        )

        // CONFIRM ATTRIBUTES AND NAVIGATE TO PIN ENTRY
        identificationCoordinator.confirmAttributesForIdentification()
        val attempts: Int? = null
        idCardManagerFlow.value = EidInteractionEvent.RequestPin(attempts, pinCallback)
        advanceUntilIdle()
        navigationParameter = destinationSlot.captured
        Assertions.assertEquals(
            IdentificationPersonalPinDestination(false).route,
            navigationParameter.route
        )

        // SET PIN AND CONTINUE
        val pin = "111111"
        every { pinCallback(pin) } just Runs
        identificationCoordinator.setPin(pin)
        verify(exactly = 1) { pinCallback(pin) }
        idCardManagerFlow.value = EidInteractionEvent.RequestCardInsertion
        advanceUntilIdle()
        verify(exactly = 1) { mockNavigator.navigate(IdentificationScanDestination) }

        // RECOGNIZE CARD
        idCardManagerFlow.value = EidInteractionEvent.CardRecognized
        advanceUntilIdle()
        Assertions.assertTrue(scanInProgress)

        // FINISH WITH SUCCESS
        idCardManagerFlow.value =
            EidInteractionEvent.ProcessCompletedSuccessfullyWithRedirect(testRedirectUrl)
        advanceUntilIdle()
        verify(exactly = 1) { mockNavigator.popToRoot() }
        Assertions.assertEquals(
            SubCoordinatorState.Finished,
            identificationCoordinator.stateFlow.value
        )

        scanJob.cancel()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun successfulIdentificationWithoutSetup() = runTest {

        // SETUP
        every { mockCoroutineContextProvider.IO } returns dispatcher

        val idCardManagerFlow = MutableStateFlow<EidInteractionEvent>(EidInteractionEvent.Idle)
        every { mockIdCardManager.eidFlow } returns idCardManagerFlow

        val mockIntent = mockkClass(Intent::class)
        mockkConstructor(Intent::class)
        every { anyConstructed<Intent>().addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) } returns mockIntent

        val identificationCoordinator = IdentificationCoordinator(
            canCoordinator = mockCanCoordinator,
            context = mockContext,
            navigator = mockNavigator,
            idCardManager = mockIdCardManager,
            storageManager = mockStorageManager,
            trackerManager = mockTrackerManager,
            issueTrackerManager = mockIssueTrackerManager,
            coroutineContextProvider = mockCoroutineContextProvider
        )

        var scanInProgress = false
        val scanJob = identificationCoordinator.scanInProgress
            .onEach { scanInProgress = it }
            .launchIn(CoroutineScope(dispatcher))

        val pinCallback = mockk<(String) -> Unit>()

        val testRequestAuthenticationRequestConfirmation =
            EidInteractionEvent.RequestAuthenticationRequestConfirmation(
                EidAuthenticationRequest(
                    "",
                    "",
                    "subject",
                    "",
                    "",
                    AuthenticationTerms.Text(""),
                    "",
                    mapOf()
                )
            ) {}

        val testRedirectUrl = "testRedirectUrl"
        mockkStatic("android.util.Base64")
        mockkStatic("android.net.Uri")
        every { Base64.encodeToString(any(), any()) } returns testRedirectUrl
        every { Uri.encode(any()) } returns testRedirectUrl
        every { Uri.decode(any()) } returns testRedirectUrl
        every { Uri.parse(testRedirectUrl) } returns Uri.EMPTY

        // START IDENTIFICATION PROCESS
        val setupSkipped = true
        identificationCoordinator.startIdentificationProcess(testTokenURL, setupSkipped)
        advanceUntilIdle()
        Assertions.assertEquals(
            SubCoordinatorState.Active,
            identificationCoordinator.stateFlow.value
        )
        verify(exactly = 1) { mockIdCardManager.cancelTask() }
        verify(exactly = 1) { mockIdCardManager.identify(mockContext, testURL) }

        // SEND AUTHENTIFICATION STARTED EVENT
        idCardManagerFlow.value = EidInteractionEvent.AuthenticationStarted
        advanceUntilIdle()
        var navigationParameter = destinationSlot.captured
        Assertions.assertEquals(
            IdentificationFetchMetadataDestination(setupSkipped).route,
            navigationParameter.route
        )

        // SEND TEST REQUEST CONFIRMATION
        idCardManagerFlow.value = testRequestAuthenticationRequestConfirmation
        advanceUntilIdle()
        navigationParameter = destinationPoppingSlot.captured
        Assertions.assertEquals(
            IdentificationAttributeConsentDestination(
                testRequestAuthenticationRequestConfirmation.request,
                setupSkipped
            ).route,
            navigationParameter.route
        )

        // CONFIRM ATTRIBUTES AND NAVIGATE TO PIN ENTRY
        identificationCoordinator.confirmAttributesForIdentification()
        val attempts: Int? = null
        idCardManagerFlow.value = EidInteractionEvent.RequestPin(attempts, pinCallback)
        advanceUntilIdle()
        navigationParameter = destinationSlot.captured
        Assertions.assertEquals(
            IdentificationPersonalPinDestination(false).route,
            navigationParameter.route
        )

        // SET PIN AND CONTINUE
        val pin = "111111"
        every { pinCallback(pin) } just Runs
        identificationCoordinator.setPin(pin)
        verify(exactly = 1) { pinCallback(pin) }
        idCardManagerFlow.value = EidInteractionEvent.RequestCardInsertion
        advanceUntilIdle()
        verify(exactly = 1) { mockNavigator.navigate(IdentificationScanDestination) }

        // RECOGNIZE CARD
        idCardManagerFlow.value = EidInteractionEvent.CardRecognized
        advanceUntilIdle()
        Assertions.assertTrue(scanInProgress)

        // FINISH WITH SUCCESS
        idCardManagerFlow.value =
            EidInteractionEvent.ProcessCompletedSuccessfullyWithRedirect(testRedirectUrl)
        advanceUntilIdle()
        verify(exactly = 1) { mockNavigator.popToRoot() }
        Assertions.assertEquals(
            SubCoordinatorState.Finished,
            identificationCoordinator.stateFlow.value
        )

        scanJob.cancel()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun successfulIdentificationWithoutSetupCardRemovedAndAddedAgain() = runTest {

        // SETUP
        every { mockCoroutineContextProvider.IO } returns dispatcher

        val idCardManagerFlow = MutableStateFlow<EidInteractionEvent>(EidInteractionEvent.Idle)
        every { mockIdCardManager.eidFlow } returns idCardManagerFlow

        val mockIntent = mockkClass(Intent::class)
        mockkConstructor(Intent::class)
        every { anyConstructed<Intent>().addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) } returns mockIntent

        val identificationCoordinator = IdentificationCoordinator(
            canCoordinator = mockCanCoordinator,
            context = mockContext,
            navigator = mockNavigator,
            idCardManager = mockIdCardManager,
            storageManager = mockStorageManager,
            trackerManager = mockTrackerManager,
            issueTrackerManager = mockIssueTrackerManager,
            coroutineContextProvider = mockCoroutineContextProvider
        )

        var scanInProgress = false
        val scanJob = identificationCoordinator.scanInProgress
            .onEach { scanInProgress = it }
            .launchIn(CoroutineScope(dispatcher))

        val pinCallback = mockk<(String) -> Unit>()

        val testRequestAuthenticationRequestConfirmation =
            EidInteractionEvent.RequestAuthenticationRequestConfirmation(
                EidAuthenticationRequest(
                    "",
                    "",
                    "subject",
                    "",
                    "",
                    AuthenticationTerms.Text(""),
                    "",
                    mapOf()
                )
            ) {}

        val testRedirectUrl = "testRedirectUrl"
        mockkStatic("android.util.Base64")
        mockkStatic("android.net.Uri")
        every { Base64.encodeToString(any(), any()) } returns testRedirectUrl
        every { Uri.encode(any()) } returns testRedirectUrl
        every { Uri.decode(any()) } returns testRedirectUrl
        every { Uri.parse(testRedirectUrl) } returns Uri.EMPTY

        // START IDENTIFICATION PROCESS
        val setupSkipped = true
        identificationCoordinator.startIdentificationProcess(testTokenURL, setupSkipped)
        advanceUntilIdle()
        Assertions.assertEquals(
            SubCoordinatorState.Active,
            identificationCoordinator.stateFlow.value
        )
        verify(exactly = 1) { mockIdCardManager.cancelTask() }
        verify(exactly = 1) { mockIdCardManager.identify(mockContext, testURL) }

        // SEND AUTHENTIFICATION STARTED EVENT
        idCardManagerFlow.value = EidInteractionEvent.AuthenticationStarted
        advanceUntilIdle()
        var navigationParameter = destinationSlot.captured
        Assertions.assertEquals(
            IdentificationFetchMetadataDestination(setupSkipped).route,
            navigationParameter.route
        )

        // SEND TEST REQUEST CONFIRMATION
        idCardManagerFlow.value = testRequestAuthenticationRequestConfirmation
        advanceUntilIdle()
        navigationParameter = destinationPoppingSlot.captured
        Assertions.assertEquals(
            IdentificationAttributeConsentDestination(
                testRequestAuthenticationRequestConfirmation.request,
                setupSkipped
            ).route,
            navigationParameter.route
        )

        // CONFIRM ATTRIBUTES AND NAVIGATE TO PIN ENTRY
        identificationCoordinator.confirmAttributesForIdentification()
        val attempts: Int? = null
        idCardManagerFlow.value = EidInteractionEvent.RequestPin(attempts, pinCallback)
        advanceUntilIdle()
        navigationParameter = destinationSlot.captured
        Assertions.assertEquals(
            IdentificationPersonalPinDestination(false).route,
            navigationParameter.route
        )

        // SET PIN AND CONTINUE
        val pin = "111111"
        every { pinCallback(pin) } just Runs
        identificationCoordinator.setPin(pin)
        verify(exactly = 1) { pinCallback(pin) }
        idCardManagerFlow.value = EidInteractionEvent.RequestCardInsertion
        advanceUntilIdle()
        verify(exactly = 1) { mockNavigator.navigate(IdentificationScanDestination) }

        // RECOGNIZE CARD
        idCardManagerFlow.value = EidInteractionEvent.CardRecognized
        advanceUntilIdle()
        Assertions.assertTrue(scanInProgress)

        // CARD IS MOVED
        idCardManagerFlow.value = EidInteractionEvent.CardRemoved
        advanceUntilIdle()
        Assertions.assertFalse(scanInProgress)

        // RECOGNIZE CARD AGAIN
        idCardManagerFlow.value = EidInteractionEvent.CardRecognized
        advanceUntilIdle()
        Assertions.assertTrue(scanInProgress)

        // FINISH WITH SUCCESS
        idCardManagerFlow.value =
            EidInteractionEvent.ProcessCompletedSuccessfullyWithRedirect(testRedirectUrl)
        advanceUntilIdle()
        verify(exactly = 1) { mockNavigator.popToRoot() }
        Assertions.assertEquals(
            SubCoordinatorState.Finished,
            identificationCoordinator.stateFlow.value
        )

        scanJob.cancel()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun successfulIdentificationPinWrongOnce() = runTest {

        // SETUP
        every { mockCoroutineContextProvider.IO } returns dispatcher

        val idCardManagerFlow = MutableStateFlow<EidInteractionEvent>(EidInteractionEvent.Idle)
        every { mockIdCardManager.eidFlow } returns idCardManagerFlow

        val mockIntent = mockkClass(Intent::class)
        mockkConstructor(Intent::class)
        every { anyConstructed<Intent>().addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) } returns mockIntent

        val identificationCoordinator = IdentificationCoordinator(
            canCoordinator = mockCanCoordinator,
            context = mockContext,
            navigator = mockNavigator,
            idCardManager = mockIdCardManager,
            storageManager = mockStorageManager,
            trackerManager = mockTrackerManager,
            issueTrackerManager = mockIssueTrackerManager,
            coroutineContextProvider = mockCoroutineContextProvider
        )

        var scanInProgress = false
        val scanJob = identificationCoordinator.scanInProgress
            .onEach { scanInProgress = it }
            .launchIn(CoroutineScope(dispatcher))

        val pinCallback = mockk<(String) -> Unit>()

        val testRequestAuthenticationRequestConfirmation =
            EidInteractionEvent.RequestAuthenticationRequestConfirmation(
                EidAuthenticationRequest(
                    "",
                    "",
                    "subject",
                    "",
                    "",
                    AuthenticationTerms.Text(""),
                    "",
                    mapOf()
                )
            ) {}

        val testRedirectUrl = "testRedirectUrl"
        mockkStatic("android.util.Base64")
        mockkStatic("android.net.Uri")
        every { Base64.encodeToString(any(), any()) } returns testRedirectUrl
        every { Uri.encode(any()) } returns testRedirectUrl
        every { Uri.decode(any()) } returns testRedirectUrl
        every { Uri.parse(testRedirectUrl) } returns Uri.EMPTY

        // START IDENTIFICATION PROCESS
        val setupSkipped = true
        identificationCoordinator.startIdentificationProcess(testTokenURL, setupSkipped)
        advanceUntilIdle()
        Assertions.assertEquals(
            SubCoordinatorState.Active,
            identificationCoordinator.stateFlow.value
        )
        verify(exactly = 1) { mockIdCardManager.cancelTask() }
        verify(exactly = 1) { mockIdCardManager.identify(mockContext, testURL) }

        // SEND AUTHENTIFICATION STARTED EVENT
        idCardManagerFlow.value = EidInteractionEvent.AuthenticationStarted
        advanceUntilIdle()
        var navigationParameter = destinationSlot.captured
        Assertions.assertEquals(
            IdentificationFetchMetadataDestination(setupSkipped).route,
            navigationParameter.route
        )

        // SEND TEST REQUEST CONFIRMATION
        idCardManagerFlow.value = testRequestAuthenticationRequestConfirmation
        advanceUntilIdle()
        navigationParameter = destinationPoppingSlot.captured
        Assertions.assertEquals(
            IdentificationAttributeConsentDestination(
                testRequestAuthenticationRequestConfirmation.request,
                setupSkipped
            ).route,
            navigationParameter.route
        )

        // CONFIRM ATTRIBUTES AND NAVIGATE TO PIN ENTRY
        identificationCoordinator.confirmAttributesForIdentification()
        var attempts: Int? = null
        idCardManagerFlow.value = EidInteractionEvent.RequestPin(attempts, pinCallback)
        advanceUntilIdle()
        navigationParameter = destinationSlot.captured
        Assertions.assertEquals(
            IdentificationPersonalPinDestination(false).route,
            navigationParameter.route
        )

        // SET WRONG PIN AND CONTINUE
        var pin = "000000"
        every { pinCallback(pin) } just Runs
        identificationCoordinator.setPin(pin)
        verify(exactly = 1) { pinCallback(pin) }

        // REQUEST CARD
        idCardManagerFlow.value = EidInteractionEvent.RequestCardInsertion
        advanceUntilIdle()
        verify(exactly = 1) { mockNavigator.navigate(IdentificationScanDestination) }

        // RECOGNIZE CARD
        idCardManagerFlow.value = EidInteractionEvent.CardRecognized
        advanceUntilIdle()
        Assertions.assertTrue(scanInProgress)

        // PIN WAS WRONG, TRY AGAIN
        attempts = 1
        idCardManagerFlow.value = EidInteractionEvent.RequestPin(attempts, pinCallback)
        advanceUntilIdle()
        Assertions.assertFalse(scanInProgress)
        navigationParameter = destinationSlot.captured
        Assertions.assertEquals(
            IdentificationPersonalPinDestination(true).route,
            navigationParameter.route
        )

        // SET CORRECT PIN
        pin = "111111"
        every { pinCallback(pin) } just Runs
        identificationCoordinator.setPin(pin)
        verify(exactly = 1) { pinCallback(pin) }

        // REQUEST CARD
        idCardManagerFlow.value = EidInteractionEvent.RequestCardInsertion
        advanceUntilIdle()
        verify(exactly = 1) { mockNavigator.popUpTo(IdentificationScanDestination) }

        // RECOGNIZE CARD
        idCardManagerFlow.value = EidInteractionEvent.CardRecognized
        advanceUntilIdle()
        Assertions.assertTrue(scanInProgress)

        // FINISH WITH SUCCESS
        idCardManagerFlow.value =
            EidInteractionEvent.ProcessCompletedSuccessfullyWithRedirect(testRedirectUrl)
        advanceUntilIdle()
        verify(exactly = 1) { mockNavigator.popToRoot() }
        Assertions.assertEquals(
            SubCoordinatorState.Finished,
            identificationCoordinator.stateFlow.value
        )

        scanJob.cancel()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun identificationPinWrongTwiceStartsCanFlow() = runTest {

        // SETUP
        every { mockCoroutineContextProvider.IO } returns dispatcher

        val idCardManagerFlow = MutableStateFlow<EidInteractionEvent>(EidInteractionEvent.Idle)
        every { mockIdCardManager.eidFlow } returns idCardManagerFlow

        val identificationCoordinator = IdentificationCoordinator(
            canCoordinator = mockCanCoordinator,
            context = mockContext,
            navigator = mockNavigator,
            idCardManager = mockIdCardManager,
            storageManager = mockStorageManager,
            trackerManager = mockTrackerManager,
            issueTrackerManager = mockIssueTrackerManager,
            coroutineContextProvider = mockCoroutineContextProvider
        )

        var scanInProgress = false
        val scanJob = identificationCoordinator.scanInProgress
            .onEach { scanInProgress = it }
            .launchIn(CoroutineScope(dispatcher))

        val pinCallback = mockk<(String) -> Unit>()

        val testRequestAuthenticationRequestConfirmation =
            EidInteractionEvent.RequestAuthenticationRequestConfirmation(
                EidAuthenticationRequest(
                    "",
                    "",
                    "subject",
                    "",
                    "",
                    AuthenticationTerms.Text(""),
                    "",
                    mapOf()
                )
            ) {}

        val testRedirectUrl = "testRedirectUrl"
        mockkStatic("android.util.Base64")
        mockkStatic("android.net.Uri")
        every { Base64.encodeToString(any(), any()) } returns testRedirectUrl
        every { Uri.encode(any()) } returns testRedirectUrl
        every { Uri.decode(any()) } returns testRedirectUrl

        // START IDENTIFICATION PROCESS
        val setupSkipped = true
        identificationCoordinator.startIdentificationProcess(testTokenURL, setupSkipped)
        advanceUntilIdle()
        Assertions.assertEquals(
            SubCoordinatorState.Active,
            identificationCoordinator.stateFlow.value
        )
        verify(exactly = 1) { mockIdCardManager.cancelTask() }
        verify(exactly = 1) { mockIdCardManager.identify(mockContext, testURL) }

        // SEND AUTHENTIFICATION STARTED EVENT
        idCardManagerFlow.value = EidInteractionEvent.AuthenticationStarted
        advanceUntilIdle()
        var navigationParameter = destinationSlot.captured
        Assertions.assertEquals(
            IdentificationFetchMetadataDestination(setupSkipped).route,
            navigationParameter.route
        )

        // SEND TEST REQUEST CONFIRMATION
        idCardManagerFlow.value = testRequestAuthenticationRequestConfirmation
        advanceUntilIdle()
        navigationParameter = destinationPoppingSlot.captured
        Assertions.assertEquals(
            IdentificationAttributeConsentDestination(
                testRequestAuthenticationRequestConfirmation.request,
                setupSkipped
            ).route,
            navigationParameter.route
        )

        // CONFIRM ATTRIBUTES AND NAVIGATE TO PIN ENTRY
        identificationCoordinator.confirmAttributesForIdentification()
        var attempts: Int? = null
        idCardManagerFlow.value = EidInteractionEvent.RequestPin(attempts, pinCallback)
        advanceUntilIdle()
        navigationParameter = destinationSlot.captured
        Assertions.assertEquals(
            IdentificationPersonalPinDestination(false).route,
            navigationParameter.route
        )

        // SET WRONG PIN AND CONTINUE
        var pin = "000000"
        every { pinCallback(pin) } just Runs
        identificationCoordinator.setPin(pin)
        verify(exactly = 1) { pinCallback(pin) }

        // REQUEST CARD
        idCardManagerFlow.value = EidInteractionEvent.RequestCardInsertion
        advanceUntilIdle()
        verify(exactly = 1) { mockNavigator.navigate(IdentificationScanDestination) }

        // RECOGNIZE CARD
        idCardManagerFlow.value = EidInteractionEvent.CardRecognized
        advanceUntilIdle()
        Assertions.assertTrue(scanInProgress)

        // PIN WAS WRONG, TRY AGAIN
        attempts = 1
        idCardManagerFlow.value = EidInteractionEvent.RequestPin(attempts, pinCallback)
        advanceUntilIdle()
        Assertions.assertFalse(scanInProgress)
        navigationParameter = destinationSlot.captured
        Assertions.assertEquals(
            IdentificationPersonalPinDestination(true).route,
            navigationParameter.route
        )

        // SET WRONG PIN AGAIN
        pin = "000000"
        identificationCoordinator.setPin(pin)
        verify(exactly = 2) { pinCallback(pin) }

        // REQUEST CARD
        idCardManagerFlow.value = EidInteractionEvent.RequestCardInsertion
        advanceUntilIdle()
        verify(exactly = 1) { mockNavigator.popUpTo(IdentificationScanDestination) }

        // RECOGNIZE CARD
        idCardManagerFlow.value = EidInteractionEvent.CardRecognized
        advanceUntilIdle()
        Assertions.assertTrue(scanInProgress)

        // CAN FLOW IS STARTED
        val canCoordinatorStateFlow = MutableStateFlow(SubCoordinatorState.Cancelled)
        every { mockCanCoordinator.stateFlow } returns canCoordinatorStateFlow
        idCardManagerFlow.value = EidInteractionEvent.RequestPinAndCan { _, _ -> }
        advanceUntilIdle()
        Assertions.assertFalse(scanInProgress)
        verify(exactly = 1) { mockCanCoordinator.startIdentCanFlow(null) }

        scanJob.cancel()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun identificationCanRightAwayStartsCanFlow() = runTest {

        // SETUP
        every { mockCoroutineContextProvider.IO } returns dispatcher

        val idCardManagerFlow = MutableStateFlow<EidInteractionEvent>(EidInteractionEvent.Idle)
        every { mockIdCardManager.eidFlow } returns idCardManagerFlow

        val identificationCoordinator = IdentificationCoordinator(
            canCoordinator = mockCanCoordinator,
            context = mockContext,
            navigator = mockNavigator,
            idCardManager = mockIdCardManager,
            storageManager = mockStorageManager,
            trackerManager = mockTrackerManager,
            issueTrackerManager = mockIssueTrackerManager,
            coroutineContextProvider = mockCoroutineContextProvider
        )

        var scanInProgress = false
        val scanJob = identificationCoordinator.scanInProgress
            .onEach { scanInProgress = it }
            .launchIn(CoroutineScope(dispatcher))

        val pinCallback = mockk<(String) -> Unit>()

        val testRequestAuthenticationRequestConfirmation =
            EidInteractionEvent.RequestAuthenticationRequestConfirmation(
                EidAuthenticationRequest(
                    "",
                    "",
                    "subject",
                    "",
                    "",
                    AuthenticationTerms.Text(""),
                    "",
                    mapOf()
                )
            ) {}

        val testRedirectUrl = "testRedirectUrl"
        mockkStatic("android.util.Base64")
        mockkStatic("android.net.Uri")
        every { Base64.encodeToString(any(), any()) } returns testRedirectUrl
        every { Uri.encode(any()) } returns testRedirectUrl
        every { Uri.decode(any()) } returns testRedirectUrl

        // START IDENTIFICATION PROCESS
        val setupSkipped = true
        identificationCoordinator.startIdentificationProcess(testTokenURL, setupSkipped)
        advanceUntilIdle()
        Assertions.assertEquals(
            SubCoordinatorState.Active,
            identificationCoordinator.stateFlow.value
        )
        verify(exactly = 1) { mockIdCardManager.cancelTask() }
        verify(exactly = 1) { mockIdCardManager.identify(mockContext, testURL) }

        // SEND AUTHENTIFICATION STARTED EVENT
        idCardManagerFlow.value = EidInteractionEvent.AuthenticationStarted
        advanceUntilIdle()
        var navigationParameter = destinationSlot.captured
        Assertions.assertEquals(
            IdentificationFetchMetadataDestination(setupSkipped).route,
            navigationParameter.route
        )

        // SEND TEST REQUEST CONFIRMATION
        idCardManagerFlow.value = testRequestAuthenticationRequestConfirmation
        advanceUntilIdle()
        navigationParameter = destinationPoppingSlot.captured
        Assertions.assertEquals(
            IdentificationAttributeConsentDestination(
                testRequestAuthenticationRequestConfirmation.request,
                setupSkipped
            ).route,
            navigationParameter.route
        )

        // CONFIRM ATTRIBUTES AND NAVIGATE TO PIN ENTRY
        identificationCoordinator.confirmAttributesForIdentification()
        var attempts: Int? = null
        idCardManagerFlow.value = EidInteractionEvent.RequestPin(attempts, pinCallback)
        advanceUntilIdle()
        navigationParameter = destinationSlot.captured
        Assertions.assertEquals(
            IdentificationPersonalPinDestination(false).route,
            navigationParameter.route
        )

        // SET WRONG PIN AND CONTINUE
        var pin = "000000"
        every { pinCallback(pin) } just Runs
        identificationCoordinator.setPin(pin)
        verify(exactly = 1) { pinCallback(pin) }

        // REQUEST CARD
        idCardManagerFlow.value = EidInteractionEvent.RequestCardInsertion
        advanceUntilIdle()
        verify(exactly = 1) { mockNavigator.navigate(IdentificationScanDestination) }

        // RECOGNIZE CARD
        idCardManagerFlow.value = EidInteractionEvent.CardRecognized
        advanceUntilIdle()
        Assertions.assertTrue(scanInProgress)

        // CAN FLOW IS STARTED
        val canCoordinatorStateFlow = MutableStateFlow(SubCoordinatorState.Cancelled)
        every { mockCanCoordinator.stateFlow } returns canCoordinatorStateFlow
        idCardManagerFlow.value = EidInteractionEvent.RequestPinAndCan { _, _ -> }
        advanceUntilIdle()
        Assertions.assertFalse(scanInProgress)
        verify(exactly = 1) { mockCanCoordinator.startIdentCanFlow(pin) }

        scanJob.cancel()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun failedIdentificationPukRequired() = runTest {

        // SETUP
        every { mockCoroutineContextProvider.IO } returns dispatcher

        val idCardManagerFlow = MutableStateFlow<EidInteractionEvent>(EidInteractionEvent.Idle)
        every { mockIdCardManager.eidFlow } returns idCardManagerFlow

        val identificationCoordinator = IdentificationCoordinator(
            canCoordinator = mockCanCoordinator,
            context = mockContext,
            navigator = mockNavigator,
            idCardManager = mockIdCardManager,
            storageManager = mockStorageManager,
            trackerManager = mockTrackerManager,
            issueTrackerManager = mockIssueTrackerManager,
            coroutineContextProvider = mockCoroutineContextProvider
        )

        var scanInProgress = false
        val scanJob = identificationCoordinator.scanInProgress
            .onEach { scanInProgress = it }
            .launchIn(CoroutineScope(dispatcher))

        val pinCallback = mockk<(String) -> Unit>()

        val testRequestAuthenticationRequestConfirmation =
            EidInteractionEvent.RequestAuthenticationRequestConfirmation(
                EidAuthenticationRequest(
                    "",
                    "",
                    "subject",
                    "",
                    "",
                    AuthenticationTerms.Text(""),
                    "",
                    mapOf()
                )
            ) {}

        val testRedirectUrl = "testRedirectUrl"
        mockkStatic("android.util.Base64")
        mockkStatic("android.net.Uri")
        every { Base64.encodeToString(any(), any()) } returns testRedirectUrl
        every { Uri.encode(any()) } returns testRedirectUrl
        every { Uri.decode(any()) } returns testRedirectUrl

        // START IDENTIFICATION PROCESS
        val setupSkipped = true
        identificationCoordinator.startIdentificationProcess(testTokenURL, setupSkipped)
        advanceUntilIdle()
        Assertions.assertEquals(
            SubCoordinatorState.Active,
            identificationCoordinator.stateFlow.value
        )
        verify(exactly = 1) { mockIdCardManager.cancelTask() }
        verify(exactly = 1) { mockIdCardManager.identify(mockContext, testURL) }

        // SEND AUTHENTIFICATION STARTED EVENT
        idCardManagerFlow.value = EidInteractionEvent.AuthenticationStarted
        advanceUntilIdle()
        var navigationParameter = destinationSlot.captured
        Assertions.assertEquals(
            IdentificationFetchMetadataDestination(setupSkipped).route,
            navigationParameter.route
        )

        // SEND TEST REQUEST CONFIRMATION
        idCardManagerFlow.value = testRequestAuthenticationRequestConfirmation
        advanceUntilIdle()
        navigationParameter = destinationPoppingSlot.captured
        Assertions.assertEquals(
            IdentificationAttributeConsentDestination(
                testRequestAuthenticationRequestConfirmation.request,
                setupSkipped
            ).route,
            navigationParameter.route
        )

        // CONFIRM ATTRIBUTES AND NAVIGATE TO PIN ENTRY
        identificationCoordinator.confirmAttributesForIdentification()
        val attempts = 3
        idCardManagerFlow.value = EidInteractionEvent.RequestPin(attempts, pinCallback)
        advanceUntilIdle()
        navigationParameter = destinationSlot.captured
        Assertions.assertEquals(
            IdentificationPersonalPinDestination(true).route,
            navigationParameter.route
        )

        // SET WRONG PIN THIRD TIME AND CONTINUE
        val pin = "000000"
        every { pinCallback(pin) } just Runs
        identificationCoordinator.setPin(pin)
        verify(exactly = 1) { pinCallback(pin) }

        // REQUEST CARD
        idCardManagerFlow.value = EidInteractionEvent.RequestCardInsertion
        advanceUntilIdle()
        verify(exactly = 1) { mockNavigator.navigate(IdentificationScanDestination) }

        // RECOGNIZE CARD
        idCardManagerFlow.value = EidInteractionEvent.CardRecognized
        advanceUntilIdle()
        Assertions.assertTrue(scanInProgress)

        // PIN WAS WRONG, PUK NEEDED
        idCardManagerFlow.value = EidInteractionEvent.RequestPuk{}
        advanceUntilIdle()
        Assertions.assertFalse(scanInProgress)
        verify(exactly = 2) { mockIdCardManager.cancelTask() }
        verify(exactly = 1) { mockNavigator.navigate(IdentificationCardBlockedDestination) }
        Assertions.assertEquals(
            SubCoordinatorState.Cancelled,
            identificationCoordinator.stateFlow.value
        )

        // CANCEL IDENTIFICATION
        identificationCoordinator.cancelIdentification()
        verify(exactly = 3) { mockIdCardManager.cancelTask() }
        verify(exactly = 1) { mockNavigator.popToRoot() }
        advanceUntilIdle()
        Assertions.assertEquals(
            SubCoordinatorState.Cancelled,
            identificationCoordinator.stateFlow.value
        )

        scanJob.cancel()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun cancelOnFetchMetaDataWithoutSetup() = runTest {

        // SETUP
        every { mockCoroutineContextProvider.IO } returns dispatcher

        val idCardManagerFlow = MutableStateFlow<EidInteractionEvent>(EidInteractionEvent.Idle)
        every { mockIdCardManager.eidFlow } returns idCardManagerFlow

        val identificationCoordinator = IdentificationCoordinator(
            canCoordinator = mockCanCoordinator,
            context = mockContext,
            navigator = mockNavigator,
            idCardManager = mockIdCardManager,
            storageManager = mockStorageManager,
            trackerManager = mockTrackerManager,
            issueTrackerManager = mockIssueTrackerManager,
            coroutineContextProvider = mockCoroutineContextProvider
        )

        // START IDENTIFICATION PROCESS
        val setupSkipped = true
        identificationCoordinator.startIdentificationProcess(testTokenURL, setupSkipped)
        advanceUntilIdle()
        Assertions.assertEquals(
            SubCoordinatorState.Active,
            identificationCoordinator.stateFlow.value
        )
        verify(exactly = 1) { mockIdCardManager.cancelTask() }
        verify(exactly = 1) { mockIdCardManager.identify(mockContext, testURL) }

        // SEND AUTHENTIFICATION STARTED EVENT
        idCardManagerFlow.value = EidInteractionEvent.AuthenticationStarted
        advanceUntilIdle()
        val navigationParameter = destinationSlot.captured
        Assertions.assertEquals(
            IdentificationFetchMetadataDestination(setupSkipped).route,
            navigationParameter.route
        )

        // CANCEL IDENTIFICATION
        identificationCoordinator.cancelIdentification()

        verify(exactly = 2) { mockIdCardManager.cancelTask() }
        verify(exactly = 1) { mockNavigator.popToRoot() }
        advanceUntilIdle()
        Assertions.assertEquals(
            SubCoordinatorState.Cancelled,
            identificationCoordinator.stateFlow.value
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun cancelOnAttributesConfirmationWithoutSetup() = runTest {

        // SETUP
        every { mockCoroutineContextProvider.IO } returns dispatcher

        val idCardManagerFlow = MutableStateFlow<EidInteractionEvent>(EidInteractionEvent.Idle)
        every { mockIdCardManager.eidFlow } returns idCardManagerFlow

        val identificationCoordinator = IdentificationCoordinator(
            canCoordinator = mockCanCoordinator,
            context = mockContext,
            navigator = mockNavigator,
            idCardManager = mockIdCardManager,
            storageManager = mockStorageManager,
            trackerManager = mockTrackerManager,
            issueTrackerManager = mockIssueTrackerManager,
            coroutineContextProvider = mockCoroutineContextProvider
        )

        val testRequestAuthenticationRequestConfirmation =
            EidInteractionEvent.RequestAuthenticationRequestConfirmation(
                EidAuthenticationRequest(
                    "",
                    "",
                    "subject",
                    "",
                    "",
                    AuthenticationTerms.Text(""),
                    "",
                    mapOf()
                )
            ) {}

        val testRedirectUrl = "testRedirectUrl"
        mockkStatic("android.util.Base64")
        mockkStatic("android.net.Uri")
        every { Base64.encodeToString(any(), any()) } returns testRedirectUrl
        every { Uri.encode(any()) } returns testRedirectUrl
        every { Uri.decode(any()) } returns testRedirectUrl

        // START IDENTIFICATION PROCESS
        val setupSkipped = true
        identificationCoordinator.startIdentificationProcess(testTokenURL, setupSkipped)
        advanceUntilIdle()
        Assertions.assertEquals(
            SubCoordinatorState.Active,
            identificationCoordinator.stateFlow.value
        )
        verify(exactly = 1) { mockIdCardManager.cancelTask() }
        verify(exactly = 1) { mockIdCardManager.identify(mockContext, testURL) }

        // SEND AUTHENTIFICATION STARTED EVENT
        idCardManagerFlow.value = EidInteractionEvent.AuthenticationStarted
        advanceUntilIdle()
        var navigationParameter = destinationSlot.captured
        Assertions.assertEquals(
            IdentificationFetchMetadataDestination(setupSkipped).route,
            navigationParameter.route
        )

        // SEND TEST REQUEST CONFIRMATION
        idCardManagerFlow.value = testRequestAuthenticationRequestConfirmation
        advanceUntilIdle()
        navigationParameter = destinationPoppingSlot.captured
        Assertions.assertEquals(
            IdentificationAttributeConsentDestination(
                testRequestAuthenticationRequestConfirmation.request,
                setupSkipped
            ).route,
            navigationParameter.route
        )

        // CANCEL IDENTIFICATION
        identificationCoordinator.cancelIdentification()

        verify(exactly = 2) { mockIdCardManager.cancelTask() }
        verify(exactly = 1) { mockNavigator.popToRoot() }
        advanceUntilIdle()
        Assertions.assertEquals(
            SubCoordinatorState.Cancelled,
            identificationCoordinator.stateFlow.value
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun cancelOnPinRetrySetup() = runTest {

        // SETUP
        every { mockCoroutineContextProvider.IO } returns dispatcher

        val idCardManagerFlow = MutableStateFlow<EidInteractionEvent>(EidInteractionEvent.Idle)
        every { mockIdCardManager.eidFlow } returns idCardManagerFlow

        val identificationCoordinator = IdentificationCoordinator(
            canCoordinator = mockCanCoordinator,
            context = mockContext,
            navigator = mockNavigator,
            idCardManager = mockIdCardManager,
            storageManager = mockStorageManager,
            trackerManager = mockTrackerManager,
            issueTrackerManager = mockIssueTrackerManager,
            coroutineContextProvider = mockCoroutineContextProvider
        )

        val pinCallback = mockk<(String) -> Unit>()

        val testRequestAuthenticationRequestConfirmation =
            EidInteractionEvent.RequestAuthenticationRequestConfirmation(
                EidAuthenticationRequest(
                    "",
                    "",
                    "subject",
                    "",
                    "",
                    AuthenticationTerms.Text(""),
                    "",
                    mapOf()
                )
            ) {}

        val testRedirectUrl = "testRedirectUrl"
        mockkStatic("android.util.Base64")
        mockkStatic("android.net.Uri")
        every { Base64.encodeToString(any(), any()) } returns testRedirectUrl
        every { Uri.encode(any()) } returns testRedirectUrl
        every { Uri.decode(any()) } returns testRedirectUrl

        // START IDENTIFICATION PROCESS
        val setupSkipped = true
        identificationCoordinator.startIdentificationProcess(testTokenURL, setupSkipped)
        advanceUntilIdle()
        Assertions.assertEquals(
            SubCoordinatorState.Active,
            identificationCoordinator.stateFlow.value
        )
        verify(exactly = 1) { mockIdCardManager.cancelTask() }
        verify(exactly = 1) { mockIdCardManager.identify(mockContext, testURL) }

        // SEND AUTHENTIFICATION STARTED EVENT
        idCardManagerFlow.value = EidInteractionEvent.AuthenticationStarted
        advanceUntilIdle()
        var navigationParameter = destinationSlot.captured
        Assertions.assertEquals(
            IdentificationFetchMetadataDestination(setupSkipped).route,
            navigationParameter.route
        )

        // SEND TEST REQUEST CONFIRMATION
        idCardManagerFlow.value = testRequestAuthenticationRequestConfirmation
        advanceUntilIdle()
        navigationParameter = destinationPoppingSlot.captured
        Assertions.assertEquals(
            IdentificationAttributeConsentDestination(
                testRequestAuthenticationRequestConfirmation.request,
                setupSkipped
            ).route,
            navigationParameter.route
        )

        // CONFIRM ATTRIBUTES AND NAVIGATE TO PIN ENTRY
        identificationCoordinator.confirmAttributesForIdentification()
        val attempts = 1
        idCardManagerFlow.value = EidInteractionEvent.RequestPin(attempts, pinCallback)
        advanceUntilIdle()
        navigationParameter = destinationSlot.captured
        Assertions.assertEquals(
            IdentificationPersonalPinDestination(true).route,
            navigationParameter.route
        )

        // CANCEL IDENTIFICATION
        identificationCoordinator.cancelIdentification()
        verify(exactly = 2) { mockIdCardManager.cancelTask() }
        verify(exactly = 1) { mockNavigator.popToRoot() }
        advanceUntilIdle()
        Assertions.assertEquals(
            SubCoordinatorState.Cancelled,
            identificationCoordinator.stateFlow.value
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun cancelOnScan() = runTest {

        // SETUP
        every { mockCoroutineContextProvider.IO } returns dispatcher

        val idCardManagerFlow = MutableStateFlow<EidInteractionEvent>(EidInteractionEvent.Idle)
        every { mockIdCardManager.eidFlow } returns idCardManagerFlow

        val identificationCoordinator = IdentificationCoordinator(
            canCoordinator = mockCanCoordinator,
            context = mockContext,
            navigator = mockNavigator,
            idCardManager = mockIdCardManager,
            storageManager = mockStorageManager,
            trackerManager = mockTrackerManager,
            issueTrackerManager = mockIssueTrackerManager,
            coroutineContextProvider = mockCoroutineContextProvider
        )

        val pinCallback = mockk<(String) -> Unit>()

        val testRequestAuthenticationRequestConfirmation =
            EidInteractionEvent.RequestAuthenticationRequestConfirmation(
                EidAuthenticationRequest(
                    "",
                    "",
                    "subject",
                    "",
                    "",
                    AuthenticationTerms.Text(""),
                    "",
                    mapOf()
                )
            ) {}

        val testRedirectUrl = "testRedirectUrl"
        mockkStatic("android.util.Base64")
        mockkStatic("android.net.Uri")
        every { Base64.encodeToString(any(), any()) } returns testRedirectUrl
        every { Uri.encode(any()) } returns testRedirectUrl
        every { Uri.decode(any()) } returns testRedirectUrl

        // START IDENTIFICATION PROCESS
        val setupSkipped = true
        identificationCoordinator.startIdentificationProcess(testTokenURL, setupSkipped)
        advanceUntilIdle()
        Assertions.assertEquals(
            SubCoordinatorState.Active,
            identificationCoordinator.stateFlow.value
        )
        verify(exactly = 1) { mockIdCardManager.cancelTask() }
        verify(exactly = 1) { mockIdCardManager.identify(mockContext, testURL) }

        // SEND AUTHENTIFICATION STARTED EVENT
        idCardManagerFlow.value = EidInteractionEvent.AuthenticationStarted
        advanceUntilIdle()
        var navigationParameter = destinationSlot.captured
        Assertions.assertEquals(
            IdentificationFetchMetadataDestination(setupSkipped).route,
            navigationParameter.route
        )

        // SEND TEST REQUEST CONFIRMATION
        idCardManagerFlow.value = testRequestAuthenticationRequestConfirmation
        advanceUntilIdle()
        navigationParameter = destinationPoppingSlot.captured
        Assertions.assertEquals(
            IdentificationAttributeConsentDestination(
                testRequestAuthenticationRequestConfirmation.request,
                setupSkipped
            ).route,
            navigationParameter.route
        )

        // CONFIRM ATTRIBUTES AND NAVIGATE TO PIN ENTRY
        identificationCoordinator.confirmAttributesForIdentification()
        val attempts: Int? = null
        idCardManagerFlow.value = EidInteractionEvent.RequestPin(attempts, pinCallback)
        advanceUntilIdle()
        navigationParameter = destinationSlot.captured
        Assertions.assertEquals(
            IdentificationPersonalPinDestination(false).route,
            navigationParameter.route
        )

        // SET CORRECT PIN
        val pin = "111111"
        every { pinCallback(pin) } just Runs
        identificationCoordinator.setPin(pin)
        verify(exactly = 1) { pinCallback(pin) }

        // REQUEST CARD
        idCardManagerFlow.value = EidInteractionEvent.RequestCardInsertion
        advanceUntilIdle()
        verify(exactly = 1) { mockNavigator.navigate(IdentificationScanDestination) }

        // CANCEL IDENTIFICATION
        identificationCoordinator.cancelIdentification()
        verify(exactly = 2) { mockIdCardManager.cancelTask() }
        verify(exactly = 1) { mockNavigator.popToRoot() }
        advanceUntilIdle()
        Assertions.assertEquals(
            SubCoordinatorState.Cancelled,
            identificationCoordinator.stateFlow.value
        )
    }

    // TEST ERROR HANDLING

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun errorCardDeactivated() = runTest {

        // SETUP
        every { mockCoroutineContextProvider.IO } returns dispatcher

        val idCardManagerFlow = MutableStateFlow<EidInteractionEvent>(EidInteractionEvent.Idle)
        every { mockIdCardManager.eidFlow } returns idCardManagerFlow

        val mockIntent = mockkClass(Intent::class)
        mockkConstructor(Intent::class)
        every { anyConstructed<Intent>().addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) } returns mockIntent

        val identificationCoordinator = IdentificationCoordinator(
            canCoordinator = mockCanCoordinator,
            context = mockContext,
            navigator = mockNavigator,
            idCardManager = mockIdCardManager,
            storageManager = mockStorageManager,
            trackerManager = mockTrackerManager,
            issueTrackerManager = mockIssueTrackerManager,
            coroutineContextProvider = mockCoroutineContextProvider
        )

        var scanInProgress = false
        val scanJob = identificationCoordinator.scanInProgress
            .onEach { scanInProgress = it }
            .launchIn(CoroutineScope(dispatcher))

        val pinCallback = mockk<(String) -> Unit>()

        val testRequestAuthenticationRequestConfirmation =
            EidInteractionEvent.RequestAuthenticationRequestConfirmation(
                EidAuthenticationRequest(
                    "",
                    "",
                    "subject",
                    "",
                    "",
                    AuthenticationTerms.Text(""),
                    "",
                    mapOf()
                )
            ) {}

        val testRedirectUrl = "testRedirectUrl"
        mockkStatic("android.util.Base64")
        mockkStatic("android.net.Uri")
        every { Base64.encodeToString(any(), any()) } returns testRedirectUrl
        every { Uri.encode(any()) } returns testRedirectUrl
        every { Uri.decode(any()) } returns testRedirectUrl
        every { Uri.parse(testRedirectUrl) } returns Uri.EMPTY

        // START IDENTIFICATION PROCESS
        val setupSkipped = false
        identificationCoordinator.startIdentificationProcess(testTokenURL, setupSkipped)
        advanceUntilIdle()
        Assertions.assertEquals(
            SubCoordinatorState.Active,
            identificationCoordinator.stateFlow.value
        )
        verify(exactly = 1) { mockIdCardManager.cancelTask() }
        verify(exactly = 1) { mockIdCardManager.identify(mockContext, testURL) }

        // SEND AUTHENTIFICATION STARTED EVENT
        idCardManagerFlow.value = EidInteractionEvent.AuthenticationStarted
        advanceUntilIdle()
        var navigationParameter = destinationSlot.captured
        Assertions.assertEquals(
            IdentificationFetchMetadataDestination(setupSkipped).route,
            navigationParameter.route
        )

        // SEND TEST REQUEST CONFIRMATION
        idCardManagerFlow.value = testRequestAuthenticationRequestConfirmation
        advanceUntilIdle()
        navigationParameter = destinationPoppingSlot.captured
        Assertions.assertEquals(
            IdentificationAttributeConsentDestination(
                testRequestAuthenticationRequestConfirmation.request,
                setupSkipped
            ).route,
            navigationParameter.route
        )

        // CONFIRM ATTRIBUTES AND NAVIGATE TO PIN ENTRY
        identificationCoordinator.confirmAttributesForIdentification()
        val attempts: Int? = null
        idCardManagerFlow.value = EidInteractionEvent.RequestPin(attempts, pinCallback)
        advanceUntilIdle()
        navigationParameter = destinationSlot.captured
        Assertions.assertEquals(
            IdentificationPersonalPinDestination(false).route,
            navigationParameter.route
        )

        // SET PIN AND CONTINUE
        val pin = "111111"
        every { pinCallback(pin) } just Runs
        identificationCoordinator.setPin(pin)
        verify(exactly = 1) { pinCallback(pin) }
        idCardManagerFlow.value = EidInteractionEvent.RequestCardInsertion
        advanceUntilIdle()
        verify(exactly = 1) { mockNavigator.navigate(IdentificationScanDestination) }

        // RECOGNIZE CARD
        idCardManagerFlow.value = EidInteractionEvent.CardRecognized
        advanceUntilIdle()
        Assertions.assertTrue(scanInProgress)

        // CARD DEACTIVATED ERROR
        idCardManagerFlow.value =
            EidInteractionEvent.Error(IdCardInteractionException.CardDeactivated)
        advanceUntilIdle()
        Assertions.assertFalse(scanInProgress)
        verify(exactly = 1) { mockTrackerManager.trackScreen("identification/cardDeactivated") }
        verify(exactly = 1) { mockNavigator.navigate(IdentificationCardDeactivatedDestination) }
        verify(exactly = 2) { mockIdCardManager.cancelTask() }

        identificationCoordinator.cancelIdentification()
        advanceUntilIdle()
        Assertions.assertEquals(
            SubCoordinatorState.Cancelled,
            identificationCoordinator.stateFlow.value
        )
        verify(exactly = 1) { mockNavigator.popToRoot() }
        verify(exactly = 3) { mockIdCardManager.cancelTask() }

        scanJob.cancel()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun errorCardBlocked() = runTest {

        // SETUP
        every { mockCoroutineContextProvider.IO } returns dispatcher

        val idCardManagerFlow = MutableStateFlow<EidInteractionEvent>(EidInteractionEvent.Idle)
        every { mockIdCardManager.eidFlow } returns idCardManagerFlow

        val mockIntent = mockkClass(Intent::class)
        mockkConstructor(Intent::class)
        every { anyConstructed<Intent>().addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) } returns mockIntent

        val identificationCoordinator = IdentificationCoordinator(
            canCoordinator = mockCanCoordinator,
            context = mockContext,
            navigator = mockNavigator,
            idCardManager = mockIdCardManager,
            storageManager = mockStorageManager,
            trackerManager = mockTrackerManager,
            issueTrackerManager = mockIssueTrackerManager,
            coroutineContextProvider = mockCoroutineContextProvider
        )

        var scanInProgress = false
        val scanJob = identificationCoordinator.scanInProgress
            .onEach { scanInProgress = it }
            .launchIn(CoroutineScope(dispatcher))

        val pinCallback = mockk<(String) -> Unit>()

        val testRequestAuthenticationRequestConfirmation =
            EidInteractionEvent.RequestAuthenticationRequestConfirmation(
                EidAuthenticationRequest(
                    "",
                    "",
                    "subject",
                    "",
                    "",
                    AuthenticationTerms.Text(""),
                    "",
                    mapOf()
                )
            ) {}

        val testRedirectUrl = "testRedirectUrl"
        mockkStatic("android.util.Base64")
        mockkStatic("android.net.Uri")
        every { Base64.encodeToString(any(), any()) } returns testRedirectUrl
        every { Uri.encode(any()) } returns testRedirectUrl
        every { Uri.decode(any()) } returns testRedirectUrl
        every { Uri.parse(testRedirectUrl) } returns Uri.EMPTY

        // START IDENTIFICATION PROCESS
        val setupSkipped = false
        identificationCoordinator.startIdentificationProcess(testTokenURL, setupSkipped)
        advanceUntilIdle()
        Assertions.assertEquals(
            SubCoordinatorState.Active,
            identificationCoordinator.stateFlow.value
        )
        verify(exactly = 1) { mockIdCardManager.cancelTask() }
        verify(exactly = 1) { mockIdCardManager.identify(mockContext, testURL) }

        // SEND AUTHENTIFICATION STARTED EVENT
        idCardManagerFlow.value = EidInteractionEvent.AuthenticationStarted
        advanceUntilIdle()
        var navigationParameter = destinationSlot.captured
        Assertions.assertEquals(
            IdentificationFetchMetadataDestination(setupSkipped).route,
            navigationParameter.route
        )

        // SEND TEST REQUEST CONFIRMATION
        idCardManagerFlow.value = testRequestAuthenticationRequestConfirmation
        advanceUntilIdle()
        navigationParameter = destinationPoppingSlot.captured
        Assertions.assertEquals(
            IdentificationAttributeConsentDestination(
                testRequestAuthenticationRequestConfirmation.request,
                setupSkipped
            ).route,
            navigationParameter.route
        )

        // CONFIRM ATTRIBUTES AND NAVIGATE TO PIN ENTRY
        identificationCoordinator.confirmAttributesForIdentification()
        val attempts: Int? = null
        idCardManagerFlow.value = EidInteractionEvent.RequestPin(attempts, pinCallback)
        advanceUntilIdle()
        navigationParameter = destinationSlot.captured
        Assertions.assertEquals(
            IdentificationPersonalPinDestination(false).route,
            navigationParameter.route
        )

        // SET PIN AND CONTINUE
        val pin = "111111"
        every { pinCallback(pin) } just Runs
        identificationCoordinator.setPin(pin)
        verify(exactly = 1) { pinCallback(pin) }
        idCardManagerFlow.value = EidInteractionEvent.RequestCardInsertion
        advanceUntilIdle()
        verify(exactly = 1) { mockNavigator.navigate(IdentificationScanDestination) }

        // RECOGNIZE CARD
        idCardManagerFlow.value = EidInteractionEvent.CardRecognized
        advanceUntilIdle()
        Assertions.assertTrue(scanInProgress)

        // CARD BLOCKED ERROR
        idCardManagerFlow.value = EidInteractionEvent.Error(IdCardInteractionException.CardBlocked)
        advanceUntilIdle()
        Assertions.assertFalse(scanInProgress)
        verify(exactly = 1) { mockTrackerManager.trackScreen("identification/cardBlocked") }
        verify(exactly = 1) { mockNavigator.navigate(IdentificationCardBlockedDestination) }
        verify(exactly = 2) { mockIdCardManager.cancelTask() }

        identificationCoordinator.cancelIdentification()
        advanceUntilIdle()
        Assertions.assertEquals(
            SubCoordinatorState.Cancelled,
            identificationCoordinator.stateFlow.value
        )
        verify(exactly = 1) { mockNavigator.popToRoot() }
        verify(exactly = 3) { mockIdCardManager.cancelTask() }

        scanJob.cancel()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun errorProcessFailedScanStateReachedWithRedirectUrl() = runTest {

        // SETUP
        every { mockCoroutineContextProvider.IO } returns dispatcher

        val idCardManagerFlow = MutableStateFlow<EidInteractionEvent>(EidInteractionEvent.Idle)
        every { mockIdCardManager.eidFlow } returns idCardManagerFlow

        val mockIntent = mockkClass(Intent::class)
        mockkConstructor(Intent::class)
        every { anyConstructed<Intent>().addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) } returns mockIntent

        val identificationCoordinator = IdentificationCoordinator(
            canCoordinator = mockCanCoordinator,
            context = mockContext,
            navigator = mockNavigator,
            idCardManager = mockIdCardManager,
            storageManager = mockStorageManager,
            trackerManager = mockTrackerManager,
            issueTrackerManager = mockIssueTrackerManager,
            coroutineContextProvider = mockCoroutineContextProvider
        )

        var scanInProgress = false
        val scanJob = identificationCoordinator.scanInProgress
            .onEach { scanInProgress = it }
            .launchIn(CoroutineScope(dispatcher))

        val pinCallback = mockk<(String) -> Unit>()

        val testRequestAuthenticationRequestConfirmation =
            EidInteractionEvent.RequestAuthenticationRequestConfirmation(
                EidAuthenticationRequest(
                    "",
                    "",
                    "subject",
                    "",
                    "",
                    AuthenticationTerms.Text(""),
                    "",
                    mapOf()
                )
            ) {}

        val testRedirectUrl = "testRedirectUrl"
        mockkStatic("android.util.Base64")
        mockkStatic("android.net.Uri")
        every { Base64.encodeToString(any(), any()) } returns testRedirectUrl
        every { Uri.encode(any()) } returns testRedirectUrl
        every { Uri.decode(any()) } returns testRedirectUrl
        every { Uri.parse(testRedirectUrl) } returns Uri.EMPTY

        // START IDENTIFICATION PROCESS
        val setupSkipped = false
        identificationCoordinator.startIdentificationProcess(testTokenURL, setupSkipped)
        advanceUntilIdle()
        Assertions.assertEquals(
            SubCoordinatorState.Active,
            identificationCoordinator.stateFlow.value
        )
        verify(exactly = 1) { mockIdCardManager.cancelTask() }
        verify(exactly = 1) { mockIdCardManager.identify(mockContext, testURL) }

        // SEND AUTHENTIFICATION STARTED EVENT
        idCardManagerFlow.value = EidInteractionEvent.AuthenticationStarted
        advanceUntilIdle()
        var navigationParameter = destinationSlot.captured
        Assertions.assertEquals(
            IdentificationFetchMetadataDestination(setupSkipped).route,
            navigationParameter.route
        )

        // SEND TEST REQUEST CONFIRMATION
        idCardManagerFlow.value = testRequestAuthenticationRequestConfirmation
        advanceUntilIdle()
        navigationParameter = destinationPoppingSlot.captured
        Assertions.assertEquals(
            IdentificationAttributeConsentDestination(
                testRequestAuthenticationRequestConfirmation.request,
                setupSkipped
            ).route,
            navigationParameter.route
        )

        // CONFIRM ATTRIBUTES AND NAVIGATE TO PIN ENTRY
        identificationCoordinator.confirmAttributesForIdentification()
        val attempts: Int? = null
        idCardManagerFlow.value = EidInteractionEvent.RequestPin(attempts, pinCallback)
        advanceUntilIdle()
        navigationParameter = destinationSlot.captured
        Assertions.assertEquals(
            IdentificationPersonalPinDestination(false).route,
            navigationParameter.route
        )

        // SET PIN AND CONTINUE
        val pin = "111111"
        every { pinCallback(pin) } just Runs
        identificationCoordinator.setPin(pin)
        verify(exactly = 1) { pinCallback(pin) }
        idCardManagerFlow.value = EidInteractionEvent.RequestCardInsertion
        advanceUntilIdle()
        verify(exactly = 1) { mockNavigator.navigate(IdentificationScanDestination) }

        // RECOGNIZE CARD
        idCardManagerFlow.value = EidInteractionEvent.CardRecognized
        advanceUntilIdle()
        Assertions.assertTrue(scanInProgress)

        // PROCESS FAILED
        idCardManagerFlow.value = EidInteractionEvent.Error(
            IdCardInteractionException.ProcessFailed(
                resultCode = ActivationResultCode.INTERNAL_ERROR,
                redirectUrl = testRedirectUrl,
                resultMinor = null
            )
        )
        advanceUntilIdle()
        Assertions.assertFalse(scanInProgress)
        navigationParameter = destinationSlot.captured
        Assertions.assertEquals(
            IdentificationCardUnreadableDestination(
                true,
                testRedirectUrl
            ).route, navigationParameter.route
        )
        verify(exactly = 2) { mockIdCardManager.cancelTask() }

        identificationCoordinator.cancelIdentification()
        advanceUntilIdle()
        Assertions.assertEquals(
            SubCoordinatorState.Cancelled,
            identificationCoordinator.stateFlow.value
        )
        verify(exactly = 1) { mockNavigator.popToRoot() }
        verify(exactly = 3) { mockIdCardManager.cancelTask() }

        scanJob.cancel()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun errorProcessFailedScanStateReachedWithoutRedirectUrl() = runTest {

        // SETUP
        every { mockCoroutineContextProvider.IO } returns dispatcher

        val idCardManagerFlow = MutableStateFlow<EidInteractionEvent>(EidInteractionEvent.Idle)
        every { mockIdCardManager.eidFlow } returns idCardManagerFlow

        val mockIntent = mockkClass(Intent::class)
        mockkConstructor(Intent::class)
        every { anyConstructed<Intent>().addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) } returns mockIntent

        val identificationCoordinator = IdentificationCoordinator(
            canCoordinator = mockCanCoordinator,
            context = mockContext,
            navigator = mockNavigator,
            idCardManager = mockIdCardManager,
            storageManager = mockStorageManager,
            trackerManager = mockTrackerManager,
            issueTrackerManager = mockIssueTrackerManager,
            coroutineContextProvider = mockCoroutineContextProvider
        )

        var scanInProgress = false
        val scanJob = identificationCoordinator.scanInProgress
            .onEach { scanInProgress = it }
            .launchIn(CoroutineScope(dispatcher))

        val pinCallback = mockk<(String) -> Unit>()

        val testRequestAuthenticationRequestConfirmation =
            EidInteractionEvent.RequestAuthenticationRequestConfirmation(
                EidAuthenticationRequest(
                    "",
                    "",
                    "subject",
                    "",
                    "",
                    AuthenticationTerms.Text(""),
                    "",
                    mapOf()
                )
            ) {}

        val testRedirectUrl = "testRedirectUrl"
        mockkStatic("android.util.Base64")
        mockkStatic("android.net.Uri")
        every { Base64.encodeToString(any(), any()) } returns testRedirectUrl
        every { Uri.encode(any()) } returns testRedirectUrl
        every { Uri.decode(any()) } returns testRedirectUrl
        every { Uri.parse(testRedirectUrl) } returns Uri.EMPTY

        // START IDENTIFICATION PROCESS
        val setupSkipped = false
        identificationCoordinator.startIdentificationProcess(testTokenURL, setupSkipped)
        advanceUntilIdle()
        Assertions.assertEquals(
            SubCoordinatorState.Active,
            identificationCoordinator.stateFlow.value
        )
        verify(exactly = 1) { mockIdCardManager.cancelTask() }
        verify(exactly = 1) { mockIdCardManager.identify(mockContext, testURL) }

        // SEND AUTHENTIFICATION STARTED EVENT
        idCardManagerFlow.value = EidInteractionEvent.AuthenticationStarted
        advanceUntilIdle()
        var navigationParameter = destinationSlot.captured
        Assertions.assertEquals(
            IdentificationFetchMetadataDestination(setupSkipped).route,
            navigationParameter.route
        )

        // SEND TEST REQUEST CONFIRMATION
        idCardManagerFlow.value = testRequestAuthenticationRequestConfirmation
        advanceUntilIdle()
        navigationParameter = destinationPoppingSlot.captured
        Assertions.assertEquals(
            IdentificationAttributeConsentDestination(
                testRequestAuthenticationRequestConfirmation.request,
                setupSkipped
            ).route,
            navigationParameter.route
        )

        // CONFIRM ATTRIBUTES AND NAVIGATE TO PIN ENTRY
        identificationCoordinator.confirmAttributesForIdentification()
        val attempts: Int? = null
        idCardManagerFlow.value = EidInteractionEvent.RequestPin(attempts, pinCallback)
        advanceUntilIdle()
        navigationParameter = destinationSlot.captured
        Assertions.assertEquals(
            IdentificationPersonalPinDestination(false).route,
            navigationParameter.route
        )

        // SET PIN AND CONTINUE
        val pin = "111111"
        every { pinCallback(pin) } just Runs
        identificationCoordinator.setPin(pin)
        verify(exactly = 1) { pinCallback(pin) }
        idCardManagerFlow.value = EidInteractionEvent.RequestCardInsertion
        advanceUntilIdle()
        verify(exactly = 1) { mockNavigator.navigate(IdentificationScanDestination) }

        // PROCESS FAILED
        idCardManagerFlow.value = EidInteractionEvent.Error(
            IdCardInteractionException.ProcessFailed(
                resultCode = ActivationResultCode.INTERNAL_ERROR,
                redirectUrl = null,
                resultMinor = null
            )
        )
        advanceUntilIdle()
        Assertions.assertFalse(scanInProgress)
        navigationParameter = destinationSlot.captured
        Assertions.assertEquals(
            IdentificationCardUnreadableDestination(true, null).route,
            navigationParameter.route
        )
        verify(exactly = 2) { mockIdCardManager.cancelTask() }

        identificationCoordinator.cancelIdentification()
        advanceUntilIdle()
        Assertions.assertEquals(
            SubCoordinatorState.Cancelled,
            identificationCoordinator.stateFlow.value
        )
        verify(exactly = 1) { mockNavigator.popToRoot() }
        verify(exactly = 3) { mockIdCardManager.cancelTask() }

        scanJob.cancel()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun errorProcessFailedScanStateNotReached() = runTest {

        // SETUP
        every { mockCoroutineContextProvider.IO } returns dispatcher

        val idCardManagerFlow = MutableStateFlow<EidInteractionEvent>(EidInteractionEvent.Idle)
        every { mockIdCardManager.eidFlow } returns idCardManagerFlow

        val mockIntent = mockkClass(Intent::class)
        mockkConstructor(Intent::class)
        every { anyConstructed<Intent>().addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) } returns mockIntent

        val identificationCoordinator = IdentificationCoordinator(
            canCoordinator = mockCanCoordinator,
            context = mockContext,
            navigator = mockNavigator,
            idCardManager = mockIdCardManager,
            storageManager = mockStorageManager,
            trackerManager = mockTrackerManager,
            issueTrackerManager = mockIssueTrackerManager,
            coroutineContextProvider = mockCoroutineContextProvider
        )

        var scanInProgress = false
        val scanJob = identificationCoordinator.scanInProgress
            .onEach { scanInProgress = it }
            .launchIn(CoroutineScope(dispatcher))

        val pinCallback = mockk<(String) -> Unit>()

        val testRequestAuthenticationRequestConfirmation =
            EidInteractionEvent.RequestAuthenticationRequestConfirmation(
                EidAuthenticationRequest(
                    "",
                    "",
                    "subject",
                    "",
                    "",
                    AuthenticationTerms.Text(""),
                    "",
                    mapOf()
                )
            ) {}

        val testRedirectUrl = "testRedirectUrl"
        mockkStatic("android.util.Base64")
        mockkStatic("android.net.Uri")
        every { Base64.encodeToString(any(), any()) } returns testRedirectUrl
        every { Uri.encode(any()) } returns testRedirectUrl
        every { Uri.decode(any()) } returns testRedirectUrl
        every { Uri.parse(testRedirectUrl) } returns Uri.EMPTY

        // START IDENTIFICATION PROCESS
        val setupSkipped = false
        identificationCoordinator.startIdentificationProcess(testTokenURL, setupSkipped)
        advanceUntilIdle()
        Assertions.assertEquals(
            SubCoordinatorState.Active,
            identificationCoordinator.stateFlow.value
        )
        verify(exactly = 1) { mockIdCardManager.cancelTask() }
        verify(exactly = 1) { mockIdCardManager.identify(mockContext, testURL) }

        // SEND AUTHENTIFICATION STARTED EVENT
        idCardManagerFlow.value = EidInteractionEvent.AuthenticationStarted
        advanceUntilIdle()
        var navigationParameter = destinationSlot.captured
        Assertions.assertEquals(
            IdentificationFetchMetadataDestination(setupSkipped).route,
            navigationParameter.route
        )

        // SEND TEST REQUEST CONFIRMATION
        idCardManagerFlow.value = testRequestAuthenticationRequestConfirmation
        advanceUntilIdle()
        navigationParameter = destinationPoppingSlot.captured
        Assertions.assertEquals(
            IdentificationAttributeConsentDestination(
                testRequestAuthenticationRequestConfirmation.request,
                setupSkipped
            ).route,
            navigationParameter.route
        )

        // CONFIRM ATTRIBUTES AND NAVIGATE TO PIN ENTRY
        identificationCoordinator.confirmAttributesForIdentification()
        val attempts: Int? = null
        idCardManagerFlow.value = EidInteractionEvent.RequestPin(attempts, pinCallback)
        advanceUntilIdle()
        navigationParameter = destinationSlot.captured
        Assertions.assertEquals(
            IdentificationPersonalPinDestination(false).route,
            navigationParameter.route
        )

        // SET PIN AND CONTINUE
        val pin = "111111"
        every { pinCallback(pin) } just Runs
        identificationCoordinator.setPin(pin)
        verify(exactly = 1) { pinCallback(pin) }

        // PROCESS FAILED
        idCardManagerFlow.value = EidInteractionEvent.Error(
            IdCardInteractionException.ProcessFailed(
                resultCode = ActivationResultCode.INTERNAL_ERROR,
                redirectUrl = null,
                resultMinor = null
            )
        )

        advanceUntilIdle()
        Assertions.assertFalse(scanInProgress)
        verify(exactly = 1) { mockNavigator.navigate(IdentificationOtherErrorDestination) }
        verify(exactly = 2) { mockIdCardManager.cancelTask() }

        identificationCoordinator.cancelIdentification()
        advanceUntilIdle()
        Assertions.assertEquals(
            SubCoordinatorState.Cancelled,
            identificationCoordinator.stateFlow.value
        )
        verify(exactly = 1) { mockNavigator.popToRoot() }
        verify(exactly = 3) { mockIdCardManager.cancelTask() }

        scanJob.cancel()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun frameworkErrorTrackedWhenScanStateNotReachedAndPinCallbackNull() = runTest {

        // SETUP
        every { mockCoroutineContextProvider.IO } returns dispatcher

        val idCardManagerFlow = MutableStateFlow<EidInteractionEvent>(EidInteractionEvent.Idle)
        every { mockIdCardManager.eidFlow } returns idCardManagerFlow

        val mockIntent = mockkClass(Intent::class)
        mockkConstructor(Intent::class)
        every { anyConstructed<Intent>().addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) } returns mockIntent

        val identificationCoordinator = IdentificationCoordinator(
            canCoordinator = mockCanCoordinator,
            context = mockContext,
            navigator = mockNavigator,
            idCardManager = mockIdCardManager,
            storageManager = mockStorageManager,
            trackerManager = mockTrackerManager,
            issueTrackerManager = mockIssueTrackerManager,
            coroutineContextProvider = mockCoroutineContextProvider
        )

        var scanInProgress = false
        val scanJob = identificationCoordinator.scanInProgress
            .onEach { scanInProgress = it }
            .launchIn(CoroutineScope(dispatcher))

        val pinCallback = mockk<(String) -> Unit>()

        val testRequestAuthenticationRequestConfirmation =
            EidInteractionEvent.RequestAuthenticationRequestConfirmation(
                EidAuthenticationRequest(
                    "",
                    "",
                    "subject",
                    "",
                    "",
                    AuthenticationTerms.Text(""),
                    "",
                    mapOf()
                )
            ) {}

        val testRedirectUrl = "testRedirectUrl"
        mockkStatic("android.util.Base64")
        mockkStatic("android.net.Uri")
        every { Base64.encodeToString(any(), any()) } returns testRedirectUrl
        every { Uri.encode(any()) } returns testRedirectUrl
        every { Uri.decode(any()) } returns testRedirectUrl
        every { Uri.parse(testRedirectUrl) } returns Uri.EMPTY

        // START IDENTIFICATION PROCESS
        val setupSkipped = false
        identificationCoordinator.startIdentificationProcess(testTokenURL, setupSkipped)
        advanceUntilIdle()
        Assertions.assertEquals(
            SubCoordinatorState.Active,
            identificationCoordinator.stateFlow.value
        )
        verify(exactly = 1) { mockIdCardManager.cancelTask() }
        verify(exactly = 1) { mockIdCardManager.identify(mockContext, testURL) }

        // SEND AUTHENTIFICATION STARTED EVENT
        idCardManagerFlow.value = EidInteractionEvent.AuthenticationStarted
        advanceUntilIdle()
        var navigationParameter = destinationSlot.captured
        Assertions.assertEquals(
            IdentificationFetchMetadataDestination(setupSkipped).route,
            navigationParameter.route
        )

        // SEND TEST REQUEST CONFIRMATION
        idCardManagerFlow.value = testRequestAuthenticationRequestConfirmation
        advanceUntilIdle()
        navigationParameter = destinationPoppingSlot.captured
        Assertions.assertEquals(
            IdentificationAttributeConsentDestination(
                testRequestAuthenticationRequestConfirmation.request,
                setupSkipped
            ).route,
            navigationParameter.route
        )

        // CONFIRM ATTRIBUTES AND NAVIGATE TO PIN ENTRY
        identificationCoordinator.confirmAttributesForIdentification()
        val attempts: Int? = null
        idCardManagerFlow.value = EidInteractionEvent.RequestPin(attempts, pinCallback)
        advanceUntilIdle()
        navigationParameter = destinationSlot.captured
        Assertions.assertEquals(
            IdentificationPersonalPinDestination(false).route,
            navigationParameter.route
        )

        // SET PIN AND CONTINUE
        val pin = "111111"
        every { pinCallback(pin) } just Runs
        identificationCoordinator.setPin(pin)
        verify(exactly = 1) { pinCallback(pin) }

        // PROCESS FAILED
        idCardManagerFlow.value =
            EidInteractionEvent.Error(IdCardInteractionException.FrameworkError("Error"))
        advanceUntilIdle()
        Assertions.assertFalse(scanInProgress)
        verify(exactly = 1) {
            mockTrackerManager.trackEvent(
                category = "identification",
                action = "loadingFailed",
                name = "attributes"
            )
        }
        verify(exactly = 1) { mockIssueTrackerManager.capture(RedactedIDCardInteractionException.FrameworkError) }
        verify(exactly = 1) { mockNavigator.navigate(IdentificationOtherErrorDestination) }
        verify(exactly = 2) { mockIdCardManager.cancelTask() }

        identificationCoordinator.cancelIdentification()
        advanceUntilIdle()
        Assertions.assertEquals(
            SubCoordinatorState.Cancelled,
            identificationCoordinator.stateFlow.value
        )
        verify(exactly = 1) { mockNavigator.popToRoot() }
        verify(exactly = 3) { mockIdCardManager.cancelTask() }

        scanJob.cancel()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun unexpectedReadAttributeErrorTrackedWhenScanStateNotReachedAndPinCallbackNull() = runTest {

        // SETUP
        every { mockCoroutineContextProvider.IO } returns dispatcher

        val idCardManagerFlow = MutableStateFlow<EidInteractionEvent>(EidInteractionEvent.Idle)
        every { mockIdCardManager.eidFlow } returns idCardManagerFlow

        val mockIntent = mockkClass(Intent::class)
        mockkConstructor(Intent::class)
        every { anyConstructed<Intent>().addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) } returns mockIntent

        val identificationCoordinator = IdentificationCoordinator(
            canCoordinator = mockCanCoordinator,
            context = mockContext,
            navigator = mockNavigator,
            idCardManager = mockIdCardManager,
            storageManager = mockStorageManager,
            trackerManager = mockTrackerManager,
            issueTrackerManager = mockIssueTrackerManager,
            coroutineContextProvider = mockCoroutineContextProvider
        )

        var scanInProgress = false
        val scanJob = identificationCoordinator.scanInProgress
            .onEach { scanInProgress = it }
            .launchIn(CoroutineScope(dispatcher))

        val pinCallback = mockk<(String) -> Unit>()

        val testRequestAuthenticationRequestConfirmation =
            EidInteractionEvent.RequestAuthenticationRequestConfirmation(
                EidAuthenticationRequest(
                    "",
                    "",
                    "subject",
                    "",
                    "",
                    AuthenticationTerms.Text(""),
                    "",
                    mapOf()
                )
            ) {}

        val testRedirectUrl = "testRedirectUrl"
        mockkStatic("android.util.Base64")
        mockkStatic("android.net.Uri")
        every { Base64.encodeToString(any(), any()) } returns testRedirectUrl
        every { Uri.encode(any()) } returns testRedirectUrl
        every { Uri.decode(any()) } returns testRedirectUrl
        every { Uri.parse(testRedirectUrl) } returns Uri.EMPTY

        // START IDENTIFICATION PROCESS
        val setupSkipped = false
        identificationCoordinator.startIdentificationProcess(testTokenURL, setupSkipped)
        advanceUntilIdle()
        Assertions.assertEquals(
            SubCoordinatorState.Active,
            identificationCoordinator.stateFlow.value
        )
        verify(exactly = 1) { mockIdCardManager.cancelTask() }
        verify(exactly = 1) { mockIdCardManager.identify(mockContext, testURL) }

        // SEND AUTHENTIFICATION STARTED EVENT
        idCardManagerFlow.value = EidInteractionEvent.AuthenticationStarted
        advanceUntilIdle()
        var navigationParameter = destinationSlot.captured
        Assertions.assertEquals(
            IdentificationFetchMetadataDestination(setupSkipped).route,
            navigationParameter.route
        )

        // SEND TEST REQUEST CONFIRMATION
        idCardManagerFlow.value = testRequestAuthenticationRequestConfirmation
        advanceUntilIdle()
        navigationParameter = destinationPoppingSlot.captured
        Assertions.assertEquals(
            IdentificationAttributeConsentDestination(
                testRequestAuthenticationRequestConfirmation.request,
                setupSkipped
            ).route,
            navigationParameter.route
        )

        // CONFIRM ATTRIBUTES AND NAVIGATE TO PIN ENTRY
        identificationCoordinator.confirmAttributesForIdentification()
        val attempts: Int? = null
        idCardManagerFlow.value = EidInteractionEvent.RequestPin(attempts, pinCallback)
        advanceUntilIdle()
        navigationParameter = destinationSlot.captured
        Assertions.assertEquals(
            IdentificationPersonalPinDestination(false).route,
            navigationParameter.route
        )

        // SET PIN AND CONTINUE
        val pin = "111111"
        every { pinCallback(pin) } just Runs
        identificationCoordinator.setPin(pin)
        verify(exactly = 1) { pinCallback(pin) }

        // PROCESS FAILED
        idCardManagerFlow.value =
            EidInteractionEvent.Error(IdCardInteractionException.UnexpectedReadAttribute("Error"))
        advanceUntilIdle()
        Assertions.assertFalse(scanInProgress)
        verify(exactly = 1) {
            mockTrackerManager.trackEvent(
                category = "identification",
                action = "loadingFailed",
                name = "attributes"
            )
        }
        verify(exactly = 1) { mockIssueTrackerManager.capture(RedactedIDCardInteractionException.UnexpectedReadAttribute) }
        verify(exactly = 1) { mockNavigator.navigate(IdentificationOtherErrorDestination) }
        verify(exactly = 2) { mockIdCardManager.cancelTask() }

        identificationCoordinator.cancelIdentification()
        advanceUntilIdle()
        Assertions.assertEquals(
            SubCoordinatorState.Cancelled,
            identificationCoordinator.stateFlow.value
        )
        verify(exactly = 1) { mockNavigator.popToRoot() }
        verify(exactly = 3) { mockIdCardManager.cancelTask() }

        scanJob.cancel()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun otherErrorsNotTrackedWhenScanStateReached() = runTest {

        // SETUP
        every { mockCoroutineContextProvider.IO } returns dispatcher

        val idCardManagerFlow = MutableStateFlow<EidInteractionEvent>(EidInteractionEvent.Idle)
        every { mockIdCardManager.eidFlow } returns idCardManagerFlow

        val mockIntent = mockkClass(Intent::class)
        mockkConstructor(Intent::class)
        every { anyConstructed<Intent>().addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) } returns mockIntent

        val identificationCoordinator = IdentificationCoordinator(
            canCoordinator = mockCanCoordinator,
            context = mockContext,
            navigator = mockNavigator,
            idCardManager = mockIdCardManager,
            storageManager = mockStorageManager,
            trackerManager = mockTrackerManager,
            issueTrackerManager = mockIssueTrackerManager,
            coroutineContextProvider = mockCoroutineContextProvider
        )

        var scanInProgress = false
        val scanJob = identificationCoordinator.scanInProgress
            .onEach { scanInProgress = it }
            .launchIn(CoroutineScope(dispatcher))

        val pinCallback = mockk<(String) -> Unit>()

        val testRequestAuthenticationRequestConfirmation =
            EidInteractionEvent.RequestAuthenticationRequestConfirmation(
                EidAuthenticationRequest(
                    "",
                    "",
                    "subject",
                    "",
                    "",
                    AuthenticationTerms.Text(""),
                    "",
                    mapOf()
                )
            ) {}

        val testRedirectUrl = "testRedirectUrl"
        mockkStatic("android.util.Base64")
        mockkStatic("android.net.Uri")
        every { Base64.encodeToString(any(), any()) } returns testRedirectUrl
        every { Uri.encode(any()) } returns testRedirectUrl
        every { Uri.decode(any()) } returns testRedirectUrl
        every { Uri.parse(testRedirectUrl) } returns Uri.EMPTY

        // START IDENTIFICATION PROCESS
        val setupSkipped = false
        identificationCoordinator.startIdentificationProcess(testTokenURL, setupSkipped)
        advanceUntilIdle()
        Assertions.assertEquals(
            SubCoordinatorState.Active,
            identificationCoordinator.stateFlow.value
        )
        verify(exactly = 1) { mockIdCardManager.cancelTask() }
        verify(exactly = 1) { mockIdCardManager.identify(mockContext, testURL) }

        // SEND AUTHENTIFICATION STARTED EVENT
        idCardManagerFlow.value = EidInteractionEvent.AuthenticationStarted
        advanceUntilIdle()
        var navigationParameter = destinationSlot.captured
        Assertions.assertEquals(
            IdentificationFetchMetadataDestination(setupSkipped).route,
            navigationParameter.route
        )

        // SEND TEST REQUEST CONFIRMATION
        idCardManagerFlow.value = testRequestAuthenticationRequestConfirmation
        advanceUntilIdle()
        navigationParameter = destinationPoppingSlot.captured
        Assertions.assertEquals(
            IdentificationAttributeConsentDestination(
                testRequestAuthenticationRequestConfirmation.request,
                setupSkipped
            ).route,
            navigationParameter.route
        )

        // CONFIRM ATTRIBUTES AND NAVIGATE TO PIN ENTRY
        identificationCoordinator.confirmAttributesForIdentification()
        val attempts: Int? = null
        idCardManagerFlow.value = EidInteractionEvent.RequestPin(attempts, pinCallback)
        advanceUntilIdle()
        navigationParameter = destinationSlot.captured
        Assertions.assertEquals(
            IdentificationPersonalPinDestination(false).route,
            navigationParameter.route
        )

        // SET PIN AND CONTINUE
        val pin = "111111"
        every { pinCallback(pin) } just Runs
        identificationCoordinator.setPin(pin)
        verify(exactly = 1) { pinCallback(pin) }

        idCardManagerFlow.value = EidInteractionEvent.RequestCardInsertion
        advanceUntilIdle()
        verify(exactly = 1) { mockNavigator.navigate(IdentificationScanDestination) }

        // PROCESS FAILED
        idCardManagerFlow.value =
            EidInteractionEvent.Error(IdCardInteractionException.FrameworkError("Error"))
        advanceUntilIdle()
        Assertions.assertFalse(scanInProgress)
        verify(exactly = 0) {
            mockTrackerManager.trackEvent(
                category = "identification",
                action = "loadingFailed",
                name = "attributes"
            )
        }
        verify(exactly = 0) { mockIssueTrackerManager.capture(RedactedIDCardInteractionException.FrameworkError) }
        verify(exactly = 1) { mockNavigator.navigate(IdentificationOtherErrorDestination) }
        verify(exactly = 2) { mockIdCardManager.cancelTask() }

        identificationCoordinator.cancelIdentification()
        advanceUntilIdle()
        Assertions.assertEquals(
            SubCoordinatorState.Cancelled,
            identificationCoordinator.stateFlow.value
        )
        verify(exactly = 1) { mockNavigator.popToRoot() }
        verify(exactly = 3) { mockIdCardManager.cancelTask() }

        scanJob.cancel()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun testNullPointerExceptionInEidInteractionFlow() = runTest {

        // SETUP
        every { mockCoroutineContextProvider.IO } returns dispatcher

        every { mockIdCardManager.eidFlow } returns flow {
            throw NullPointerException()
        }

        val identificationCoordinator = IdentificationCoordinator(
            canCoordinator = mockCanCoordinator,
            context = mockContext,
            navigator = mockNavigator,
            idCardManager = mockIdCardManager,
            storageManager = mockStorageManager,
            trackerManager = mockTrackerManager,
            issueTrackerManager = mockIssueTrackerManager,
            coroutineContextProvider = mockCoroutineContextProvider
        )

        var scanInProgress = false
        val scanJob = identificationCoordinator.scanInProgress
            .onEach { scanInProgress = it }
            .launchIn(CoroutineScope(dispatcher))

        // START IDENTIFICATION PROCESS
        val setupSkipped = false
        identificationCoordinator.startIdentificationProcess(testTokenURL, setupSkipped)
        advanceUntilIdle()
        Assertions.assertEquals(
            SubCoordinatorState.Active,
            identificationCoordinator.stateFlow.value
        )

        Assertions.assertFalse(scanInProgress)
        verify(exactly = 1) { mockNavigator.navigate(IdentificationOtherErrorDestination) }
        verify(exactly = 2) { mockIdCardManager.cancelTask() }

        identificationCoordinator.cancelIdentification()
        advanceUntilIdle()
        Assertions.assertEquals(
            SubCoordinatorState.Cancelled,
            identificationCoordinator.stateFlow.value
        )
        verify(exactly = 1) { mockNavigator.popToRoot() }
        verify(exactly = 3) { mockIdCardManager.cancelTask() }

        scanJob.cancel()
    }
}
