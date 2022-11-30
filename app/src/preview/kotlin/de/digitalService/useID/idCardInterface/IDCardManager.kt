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

class IDCardManager {
    private val logger by getLogger()

    private lateinit var identifyChannel: BroadcastChannel<EIDInteractionEvent>
    private lateinit var changePinChannel: BroadcastChannel<EIDInteractionEvent>

    fun handleNFCTag(tag: Tag) = logger.debug("Ignoring NFC tag in preview.")

    fun identify(context: Context, url: String): Flow<EIDInteractionEvent> {
        identifyChannel = BroadcastChannel(1)
        return identifyChannel.asFlow()
    }

    fun changePin(context: Context): Flow<EIDInteractionEvent> {
        changePinChannel = BroadcastChannel(1)
        CoroutineScope(Dispatchers.Default).launch {
            delay(100L)
            changePinChannel.send(EIDInteractionEvent.RequestCardInsertion)
        }
        return changePinChannel.asFlow()
    }

    fun cancelTask() {
        logger.debug("Cancel task")
    }

    suspend fun injectIdentifyEvent(event: EIDInteractionEvent) {
        identifyChannel.send(event)
    }

    suspend fun injectIdentifyException(exception: Exception) {
        identifyChannel.close(exception)
    }

    suspend fun injectChangePinEvent(event: EIDInteractionEvent) {
        changePinChannel.send(event)
    }

    suspend fun injectChangePinException(exception: Exception) {
        changePinChannel.close(exception)
    }
}
