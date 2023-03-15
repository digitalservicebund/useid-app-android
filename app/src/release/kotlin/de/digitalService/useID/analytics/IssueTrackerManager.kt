package de.digitalService.useID.analytics

import io.sentry.Breadcrumb
import io.sentry.Sentry
import io.sentry.SentryLevel
import javax.inject.Inject

class IssueTrackerManager @Inject constructor() : IssueTrackerManagerType {
    override fun capture(exception: Throwable) {
        Sentry.captureException(exception)
    }

    override fun addInfoBreadcrumbs(category: String, message: String) {
        val breadcrumb = Breadcrumb()
        breadcrumb.category = category
        breadcrumb.message = message
        breadcrumb.level = SentryLevel.INFO
        Sentry.addBreadcrumb(breadcrumb)
    }
}
