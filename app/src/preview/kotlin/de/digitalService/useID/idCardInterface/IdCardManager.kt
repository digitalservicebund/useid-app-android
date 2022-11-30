package de.digitalService.useID.idCardInterface

import android.content.Context
import android.nfc.Tag
import de.digitalService.useID.getLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.launch

class IdCardManager {
    private val logger by getLogger()

    private lateinit var identifyChannel: BroadcastChannel<EidInteractionEvent>
    private lateinit var changePinChannel: BroadcastChannel<EidInteractionEvent>

    fun handleNfcTag(tag: Tag) = logger.debug("Ignoring NFC tag in preview.")

    fun identify(context: Context, url: String): Flow<EidInteractionEvent> {
        identifyChannel = BroadcastChannel(1)
        return identifyChannel.asFlow()
    }

    fun changePin(context: Context): Flow<EidInteractionEvent> {
        changePinChannel = BroadcastChannel(1)
        CoroutineScope(Dispatchers.Default).launch {
            delay(100L)
            changePinChannel.send(EidInteractionEvent.RequestCardInsertion)
        }
        return changePinChannel.asFlow()
    }

    fun cancelTask() {
        logger.debug("Cancel task")
    }

    suspend fun injectIdentifyEvent(event: EidInteractionEvent) {
        identifyChannel.send(event)
    }

    suspend fun injectIdentifyException(exception: Exception) {
        identifyChannel.close(exception)
    }

    suspend fun injectChangePinEvent(event: EidInteractionEvent) {
        changePinChannel.send(event)
    }

    suspend fun injectChangePinException(exception: Exception) {
        changePinChannel.close(exception)
    }
}
