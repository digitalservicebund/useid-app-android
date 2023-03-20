package de.digitalService.useID.hilt

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import de.digitalService.useID.analytics.IssueTrackerManagerType
import de.digitalService.useID.analytics.TrackerManagerType
import de.digitalService.useID.util.AbTestManager
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AbTestModule {

    @Provides
    @Singleton
    fun providesAbTestManager(
        @Named(ConfigModule.UNLEASH_API_URL) url: String,
        @Named(ConfigModule.UNLEASH_API_KEY) apiKey: String,
        trackerManager: TrackerManagerType,
        issueTrackerManager: IssueTrackerManagerType
    ) = AbTestManager(url, apiKey, trackerManager, issueTrackerManager)
}
