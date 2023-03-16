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

    private val _initialised = mutableStateOf(false)
    val initialised: State<Boolean> = _initialised

    suspend fun initialise() = suspendCancellableCoroutine { continuation ->
        val config = UnleashConfig.newBuilder()
            .proxyUrl(url)
            .clientKey(apiKey)
            .pollingMode(PollingModes.autoPoll(3600))
            .build()

        val supportedAbTests = AbTest.values().joinToString(",") { it.testName }
        val context = UnleashContext.newBuilder()
            .appName("useid-android")
            .sessionId(UUID.randomUUID().toString())
            .properties(mutableMapOf(Pair("supportedToggles", supportedAbTests)))
            .build()

        unleashClient = UnleashClient(config, context)

        unleashClient.addTogglesUpdatedListener {
            _initialised.value = true
            continuation.resume(Unit)
        }
        unleashClient.addTogglesErroredListener {
            issueTrackerManager.capture(it)
            _initialised.value = false
            continuation.resume(Unit)
        }

        continuation.invokeOnCancellation {
            //todo - can we stop unleash trying to fetch data? because we don't care anymore at this point
        }
    }

    fun disable() {
        _initialised.value = false
        issueTrackerManager.addInfoBreadcrumbs("Unleash", "Request taking too long.")
    }

    fun isVariationActivatedFor(test: AbTest): Boolean =
        if (test.testName.isEnabled()) {
            val variantName = unleashClient.getVariant(test.testName).name
            trackerManager.trackEvent("abtesting", test.testName, variantName)
            variantName == "variation"
        } else false

    private fun String.isEnabled(): Boolean =
        _initialised.value && unleashClient.isEnabled(this)
}

enum class AbTest(val testName: String) {
    DARIA_TEST("daria.test")
}
