package de.digitalService.useID.util

import de.digitalService.useID.analytics.IssueTrackerManagerType
import de.digitalService.useID.analytics.TrackerManagerType
import de.digitalService.useID.hilt.ConfigModule
import io.getunleash.UnleashClient
import io.getunleash.UnleashConfig
import io.getunleash.UnleashContext
import io.getunleash.polling.PollingModes
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.*
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class AbTestManager @Inject constructor(
    @Named(ConfigModule.UNLEASH_API_URL) private val url: String,
    @Named(ConfigModule.UNLEASH_API_KEY) private val apiKey: String,
    private val trackerManager: TrackerManagerType,
    private val issueTrackerManager: IssueTrackerManagerType
) {

    private val unleashClient: UnleashClient

    private val _state: MutableStateFlow<State> = MutableStateFlow(State.LOADING)
    val state: Flow<State> = _state

    enum class State { LOADING, ACTIVE, DISABLED }

    init {
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
            _state.value = State.ACTIVE
        }
        unleashClient.addTogglesErroredListener {
            issueTrackerManager.capture(it)
            _state.value = State.DISABLED
        }
    }

    fun disable() {
        _state.value = State.DISABLED
        issueTrackerManager.addInfoBreadcrumbs("Unleash", "Request taking too long.")
    }

    fun isVariationActivatedFor(test: AbTest): Boolean =
        if (test.testName.isEnabled()) {
            val variantName = unleashClient.getVariant(test.testName).name
            trackerManager.trackEvent("abtesting", test.testName, variantName)
            variantName == "variation"
        } else false

    private fun String.isEnabled(): Boolean =
        if (_state.value == State.ACTIVE) unleashClient.isEnabled(this) else false
}

enum class AbTest(val testName: String) {
    DARIA_TEST("daria.test")
}
