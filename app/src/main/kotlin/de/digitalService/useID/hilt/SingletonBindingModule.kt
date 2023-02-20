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
import de.digitalService.useID.ui.navigation.AppNavigator
import de.digitalService.useID.ui.navigation.Navigator
import de.digitalService.useID.util.*

@Module
@InstallIn(SingletonComponent::class)
abstract class SingletonBindingModule {
    @Binds
    abstract fun bindAppCoordinator(appCoordinator: AppCoordinator): AppCoordinatorType

    @Binds
    abstract fun bindNavigator(appNavigator: AppNavigator): Navigator

    @Binds
    abstract fun bindStorageManager(storageManager: StorageManager): StorageManagerType

    @Binds
    abstract fun bindTrackerManager(trackerManager: TrackerManager): TrackerManagerType

    @Binds
    abstract fun bindIssueTrackerManager(issueTrackerManager: IssueTrackerManager): IssueTrackerManagerType

    @Binds
    abstract fun bindCurrentTimeProvider(currentTimeProvider: CurrentTimeProvider): CurrentTimeProviderInterface
}

@Module
@InstallIn(SingletonComponent::class)
abstract class CoroutineContextProviderModule {
    @Binds
    abstract fun bindCoroutineContextProvider(coroutineContextProvider: CoroutineContextProvider): CoroutineContextProviderType
}

@Module
@InstallIn(SingletonComponent::class)
abstract class NfcInterfaceMangerModule {
    @Binds
    abstract fun bindNfcInterfaceManager(nfcInterfaceManager: NfcInterfaceManager): NfcInterfaceManagerType
}
