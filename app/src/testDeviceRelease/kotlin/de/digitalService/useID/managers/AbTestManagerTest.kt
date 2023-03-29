package de.digitalService.useID.managers

import de.digitalService.useID.analytics.IssueTrackerManager
import de.digitalService.useID.analytics.TrackerManager
import de.digitalService.useID.util.AbTest
import de.digitalService.useID.util.AbTestManager
import de.digitalService.useID.util.UnleashException
import io.getunleash.UnleashClient
import io.getunleash.UnleashConfig
import io.getunleash.UnleashContext
import io.getunleash.data.Variant
import io.getunleash.polling.AutoPollingMode
import io.getunleash.polling.PollingModes
import io.getunleash.polling.TogglesErroredListener
import io.getunleash.polling.TogglesUpdatedListener
import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.util.*

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MockKExtension::class)
class AbTestManagerTest {

    @MockK(relaxUnitFun = true)
    lateinit var mockTrackerManager: TrackerManager

    @MockK(relaxUnitFun = true)
    lateinit var mockIssueTrackerManager: IssueTrackerManager

    @MockK
    lateinit var mockConfig: UnleashConfig

    @MockK
    lateinit var mockContext: UnleashContext

    @MockK
    lateinit var mockPollingMode: AutoPollingMode

    private val testApiUrl = "url"
    private val testApiKey = "key"
    private val togglesUpdatedListenerSlot = slot<TogglesUpdatedListener>()
    private val togglesErroredListenerSlot = slot<TogglesErroredListener>()


    @BeforeEach
    fun beforeEach() {
        every { mockPollingMode.togglesUpdatedListener } returns TogglesUpdatedListener { }
        every { mockPollingMode.erroredListener } returns TogglesErroredListener { }
        every { mockPollingMode.pollImmediate } returns true
        every { mockPollingMode.pollRateDuration } returns 100

        every { mockConfig.proxyUrl } returns "https://unleash.proxy"
        every { mockConfig.clientKey } returns ""
        every { mockConfig.httpClientReadTimeout } returns 100
        every { mockConfig.httpClientConnectionTimeout } returns 100
        every { mockConfig.httpClientCacheSize } returns 100
        every { mockConfig.reportMetrics } returns null
        every { mockConfig.pollingMode } returns mockPollingMode

        mockkObject(PollingModes)
        every { PollingModes.autoPoll(any()) } returns mockPollingMode

        mockkObject(UnleashConfig.Companion)
        every {
            UnleashConfig.Companion.newBuilder()
                .proxyUrl(testApiUrl)
                .clientKey(testApiKey)
                .pollingMode(mockPollingMode)
                .build()
        } returns mockConfig

        mockkStatic(UUID::class)
        every { UUID.randomUUID().toString() } returns ""

        mockkObject(UnleashContext.Companion)
        every {
            UnleashContext.newBuilder()
                .appName("bundesIdent.Android")
                .sessionId(UUID.randomUUID().toString())
                .properties(mutableMapOf(Pair("supportedToggles", AbTest.values().joinToString(",") { it.testName })))
                .build()
        } returns mockContext

        mockkConstructor(UnleashClient::class)
        every {
            anyConstructed<UnleashClient>().addTogglesUpdatedListener(capture(togglesUpdatedListenerSlot))
        } returns Unit
        every {
            anyConstructed<UnleashClient>().addTogglesErroredListener(capture(togglesErroredListenerSlot))
        } returns Unit
    }

    @Test
    fun unleashInitialisation() = runTest {
        launch {
            delay(100)
            togglesUpdatedListenerSlot.captured.onTogglesUpdated()
        }

        val abTestManager = AbTestManager(testApiUrl, testApiKey, mockTrackerManager, mockIssueTrackerManager)
        abTestManager.initialise()

        verify {
            UnleashConfig.newBuilder()
                .proxyUrl(testApiUrl)
                .clientKey(testApiKey)
                .pollingMode(mockPollingMode)
                .build()
        }

        verify {
            UnleashContext.newBuilder()
                .appName("bundesIdent.Android")
                .sessionId(UUID.randomUUID().toString())
                .properties(mutableMapOf(Pair("supportedToggles", AbTest.values().joinToString(",") { it.testName })))
                .build()
        }

        verify {
            anyConstructed<UnleashClient>().addTogglesUpdatedListener(togglesUpdatedListenerSlot.captured)
        }
        verify {
            anyConstructed<UnleashClient>().addTogglesErroredListener(togglesErroredListenerSlot.captured)
        }
    }

    @Test
    fun togglesUpdateWithVariationVariant() = runTest {
        val mockVariant = mockk<Variant> {
            every { name } returns "Variation"
        }

        every { anyConstructed<UnleashClient>().isEnabled(any()) } returns true
        every { anyConstructed<UnleashClient>().getVariant(any()) } returns mockVariant

        launch {
            delay(100)
            togglesUpdatedListenerSlot.captured.onTogglesUpdated()
            delay(100)
        }

        val abTestManager = AbTestManager(testApiUrl, testApiKey, mockTrackerManager, mockIssueTrackerManager)
        abTestManager.initialise()

        val testName = AbTest.SETUP_INTRODUCTION_EXPLANATION.testName.replace(".", "_")
        verify { mockTrackerManager.trackEvent("abtesting", testName, "Variation") }
        assertTrue(abTestManager.isSetupIntroTestVariation.value)
    }

    @Test
    fun togglesUpdateWithDisabledToggle() = runTest {
        every { anyConstructed<UnleashClient>().isEnabled(any()) } returns false

        launch {
            delay(100)
            togglesUpdatedListenerSlot.captured.onTogglesUpdated()
            delay(100)
        }

        val abTestManager = AbTestManager(testApiUrl, testApiKey, mockTrackerManager, mockIssueTrackerManager)
        abTestManager.initialise()

        val testName = AbTest.SETUP_INTRODUCTION_EXPLANATION.testName.replace(".", "_")
        verify(exactly = 0) { mockTrackerManager.trackEvent(any(), any(), any()) }
        assertFalse(abTestManager.isSetupIntroTestVariation.value)
    }

    @Test
    fun togglesUpdateWithOriginalVariant() = runTest {
        every { anyConstructed<UnleashClient>().isEnabled(any()) } returns false

        launch {
            delay(100)
            togglesUpdatedListenerSlot.captured.onTogglesUpdated()
            delay(100)
        }

        val mockVariant = mockk<Variant> {
            every { name } returns "Original"
        }

        every { anyConstructed<UnleashClient>().isEnabled(any()) } returns true
        every { anyConstructed<UnleashClient>().getVariant(any()) } returns mockVariant

        val abTestManager = AbTestManager(testApiUrl, testApiKey, mockTrackerManager, mockIssueTrackerManager)
        abTestManager.initialise()

        val testName = AbTest.SETUP_INTRODUCTION_EXPLANATION.testName.replace(".", "_")
        verify { mockTrackerManager.trackEvent("abtesting", testName, "Original") }
        assertFalse(abTestManager.isSetupIntroTestVariation.value)
    }

    @Test
    fun testTogglesErrored() = runTest {
        val exception = Exception()

        launch {
            delay(100)
            togglesErroredListenerSlot.captured.onError(exception)
        }

        val abTestManager = AbTestManager(testApiUrl, testApiKey, mockTrackerManager, mockIssueTrackerManager)
        abTestManager.initialise()

        verify { mockIssueTrackerManager.capture(exception) }
        assertFalse(abTestManager.isSetupIntroTestVariation.value)
    }

    @Test
    fun initialisationCancelled() = runTest {
        val abTestManager = AbTestManager(testApiUrl, testApiKey, mockTrackerManager, mockIssueTrackerManager)
        val job = launch {
            abTestManager.initialise()
        }
        launch {
            delay(100)
            job.cancel()
            verify { mockIssueTrackerManager.capture(withArg { (UnleashException("Timed out while fetching toggles.")) }) }
            assertFalse(abTestManager.isSetupIntroTestVariation.value)
        }
    }
}
