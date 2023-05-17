package de.digitalService.useID.hilt

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import de.digitalService.useID.analytics.IssueTrackerManagerType
import de.digitalService.useID.idCardInterface.EidInteractionManager
import de.digitalService.useID.util.CoroutineContextProvider
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class SingletonModule {
    @Provides
    @Singleton
    fun provideEidInteractionManager(coroutineContextProvider: CoroutineContextProvider, issueTrackerManager: IssueTrackerManagerType) = EidInteractionManager(coroutineContextProvider, issueTrackerManager)
}
