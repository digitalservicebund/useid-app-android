package de.digitalService.useID

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

interface SecureStorageManagerInterface {
    fun setTransportPIN(value: String)
    fun loadTransportPIN(): String?

    fun setPersonalPIN(value: String)
    fun loadPersonalPIN(): String?

    fun clearStorage()
}

class SecureStorageManager @Inject constructor(@ApplicationContext context: Context) : SecureStorageManagerInterface {
    private enum class StorageKey { TransportPIN, PersonalPIN }

    companion object {
        const val sharedPreferencesFileName = BuildConfig.APPLICATION_ID + ".encryptedSharedPreferences"
    }

    private val sharedPreferences: SharedPreferences

    init {
        val mainKey = MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build()
        sharedPreferences = EncryptedSharedPreferences(context, sharedPreferencesFileName, mainKey)
    }

    private fun save(key: StorageKey, value: String) {
        with(sharedPreferences.edit()) {
            putString(key.name, value)
            apply()
        }
    }

    private fun load(key: StorageKey): String? = sharedPreferences.getString(key.name, null)

    override fun setTransportPIN(value: String) =
        save(StorageKey.TransportPIN, value)

    override fun loadTransportPIN(): String? =
        load(StorageKey.TransportPIN)

    override fun setPersonalPIN(value: String) =
        save(StorageKey.PersonalPIN, value)

    override fun loadPersonalPIN(): String? =
        load(StorageKey.PersonalPIN)

    override fun clearStorage() {
        with(sharedPreferences.edit()) {
            clear()
            apply()
        }
    }
}
