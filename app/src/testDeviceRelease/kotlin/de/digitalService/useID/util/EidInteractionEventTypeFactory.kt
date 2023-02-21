package de.digitalService.useID.util

import de.digitalService.useID.idCardInterface.AuthenticationTerms
import de.digitalService.useID.idCardInterface.EidAuthenticationRequest
import de.digitalService.useID.idCardInterface.EidInteractionEvent
import de.digitalService.useID.idCardInterface.IdCardInteractionException
import de.jodamob.junit5.DefaultTypeFactory
import kotlin.reflect.KClass

class EidInteractionEventTypeFactory: DefaultTypeFactory() {
    override fun create(what: KClass<*>): EidInteractionEvent {
        return when (what) {
            EidInteractionEvent.Idle::class -> EidInteractionEvent.Idle
            EidInteractionEvent.Error::class -> EidInteractionEvent.Error(exception = IdCardInteractionException.CardDeactivated)
            EidInteractionEvent.RequestCardInsertion::class -> EidInteractionEvent.RequestCardInsertion
            EidInteractionEvent.CardInteractionComplete::class -> EidInteractionEvent.CardInteractionComplete
            EidInteractionEvent.CardRecognized::class -> EidInteractionEvent.CardRecognized
            EidInteractionEvent.CardRemoved::class -> EidInteractionEvent.CardRemoved
            EidInteractionEvent.RequestCan::class -> EidInteractionEvent.RequestCan { }
            EidInteractionEvent.RequestPin::class -> EidInteractionEvent.RequestPin(null) { }
            EidInteractionEvent.RequestPinAndCan::class -> EidInteractionEvent.RequestPinAndCan { _, _ -> }
            EidInteractionEvent.RequestPuk::class -> EidInteractionEvent.RequestPuk { }
            EidInteractionEvent.ProcessCompletedSuccessfullyWithoutResult::class -> EidInteractionEvent.ProcessCompletedSuccessfullyWithoutResult
            EidInteractionEvent.ProcessCompletedSuccessfullyWithRedirect::class -> EidInteractionEvent.ProcessCompletedSuccessfullyWithRedirect("")
            EidInteractionEvent.AuthenticationStarted::class -> EidInteractionEvent.AuthenticationStarted
            EidInteractionEvent.RequestAuthenticationRequestConfirmation::class -> EidInteractionEvent.RequestAuthenticationRequestConfirmation(
                EidAuthenticationRequest("", "", "", "", "", AuthenticationTerms.Text(""), null, mapOf()), { })
            EidInteractionEvent.AuthenticationSuccessful::class -> EidInteractionEvent.AuthenticationSuccessful
            EidInteractionEvent.PinManagementStarted::class -> EidInteractionEvent.PinManagementStarted
            EidInteractionEvent.RequestChangedPin::class -> EidInteractionEvent.RequestChangedPin(null) { _, _ -> }
            EidInteractionEvent.RequestCanAndChangedPin::class -> EidInteractionEvent.RequestCanAndChangedPin { _, _, _ -> }
            else -> throw IllegalArgumentException()
        }
    }
}
