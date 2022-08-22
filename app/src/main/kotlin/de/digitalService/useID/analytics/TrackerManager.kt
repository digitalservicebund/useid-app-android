package de.digitalService.useID.analytics

import android.content.Context
import androidx.navigation.NavDestination
import org.matomo.sdk.Matomo
import org.matomo.sdk.Tracker
import org.matomo.sdk.TrackerBuilder
import org.matomo.sdk.extra.TrackHelper
import javax.inject.Inject
import javax.inject.Singleton

interface TrackerManagerType {
    fun initTracker(context: Context)
    fun trackDestination(destination: NavDestination)
    fun trackEvent(category: String, action: String, name: String)
}

@Singleton
class TrackerManager @Inject constructor(): TrackerManagerType {
    private val API_URL = "https://bund.matomo.cloud/matomo.php"

    private lateinit var tracker: Tracker

    override  fun initTracker(context: Context) {
        tracker = TrackerBuilder.createDefault(API_URL, siteId()).build(Matomo.getInstance(context))
    }

    override fun trackDestination(destination: NavDestination) {
        TrackHelper.track().screen(destination.route).with(tracker)
    }

    override fun trackEvent(category: String, action: String, name: String) {
        TrackHelper.track().event(category, action).name(name).with(tracker)
    }
}

class MockTrackerManager: TrackerManagerType {
    override fun initTracker(context: Context) {}
    override fun trackDestination(destination: NavDestination) {}
    override fun trackEvent(category: String, action: String, name: String) { }
}
