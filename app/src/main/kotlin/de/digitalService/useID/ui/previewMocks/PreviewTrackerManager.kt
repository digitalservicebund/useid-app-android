package de.digitalService.useID.ui.previewMocks

import android.content.Context
import de.digitalService.useID.analytics.TrackerManagerType

class PreviewTrackerManager : TrackerManagerType {
    override fun initTracker(context: Context) {}
    override fun trackScreen(route: String) {}
    override fun trackEvent(category: String, action: String, name: String) {}
    override fun trackButtonPressed(category: String, name: String) {}
    override fun dispatch() {}
}
