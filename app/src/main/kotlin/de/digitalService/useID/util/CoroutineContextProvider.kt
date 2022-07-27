package de.digitalService.useID.util

import kotlinx.coroutines.Dispatchers
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.CoroutineContext

@Singleton
class CoroutineContextProvider @Inject constructor() : CoroutineContextProviderType {
    override val Default: CoroutineContext by lazy { Dispatchers.Default }
    override val Main: CoroutineContext by lazy { Dispatchers.Main }
    override val IO: CoroutineContext by lazy { Dispatchers.IO }
}
