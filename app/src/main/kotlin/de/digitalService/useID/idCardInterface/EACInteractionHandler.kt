package de.digitalService.useID.idCardInterface

import android.util.Log
import kotlinx.coroutines.channels.SendChannel
import org.openecard.mobile.activation.*

class EACInteractionHandler(private val channel: SendChannel<EIDInteractionEvent>): EacInteraction {
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

    override fun onCanRequest(p0: ConfirmPasswordOperation?) {
        Log.d(logTag, "Requesting CAN.")

        if (p0 == null) {
            channel.close(IDCardInteractionException.FrameworkError())
            return
        }

        channel.trySendClosingOnError(EIDInteractionEvent.RequestCAN(p0::confirmPassword))
    }

    override fun onPinRequest(p0: ConfirmPasswordOperation?) {
        Log.d(logTag, "Requesting PIN without attempts.")

        if (p0 == null) {
            channel.close(IDCardInteractionException.FrameworkError())
            return
        }

        onGeneralPinRequest(null, p0)
    }

    override fun onPinRequest(p0: Int, p1: ConfirmPasswordOperation?) {
        Log.d(logTag, "Requesting PIN with attempts.")

        if (p1 == null) {
            channel.close(IDCardInteractionException.FrameworkError())
            return
        }

        onGeneralPinRequest(p0, p1)
    }

    private fun onGeneralPinRequest(attempts: Int?, confirmPasswordOperation: ConfirmPasswordOperation) {
        channel.trySendClosingOnError(EIDInteractionEvent.RequestPIN(attempts, confirmPasswordOperation::confirmPassword))
    }

    override fun onPinCanRequest(p0: ConfirmPinCanOperation?) {
        Log.d(logTag, "Requesting PIN and CAN.")

        if (p0 == null) {
            channel.close(IDCardInteractionException.FrameworkError())
            return
        }

        channel.trySendClosingOnError(EIDInteractionEvent.RequestPINAndCAN(p0::confirmPassword))
    }

    override fun onCardBlocked() {
        Log.w(logTag, "Card blocked.")
        channel.close(IDCardInteractionException.CardBlocked)
    }

    override fun onCardDeactivated() {
        Log.w(logTag, "Card deactivated.")
        channel.close(IDCardInteractionException.CardDeactivated)
    }

    override fun onServerData(
        p0: ServerData?,
        p1: String?,
        p2: ConfirmAttributeSelectionOperation?
    ) {
        Log.d(logTag, "Requesting to confirm server data.")

        if (p0 == null || p1 == null || p2 == null) {
            channel.close(IDCardInteractionException.FrameworkError())
            return
        }

        val readAttributes =
            try { p0.readAccessAttributes.reduceToMap() }
            catch (e: IDCardInteractionException.UnexpectedReadAttribute) {
                channel.close(e)
                return
            }

        val eidServerData = EIDAuthenticationRequest(
            p0.issuer,
            p0.issuerUrl,
            p0.subject,
            p0.subjectUrl,
            p0.validity,
            AuthenticationTerms.Text(p0.termsOfUsage.dataString),
            readAttributes
        )

        val confirmationCallback: (Map<IDCardAttribute, Boolean>) -> Unit = { p2.enterAttributeSelection(it.toSelectableItems(), listOf()) }

        channel.trySendClosingOnError(EIDInteractionEvent.RequestAuthenticationRequestConfirmation(eidServerData, confirmationCallback))
    }

    override fun onCardAuthenticationSuccessful() {
        Log.d(logTag, "Card authentication successful.")
        channel.trySendClosingOnError(EIDInteractionEvent.AuthenticationSuccessful)
    }
}