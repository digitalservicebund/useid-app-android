package de.digitalService.useID

import android.content.Context
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

interface StorageManagerType {
    val firstTimeUser: Boolean
    fun setIsNotFirstTimeUser()
}

@Singleton
class StorageManager @Inject constructor(@ApplicationContext context: Context) : StorageManagerType {
    private val PREFS_KEY = "StorageManager"

    private enum class StorageKeys {
        FIRST_TIME_USER_KEY
    }

    private val sharedPrefs = context.getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE)

    override val firstTimeUser: Boolean
        get() = sharedPrefs.getBoolean(StorageKeys.FIRST_TIME_USER_KEY.name, true)

    override fun setIsNotFirstTimeUser() {
        sharedPrefs.edit {
            putBoolean(StorageKeys.FIRST_TIME_USER_KEY.name, false)
        }
    }
}
