package de.digitalService.useID

import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.core.splashscreen.SplashScreen
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import dagger.hilt.android.AndroidEntryPoint
import de.digitalService.useID.analytics.TrackerManagerType
import de.digitalService.useID.hilt.ConfigModule
import de.digitalService.useID.idCardInterface.IdCardManager
import de.digitalService.useID.ui.UseIDApp
import de.digitalService.useID.ui.coordinators.AppCoordinatorType
import de.digitalService.useID.ui.navigation.Navigator
import de.digitalService.useID.util.AbTestManager
import de.digitalService.useID.util.NfcInterfaceManagerType
import io.sentry.Sentry
import kotlinx.coroutines.*
import java.io.IOException
import javax.inject.Inject
import javax.inject.Named
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val logger by getLogger()

    @Inject
    lateinit var idCardManager: IdCardManager

    @Inject
    lateinit var appCoordinator: AppCoordinatorType

    @Inject
    lateinit var appNavigator: Navigator

    @Inject
    lateinit var trackerManager: TrackerManagerType

    @Inject
    lateinit var nfcInterfaceManager: NfcInterfaceManagerType

    @Inject
    lateinit var abTestManager: AbTestManager

    @Inject
    @Named(ConfigModule.SENTRY_DSN)
    lateinit var sentryDsn: String

    private lateinit var splashScreen: SplashScreen

    override fun onCreate(savedInstanceState: Bundle?) {
        splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        prepareAppLaunch()

        Sentry.init { options ->
            options.dsn = sentryDsn
            options.dist = "${BuildConfig.VERSION_CODE}"
        }

        handleNewIntent(intent)

        setContent {
            val nfcAvailability = nfcInterfaceManager.nfcAvailability.collectAsState()
            UseIDApp(nfcAvailability.value, appNavigator, trackerManager)
        }
    }

    override fun onResume() {
        super.onResume()

        nfcInterfaceManager.refreshNfcAvailability()
        nfcInterfaceManager.enableForegroundDispatch(this)
    }

    override fun onPause() {
        super.onPause()
        nfcInterfaceManager.disableForegroundDispatch(this)
        trackerManager.dispatch()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleNewIntent(intent)
    }

    @OptIn(ExperimentalTime::class)
    private fun prepareAppLaunch() {
        var keepOnScreen = true
        splashScreen.setKeepOnScreenCondition { keepOnScreen }

        CoroutineScope(Dispatchers.Default).launch {
            try {
                val duration = measureTime {
                    withTimeout(1500) {
                        abTestManager.initialise()
                    }
                }

                val splashScreenDelay = 600 - duration.inWholeMilliseconds
                if (splashScreenDelay > 0) {
                    delay(splashScreenDelay)
                }
            } finally {
                keepOnScreen = false
            }
        }
    }

    private fun handleNewIntent(intent: Intent) {
        nfcInterfaceManager.refreshNfcAvailability()

        when (intent.action) {
            NfcAdapter.ACTION_TAG_DISCOVERED -> {
                intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)?.let {
                    try {
                        idCardManager.handleNfcTag(it)
                    } catch (e: IOException) {
                        logger.error("IDCardManager failed to handle NFC tag.")
                    }
                }
            }

            Intent.ACTION_VIEW -> {
                intent.data?.let {
                    appCoordinator.handleDeepLink(it)
                }
            }
        }
    }
}
