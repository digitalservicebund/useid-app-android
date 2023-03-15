package de.digitalService.useID

import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
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
    @Named(ConfigModule.SENTRY_DSN)
    lateinit var sentryDsn: String

    @Inject
    lateinit var abTestManager: AbTestManager

    override fun onCreate(savedInstanceState: Bundle?) {
        showSplashScreen()
        super.onCreate(savedInstanceState)

        Sentry.init(sentryDsn)

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

    private fun showSplashScreen() {
        val splashScreen = installSplashScreen()

        var minimumSplashDurationExpired = false
        var abTestManagerLoaded = false
        splashScreen.setKeepOnScreenCondition { !(minimumSplashDurationExpired && abTestManagerLoaded) }

        CoroutineScope(Dispatchers.Main).launch {
            delay(600)
            minimumSplashDurationExpired = true
        }

        CoroutineScope(Dispatchers.Main).launch {
            launch {
                delay(1500)
                abTestManager.disable()
                abTestManagerLoaded = true
                this.cancel()
            }

            abTestManager.state.collect {
                if (it != AbTestManager.State.LOADING) {
                    abTestManagerLoaded = true
                    this.cancel()
                }
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
