package de.digitalService.useID.idCardInterface

sealed class EidInteractionEvent {
    object Idle : EidInteractionEvent()
    class Error(val exception: IdCardInteractionException) : EidInteractionEvent()

    object RequestCardInsertion : EidInteractionEvent()
    object CardInteractionComplete : EidInteractionEvent()
    object CardRecognized : EidInteractionEvent()
    object CardRemoved : EidInteractionEvent()
    class RequestCan(val canCallback: (String) -> Unit) : EidInteractionEvent()
    class RequestPin(val attempts: Int?, val pinCallback: (String) -> Unit) : EidInteractionEvent()
    class RequestPinAndCan(val pinCanCallback: (pin: String, can: String) -> Unit) : EidInteractionEvent()
    class RequestPuk(val pukCallback: (String) -> Unit) : EidInteractionEvent()
    object ProcessCompletedSuccessfullyWithoutResult : EidInteractionEvent()
    class ProcessCompletedSuccessfullyWithRedirect(val redirectURL: String) : EidInteractionEvent()

    object AuthenticationStarted : EidInteractionEvent()
    class RequestAuthenticationRequestConfirmation(val request: EidAuthenticationRequest, val confirmationCallback: (Map<IdCardAttribute, Boolean>) -> Unit) : EidInteractionEvent()
    object AuthenticationSuccessful : EidInteractionEvent()

    object PinManagementStarted : EidInteractionEvent()
    class RequestChangedPin(val attempts: Int?, val pinCallback: (oldPin: String, newPin: String) -> Unit) : EidInteractionEvent()
    class RequestCanAndChangedPin(val pinCallback: (oldPin: String, can: String, newPin: String) -> Unit) : EidInteractionEvent()

    val redacted: RedactedEidInteractionEvent
        get() = when (this) {
            is Idle -> RedactedEidInteractionEvent.Idle
            is Error -> RedactedEidInteractionEvent.Error
            is RequestCardInsertion -> RedactedEidInteractionEvent.RequestCardInsertion
            is CardInteractionComplete -> RedactedEidInteractionEvent.CardInteractionComplete
            is CardRecognized -> RedactedEidInteractionEvent.CardRecognized
            is CardRemoved -> RedactedEidInteractionEvent.CardRemoved
            is RequestCan -> RedactedEidInteractionEvent.RequestCan
            is RequestPin -> RedactedEidInteractionEvent.RequestPin
            is RequestPinAndCan -> RedactedEidInteractionEvent.RequestPinAndCan
            is RequestPuk -> RedactedEidInteractionEvent.RequestPUK
            is ProcessCompletedSuccessfullyWithoutResult -> RedactedEidInteractionEvent.ProcessCompletedSuccessfullyWithoutResult
            is ProcessCompletedSuccessfullyWithRedirect -> RedactedEidInteractionEvent.ProcessCompletedSuccessfullyWithRedirect
            is AuthenticationStarted -> RedactedEidInteractionEvent.AuthenticationStarted
            is RequestAuthenticationRequestConfirmation -> RedactedEidInteractionEvent.RequestAuthenticationRequestConfirmation
            is AuthenticationSuccessful -> RedactedEidInteractionEvent.AuthenticationSuccessful
            is PinManagementStarted -> RedactedEidInteractionEvent.PinManagementStarted
            is RequestChangedPin -> RedactedEidInteractionEvent.RequestChangedPin
            is RequestCanAndChangedPin -> RedactedEidInteractionEvent.RequestCanAndChangedPin
        }
}

sealed class RedactedEidInteractionEvent : Exception() {
    object Idle: RedactedEidInteractionEvent()
    object Error: RedactedEidInteractionEvent()
    object RequestCardInsertion : RedactedEidInteractionEvent()
    object CardInteractionComplete : RedactedEidInteractionEvent()
    object CardRecognized : RedactedEidInteractionEvent()
    object CardRemoved : RedactedEidInteractionEvent()
    object RequestCan : RedactedEidInteractionEvent()
    object RequestPin : RedactedEidInteractionEvent()
    object RequestPinAndCan : RedactedEidInteractionEvent()
    object RequestPUK : RedactedEidInteractionEvent()
    object ProcessCompletedSuccessfullyWithoutResult : RedactedEidInteractionEvent()
    object ProcessCompletedSuccessfullyWithRedirect : RedactedEidInteractionEvent()

    object AuthenticationStarted : RedactedEidInteractionEvent()
    object RequestAuthenticationRequestConfirmation : RedactedEidInteractionEvent()
    object AuthenticationSuccessful : RedactedEidInteractionEvent()

    object PinManagementStarted : RedactedEidInteractionEvent()
    object RequestChangedPin : RedactedEidInteractionEvent()
    object RequestCanAndChangedPin : RedactedEidInteractionEvent()
}
