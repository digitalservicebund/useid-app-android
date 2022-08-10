package de.digitalService.useID.util

import android.content.Context
import android.nfc.NfcAdapter
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
open class NfcAdapterUtil @Inject constructor(
    @ApplicationContext private val context: Context
) {
    open fun getNfcAdapter(): NfcAdapter? {
        return NfcAdapter.getDefaultAdapter(context)
    }
}
