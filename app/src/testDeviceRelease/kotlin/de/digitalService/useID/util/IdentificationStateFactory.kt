package de.digitalService.useID.util

import de.digitalService.useID.flows.AttributeConfirmationCallback
import de.digitalService.useID.flows.IdentificationStateMachine
import de.digitalService.useID.flows.PinCallback
import de.digitalService.useID.idCardInterface.EidAuthenticationRequest
import de.jodamob.junit5.DefaultTypeFactory
import io.mockk.mockk
import kotlin.reflect.KClass

class IdentificationStateFactory: DefaultTypeFactory() {
    private val pinCallback: PinCallback = mockk()
    private val request: EidAuthenticationRequest = mockk()
    private val attributeConfirmationCallback: AttributeConfirmationCallback = mockk()

    override fun create(what: KClass<*>): IdentificationStateMachine.State {
        return when (what) {
            IdentificationStateMachine.State.Invalid::class -> IdentificationStateMachine.State.Invalid

            IdentificationStateMachine.State.StartIdentification::class -> IdentificationStateMachine.State.StartIdentification(false, "")
            IdentificationStateMachine.State.FetchingMetadata::class -> IdentificationStateMachine.State.FetchingMetadata(false, "")
            IdentificationStateMachine.State.FetchingMetadataFailed::class -> IdentificationStateMachine.State.FetchingMetadataFailed(false, "")
            IdentificationStateMachine.State.RequestAttributeConfirmation::class -> IdentificationStateMachine.State.RequestAttributeConfirmation(false, request, attributeConfirmationCallback)
            IdentificationStateMachine.State.SubmitAttributeConfirmation::class -> IdentificationStateMachine.State.SubmitAttributeConfirmation(false, request, attributeConfirmationCallback)
            IdentificationStateMachine.State.RevisitAttributes::class -> IdentificationStateMachine.State.RevisitAttributes(false, request, pinCallback)
            IdentificationStateMachine.State.PinInput::class -> IdentificationStateMachine.State.PinInput(false, request, pinCallback)
            IdentificationStateMachine.State.PinInputRetry::class -> IdentificationStateMachine.State.PinInputRetry(pinCallback)
            IdentificationStateMachine.State.PinEntered::class -> IdentificationStateMachine.State.PinEntered("", false, pinCallback)
            IdentificationStateMachine.State.CanRequested::class -> IdentificationStateMachine.State.CanRequested(null)
            IdentificationStateMachine.State.WaitingForCardAttachment::class -> IdentificationStateMachine.State.WaitingForCardAttachment(null)
            IdentificationStateMachine.State.Finished::class -> IdentificationStateMachine.State.Finished("")

            IdentificationStateMachine.State.CardDeactivated::class -> IdentificationStateMachine.State.CardDeactivated
            IdentificationStateMachine.State.CardBlocked::class -> IdentificationStateMachine.State.CardBlocked
            IdentificationStateMachine.State.CardUnreadable::class -> IdentificationStateMachine.State.CardUnreadable(null)

            else -> throw IllegalArgumentException()
        }
    }
}
