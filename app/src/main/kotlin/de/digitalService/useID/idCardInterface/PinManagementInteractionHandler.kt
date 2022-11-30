package de.digitalService.useID.idCardInterface

import android.util.Log
import kotlinx.coroutines.channels.SendChannel
import org.openecard.mobile.activation.*

class PinManagementInteractionHandler(private val channel: SendChannel<EidInteractionEvent>) :
    PinManagementInteraction {
    private val logTag = javaClass.canonicalName!!

    override fun requestCardInsertion() {
        Log.d(logTag, "Request card insertion.")
        channel.trySendClosingOnError(EidInteractionEvent.RequestCardInsertion)
    }

    override fun requestCardInsertion(p0: NFCOverlayMessageHandler?) {
        Log.e(logTag, "Requesting card insertion with overlay message handler not implemented.")
        channel.close(IdCardInteractionException.FrameworkError())
    }

    override fun onCardInteractionComplete() {
        Log.d(logTag, "Card interaction complete.")
        channel.trySendClosingOnError(EidInteractionEvent.CardInteractionComplete)
    }

    override fun onCardRecognized() {
        Log.d(logTag, "Card recognized.")
        channel.trySendClosingOnError(EidInteractionEvent.CardRecognized)
    }

    override fun onCardRemoved() {
        Log.d(logTag, "Card removed.")
        channel.trySendClosingOnError(EidInteractionEvent.CardRemoved)
    }

    override fun onPinChangeable(p0: ConfirmOldSetNewPasswordOperation?) {
        Log.d(logTag, "Request old and new PIN without attempts.")
        if (p0 == null) {
            channel.close(IdCardInteractionException.FrameworkError())
            return
        }

        onGeneralPinChangeable(null, p0)
    }

    override fun onPinChangeable(p0: Int, p1: ConfirmOldSetNewPasswordOperation?) {
        Log.d(logTag, "Request old and new PIN with attempts.")
        if (p1 == null) {
            channel.close(IdCardInteractionException.FrameworkError())
            return
        }

        onGeneralPinChangeable(p0, p1)
    }

    private fun onGeneralPinChangeable(attempts: Int?, pinCallback: ConfirmOldSetNewPasswordOperation) {
        channel.trySendClosingOnError(EidInteractionEvent.RequestChangedPin(attempts, pinCallback::confirmPassword))
    }

    override fun onPinCanNewPinRequired(p0: ConfirmPinCanNewPinOperation?) {
        Log.d(logTag, "Request CAN and old and new PIN.")
        if (p0 == null) {
            channel.close(IdCardInteractionException.FrameworkError())
            return
        }

        channel.trySendClosingOnError(EidInteractionEvent.RequestCanAndChangedPin(p0::confirmChangePassword))
    }

    override fun onPinBlocked(p0: ConfirmPasswordOperation?) {
        Log.d(logTag, "Request PUK.")

        if (p0 == null) {
            channel.close(IdCardInteractionException.FrameworkError())
            return
        }

        channel.trySendClosingOnError(EidInteractionEvent.RequestPUK(p0::confirmPassword))
    }

    override fun onCardPukBlocked() {
        Log.w(logTag, "Card blocked.")
        channel.close(IdCardInteractionException.CardBlocked)
    }

    override fun onCardDeactivated() {
        Log.w(logTag, "Card deactivated.")
        channel.close(IdCardInteractionException.CardDeactivated)
    }
}
