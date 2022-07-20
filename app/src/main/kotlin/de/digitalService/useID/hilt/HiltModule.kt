package de.digitalService.useID.hilt

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.components.SingletonComponent
import de.digitalService.useID.SecureStorageManager
import de.digitalService.useID.SecureStorageManagerInterface
import de.digitalService.useID.idCardInterface.IDCardManager
import kotlinx.coroutines.CoroutineScope
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class SingletonModule {
    @Provides
    @Singleton
    fun provideIDCardManager() = IDCardManager()
}

@Module
@InstallIn(ViewModelComponent::class)
class ViewModelModule {
    @Provides
    fun provideViewModelCoroutineScope(): CoroutineScope? = null
}

@Module
@InstallIn(SingletonComponent::class)
abstract class SingletonBindingModule {
    @Binds
    abstract fun bindSecureStorageManager(secureStorageManager: SecureStorageManager): SecureStorageManagerInterface
}
