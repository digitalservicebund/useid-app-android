package de.digitalService.useID.analytics

import android.content.Context
import de.digitalService.useID.getLogger
import de.digitalService.useID.util.CurrentTimeProviderInterface
import org.matomo.sdk.Matomo
import org.matomo.sdk.Tracker
import org.matomo.sdk.TrackerBuilder
import org.matomo.sdk.extra.TrackHelper
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class TrackerManager @Inject constructor() : TrackerManagerType {
    override fun initTracker(context: Context) {}
    override fun trackScreen(route: String) {}
    override fun trackEvent(category: String, action: String, name: String) {}
    override fun dispatch() {}
}
