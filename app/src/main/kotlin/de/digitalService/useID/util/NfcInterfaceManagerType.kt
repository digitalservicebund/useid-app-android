package de.digitalService.useID.util

import android.app.Activity
import de.digitalService.useID.models.NfcAvailability
import kotlinx.coroutines.flow.StateFlow

interface NfcInterfaceManagerType {
    val nfcAvailability: StateFlow<NfcAvailability>
    fun refreshNfcAvailability()
    fun enableForegroundDispatch(activity: Activity)
    fun disableForegroundDispatch(activity: Activity)
}
