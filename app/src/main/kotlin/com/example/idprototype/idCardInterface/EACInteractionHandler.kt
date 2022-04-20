package com.example.idprototype.idCardInterface

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

    override fun onCanRequest(p0: ConfirmPasswordOperation?) {
        TODO("Not yet implemented")
    }

    override fun onPinRequest(p0: ConfirmPasswordOperation?) {
        Log.d(logTag, "Requesting PIN.")

        if (p0 == null) {
            channel.close(IDCardManagerException.FrameworkError)
            return
        }

        channel.trySendClosingOnError(EIDInteractionEvent.RequestPIN { p0.confirmPassword(it) })
    }

    override fun onPinRequest(p0: Int, p1: ConfirmPasswordOperation?) {
        TODO("Not yet implemented")
    }

    override fun onPinCanRequest(p0: ConfirmPinCanOperation?) {
        TODO("Not yet implemented")
    }

    override fun onCardBlocked() {
        TODO("Not yet implemented")
    }

    override fun onCardDeactivated() {
        TODO("Not yet implemented")
    }

    override fun onServerData(
        p0: ServerData?,
        p1: String?,
        p2: ConfirmAttributeSelectionOperation?
    ) {
        if (p0 == null || p1 == null || p2 == null) {
            channel.close(IDCardManagerException.FrameworkError)
            return
        }

        val readAttributes =
            try { p0.readAccessAttributes.reduceToMap() }
            catch (e: IDCardManagerException.UnexpectedReadAttribute) {
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