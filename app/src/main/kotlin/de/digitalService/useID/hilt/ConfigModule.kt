package de.digitalService.useID.hilt

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Named

@Module
@InstallIn(SingletonComponent::class)
class ConfigModule {
    companion object {
        const val TRACKING_API_URL = "TRACKING_API_URL"
        const val TRACKING_SESSION_TIMEOUT = "TRACKING_SESSION_TIMEOUT"
    }

    @Provides
    @Named(TRACKING_API_URL)
    fun provideTrackingApiUrl(): String = "https://bund.matomo.cloud/matomo.php"

    @Provides
    @Named(TRACKING_SESSION_TIMEOUT)
    fun provideTrackingSessionTimeout(): Long = 1800L
}
