package de.digitalService.useID.idCardInterface

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.openecard.mobile.activation.*

class PinManagementInteractionHandler(private val eidFlow: MutableStateFlow<EidInteractionEvent>) :
    PinManagementInteraction {
    private val logTag = javaClass.canonicalName!!

    override fun requestCardInsertion() {
        Log.d(logTag, "Request card insertion.")
        CoroutineScope(Dispatchers.IO).launch {
            eidFlow.emit(EidInteractionEvent.RequestCardInsertion)
        }
    }

    override fun requestCardInsertion(p0: NFCOverlayMessageHandler?) {
        Log.e(logTag, "Requesting card insertion with overlay message handler not implemented.")
        CoroutineScope(Dispatchers.IO).launch {
            eidFlow.emit(EidInteractionEvent.Error(IdCardInteractionException.FrameworkError()))
        }
    }

    override fun onCardInteractionComplete() {
        Log.d(logTag, "Card interaction complete.")
        CoroutineScope(Dispatchers.IO).launch {
            eidFlow.emit(EidInteractionEvent.CardInteractionComplete)
        }
    }

    override fun onCardRecognized() {
        Log.d(logTag, "Card recognized.")
        CoroutineScope(Dispatchers.IO).launch {
            eidFlow.emit(EidInteractionEvent.CardRecognized)
        }
    }

    override fun onCardRemoved() {
        Log.d(logTag, "Card removed.")
        CoroutineScope(Dispatchers.IO).launch {
            eidFlow.emit(EidInteractionEvent.CardRemoved)
        }
    }

    override fun onPinChangeable(p0: ConfirmOldSetNewPasswordOperation?) {
        Log.d(logTag, "Request old and new PIN without attempts.")
        if (p0 == null) {
            CoroutineScope(Dispatchers.IO).launch {
                eidFlow.emit(EidInteractionEvent.Error(IdCardInteractionException.FrameworkError()))
            }
            return
        }

        onGeneralPinChangeable(null, p0)
    }

    override fun onPinChangeable(p0: Int, p1: ConfirmOldSetNewPasswordOperation?) {
        Log.d(logTag, "Request old and new PIN with attempts.")
        if (p1 == null) {
            CoroutineScope(Dispatchers.IO).launch {
                eidFlow.emit(EidInteractionEvent.Error(IdCardInteractionException.FrameworkError()))
            }
            return
        }

        onGeneralPinChangeable(p0, p1)
    }

    private fun onGeneralPinChangeable(attempts: Int?, pinCallback: ConfirmOldSetNewPasswordOperation) {
        CoroutineScope(Dispatchers.IO).launch {
            eidFlow.emit(
                EidInteractionEvent.RequestChangedPin(
                    attempts,
                    pinCallback::confirmPassword
                )
            )
        }
    }

    override fun onPinCanNewPinRequired(p0: ConfirmPinCanNewPinOperation?) {
        Log.d(logTag, "Request CAN and old and new PIN.")
        if (p0 == null) {
            CoroutineScope(Dispatchers.IO).launch {
                eidFlow.emit(EidInteractionEvent.Error(IdCardInteractionException.FrameworkError()))
            }
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            eidFlow.emit(EidInteractionEvent.RequestCanAndChangedPin(p0::confirmChangePassword))
        }
    }

    override fun onPinBlocked(p0: ConfirmPasswordOperation?) {
        Log.d(logTag, "Request PUK.")

        if (p0 == null) {
            CoroutineScope(Dispatchers.IO).launch {
                eidFlow.emit(EidInteractionEvent.Error(IdCardInteractionException.FrameworkError()))
            }
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            eidFlow.emit(EidInteractionEvent.RequestPUK(p0::confirmPassword))
        }
    }

    override fun onCardPukBlocked() {
        Log.w(logTag, "Card blocked.")
        CoroutineScope(Dispatchers.IO).launch {
            eidFlow.emit(EidInteractionEvent.Error(IdCardInteractionException.CardBlocked))
        }
    }

    override fun onCardDeactivated() {
        Log.w(logTag, "Card deactivated.")
        CoroutineScope(Dispatchers.IO).launch {
            eidFlow.emit(EidInteractionEvent.Error(IdCardInteractionException.CardDeactivated))
        }
    }
}
