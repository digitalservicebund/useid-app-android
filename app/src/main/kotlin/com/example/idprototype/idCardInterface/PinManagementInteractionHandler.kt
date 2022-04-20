package com.example.idprototype.idCardInterface

import android.util.Log
import kotlinx.coroutines.channels.SendChannel
import org.openecard.mobile.activation.*

class PINManagementInteractionHandler(private val channel: SendChannel<EIDInteractionEvent>):
    PinManagementInteraction {
    private val logTag = javaClass.canonicalName!!

    override fun requestCardInsertion() {
        Log.d(logTag, "Request card insertion.")
        channel.trySendClosingOnError(EIDInteractionEvent.RequestCardInsertion)
    }

    override fun requestCardInsertion(p0: NFCOverlayMessageHandler?) {
        TODO("Not yet implemented")
    }

    override fun onCardInteractionComplete() {
        Log.d(logTag, "Card interaction complete.")
        channel.trySendClosingOnError(EIDInteractionEvent.CardInteractionComplete)
    }

    override fun onCardRecognized() {
        Log.d(logTag, "Card recognized.")
        channel.trySendClosingOnError(EIDInteractionEvent.CardRecognized)
    }

    override fun onCardRemoved() {
        Log.d(logTag, "Card removed.")
        channel.trySendClosingOnError(EIDInteractionEvent.CardRemoved)
    }

    override fun onPinChangeable(p0: ConfirmOldSetNewPasswordOperation?) {
        TODO("Not yet implemented")
    }

    override fun onPinChangeable(p0: Int, p1: ConfirmOldSetNewPasswordOperation?) {
        Log.d(logTag, "Request old and new PIN.")
        if (p1 == null) {
            channel.close(IDCardManagerException.FrameworkError)
            return
        }

        val pinCallback: (oldPin: String, newPin: String) -> Unit = { oldPin, newPin ->
            p1.confirmPassword(oldPin, newPin)
        }

        channel.trySendClosingOnError(EIDInteractionEvent.RequestChangedPIN(p0, pinCallback))
    }

    override fun onPinCanNewPinRequired(p0: ConfirmPinCanNewPinOperation?) {
        TODO("Not yet implemented")
    }

    override fun onPinBlocked(p0: ConfirmPasswordOperation?) {
        TODO("Not yet implemented")
    }

    override fun onCardPukBlocked() {
        TODO("Not yet implemented")
    }

    override fun onCardDeactivated() {
        TODO("Not yet implemented")
    }
}