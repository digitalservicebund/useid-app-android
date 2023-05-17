package de.digitalService.useID.analytics

interface IssueTrackerManagerType {
    fun capture(exception: Throwable)
    fun captureMessage(message: String)
    fun addInfoBreadcrumb(category: String, message: String)
}
