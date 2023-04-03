package de.digitalService.useID.idCardInterface

import de.governikus.ausweisapp2.sdkwrapper.card.core.CertificateDescription

sealed class EidInteractionEvent {
    object Idle : EidInteractionEvent()
    class Error(val exception: IdCardInteractionException) : EidInteractionEvent()

    object RequestCardInsertion : EidInteractionEvent()
    object CardInteractionComplete : EidInteractionEvent() //todo remove
    object CardRecognized : EidInteractionEvent()
    object CardRemoved : EidInteractionEvent()
    object RequestCan : EidInteractionEvent()
    class RequestPin(val attempts: Int?) : EidInteractionEvent()
    object RequestPuk : EidInteractionEvent()

    object AuthenticationStarted : EidInteractionEvent()
    class RequestAuthenticationRequestConfirmation(val request: AuthenticationRequest) : EidInteractionEvent()
    class AuthenticationCertificate(val certification: de.digitalService.useID.idCardInterface.CertificateDescription) : EidInteractionEvent()
    class AuthenticationSucceededWithRedirect(val redirectURL: String?) : EidInteractionEvent()

    object ChangingPinStarted : EidInteractionEvent()
    class RequestNewPin(val attempts: Int?) : EidInteractionEvent()
    object ChangingPinSucceeded : EidInteractionEvent()

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
