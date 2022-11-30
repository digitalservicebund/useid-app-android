package de.digitalService.useID

import android.app.Activity
import android.app.PendingIntent
import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import dagger.hilt.android.AndroidEntryPoint
import de.digitalService.useID.analytics.TrackerManagerType
import de.digitalService.useID.hilt.ConfigModule
import de.digitalService.useID.idCardInterface.IdCardManager
import de.digitalService.useID.models.NfcAvailability
import de.digitalService.useID.ui.UseIDApp
import de.digitalService.useID.ui.coordinators.AppCoordinatorType
import de.digitalService.useID.util.NfcAdapterUtil
import io.sentry.Sentry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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
    lateinit var trackerManager: TrackerManagerType

    @Inject
    lateinit var nfcAdapterUtil: NfcAdapterUtil

    @Inject
    @Named(ConfigModule.SENTRY_DSN)
    lateinit var sentryDsn: String

    private var nfcAdapter: NfcAdapter? = null

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

    override fun onResume() {
        super.onResume()

        this.nfcAdapter = nfcAdapterUtil.getNfcAdapter()
        nfcAdapter?.let {
            foregroundDispatch(this)
            if (it.isEnabled) {
                appCoordinator.setNfcAvailability(NfcAvailability.Available)
            } else {
                appCoordinator.setNfcAvailability(NfcAvailability.Deactivated)
            }
        } ?: run {
            appCoordinator.setNfcAvailability(NfcAvailability.NoNfc)
        }
    }

    override fun onPause() {
        super.onPause()
        nfcAdapter?.disableForegroundDispatch(this)
        trackerManager.dispatch()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleNewIntent(intent)
    }

    private fun foregroundDispatch(activity: Activity) {
        val intent = Intent(
            activity.applicationContext,
            activity.javaClass
        ).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        val flag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_MUTABLE else 0

        val nfcPendingIntent = PendingIntent.getActivity(this, 0, intent, flag)
        nfcAdapter?.enableForegroundDispatch(activity, nfcPendingIntent, null, null)
    }

    private fun handleNewIntent(intent: Intent) {
        when (intent.action) {
            NfcAdapter.ACTION_TAG_DISCOVERED -> {
                intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)?.let {
                    if (appCoordinator.currentlyHandlingNfcTags) {
                        try {
                            idCardManager.handleNfcTag(it)
                        } catch (e: IOException) {
                            logger.error("IDCardManager failed to handle NFC tag.")
                        }
                    } else {
                        logger.debug("Got new NFC tag but app coordinator is not awaiting any. Ignoring.")
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
