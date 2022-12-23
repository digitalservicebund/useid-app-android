package de.digitalService.useID.coordinator

import android.content.Context
import android.net.Uri
import android.util.Base64
import com.ramcosta.composedestinations.spec.Direction
import de.digitalService.useID.analytics.IssueTrackerManagerType
import de.digitalService.useID.analytics.TrackerManagerType
import de.digitalService.useID.idCardInterface.*
import de.digitalService.useID.models.ScanError
import de.digitalService.useID.ui.coordinators.AppCoordinator
import de.digitalService.useID.ui.coordinators.IdentificationCoordinator
import de.digitalService.useID.ui.screens.destinations.*
import de.digitalService.useID.ui.screens.identification.ScanEvent
import de.digitalService.useID.util.CoroutineContextProvider
import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.openecard.mobile.activation.ActivationResultCode

@ExtendWith(MockKExtension::class)
class IdentificationCoordinatorTest {

    @MockK(relaxUnitFun = true)
    lateinit var mockContext: Context

    @MockK(relaxUnitFun = true)
    lateinit var mockAppCoordinator: AppCoordinator

    @MockK(relaxUnitFun = true)
    lateinit var mockIDCardManager: IdCardManager

    @MockK(relaxUnitFun = true)
    lateinit var mockTrackerManager: TrackerManagerType

    @MockK(relaxUnitFun = true)
    lateinit var mockIssueTrackerManager: IssueTrackerManagerType

    @MockK(relaxUnitFun = true)
    lateinit var mockCoroutineContextProvider: CoroutineContextProvider

    @OptIn(ExperimentalCoroutinesApi::class)
    val dispatcher = StandardTestDispatcher()

    private val destinationSlot = slot<Direction>()

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
                .appendQueryParameter("tcTokenUrl", testTokenURL)
        } returns mockUriBuilder

        val mockedUri = mockk<Uri>()

        every { mockUriBuilder.build() } returns mockedUri

        every { mockedUri.toString() } returns testURL

        every { mockAppCoordinator.navigate(capture(destinationSlot)) } returns Unit
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
    fun startIdentificationProcess_ProcessCompletedSuccessfully() = runTest {
        val testRedirectUrl = "testRedirectUrl"
        val testRequestAuthenticationRequestConfirmation = EidInteractionEvent.RequestAuthenticationRequestConfirmation(
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
        val testFlow = MutableStateFlow<EidInteractionEvent>(EidInteractionEvent.AuthenticationStarted)

        every { mockIDCardManager.identify(mockContext, testURL) } returns testFlow

        mockkStatic("android.util.Base64")
        mockkStatic("android.net.Uri")
        every { Base64.encodeToString(any(), any()) } returns testRedirectUrl
        every { Uri.encode(any()) } returns testRedirectUrl
        every { Uri.decode(any()) } returns testRedirectUrl

        every { mockCoroutineContextProvider.IO } returns dispatcher

        val identificationCoordinator = IdentificationCoordinator(
            mockContext,
            mockAppCoordinator,
            mockIDCardManager,
            mockTrackerManager,
            mockIssueTrackerManager,
            mockCoroutineContextProvider
        )

        identificationCoordinator.startIdentificationProcess(testTokenURL, true)

        verify { mockIDCardManager.cancelTask() }

        val scanResults = mutableListOf<ScanEvent>()
        val scanJob = identificationCoordinator.scanEventFlow
            .onEach(scanResults::add)
            .launchIn(CoroutineScope(dispatcher))

        advanceUntilIdle()

        Assertions.assertTrue(identificationCoordinator.didSetup)

        Assertions.assertEquals(ScanEvent.CardRequested, scanResults.get(0))

        testFlow.value = testRequestAuthenticationRequestConfirmation
        advanceUntilIdle()

        Assertions.assertEquals(1, scanResults.size)
        verify(exactly = 1) { mockAppCoordinator.navigate(any()) }

        testFlow.value = EidInteractionEvent.ProcessCompletedSuccessfullyWithRedirect(testRedirectUrl)
        advanceUntilIdle()

        Assertions.assertEquals(ScanEvent.Finished(testRedirectUrl), scanResults.get(1))

        scanJob.cancel()
        verify(exactly = 1) { mockAppCoordinator.navigate(any()) }
        verify(exactly = 1) { mockAppCoordinator.popToRoot() }

    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun startIdentificationProcess_ProcessCompletedSuccessfully_WithoutRequest() = runTest {
        val testRedirectUrl = "testRedirectUrl"
        val testFlow = MutableStateFlow<EidInteractionEvent>(EidInteractionEvent.AuthenticationStarted)

        every { mockIDCardManager.identify(mockContext, testURL) } returns testFlow

        mockkStatic("android.util.Base64")
        mockkStatic("android.net.Uri")
        every { Base64.encodeToString(any(), any()) } returns testRedirectUrl
        every { Uri.encode(any()) } returns testRedirectUrl
        every { Uri.decode(any()) } returns testRedirectUrl

        every { mockCoroutineContextProvider.IO } returns dispatcher

        val identificationCoordinator = IdentificationCoordinator(
            mockContext,
            mockAppCoordinator,
            mockIDCardManager,
            mockTrackerManager,
            mockIssueTrackerManager,
            mockCoroutineContextProvider
        )

        identificationCoordinator.startIdentificationProcess(testTokenURL, false)

        verify { mockIDCardManager.cancelTask() }

        val results = mutableListOf<ScanEvent>()
        val job = identificationCoordinator.scanEventFlow
            .onEach(results::add)
            .launchIn(CoroutineScope(dispatcher))

        advanceUntilIdle()

        testFlow.value = EidInteractionEvent.ProcessCompletedSuccessfullyWithRedirect(testRedirectUrl)
        advanceUntilIdle()

        Assertions.assertEquals(ScanEvent.Finished(testRedirectUrl), results.get(1))

        job.cancel()
        verify(exactly = 0) { mockAppCoordinator.navigate(any()) }

        Assertions.assertFalse(identificationCoordinator.didSetup)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun startIdentificationProcess_VisitingPinEntryTwice() = runTest {
        val testRedirectUrl = "testRedirectUrl"
        val testFlow = MutableStateFlow<EidInteractionEvent>(EidInteractionEvent.AuthenticationStarted)

        every { mockIDCardManager.identify(mockContext, testURL) } returns testFlow

        mockkStatic("android.util.Base64")
        mockkStatic("android.net.Uri")
        every { Base64.encodeToString(any(), any()) } returns testRedirectUrl
        every { Uri.encode(any()) } returns testRedirectUrl
        every { Uri.decode(any()) } returns testRedirectUrl

        every { mockCoroutineContextProvider.IO } returns dispatcher

        val identificationCoordinator = IdentificationCoordinator(
            mockContext,
            mockAppCoordinator,
            mockIDCardManager,
            mockTrackerManager,
            mockIssueTrackerManager,
            mockCoroutineContextProvider
        )

        identificationCoordinator.startIdentificationProcess(testTokenURL, false)

        verify { mockIDCardManager.cancelTask() }

        val results = mutableListOf<ScanEvent>()
        val job = identificationCoordinator.scanEventFlow
            .onEach(results::add)
            .launchIn(CoroutineScope(dispatcher))

        advanceUntilIdle()

        val confirmationRequest = EidAuthenticationRequest(
                "",
                "",
                "subject",
                "",
                "",
                AuthenticationTerms.Text(""),
                "",
                mapOf()
            )
        val confirmationRequestCallback = mockk<(Map<IdCardAttribute, Boolean>) -> Unit>()
        every { confirmationRequestCallback(any()) } just Runs

        testFlow.value = EidInteractionEvent.RequestAuthenticationRequestConfirmation(confirmationRequest, confirmationRequestCallback)
        advanceUntilIdle()

        identificationCoordinator.confirmAttributesForIdentification()
        verify(exactly = 1) { confirmationRequestCallback(any()) }

        testFlow.value = EidInteractionEvent.RequestPin(null, { })
        advanceUntilIdle()

        var navigationParameter = destinationSlot.captured
        Assertions.assertEquals(IdentificationPersonalPinDestination(null).route, navigationParameter.route)

        identificationCoordinator.pop()
        verify(exactly = 1) { mockAppCoordinator.pop() }

        identificationCoordinator.confirmAttributesForIdentification()
        verify(exactly = 1) { confirmationRequestCallback(any()) }

        advanceUntilIdle()

        navigationParameter = destinationSlot.captured
        Assertions.assertEquals(IdentificationPersonalPinDestination(null).route, navigationParameter.route)

        job.cancel()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun startIdentificationProcess_CancelOnAttributeConfirmationWithoutSetup() = runTest {
        val testRedirectUrl = "testRedirectUrl"
        val testFlow = MutableStateFlow<EidInteractionEvent>(EidInteractionEvent.AuthenticationStarted)

        every { mockIDCardManager.identify(mockContext, testURL) } returns testFlow

        mockkStatic("android.util.Base64")
        mockkStatic("android.net.Uri")
        every { Base64.encodeToString(any(), any()) } returns testRedirectUrl
        every { Uri.encode(any()) } returns testRedirectUrl
        every { Uri.decode(any()) } returns testRedirectUrl

        every { mockCoroutineContextProvider.IO } returns dispatcher

        val identificationCoordinator = IdentificationCoordinator(
            mockContext,
            mockAppCoordinator,
            mockIDCardManager,
            mockTrackerManager,
            mockIssueTrackerManager,
            mockCoroutineContextProvider
        )

        identificationCoordinator.startIdentificationProcess(testTokenURL, false)

        verify { mockIDCardManager.cancelTask() }

        val results = mutableListOf<ScanEvent>()
        val job = identificationCoordinator.scanEventFlow
            .onEach(results::add)
            .launchIn(CoroutineScope(dispatcher))

        advanceUntilIdle()

        val confirmationRequest = EidAuthenticationRequest(
            "",
            "",
            "subject",
            "",
            "",
            AuthenticationTerms.Text(""),
            "",
            mapOf()
        )
        val confirmationRequestCallback = mockk<(Map<IdCardAttribute, Boolean>) -> Unit>()
        every { confirmationRequestCallback(any()) } just Runs

        testFlow.value = EidInteractionEvent.RequestAuthenticationRequestConfirmation(confirmationRequest, confirmationRequestCallback)
        advanceUntilIdle()

        val navigationParameter = destinationSlot.captured
        Assertions.assertEquals(IdentificationAttributeConsentDestination(confirmationRequest).route, navigationParameter.route)

        identificationCoordinator.cancelIdentification()

        verify(exactly = 1) { mockAppCoordinator.stopNfcTagHandling() }
        verify(exactly = 2) { mockIDCardManager.cancelTask() }

        advanceUntilIdle()

        verify(exactly = 1) { mockAppCoordinator.popToRoot() }

        job.cancel()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun startIdentificationProcess_CancelOnAttributeConfirmationAfterSetup() = runTest {
        val testRedirectUrl = "testRedirectUrl"
        val testFlow = MutableStateFlow<EidInteractionEvent>(EidInteractionEvent.AuthenticationStarted)

        every { mockIDCardManager.identify(mockContext, testURL) } returns testFlow

        mockkStatic("android.util.Base64")
        mockkStatic("android.net.Uri")
        every { Base64.encodeToString(any(), any()) } returns testRedirectUrl
        every { Uri.encode(any()) } returns testRedirectUrl
        every { Uri.decode(any()) } returns testRedirectUrl

        every { mockCoroutineContextProvider.IO } returns dispatcher

        val identificationCoordinator = IdentificationCoordinator(
            mockContext,
            mockAppCoordinator,
            mockIDCardManager,
            mockTrackerManager,
            mockIssueTrackerManager,
            mockCoroutineContextProvider
        )

        identificationCoordinator.startIdentificationProcess(testTokenURL, true)

        verify { mockIDCardManager.cancelTask() }

        val results = mutableListOf<ScanEvent>()
        val job = identificationCoordinator.scanEventFlow
            .onEach(results::add)
            .launchIn(CoroutineScope(dispatcher))

        advanceUntilIdle()

        val confirmationRequest = EidAuthenticationRequest(
            "",
            "",
            "subject",
            "",
            "",
            AuthenticationTerms.Text(""),
            "",
            mapOf()
        )
        val confirmationRequestCallback = mockk<(Map<IdCardAttribute, Boolean>) -> Unit>()
        every { confirmationRequestCallback(any()) } just Runs

        testFlow.value = EidInteractionEvent.RequestAuthenticationRequestConfirmation(confirmationRequest, confirmationRequestCallback)
        advanceUntilIdle()

        val navigationParameter = destinationSlot.captured
        Assertions.assertEquals(IdentificationAttributeConsentDestination(confirmationRequest).route, navigationParameter.route)

        identificationCoordinator.cancelIdentification()

        verify(exactly = 1) { mockAppCoordinator.stopNfcTagHandling() }
        verify(exactly = 2) { mockIDCardManager.cancelTask() }

        advanceUntilIdle()

        verify(exactly = 1) { mockAppCoordinator.popUpTo(SetupIntroDestination) }

        job.cancel()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun startIdentificationProcess_CancelOnScanWithoutSetup() = runTest {
        val testRedirectUrl = "testRedirectUrl"
        val testFlow = MutableStateFlow<EidInteractionEvent>(EidInteractionEvent.AuthenticationStarted)

        every { mockIDCardManager.identify(mockContext, testURL) } returns testFlow

        mockkStatic("android.util.Base64")
        mockkStatic("android.net.Uri")
        every { Base64.encodeToString(any(), any()) } returns testRedirectUrl
        every { Uri.encode(any()) } returns testRedirectUrl
        every { Uri.decode(any()) } returns testRedirectUrl

        every { mockCoroutineContextProvider.IO } returns dispatcher

        val identificationCoordinator = IdentificationCoordinator(
            mockContext,
            mockAppCoordinator,
            mockIDCardManager,
            mockTrackerManager,
            mockIssueTrackerManager,
            mockCoroutineContextProvider
        )

        identificationCoordinator.startIdentificationProcess(testTokenURL, false)

        verify { mockIDCardManager.cancelTask() }

        val results = mutableListOf<ScanEvent>()
        val job = identificationCoordinator.scanEventFlow
            .onEach(results::add)
            .launchIn(CoroutineScope(dispatcher))

        advanceUntilIdle()

        val confirmationRequest = EidAuthenticationRequest(
            "",
            "",
            "subject",
            "",
            "",
            AuthenticationTerms.Text(""),
            "",
            mapOf()
        )
        val confirmationRequestCallback = mockk<(Map<IdCardAttribute, Boolean>) -> Unit>()
        every { confirmationRequestCallback(any()) } just Runs

        testFlow.value = EidInteractionEvent.RequestAuthenticationRequestConfirmation(confirmationRequest, confirmationRequestCallback)
        advanceUntilIdle()

        identificationCoordinator.confirmAttributesForIdentification()
        verify(exactly = 1) { confirmationRequestCallback(any()) }

        testFlow.value = EidInteractionEvent.RequestPin(null, { })
        testFlow.value = EidInteractionEvent.RequestCardInsertion
        advanceUntilIdle()

        verify(exactly = 1) { mockAppCoordinator.navigate(IdentificationScanDestination) }

        identificationCoordinator.cancelIdentification()

        verify(exactly = 1) { mockAppCoordinator.stopNfcTagHandling() }
        verify(exactly = 2) { mockIDCardManager.cancelTask() }

        advanceUntilIdle()

        verify(exactly = 1) { mockAppCoordinator.popToRoot() }

        job.cancel()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun startIdentificationProcess_CancelOnScanAfterSetup() = runTest {
        val testRedirectUrl = "testRedirectUrl"
        val testFlow = MutableStateFlow<EidInteractionEvent>(EidInteractionEvent.AuthenticationStarted)

        every { mockIDCardManager.identify(mockContext, testURL) } returns testFlow

        mockkStatic("android.util.Base64")
        mockkStatic("android.net.Uri")
        every { Base64.encodeToString(any(), any()) } returns testRedirectUrl
        every { Uri.encode(any()) } returns testRedirectUrl
        every { Uri.decode(any()) } returns testRedirectUrl

        every { mockCoroutineContextProvider.IO } returns dispatcher

        val identificationCoordinator = IdentificationCoordinator(
            mockContext,
            mockAppCoordinator,
            mockIDCardManager,
            mockTrackerManager,
            mockIssueTrackerManager,
            mockCoroutineContextProvider
        )

        identificationCoordinator.startIdentificationProcess(testTokenURL, true)

        verify { mockIDCardManager.cancelTask() }

        val results = mutableListOf<ScanEvent>()
        val job = identificationCoordinator.scanEventFlow
            .onEach(results::add)
            .launchIn(CoroutineScope(dispatcher))

        advanceUntilIdle()

        val confirmationRequest = EidAuthenticationRequest(
            "",
            "",
            "subject",
            "",
            "",
            AuthenticationTerms.Text(""),
            "",
            mapOf()
        )
        val confirmationRequestCallback = mockk<(Map<IdCardAttribute, Boolean>) -> Unit>()
        every { confirmationRequestCallback(any()) } just Runs

        testFlow.value = EidInteractionEvent.RequestAuthenticationRequestConfirmation(confirmationRequest, confirmationRequestCallback)
        advanceUntilIdle()

        identificationCoordinator.confirmAttributesForIdentification()
        verify(exactly = 1) { confirmationRequestCallback(any()) }

        testFlow.value = EidInteractionEvent.RequestPin(null, { })
        testFlow.value = EidInteractionEvent.RequestCardInsertion
        advanceUntilIdle()

        verify(exactly = 1) { mockAppCoordinator.navigate(IdentificationScanDestination) }

        identificationCoordinator.cancelIdentification()

        verify(exactly = 1) { mockAppCoordinator.stopNfcTagHandling() }
        verify(exactly = 2) { mockIDCardManager.cancelTask() }

        advanceUntilIdle()

        verify(exactly = 1) { mockAppCoordinator.popToRoot() }

        job.cancel()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun startIdentificationProcess_SendEventBeforeListening_SkipFirstCardRequestedEvent() = runTest {
        val testRedirectUrl = "testRedirectUrl"
        val testFlow = MutableStateFlow<EidInteractionEvent>(EidInteractionEvent.AuthenticationStarted)

        every { mockIDCardManager.identify(mockContext, testURL) } returns testFlow

        mockkStatic("android.util.Base64")
        mockkStatic("android.net.Uri")
        every { Base64.encodeToString(any(), any()) } returns testRedirectUrl
        every { Uri.encode(any()) } returns testRedirectUrl
        every { Uri.decode(any()) } returns testRedirectUrl

        every { mockCoroutineContextProvider.IO } returns dispatcher

        val identificationCoordinator = IdentificationCoordinator(
            mockContext,
            mockAppCoordinator,
            mockIDCardManager,
            mockTrackerManager,
            mockIssueTrackerManager,
            mockCoroutineContextProvider
        )

        identificationCoordinator.startIdentificationProcess(testTokenURL, true)

        verify { mockIDCardManager.cancelTask() }

        testFlow.value = EidInteractionEvent.ProcessCompletedSuccessfullyWithRedirect(testRedirectUrl)
        advanceUntilIdle()

        val results = mutableListOf<ScanEvent>()
        val job = identificationCoordinator.scanEventFlow
            .onEach(results::add)
            .launchIn(CoroutineScope(dispatcher))

        advanceUntilIdle()

        Assertions.assertEquals(ScanEvent.Finished(testRedirectUrl), results.get(0))

        job.cancel()
        verify(exactly = 0) { mockAppCoordinator.navigate(any()) }

        Assertions.assertTrue(identificationCoordinator.didSetup)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @ParameterizedTest
    @ValueSource(ints = [1, 2, 3])
    fun startIdentificationProcess_RequestPin_WithAttempts(testValue: Int) = runTest {
        val testFlow = MutableStateFlow<EidInteractionEvent>(EidInteractionEvent.AuthenticationStarted)

        every { mockIDCardManager.identify(mockContext, testURL) } returns testFlow

        every { mockCoroutineContextProvider.IO } returns dispatcher

        val identificationCoordinator = IdentificationCoordinator(
            mockContext,
            mockAppCoordinator,
            mockIDCardManager,
            mockTrackerManager,
            mockIssueTrackerManager,
            mockCoroutineContextProvider
        )

        identificationCoordinator.startIdentificationProcess(testTokenURL, true)

        verify { mockIDCardManager.cancelTask() }

        val results = mutableListOf<ScanEvent>()
        val job = identificationCoordinator.scanEventFlow
            .onEach(results::add)
            .launchIn(CoroutineScope(dispatcher))

        advanceUntilIdle()

        testFlow.value = EidInteractionEvent.RequestPin(testValue) {}
        advanceUntilIdle()

        Assertions.assertEquals(ScanEvent.CardRequested, results[0])
        verify(exactly = 1) { mockAppCoordinator.navigate(any()) }

        val navigationParameter = destinationSlot.captured
        Assertions.assertEquals(IdentificationPersonalPinDestination(testValue).route, navigationParameter.route)

        job.cancel()

        Assertions.assertTrue(identificationCoordinator.didSetup)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun startIdentificationProcess_RequestPin_WithoutAttempts() = runTest {
        val testFlow = MutableStateFlow<EidInteractionEvent>(EidInteractionEvent.AuthenticationStarted)

        every { mockIDCardManager.identify(mockContext, testURL) } returns testFlow

        every { mockCoroutineContextProvider.IO } returns dispatcher


        val identificationCoordinator = IdentificationCoordinator(
            mockContext,
            mockAppCoordinator,
            mockIDCardManager,
            mockTrackerManager,
            mockIssueTrackerManager,
            mockCoroutineContextProvider
        )

        identificationCoordinator.startIdentificationProcess(testTokenURL, true)

        verify { mockIDCardManager.cancelTask() }

        testFlow.value = EidInteractionEvent.RequestPin(null) {}
        advanceUntilIdle()

        verify(exactly = 1) { mockAppCoordinator.navigate(any()) }

        val navigationParameter = destinationSlot.captured
        Assertions.assertEquals(IdentificationPersonalPinDestination(null).route, navigationParameter.route)

    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun startIdentificationProcess_RequestCan_Error() = runTest {
        val testFlow = MutableStateFlow<EidInteractionEvent>(EidInteractionEvent.AuthenticationStarted)

        every { mockIDCardManager.identify(mockContext, testURL) } returns testFlow

        every { mockCoroutineContextProvider.IO } returns dispatcher

        val identificationCoordinator = IdentificationCoordinator(
            mockContext,
            mockAppCoordinator,
            mockIDCardManager,
            mockTrackerManager,
            mockIssueTrackerManager,
            mockCoroutineContextProvider
        )

        identificationCoordinator.startIdentificationProcess(testTokenURL, true)

        verify { mockIDCardManager.cancelTask() }

        val results = mutableListOf<ScanEvent>()
        val job = identificationCoordinator.scanEventFlow
            .onEach(results::add)
            .launchIn(CoroutineScope(dispatcher))

        advanceUntilIdle()

        testFlow.value = EidInteractionEvent.RequestCan {}
        advanceUntilIdle()

        Assertions.assertEquals(ScanEvent.Error(ScanError.PinSuspended), results.get(1))

        job.cancel()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun startIdentificationProcess_RequestPinAndCan_Error() = runTest {
        val testFlow = MutableStateFlow<EidInteractionEvent>(EidInteractionEvent.AuthenticationStarted)

        every { mockIDCardManager.identify(mockContext, testURL) } returns testFlow

        every { mockCoroutineContextProvider.IO } returns dispatcher

        val identificationCoordinator = IdentificationCoordinator(
            mockContext,
            mockAppCoordinator,
            mockIDCardManager,
            mockTrackerManager,
            mockIssueTrackerManager,
            mockCoroutineContextProvider
        )

        identificationCoordinator.startIdentificationProcess(testTokenURL, true)

        verify { mockIDCardManager.cancelTask() }

        val results = mutableListOf<ScanEvent>()
        val job = identificationCoordinator.scanEventFlow
            .onEach(results::add)
            .launchIn(CoroutineScope(dispatcher))

        advanceUntilIdle()

        testFlow.value = EidInteractionEvent.RequestPinAndCan { _, _ -> }
        advanceUntilIdle()

        Assertions.assertEquals(ScanEvent.Error(ScanError.PinSuspended), results.get(1))

        job.cancel()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun startIdentificationProcess_RequestPUK_Error() = runTest {
        val testFlow = MutableStateFlow<EidInteractionEvent>(EidInteractionEvent.AuthenticationStarted)

        every { mockIDCardManager.identify(mockContext, testURL) } returns testFlow

        every { mockCoroutineContextProvider.IO } returns dispatcher

        val identificationCoordinator = IdentificationCoordinator(
            mockContext,
            mockAppCoordinator,
            mockIDCardManager,
            mockTrackerManager,
            mockIssueTrackerManager,
            mockCoroutineContextProvider
        )

        identificationCoordinator.startIdentificationProcess(testTokenURL, true)

        verify { mockIDCardManager.cancelTask() }

        val results = mutableListOf<ScanEvent>()
        val job = identificationCoordinator.scanEventFlow
            .onEach(results::add)
            .launchIn(CoroutineScope(dispatcher))

        advanceUntilIdle()

        testFlow.value = EidInteractionEvent.RequestPUK {}
        advanceUntilIdle()

        Assertions.assertEquals(ScanEvent.Error(ScanError.PinBlocked), results.get(1))

        job.cancel()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun startIdentificationProcess_RequestCardInsertion() = runTest {
        val testFlow = MutableStateFlow<EidInteractionEvent>(EidInteractionEvent.AuthenticationStarted)

        every { mockIDCardManager.identify(mockContext, testURL) } returns testFlow

        every { mockCoroutineContextProvider.IO } returns dispatcher

        val identificationCoordinator = IdentificationCoordinator(
            mockContext,
            mockAppCoordinator,
            mockIDCardManager,
            mockTrackerManager,
            mockIssueTrackerManager,
            mockCoroutineContextProvider
        )

        identificationCoordinator.startIdentificationProcess(testTokenURL, true)

        verify { mockIDCardManager.cancelTask() }

        advanceUntilIdle()

        testFlow.value = EidInteractionEvent.RequestCardInsertion
        advanceUntilIdle()

        verify(exactly = 1) { mockAppCoordinator.navigate(IdentificationScanDestination) }
        verify(exactly = 1) { mockAppCoordinator.startNfcTagHandling() }

        testFlow.value = EidInteractionEvent.CardRecognized
        advanceUntilIdle()

        testFlow.value = EidInteractionEvent.CardRemoved
        advanceUntilIdle()

        testFlow.value = EidInteractionEvent.RequestCardInsertion
        advanceUntilIdle()

        verify(exactly = 1) { mockAppCoordinator.navigate(any()) }
        verify(exactly = 2) { mockAppCoordinator.startNfcTagHandling() }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun startIdentificationProcess_CardInteractionComplete() = runTest {
        val testFlow = MutableStateFlow<EidInteractionEvent>(EidInteractionEvent.AuthenticationStarted)

        every { mockIDCardManager.identify(mockContext, testURL) } returns testFlow

        every { mockCoroutineContextProvider.IO } returns dispatcher

        val identificationCoordinator = IdentificationCoordinator(
            mockContext,
            mockAppCoordinator,
            mockIDCardManager,
            mockTrackerManager,
            mockIssueTrackerManager,
            mockCoroutineContextProvider
        )

        identificationCoordinator.startIdentificationProcess(testTokenURL, true)

        verify { mockIDCardManager.cancelTask() }

        advanceUntilIdle()

        testFlow.value = EidInteractionEvent.CardInteractionComplete
        advanceUntilIdle()

        verify(exactly = 1) { mockAppCoordinator.stopNfcTagHandling() }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun startIdentificationProcess_CardDeactivatedException() = runTest {
        val testFlow: Flow<EidInteractionEvent> = flow {
            throw IdCardInteractionException.CardDeactivated
        }

        every { mockIDCardManager.identify(mockContext, testURL) } returns testFlow

        every { mockCoroutineContextProvider.IO } returns dispatcher

        val identificationCoordinator = IdentificationCoordinator(
            mockContext,
            mockAppCoordinator,
            mockIDCardManager,
            mockTrackerManager,
            mockIssueTrackerManager,
            mockCoroutineContextProvider
        )

        identificationCoordinator.startIdentificationProcess(testTokenURL, true)

        verify { mockIDCardManager.cancelTask() }

        val results = mutableListOf<ScanEvent>()
        val job = identificationCoordinator.scanEventFlow
            .onEach(results::add)
            .launchIn(CoroutineScope(dispatcher))

        advanceUntilIdle()

        Assertions.assertEquals(ScanEvent.Error(ScanError.CardDeactivated), results.get(0))

        verify(exactly = 1) { mockAppCoordinator.navigate(IdentificationCardDeactivatedDestination) }

        job.cancel()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun startIdentificationProcess_ScanErrorWithoutRedirect() = runTest {
        val testFlow: Flow<EidInteractionEvent> = flow {
            emit(EidInteractionEvent.RequestCardInsertion)
            emit(EidInteractionEvent.CardRecognized)
            delay(500L)
            throw IdCardInteractionException.ProcessFailed(ActivationResultCode.INTERNAL_ERROR, null, null)
        }

        every { mockIDCardManager.identify(mockContext, testURL) } returns testFlow

        every { mockCoroutineContextProvider.IO } returns dispatcher

        val identificationCoordinator = IdentificationCoordinator(
            mockContext,
            mockAppCoordinator,
            mockIDCardManager,
            mockTrackerManager,
            mockIssueTrackerManager,
            mockCoroutineContextProvider
        )

        identificationCoordinator.startIdentificationProcess(testTokenURL, true)

        verify { mockIDCardManager.cancelTask() }

        val results = mutableListOf<ScanEvent>()
        val job = identificationCoordinator.scanEventFlow
            .onEach(results::add)
            .launchIn(CoroutineScope(dispatcher))

        advanceTimeBy(100L)

        Assertions.assertEquals(ScanEvent.CardAttached, results.get(0))

        advanceUntilIdle()

        Assertions.assertEquals(2, results.size)
        Assertions.assertEquals(ScanEvent.Error(ScanError.CardErrorWithoutRedirect), results.last())

        verify(exactly = 2) { mockAppCoordinator.navigate(any()) }

        val navigationParameter = destinationSlot.captured
        Assertions.assertEquals(IdentificationCardUnreadableDestination(true, null).route, navigationParameter.route)

        job.cancel()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun startIdentificationProcess_ScanErrorWithRedirect() = runTest {
        val redirectUrl = "redirectUrl"

        val testFlow: Flow<EidInteractionEvent> = flow {
            emit(EidInteractionEvent.RequestCardInsertion)
            emit(EidInteractionEvent.CardRecognized)
            delay(500L)
            throw IdCardInteractionException.ProcessFailed(ActivationResultCode.REDIRECT, redirectUrl, null)
        }

        mockkStatic("android.util.Base64")
        mockkStatic("android.net.Uri")
        every { Base64.encodeToString(any(), any()) } returns redirectUrl
        every { Uri.encode(any()) } returns redirectUrl
        every { Uri.decode(any()) } returns redirectUrl

        every { mockIDCardManager.identify(mockContext, testURL) } returns testFlow

        every { mockCoroutineContextProvider.IO } returns dispatcher

        val identificationCoordinator = IdentificationCoordinator(
            mockContext,
            mockAppCoordinator,
            mockIDCardManager,
            mockTrackerManager,
            mockIssueTrackerManager,
            mockCoroutineContextProvider
        )

        identificationCoordinator.startIdentificationProcess(testTokenURL, true)

        verify { mockIDCardManager.cancelTask() }

        val results = mutableListOf<ScanEvent>()
        val job = identificationCoordinator.scanEventFlow
            .onEach(results::add)
            .launchIn(CoroutineScope(dispatcher))

        advanceUntilIdle()

        Assertions.assertEquals(ScanEvent.CardAttached, results.get(0))

        advanceUntilIdle()

        Assertions.assertEquals(2, results.size)
        Assertions.assertEquals(ScanEvent.Error(ScanError.CardErrorWithRedirect(redirectUrl)), results.last())

        verify(exactly = 2) { mockAppCoordinator.navigate(any()) }

        val navigationParameter = destinationSlot.captured
        Assertions.assertEquals(IdentificationCardUnreadableDestination(true, redirectUrl).route, navigationParameter.route)

        job.cancel()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun startIdentificationProcess_ProcessFailedAfterCancellation_BeforeScanState() = runTest {
        val redirectUrl = "redirectUrl"

        val testFlow: Flow<EidInteractionEvent> = flow {
            throw IdCardInteractionException.ProcessFailed(ActivationResultCode.REDIRECT, redirectUrl, null)
        }

        every { mockIDCardManager.identify(mockContext, testURL) } returns testFlow

        every { mockCoroutineContextProvider.IO } returns dispatcher

        val identificationCoordinator = IdentificationCoordinator(
            mockContext,
            mockAppCoordinator,
            mockIDCardManager,
            mockTrackerManager,
            mockIssueTrackerManager,
            mockCoroutineContextProvider
        )

        identificationCoordinator.startIdentificationProcess(testTokenURL, true)

        verify { mockIDCardManager.cancelTask() }

        val results = mutableListOf<ScanEvent>()
        val job = identificationCoordinator.scanEventFlow
            .onEach(results::add)
            .launchIn(CoroutineScope(dispatcher))

        advanceUntilIdle()

        Assertions.assertEquals(1, results.size)
        Assertions.assertEquals(ScanEvent.CardRequested, results.get(0))

        verify(exactly = 0) { mockAppCoordinator.navigate(any()) }

        job.cancel()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun startIdentificationProcess_ProcessFailedAfterCancellation_AfterScanState() = runTest {
        val testFlow: Flow<EidInteractionEvent> = flow {
            emit(EidInteractionEvent.CardRecognized)
            delay(500L)
            throw IdCardInteractionException.ProcessFailed(ActivationResultCode.REDIRECT, null, "resultMinor")
        }

        every { mockIDCardManager.identify(mockContext, testURL) } returns testFlow

        every { mockCoroutineContextProvider.IO } returns dispatcher

        val identificationCoordinator = IdentificationCoordinator(
            mockContext,
            mockAppCoordinator,
            mockIDCardManager,
            mockTrackerManager,
            mockIssueTrackerManager,
            mockCoroutineContextProvider
        )

        identificationCoordinator.startIdentificationProcess(testTokenURL, true)

        verify { mockIDCardManager.cancelTask() }

        val results = mutableListOf<ScanEvent>()
        val job = identificationCoordinator.scanEventFlow
            .onEach(results::add)
            .launchIn(CoroutineScope(dispatcher))

        advanceTimeBy(100L)

        Assertions.assertEquals(ScanEvent.CardAttached, results.get(0))
        identificationCoordinator.cancelIdentification()

        advanceUntilIdle()

        Assertions.assertEquals(1, results.size)
        Assertions.assertEquals(ScanEvent.CardAttached, results.last())

        verify(exactly = 0) { mockAppCoordinator.navigate(any()) }

        job.cancel()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun startIdentificationProcess_NullPointerException() = runTest {
        val testRedirectUrl = "redirectUrl"

        val testFlow: Flow<EidInteractionEvent> = flow {
            throw NullPointerException()
        }

        mockkStatic("android.util.Base64")
        mockkStatic("android.net.Uri")
        every { Base64.encodeToString(any(), any()) } returns testRedirectUrl
        every { Uri.encode(any()) } returns testRedirectUrl
        every { Uri.decode(any()) } returns testRedirectUrl

        every { mockIDCardManager.identify(mockContext, testURL) } returns testFlow

        every { mockCoroutineContextProvider.IO } returns dispatcher

        val identificationCoordinator = IdentificationCoordinator(
            mockContext,
            mockAppCoordinator,
            mockIDCardManager,
            mockTrackerManager,
            mockIssueTrackerManager,
            mockCoroutineContextProvider
        )

        identificationCoordinator.startIdentificationProcess(testTokenURL, true)

        verify { mockIDCardManager.cancelTask() }

        val scanResults = mutableListOf<ScanEvent>()
        val scanJob = identificationCoordinator.scanEventFlow
            .onEach(scanResults::add)
            .launchIn(CoroutineScope(dispatcher))

        advanceUntilIdle()

        Assertions.assertEquals(ScanEvent.Error(ScanError.Other(null)), scanResults.get(0))

        verify(exactly = 1) { mockAppCoordinator.navigate(any()) }

        val navigationParameter = destinationSlot.captured
        Assertions.assertEquals(IdentificationOtherErrorDestination(testTokenURL).route, navigationParameter.route)

        scanJob.cancel()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun onPinEntered_WithPinCallback() = runTest {
        val testFlow = MutableStateFlow<EidInteractionEvent>(EidInteractionEvent.AuthenticationStarted)

        every { mockIDCardManager.identify(mockContext, testURL) } returns testFlow

        every { mockCoroutineContextProvider.IO } returns dispatcher

        val identificationCoordinator = IdentificationCoordinator(
            mockContext,
            mockAppCoordinator,
            mockIDCardManager,
            mockTrackerManager,
            mockIssueTrackerManager,
            mockCoroutineContextProvider
        )

        identificationCoordinator.startIdentificationProcess(testTokenURL, true)

        verify { mockIDCardManager.cancelTask() }

        var didCallCallback = false
        testFlow.value = EidInteractionEvent.RequestPin(null) {
            didCallCallback = true
        }

        advanceUntilIdle()

        identificationCoordinator.onPinEntered("testPin")

        Assertions.assertTrue(didCallCallback)

        verify(exactly = 1) { mockAppCoordinator.navigate(any()) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun onPinEntered_CalledTwice() = runTest {
        val testFlow = MutableStateFlow<EidInteractionEvent>(EidInteractionEvent.AuthenticationStarted)

        every { mockIDCardManager.identify(mockContext, testURL) } returns testFlow

        every { mockCoroutineContextProvider.IO } returns dispatcher

        val identificationCoordinator = IdentificationCoordinator(
            mockContext,
            mockAppCoordinator,
            mockIDCardManager,
            mockTrackerManager,
            mockIssueTrackerManager,
            mockCoroutineContextProvider
        )

        identificationCoordinator.startIdentificationProcess(testTokenURL, true)

        verify { mockIDCardManager.cancelTask() }

        var callbackCalledCount = 0
        testFlow.value = EidInteractionEvent.RequestPin(null) {
            callbackCalledCount++
        }

        advanceUntilIdle()

        identificationCoordinator.onPinEntered("testPin1")
        identificationCoordinator.onPinEntered("testPin2")

        Assertions.assertEquals(1, callbackCalledCount)

        verify(exactly = 1) { mockAppCoordinator.navigate(any()) }
    }

    @Test
    fun pop() {
        val identificationCoordinator = IdentificationCoordinator(
            mockContext,
            mockAppCoordinator,
            mockIDCardManager,
            mockTrackerManager,
            mockIssueTrackerManager,
            mockCoroutineContextProvider
        )

        identificationCoordinator.pop()

        verify(exactly = 1) { mockAppCoordinator.pop() }
    }
}
