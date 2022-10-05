package de.digitalService.useID.analytics

import io.sentry.Sentry
import javax.inject.Inject

class IssueTrackerManager @Inject constructor() : IssueTrackerManagerType {
    override fun capture(exception: Throwable) {
        Sentry.captureException(exception)
    }
}
