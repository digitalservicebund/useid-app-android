package de.digitalService.useID.idCardInterface


sealed class EidInteractionEvent {
    object Idle : EidInteractionEvent()
    class Error(val exception: IdCardInteractionException) : EidInteractionEvent()

    object CardInsertionRequested : EidInteractionEvent()
    object CardRecognized : EidInteractionEvent()
    object CardRemoved : EidInteractionEvent()
    class CanRequested : EidInteractionEvent() // this needs to be class so that it can be posted to the same flow multiple times in a row
    class PinRequested(val attempts: Int) : EidInteractionEvent()
    object NewPinRequested : EidInteractionEvent()
    object PukRequested : EidInteractionEvent()

    object AuthenticationStarted : EidInteractionEvent()
    class AuthenticationRequestConfirmationRequested(val request: AuthenticationRequest) : EidInteractionEvent()
    class CertificateDescriptionReceived(val certificateDescription: CertificateDescription) : EidInteractionEvent()
    class AuthenticationSucceededWithRedirect(val redirectURL: String) : EidInteractionEvent()

    object PinChangeStarted : EidInteractionEvent()
    object PinChangeSucceeded : EidInteractionEvent()

//    val redacted: RedactedEidInteractionEvent
//        get() = when (this) {
//            is Idle -> RedactedEidInteractionEvent.Idle
//            is Error -> RedactedEidInteractionEvent.Error
//            is RequestCardInsertion -> RedactedEidInteractionEvent.RequestCardInsertion
//            is CardInteractionComplete -> RedactedEidInteractionEvent.CardInteractionComplete
//            is CardRecognized -> RedactedEidInteractionEvent.CardRecognized
//            is CardRemoved -> RedactedEidInteractionEvent.CardRemoved
//            is RequestCan -> RedactedEidInteractionEvent.RequestCan
//            is RequestPin -> RedactedEidInteractionEvent.RequestPin
//            is RequestPuk -> RedactedEidInteractionEvent.RequestPUK
//            is ProcessCompletedSuccessfullyWithoutResult -> RedactedEidInteractionEvent.ProcessCompletedSuccessfullyWithoutResult
//            is ProcessCompletedSuccessfullyWithRedirect -> RedactedEidInteractionEvent.ProcessCompletedSuccessfullyWithRedirect
//            is AuthenticationStarted -> RedactedEidInteractionEvent.AuthenticationStarted
//            is RequestAuthenticationRequestConfirmation -> RedactedEidInteractionEvent.RequestAuthenticationRequestConfirmation
//            is AuthenticationSuccessful -> RedactedEidInteractionEvent.AuthenticationSuccessful
//            is PinManagementStarted -> RedactedEidInteractionEvent.PinManagementStarted
//            is RequestNewPin -> RedactedEidInteractionEvent.RequestChangedPin
//            is PinManagementFinished ->
//        }
}

//sealed class RedactedEidInteractionEvent : Exception() {
//    object Idle : RedactedEidInteractionEvent()
//    object Error : RedactedEidInteractionEvent()
//    object RequestCardInsertion : RedactedEidInteractionEvent()
//    object CardInteractionComplete : RedactedEidInteractionEvent()
//    object CardRecognized : RedactedEidInteractionEvent()
//    object CardRemoved : RedactedEidInteractionEvent()
//    object RequestCan : RedactedEidInteractionEvent()
//    object RequestPin : RedactedEidInteractionEvent()
//    object RequestPinAndCan : RedactedEidInteractionEvent()
//    object RequestPUK : RedactedEidInteractionEvent()
//    object ProcessCompletedSuccessfullyWithoutResult : RedactedEidInteractionEvent()
//    object ProcessCompletedSuccessfullyWithRedirect : RedactedEidInteractionEvent()
//
//    object AuthenticationStarted : RedactedEidInteractionEvent()
//    object RequestAuthenticationRequestConfirmation : RedactedEidInteractionEvent()
//    object AuthenticationSuccessful : RedactedEidInteractionEvent()
//
//    object PinManagementStarted : RedactedEidInteractionEvent()
//    object RequestChangedPin : RedactedEidInteractionEvent()
//    object RequestCanAndChangedPin : RedactedEidInteractionEvent()
//}
