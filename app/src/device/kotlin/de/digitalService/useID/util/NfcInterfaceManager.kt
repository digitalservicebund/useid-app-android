package de.digitalService.useID.util

import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.nfc.NfcAdapter
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import de.digitalService.useID.models.NfcAvailability
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
open class NfcInterfaceManager @Inject constructor(@ApplicationContext private val context: Context): NfcInterfaceManagerType {
    override val nfcAvailability: StateFlow<NfcAvailability>
        get() = _nfcAvailability
    private val _nfcAvailability: MutableStateFlow<NfcAvailability> = MutableStateFlow(NfcAvailability.Available)

    private val nfcAdapter: NfcAdapter?
        get() = NfcAdapter.getDefaultAdapter(context)

    override fun refreshNfcAvailability() {
        _nfcAvailability.value = nfcAdapter?.let {
            if (it.isEnabled) NfcAvailability.Available else NfcAvailability.Deactivated
        } ?: NfcAvailability.NoNfc
    }

    override fun enableForegroundDispatch(activity: Activity) {
        val intent = Intent(
            activity.applicationContext,
            activity.javaClass
        ).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        val flag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_MUTABLE else 0

        val nfcPendingIntent = PendingIntent.getActivity(activity, 0, intent, flag)
        nfcAdapter?.enableForegroundDispatch(activity, nfcPendingIntent, null, null)
    }

    override fun disableForegroundDispatch(activity: Activity) {
        nfcAdapter?.disableForegroundDispatch(activity)
    }
}
