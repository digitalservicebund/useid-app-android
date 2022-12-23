package de.digitalService.useID.idCardInterface

import android.content.Context
import android.nfc.Tag
import de.digitalService.useID.getLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.launch

class IdCardManager {
    private val logger by getLogger()

    private val _eidFlow: MutableStateFlow<EidInteractionEvent> = MutableStateFlow(EidInteractionEvent.Idle)
    val eidFlow: Flow<EidInteractionEvent>
        get() = _eidFlow

    fun handleNfcTag(tag: Tag) = logger.debug("Ignoring NFC tag in preview.")

    fun identify(context: Context, url: String) {
        CoroutineScope(Dispatchers.IO).launch {
            delay(100L)
            _eidFlow.emit(EidInteractionEvent.AuthenticationStarted)
        }
    }

    fun changePin(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            delay(100L)
            _eidFlow.emit(EidInteractionEvent.RequestCardInsertion)
        }
    }

    fun cancelTask() {
        _eidFlow.value = EidInteractionEvent.Idle
    }

    suspend fun injectEvent(event: EidInteractionEvent) {
        _eidFlow.emit(event)
    }

    suspend fun injectException(exception: IdCardInteractionException) {
        _eidFlow.emit(EidInteractionEvent.Error(exception))
    }
}
