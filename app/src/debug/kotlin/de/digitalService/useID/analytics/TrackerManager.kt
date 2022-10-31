package de.digitalService.useID.analytics

import android.content.Context
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TrackerManager @Inject constructor() : TrackerManagerType {
    override fun initTracker(context: Context) {}
    override fun trackScreen(route: String) {}
    override fun trackEvent(category: String, action: String, name: String) {}
    override fun dispatch() {}
}
