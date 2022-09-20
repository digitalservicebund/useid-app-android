package de.digitalService.useID.hilt

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.components.SingletonComponent
import de.digitalService.useID.SecureStorageManager
import de.digitalService.useID.SecureStorageManagerInterface
import de.digitalService.useID.StorageManager
import de.digitalService.useID.StorageManagerType
import de.digitalService.useID.analytics.TrackerManager
import de.digitalService.useID.analytics.TrackerManagerType
import de.digitalService.useID.idCardInterface.IDCardManager
import de.digitalService.useID.ui.coordinators.AppCoordinator
import de.digitalService.useID.ui.coordinators.AppCoordinatorType
import de.digitalService.useID.util.CoroutineContextProvider
import de.digitalService.useID.util.CoroutineContextProviderType
import de.digitalService.useID.util.CurrentTimeProvider
import de.digitalService.useID.util.CurrentTimeProviderInterface
import kotlinx.coroutines.CoroutineScope
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class SingletonModule {
    @Provides
    @Singleton
    fun provideIDCardManager() = IDCardManager()
}
