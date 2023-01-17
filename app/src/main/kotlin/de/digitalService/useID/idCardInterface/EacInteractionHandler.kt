package de.digitalService.useID.idCardInterface

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.openecard.mobile.activation.*

class EacInteractionHandler(private val eidFlow: MutableStateFlow<EidInteractionEvent>) : EacInteraction {
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

    override fun onCanRequest(p0: ConfirmPasswordOperation?) {
        Log.d(logTag, "Requesting CAN.")

        if (p0 == null) {
            CoroutineScope(Dispatchers.IO).launch {
                eidFlow.emit(EidInteractionEvent.Error(IdCardInteractionException.FrameworkError()))
            }
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            eidFlow.emit(EidInteractionEvent.RequestCan(p0::confirmPassword))
        }
    }

    override fun onPinRequest(p0: ConfirmPasswordOperation?) {
        Log.d(logTag, "Requesting PIN without attempts.")

        if (p0 == null) {
            CoroutineScope(Dispatchers.IO).launch {
                eidFlow.emit(EidInteractionEvent.Error(IdCardInteractionException.FrameworkError()))
            }
            return
        }

        onGeneralPinRequest(null, p0)
    }

    override fun onPinRequest(p0: Int, p1: ConfirmPasswordOperation?) {
        Log.d(logTag, "Requesting PIN with attempts.")

        if (p1 == null) {
            CoroutineScope(Dispatchers.IO).launch {
                eidFlow.emit(EidInteractionEvent.Error(IdCardInteractionException.FrameworkError()))
            }
            return
        }

        onGeneralPinRequest(p0, p1)
    }

    private fun onGeneralPinRequest(attempts: Int?, confirmPasswordOperation: ConfirmPasswordOperation) {
        CoroutineScope(Dispatchers.IO).launch {
            eidFlow.emit(
                EidInteractionEvent.RequestPin(
                    attempts,
                    confirmPasswordOperation::confirmPassword
                )
            )
        }
    }

    override fun onPinCanRequest(p0: ConfirmPinCanOperation?) {
        Log.d(logTag, "Requesting PIN and CAN.")

        if (p0 == null) {
            CoroutineScope(Dispatchers.IO).launch {
                eidFlow.emit(EidInteractionEvent.Error(IdCardInteractionException.FrameworkError()))
            }
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            eidFlow.emit(EidInteractionEvent.RequestPinAndCan(p0::confirmPassword))
        }
    }

    override fun onCardBlocked() {
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

    override fun onServerData(
        p0: ServerData?,
        p1: String?,
        p2: ConfirmAttributeSelectionOperation?
    ) {
        Log.d(logTag, "Requesting to confirm server data.")

        if (p0 == null || p2 == null) {
            CoroutineScope(Dispatchers.IO).launch {
                eidFlow.emit(EidInteractionEvent.Error(IdCardInteractionException.FrameworkError()))
            }
            return
        }

        val readAttributes =
            try { p0.readAccessAttributes.reduceToMap() } catch (e: IdCardInteractionException.UnexpectedReadAttribute) {
                CoroutineScope(Dispatchers.IO).launch {
                    eidFlow.emit(EidInteractionEvent.Error(e))
                }
                return
            }

        val eidServerData = EidAuthenticationRequest(
            p0.issuer,
            p0.issuerUrl,
            p0.subject,
            p0.subjectUrl,
            p0.validity,
            AuthenticationTerms.Text(p0.termsOfUsage.dataString),
            p1,
            readAttributes
        )

        val confirmationCallback: (Map<IdCardAttribute, Boolean>) -> Unit = { p2.enterAttributeSelection(it.toSelectableItems(), listOf()) }

        CoroutineScope(Dispatchers.IO).launch {
            eidFlow.emit(
                EidInteractionEvent.RequestAuthenticationRequestConfirmation(
                    eidServerData,
                    confirmationCallback
                )
            )
        }
    }

    override fun onCardAuthenticationSuccessful() {
        Log.d(logTag, "Card authentication successful.")
        CoroutineScope(Dispatchers.IO).launch {
            eidFlow.emit(EidInteractionEvent.AuthenticationSuccessful)
        }
    }
}
