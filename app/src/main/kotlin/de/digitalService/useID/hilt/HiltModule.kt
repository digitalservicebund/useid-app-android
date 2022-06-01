package de.digitalService.useID.hilt

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.components.ViewModelComponent
import de.digitalService.useID.idCardInterface.IDCardManager
import kotlinx.coroutines.CoroutineScope

@Module
@InstallIn(ViewModelComponent::class, ActivityComponent::class)
class HiltModule {

    @Provides
    fun provideIDCardManager() = IDCardManager()

    @Provides
    fun provideViewModelCoroutineScope(): CoroutineScope? = null
}
