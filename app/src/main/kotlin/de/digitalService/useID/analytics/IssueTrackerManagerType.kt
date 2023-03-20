package de.digitalService.useID.analytics

interface IssueTrackerManagerType {
    fun capture(exception: Throwable)

    fun addInfoBreadcrumb(category: String, message: String)
}
