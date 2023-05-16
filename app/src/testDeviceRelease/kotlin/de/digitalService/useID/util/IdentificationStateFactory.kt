package de.digitalService.useID.util

import android.net.Uri
import de.digitalService.useID.flows.IdentificationStateMachine
import de.digitalService.useID.idCardInterface.IdentificationRequest
import de.digitalService.useID.idCardInterface.CertificateDescription
import de.jodamob.junit5.DefaultTypeFactory
import io.mockk.mockk
import kotlin.reflect.KClass

class IdentificationStateFactory : DefaultTypeFactory() {
    private val request: IdentificationRequest = mockk()
    private val certificateDescription: CertificateDescription = mockk()
    private val tcTokenUrl: Uri = mockk()

    override fun create(what: KClass<*>): IdentificationStateMachine.State {
        return when (what) {
            IdentificationStateMachine.State.Invalid::class -> IdentificationStateMachine.State.Invalid

            IdentificationStateMachine.State.StartIdentification::class -> IdentificationStateMachine.State.StartIdentification(false, tcTokenUrl)
            IdentificationStateMachine.State.FetchingMetadata::class -> IdentificationStateMachine.State.FetchingMetadata(false, tcTokenUrl)
            IdentificationStateMachine.State.FetchingMetadataFailed::class -> IdentificationStateMachine.State.FetchingMetadataFailed(false, tcTokenUrl)
            IdentificationStateMachine.State.RequestCertificate::class -> IdentificationStateMachine.State.RequestCertificate(false, request)
            IdentificationStateMachine.State.CertificateDescriptionReceived::class -> IdentificationStateMachine.State.CertificateDescriptionReceived(false, request, certificateDescription)
            IdentificationStateMachine.State.PinInput::class -> IdentificationStateMachine.State.PinInput(false, request, certificateDescription)
            IdentificationStateMachine.State.PinInputRetry::class -> IdentificationStateMachine.State.PinInputRetry
            IdentificationStateMachine.State.PinEntered::class -> IdentificationStateMachine.State.PinEntered("", false)
            IdentificationStateMachine.State.PinRequested::class -> IdentificationStateMachine.State.PinRequested("")
            IdentificationStateMachine.State.CanRequested::class -> IdentificationStateMachine.State.CanRequested(null)
            IdentificationStateMachine.State.Finished::class -> IdentificationStateMachine.State.Finished("")

            IdentificationStateMachine.State.CardDeactivated::class -> IdentificationStateMachine.State.CardDeactivated
            IdentificationStateMachine.State.CardBlocked::class -> IdentificationStateMachine.State.CardBlocked
            IdentificationStateMachine.State.CardUnreadable::class -> IdentificationStateMachine.State.CardUnreadable(null)

            else -> throw IllegalArgumentException()
        }
    }
}
