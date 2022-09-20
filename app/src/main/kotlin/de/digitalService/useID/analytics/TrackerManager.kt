package de.digitalService.useID.analytics

import android.content.Context
import androidx.navigation.NavDestination
import de.digitalService.useID.getLogger
import de.digitalService.useID.util.CurrentTimeProviderInterface
import org.matomo.sdk.Matomo
import org.matomo.sdk.Tracker
import org.matomo.sdk.TrackerBuilder
import org.matomo.sdk.extra.TrackHelper
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

interface TrackerManagerType {
    fun initTracker(context: Context)
    fun trackDestination(destination: NavDestination)
    fun trackEvent(category: String, action: String, name: String)
}

@Singleton
class TrackerManager @Inject constructor(
    private val currentTimeProvider: CurrentTimeProviderInterface,
    @Named("TRACKING_SESSION_TIMEOUT") private val sessionTimeout: Long,
    @Named("TRACKING_API_URL") private val apiUrl: String

) : TrackerManagerType {
    private val logger by getLogger()

    private var lastActivity = currentTimeProvider.currentTime

    private lateinit var tracker: Tracker

    override fun initTracker(context: Context) {
        tracker = TrackerBuilder.createDefault(apiUrl, siteId()).build(Matomo.getInstance(context))
    }

    override fun trackDestination(destination: NavDestination) {
        val route = destination.route
        TrackHelper.track().screen(route).with(tracker)
        updateSession()
    }

    override fun trackEvent(category: String, action: String, name: String) {
        TrackHelper.track().event(category, action).name(name).with(tracker)
        updateSession()
    }

    private fun updateSession() {
        val currentTime = currentTimeProvider.currentTime
        val sessionLength = currentTime - lastActivity

        if (sessionLength > sessionTimeout) {
            logger.debug("Resetting tracker session.")
            tracker.reset()
        }

        lastActivity = currentTime
    }
}

class MockTrackerManager : TrackerManagerType {
    override fun initTracker(context: Context) {}
    override fun trackDestination(destination: NavDestination) {}
    override fun trackEvent(category: String, action: String, name: String) { }
}
