package de.digitalService.useID.analytics

import android.content.Context

interface TrackerManagerType {
    fun initTracker(context: Context)
    fun trackScreen(route: String)
    fun trackEvent(category: String, action: String, name: String)

    fun trackButtonPressed(category: String, name: String)
    fun dispatch()
}
