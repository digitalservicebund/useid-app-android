package de.digitalService.useID.hilt

import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Named

@Module
@InstallIn(SingletonComponent::class)
class ConfigModule {
    companion object {
        const val TRACKING_API_URL = "TRACKING_API_URL"
        const val TRACKING_SESSION_TIMEOUT = "TRACKING_SESSION_TIMEOUT"
        const val TRACKING_SITE_ID = "TRACKING_SITE_ID"

        const val SENTRY_DSN = "SENTRY_DSN"

        const val UNLEASH_API_URL = "CONFIGURATION_API_URL"
        const val UNLEASH_API_KEY = "CONFIGURATION_API_KEY"
    }

    @Provides
    @Named(TRACKING_API_URL)
    fun provideTrackingApiUrl(@ApplicationContext context: Context): String {
        val matomoHost = getMetadata(context).getString("matomoHost")
        return "https://$matomoHost/matomo.php"
    }

    @Provides
    @Named(TRACKING_SESSION_TIMEOUT)
    fun provideTrackingSessionTimeout(): Long = 1800000L

    @Provides
    @Named(TRACKING_SITE_ID)
    fun provideTrackingSiteId(@ApplicationContext context: Context): Int =
        getMetadata(context).getInt("matomoSiteId")

    @Provides
    @Named(SENTRY_DSN)
    fun provideSentryDsn(@ApplicationContext context: Context): String {
        val sentryPublicKey = getMetadata(context).getString("sentryPublicKey")
        val sentryProjectId = getMetadata(context).getInt("sentryProjectId")

        return "https://$sentryPublicKey@o1248831.ingest.sentry.io/$sentryProjectId"
    }

    @Provides
    @Named(UNLEASH_API_URL)
    fun providesUnleashApiUrl(@ApplicationContext context: Context): String {
        val unleashHost = getMetadata(context).getString("unleashHost")
        return "$unleashHost/api/frontend"
    }

    @Provides
    @Named(UNLEASH_API_KEY)
    fun providesUnleashApiKey(@ApplicationContext context: Context): String {
        return getMetadata(context).getString("unleashKey").orEmpty()
    }

    private fun getMetadata(context: Context): Bundle = context.packageManager.getApplicationInfo(
        context.packageName,
        PackageManager.GET_META_DATA
    ).metaData
}
