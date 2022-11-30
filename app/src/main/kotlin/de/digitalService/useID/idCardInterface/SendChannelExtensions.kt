package de.digitalService.useID.idCardInterface

import android.util.Log
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.onClosed
import kotlinx.coroutines.channels.onFailure

fun SendChannel<EidInteractionEvent>.trySendClosingOnError(event: EidInteractionEvent) = trySend(event)
    .onClosed { Log.e("Channel", "Tried to send value to closed channel.") }
    .onFailure {
        Log.e("Channel", "Sending value to channel failed: ${it?.message}")
        close(it)
    }
