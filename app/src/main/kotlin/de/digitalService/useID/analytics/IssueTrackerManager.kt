package de.digitalService.useID.analytics

import io.sentry.Sentry
import javax.inject.Inject

interface IssueTrackerManagerType {
    fun capture(exception: Throwable)
}

class IssueTrackerManager @Inject constructor() : IssueTrackerManagerType {
    override fun capture(exception: Throwable) {
        Sentry.captureException(exception)
    }
}
