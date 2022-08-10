package de.digitalService.useID.util

import android.nfc.NfcAdapter
import io.mockk.every
import io.mockk.mockk

class MockNfcAdapterUtil() : NfcAdapterUtil(mockk()) {
    override fun getNfcAdapter(): NfcAdapter {
        val mockNfcAdapter: NfcAdapter = mockk(relaxed = true)
        every { mockNfcAdapter.isEnabled } returns true

        return mockNfcAdapter
    }
}
