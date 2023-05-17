package de.digitalService.useID.idCardInterface

sealed class EidInteractionEvent {
    object Idle : EidInteractionEvent()
    class Error(val exception: EidInteractionException) : EidInteractionEvent()

    object CardInsertionRequested : EidInteractionEvent()
    object CardRecognized : EidInteractionEvent()
    object CardRemoved : EidInteractionEvent()
    class CanRequested : EidInteractionEvent() // this needs to be class so that it can be posted to the same flow multiple times in a row
    class PinRequested(val attempts: Int) : EidInteractionEvent()
    object NewPinRequested : EidInteractionEvent()
    object PukRequested : EidInteractionEvent()

    object IdentificationStarted : EidInteractionEvent()
    class IdentificationRequestConfirmationRequested(val request: IdentificationRequest) : EidInteractionEvent()
    class CertificateDescriptionReceived(val certificateDescription: CertificateDescription) : EidInteractionEvent()
    class IdentificationSucceededWithRedirect(val redirectURL: String) : EidInteractionEvent()

    object PinChangeStarted : EidInteractionEvent()
    object PinChangeSucceeded : EidInteractionEvent()
}
