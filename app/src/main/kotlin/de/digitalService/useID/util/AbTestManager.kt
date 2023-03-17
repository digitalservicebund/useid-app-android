package de.digitalService.useID.util

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import de.digitalService.useID.analytics.IssueTrackerManagerType
import de.digitalService.useID.analytics.TrackerManagerType
import de.digitalService.useID.hilt.ConfigModule
import io.getunleash.UnleashClient
import io.getunleash.UnleashConfig
import io.getunleash.UnleashContext
import io.getunleash.polling.PollingModes
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.*
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class AbTestManager @Inject constructor(
    @Named(ConfigModule.UNLEASH_API_URL) private val url: String,
    @Named(ConfigModule.UNLEASH_API_KEY) private val apiKey: String,
    private val trackerManager: TrackerManagerType,
    private val issueTrackerManager: IssueTrackerManagerType
) {

    private lateinit var unleashClient: UnleashClient

    private val _isSetupIntroTestVariant = mutableStateOf(false)
    val isSetupIntroTestVariant: State<Boolean> = _isSetupIntroTestVariant

    suspend fun initialise() = suspendCancellableCoroutine { continuation ->
        val config = UnleashConfig.newBuilder()
            .proxyUrl(url)
            .clientKey(apiKey)
            .pollingMode(PollingModes.autoPoll(Int.MAX_VALUE.toLong()))
            .build()

        val supportedAbTests = AbTest.values().joinToString(",") { it.testName }
        val context = UnleashContext.newBuilder()
            .appName("useid-android")
            .sessionId(UUID.randomUUID().toString())
            .properties(mutableMapOf(Pair("supportedToggles", supportedAbTests)))
            .build()

        unleashClient = UnleashClient(config, context)

        unleashClient.addTogglesUpdatedListener {
            _isSetupIntroTestVariant.value = isVariationActivatedFor(AbTest.SETUP_INTRODUCTION_EXPLANATION)
            continuation.resume(Unit)
        }
        unleashClient.addTogglesErroredListener {
            issueTrackerManager.capture(it)
            trackDisabled("Error fetching toggles.")
            continuation.resume(Unit)
        }

        continuation.invokeOnCancellation {
            unleashClient.close()
            trackDisabled("Timed out while fetching toggles.")
        }
    }

    private fun isVariationActivatedFor(test: AbTest): Boolean =
        if (unleashClient.isEnabled(test.testName)) {
            val variantName = unleashClient.getVariant(test.testName).name
            trackerManager.trackEvent("abtesting", test.testName, variantName)
            variantName == "variation"
        } else false

    private fun trackDisabled(message: String) {
        issueTrackerManager.addInfoBreadcrumbs("Unleash", message)
    }
}

enum class AbTest(val testName: String) {
    SETUP_INTRODUCTION_EXPLANATION("bundesIdent.setup_introduction_explanation")
}
