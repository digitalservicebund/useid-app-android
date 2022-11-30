package de.digitalService.useID.idCardInterface

sealed class EidInteractionEvent {
    object RequestCardInsertion : EidInteractionEvent()
    object CardInteractionComplete : EidInteractionEvent()
    object CardRecognized : EidInteractionEvent()
    object CardRemoved : EidInteractionEvent()
    class RequestCan(val canCallback: (String) -> Unit) : EidInteractionEvent()
    class RequestPin(val attempts: Int?, val pinCallback: (String) -> Unit) : EidInteractionEvent()
    class RequestPinAndCan(val pinCanCallback: (pin: String, can: String) -> Unit) : EidInteractionEvent()
    class RequestPUK(val pukCallback: (String) -> Unit) : EidInteractionEvent()
    object ProcessCompletedSuccessfullyWithoutResult : EidInteractionEvent()
    class ProcessCompletedSuccessfullyWithRedirect(val redirectURL: String) : EidInteractionEvent()

    object AuthenticationStarted : EidInteractionEvent()
    class RequestAuthenticationRequestConfirmation(val request: EidAuthenticationRequest, val confirmationCallback: (Map<IdCardAttribute, Boolean>) -> Unit) : EidInteractionEvent()
    object AuthenticationSuccessful : EidInteractionEvent()

    object PinManagementStarted : EidInteractionEvent()
    class RequestChangedPin(val attempts: Int?, val pinCallback: (oldPin: String, newPin: String) -> Unit) : EidInteractionEvent()
    class RequestCanAndChangedPin(val pinCallback: (oldPin: String, can: String, newPin: String) -> Unit) : EidInteractionEvent()

    val redacted: RedactedEIDInteractionEvent
        get() = when (this) {
            is RequestCardInsertion -> RedactedEIDInteractionEvent.RequestCardInsertion
            is CardInteractionComplete -> RedactedEIDInteractionEvent.CardInteractionComplete
            is CardRecognized -> RedactedEIDInteractionEvent.CardRecognized
            is CardRemoved -> RedactedEIDInteractionEvent.CardRemoved
            is RequestCan -> RedactedEIDInteractionEvent.RequestCan
            is RequestPin -> RedactedEIDInteractionEvent.RequestPin
            is RequestPinAndCan -> RedactedEIDInteractionEvent.RequestPinAndCan
            is RequestPUK -> RedactedEIDInteractionEvent.RequestPUK
            is ProcessCompletedSuccessfullyWithoutResult -> RedactedEIDInteractionEvent.ProcessCompletedSuccessfullyWithoutResult
            is ProcessCompletedSuccessfullyWithRedirect -> RedactedEIDInteractionEvent.ProcessCompletedSuccessfullyWithRedirect
            is AuthenticationStarted -> RedactedEIDInteractionEvent.AuthenticationStarted
            is RequestAuthenticationRequestConfirmation -> RedactedEIDInteractionEvent.RequestAuthenticationRequestConfirmation
            is AuthenticationSuccessful -> RedactedEIDInteractionEvent.AuthenticationSuccessful
            is PinManagementStarted -> RedactedEIDInteractionEvent.PinManagementStarted
            is RequestChangedPin -> RedactedEIDInteractionEvent.RequestChangedPin
            is RequestCanAndChangedPin -> RedactedEIDInteractionEvent.RequestCanAndChangedPin
        }
}

sealed class RedactedEIDInteractionEvent : Exception() {
    object RequestCardInsertion : RedactedEIDInteractionEvent()
    object CardInteractionComplete : RedactedEIDInteractionEvent()
    object CardRecognized : RedactedEIDInteractionEvent()
    object CardRemoved : RedactedEIDInteractionEvent()
    object RequestCan : RedactedEIDInteractionEvent()
    object RequestPin : RedactedEIDInteractionEvent()
    object RequestPinAndCan : RedactedEIDInteractionEvent()
    object RequestPUK : RedactedEIDInteractionEvent()
    object ProcessCompletedSuccessfullyWithoutResult : RedactedEIDInteractionEvent()
    object ProcessCompletedSuccessfullyWithRedirect : RedactedEIDInteractionEvent()

    object AuthenticationStarted : RedactedEIDInteractionEvent()
    object RequestAuthenticationRequestConfirmation : RedactedEIDInteractionEvent()
    object AuthenticationSuccessful : RedactedEIDInteractionEvent()

    object PinManagementStarted : RedactedEIDInteractionEvent()
    object RequestChangedPin : RedactedEIDInteractionEvent()
    object RequestCanAndChangedPin : RedactedEIDInteractionEvent()
}
