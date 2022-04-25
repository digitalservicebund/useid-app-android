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
        Log.e(logTag, "Requesting card insertion with overlay message handler not implemented.")
        channel.close(IDCardInteractionException.FrameworkError())
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
        Log.d(logTag, "Request old and new PIN without attempts.")
        if (p0 == null) {
            channel.close(IDCardInteractionException.FrameworkError())
            return
        }

        onGeneralPinChangeable(null, p0)
    }

    override fun onPinChangeable(p0: Int, p1: ConfirmOldSetNewPasswordOperation?) {
        Log.d(logTag, "Request old and new PIN with attempts.")
        if (p1 == null) {
            channel.close(IDCardInteractionException.FrameworkError())
            return
        }

        onGeneralPinChangeable(p0, p1)
    }

    private fun onGeneralPinChangeable(attempts: Int?, pinCallback: ConfirmOldSetNewPasswordOperation) {
        channel.trySendClosingOnError(EIDInteractionEvent.RequestChangedPIN(attempts, pinCallback::confirmPassword))
    }

    override fun onPinCanNewPinRequired(p0: ConfirmPinCanNewPinOperation?) {
        Log.d(logTag, "Request CAN and old and new PIN.")
        if (p0 == null) {
            channel.close(IDCardInteractionException.FrameworkError())
            return
        }

        channel.trySendClosingOnError(EIDInteractionEvent.RequestCANAndChangedPIN(p0::confirmChangePassword))
    }

    override fun onPinBlocked(p0: ConfirmPasswordOperation?) {
        Log.d(logTag, "Request PUK.")

        if (p0 == null) {
            channel.close(IDCardInteractionException.FrameworkError())
            return
        }

        channel.trySendClosingOnError(EIDInteractionEvent.RequestPUK(p0::confirmPassword))
    }

    override fun onCardPukBlocked() {
        Log.w(logTag, "Card blocked.")
        channel.close(IDCardInteractionException.CardBlocked)
    }

    override fun onCardDeactivated() {
        Log.w(logTag, "Card deactivated.")
        channel.close(IDCardInteractionException.CardDeactivated)
    }
}