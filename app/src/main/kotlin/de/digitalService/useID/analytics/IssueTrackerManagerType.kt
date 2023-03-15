package de.digitalService.useID.analytics

interface IssueTrackerManagerType {
    fun capture(exception: Throwable)

    fun addInfoBreadcrumbs(category: String, message: String)
}
