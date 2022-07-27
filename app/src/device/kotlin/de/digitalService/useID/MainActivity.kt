package de.digitalService.useID

import android.app.Activity
import android.app.PendingIntent
import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import dagger.hilt.android.AndroidEntryPoint
import de.digitalService.useID.idCardInterface.IDCardManager
import de.digitalService.useID.ui.AppCoordinator
import de.digitalService.useID.ui.composables.UseIDApp
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var idCardManager: IDCardManager

    @Inject
    lateinit var appCoordinator: AppCoordinator

    private var nfcAdapter: NfcAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.nfcAdapter = NfcAdapter.getDefaultAdapter(this)

        setContent {
            UseIDApp(appCoordinator)
        }
    }

    override fun onResume() {
        super.onResume()

        foregroundDispatch(this)
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
