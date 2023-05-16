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

    val redacted: RedactedEidInteractionEvent
        get() = when (this) {
            is Idle -> RedactedEidInteractionEvent.Idle
            is Error -> RedactedEidInteractionEvent.Error
            is CardInsertionRequested -> RedactedEidInteractionEvent.CardInsertionRequested
            is CardRecognized -> RedactedEidInteractionEvent.CardRecognized
            is CardRemoved -> RedactedEidInteractionEvent.CardRemoved
            is CanRequested -> RedactedEidInteractionEvent.CanRequested
            is PinRequested -> RedactedEidInteractionEvent.PinRequested
            is NewPinRequested -> RedactedEidInteractionEvent.NewPinRequested
            is PukRequested -> RedactedEidInteractionEvent.PukRequested
            is IdentificationStarted -> RedactedEidInteractionEvent.IdentificationStarted
            is IdentificationRequestConfirmationRequested -> RedactedEidInteractionEvent.IdentificationRequestConfirmationRequested
            is CertificateDescriptionReceived -> RedactedEidInteractionEvent.CertificateDescriptionReceived
            is IdentificationSucceededWithRedirect -> RedactedEidInteractionEvent.IdentificationSucceededWithRedirect
            is PinChangeStarted -> RedactedEidInteractionEvent.PinChangeStarted
            is PinChangeSucceeded -> RedactedEidInteractionEvent.PinChangeSucceeded
        }
}

sealed class RedactedEidInteractionEvent : Exception() {
    object Idle : RedactedEidInteractionEvent()
    object Error : RedactedEidInteractionEvent()
    object CardInsertionRequested : RedactedEidInteractionEvent()
    object CardRecognized : RedactedEidInteractionEvent()
    object CardRemoved : RedactedEidInteractionEvent()
    object CanRequested : RedactedEidInteractionEvent()
    object PinRequested : RedactedEidInteractionEvent()
    object NewPinRequested : RedactedEidInteractionEvent()
    object PukRequested : RedactedEidInteractionEvent()
    object IdentificationStarted : RedactedEidInteractionEvent()
    object IdentificationRequestConfirmationRequested : RedactedEidInteractionEvent()
    object CertificateDescriptionReceived : RedactedEidInteractionEvent()
    object IdentificationSucceededWithRedirect : RedactedEidInteractionEvent()
    object PinChangeStarted : RedactedEidInteractionEvent()
    object PinChangeSucceeded : RedactedEidInteractionEvent()
}
