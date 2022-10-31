package de.digitalService.useID

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import dagger.hilt.android.AndroidEntryPoint
import de.digitalService.useID.analytics.TrackerManagerType
import de.digitalService.useID.hilt.ConfigModule
import de.digitalService.useID.ui.UseIDApp
import de.digitalService.useID.ui.coordinators.AppCoordinatorType
import io.sentry.Sentry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val logger by getLogger()

    @Inject
    lateinit var appCoordinator: AppCoordinatorType

    @Inject
    lateinit var trackerManager: TrackerManagerType

    @Inject
    @Named(ConfigModule.SENTRY_DSN)
    lateinit var sentryDsn: String

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()

        var keepSplashScreen = true
        splashScreen.setKeepOnScreenCondition { keepSplashScreen }

        CoroutineScope(Dispatchers.Main).launch {
            delay(600)
            keepSplashScreen = false
        }

        super.onCreate(savedInstanceState)

        Sentry.init(sentryDsn)

        handleNewIntent(intent)

        setContent {
            UseIDApp(appCoordinator, trackerManager)
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        intent?.let { handleNewIntent(it) }
    }

    override fun onPause() {
        super.onPause()
        trackerManager.dispatch()
    }

    private fun handleNewIntent(intent: Intent) {
        val intentData = intent.data
        if (intent.action == Intent.ACTION_VIEW && intentData != null) {
            appCoordinator.handleDeepLink(intentData)
        }
    }
}
