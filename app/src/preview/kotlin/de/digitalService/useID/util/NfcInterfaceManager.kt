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
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
open class NfcInterfaceManager @Inject constructor(@ApplicationContext private val context: Context): NfcInterfaceManagerType {
    override val nfcAvailability: StateFlow<NfcAvailability> = MutableStateFlow(NfcAvailability.Available)

    override fun refreshNfcAvailability() {}
    override fun enableForegroundDispatch(activity: Activity) { }
    override fun disableForegroundDispatch(activity: Activity) {}
}
