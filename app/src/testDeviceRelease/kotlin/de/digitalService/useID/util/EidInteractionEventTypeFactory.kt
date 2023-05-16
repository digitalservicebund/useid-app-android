package de.digitalService.useID.util

import de.digitalService.useID.idCardInterface.IdentificationRequest
import de.digitalService.useID.idCardInterface.CertificateDescription
import de.digitalService.useID.idCardInterface.EidInteractionEvent
import de.digitalService.useID.idCardInterface.EidInteractionException
import de.jodamob.junit5.DefaultTypeFactory
import kotlin.reflect.KClass

class EidInteractionEventTypeFactory: DefaultTypeFactory() {
    override fun create(what: KClass<*>): EidInteractionEvent {
        return when (what) {
            EidInteractionEvent.Idle::class -> EidInteractionEvent.Idle
            EidInteractionEvent.Error::class -> EidInteractionEvent.Error(exception = EidInteractionException.CardDeactivated)
            EidInteractionEvent.CardInsertionRequested::class -> EidInteractionEvent.CardInsertionRequested
            EidInteractionEvent.CardRecognized::class -> EidInteractionEvent.CardRecognized
            EidInteractionEvent.CardRemoved::class -> EidInteractionEvent.CardRemoved
            EidInteractionEvent.CanRequested::class -> EidInteractionEvent.CanRequested()
            EidInteractionEvent.PinRequested::class -> EidInteractionEvent.PinRequested(3)
            EidInteractionEvent.NewPinRequested::class -> EidInteractionEvent.NewPinRequested
            EidInteractionEvent.PukRequested::class -> EidInteractionEvent.PukRequested
            EidInteractionEvent.IdentificationStarted::class -> EidInteractionEvent.IdentificationStarted
            EidInteractionEvent.IdentificationRequestConfirmationRequested::class -> EidInteractionEvent.IdentificationRequestConfirmationRequested(IdentificationRequest(emptyList(), ""))
            EidInteractionEvent.CertificateDescriptionReceived::class -> EidInteractionEvent.CertificateDescriptionReceived(CertificateDescription("", "", "", "", "", ""))
            EidInteractionEvent.IdentificationSucceededWithRedirect::class -> EidInteractionEvent.IdentificationSucceededWithRedirect("")
            EidInteractionEvent.PinChangeStarted::class -> EidInteractionEvent.PinChangeStarted
            EidInteractionEvent.PinChangeSucceeded::class -> EidInteractionEvent.PinChangeSucceeded
            else -> throw IllegalArgumentException()
        }
    }
}
