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
import dagger.hilt.android.AndroidEntryPoint
import de.digitalService.useID.idCardInterface.IDCardManager
import de.digitalService.useID.models.NfcAvailability
import de.digitalService.useID.ui.coordinators.AppCoordinator
import de.digitalService.useID.ui.UseIDApp
import de.digitalService.useID.util.NfcAdapterUtil
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var idCardManager: IDCardManager

    @Inject
    lateinit var appCoordinator: AppCoordinator

    @Inject
    lateinit var nfcAdapterUtil: NfcAdapterUtil

    private var nfcAdapter: NfcAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            UseIDApp(appCoordinator)
        }
    }

    override fun onResume() {
        super.onResume()

        foregroundDispatch(this)

        this.nfcAdapter = nfcAdapterUtil.getNfcAdapter()
        nfcAdapter?.let {
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
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        val tag = intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)
        if (tag != null) {
            idCardManager.handleNFCTag(tag)
        }
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
}
