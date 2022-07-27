package de.digitalService.useID.util

import kotlin.coroutines.CoroutineContext

interface CoroutineContextProviderType {
    val Default: CoroutineContext
    val Main: CoroutineContext
    val IO: CoroutineContext
}
