package de.digitalService.useID.hilt

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import de.digitalService.useID.StorageManager
import de.digitalService.useID.StorageManagerType
import de.digitalService.useID.analytics.IssueTrackerManager
import de.digitalService.useID.analytics.IssueTrackerManagerType
import de.digitalService.useID.analytics.TrackerManager
import de.digitalService.useID.analytics.TrackerManagerType
import de.digitalService.useID.ui.coordinators.AppCoordinator
import de.digitalService.useID.ui.coordinators.AppCoordinatorType
import de.digitalService.useID.ui.coordinators.NavigatorDelegate
import de.digitalService.useID.util.CoroutineContextProvider
import de.digitalService.useID.util.CoroutineContextProviderType
import de.digitalService.useID.util.CurrentTimeProvider
import de.digitalService.useID.util.CurrentTimeProviderInterface

@Module
@InstallIn(SingletonComponent::class)
abstract class SingletonBindingModule {
    @Binds
    abstract fun bindAppCoordinator(appCoordinator: AppCoordinator): AppCoordinatorType

    @Binds
    abstract fun bindNavigator(appCoordinator: AppCoordinator): NavigatorDelegate

    @Binds
    abstract fun bindCoroutineContextProvider(coroutineContextProvider: CoroutineContextProvider): CoroutineContextProviderType

    @Binds
    abstract fun bindStorageManager(storageManager: StorageManager): StorageManagerType

    @Binds
    abstract fun bindTrackerManager(trackerManager: TrackerManager): TrackerManagerType

    @Binds
    abstract fun bindIssueTrackerManager(issueTrackerManager: IssueTrackerManager): IssueTrackerManagerType

    @Binds
    abstract fun bindCurrentTimeProvider(currentTimeProvider: CurrentTimeProvider): CurrentTimeProviderInterface
}
