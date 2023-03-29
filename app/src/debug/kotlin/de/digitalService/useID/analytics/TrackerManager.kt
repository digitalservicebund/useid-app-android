package de.digitalService.useID.analytics

import android.content.Context
import de.digitalService.useID.getLogger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TrackerManager @Inject constructor() : TrackerManagerType {
    private val logger by getLogger()

    override fun initTracker(context: Context) {}
    override fun trackScreen(route: String) = logger.debug("Track Screen: $route")
    override fun trackEvent(category: String, action: String, name: String) = logger.debug("Track event: $category, $action, $name")

    override fun trackButtonPressed(category: String, name: String) = logger.debug("Track button pressed: $category, $name")
    override fun dispatch() {}
}
