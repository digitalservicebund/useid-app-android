package de.digitalService.useID.util

import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

interface CurrentTimeProviderInterface {
    val currentTime: Long
}

@Singleton
class CurrentTimeProvider @Inject constructor(): CurrentTimeProviderInterface {
    override val currentTime: Long
        get() = Date().time
}
