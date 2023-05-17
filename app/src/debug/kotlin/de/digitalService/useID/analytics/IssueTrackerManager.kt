package de.digitalService.useID.analytics

import javax.inject.Inject

class IssueTrackerManager @Inject constructor() : IssueTrackerManagerType {
    override fun capture(exception: Throwable) {}
    override fun captureMessage(message: String) {}
    override fun addInfoBreadcrumb(category: String, message: String) {}
}
