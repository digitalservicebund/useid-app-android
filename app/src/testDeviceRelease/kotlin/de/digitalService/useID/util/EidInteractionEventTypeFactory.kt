package de.digitalService.useID.util

import de.digitalService.useID.idCardInterface.AuthenticationRequest
import de.digitalService.useID.idCardInterface.CertificateDescription
import de.digitalService.useID.idCardInterface.EidInteractionEvent
import de.digitalService.useID.idCardInterface.IdCardInteractionException
import de.jodamob.junit5.DefaultTypeFactory
import kotlin.reflect.KClass

class EidInteractionEventTypeFactory: DefaultTypeFactory() {
    override fun create(what: KClass<*>): EidInteractionEvent {
        return when (what) {
            EidInteractionEvent.Idle::class -> EidInteractionEvent.Idle
            EidInteractionEvent.Error::class -> EidInteractionEvent.Error(exception = IdCardInteractionException.CardDeactivated)
            EidInteractionEvent.CardInsertionRequested::class -> EidInteractionEvent.CardInsertionRequested
            EidInteractionEvent.CardRecognized::class -> EidInteractionEvent.CardRecognized
            EidInteractionEvent.CardRemoved::class -> EidInteractionEvent.CardRemoved
            EidInteractionEvent.CanRequested::class -> EidInteractionEvent.CanRequested()
            EidInteractionEvent.PinRequested::class -> EidInteractionEvent.PinRequested(3)
            EidInteractionEvent.NewPinRequested::class -> EidInteractionEvent.NewPinRequested
            EidInteractionEvent.PukRequested::class -> EidInteractionEvent.PukRequested
            EidInteractionEvent.AuthenticationStarted::class -> EidInteractionEvent.AuthenticationStarted
            EidInteractionEvent.AuthenticationRequestConfirmationRequested::class -> EidInteractionEvent.AuthenticationRequestConfirmationRequested(AuthenticationRequest(emptyList(), ""))
            EidInteractionEvent.CertificateDescriptionReceived::class -> EidInteractionEvent.CertificateDescriptionReceived(CertificateDescription("", "", "", "", "", ""))
            EidInteractionEvent.AuthenticationSucceededWithRedirect::class -> EidInteractionEvent.AuthenticationSucceededWithRedirect("")
            EidInteractionEvent.PinChangeStarted::class -> EidInteractionEvent.PinChangeStarted
            EidInteractionEvent.PinChangeSucceeded::class -> EidInteractionEvent.PinChangeSucceeded
            else -> throw IllegalArgumentException()
        }
    }
}
