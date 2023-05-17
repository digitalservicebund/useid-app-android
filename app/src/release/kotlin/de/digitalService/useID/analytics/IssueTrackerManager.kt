package de.digitalService.useID.analytics

import io.sentry.Breadcrumb
import io.sentry.Sentry
import io.sentry.SentryLevel
import javax.inject.Inject

class IssueTrackerManager @Inject constructor() : IssueTrackerManagerType {
    override fun capture(exception: Throwable) {
        Sentry.captureException(exception)
    }

    override fun captureMessage(message: String) {
        Sentry.captureMessage(message)
    }

    override fun addInfoBreadcrumb(category: String, message: String) {
        val breadcrumb = Breadcrumb()
        breadcrumb.category = category
        breadcrumb.message = message
        breadcrumb.level = SentryLevel.INFO
        Sentry.addBreadcrumb(breadcrumb)
    }
}
