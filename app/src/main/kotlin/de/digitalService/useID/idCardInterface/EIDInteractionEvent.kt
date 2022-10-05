package de.digitalService.useID.idCardInterface

sealed class EIDInteractionEvent {
    object RequestCardInsertion : EIDInteractionEvent()
    object CardInteractionComplete : EIDInteractionEvent()
    object CardRecognized : EIDInteractionEvent()
    object CardRemoved : EIDInteractionEvent()
    class RequestCAN(val canCallback: (String) -> Unit) : EIDInteractionEvent()
    class RequestPIN(val attempts: Int?, val pinCallback: (String) -> Unit) : EIDInteractionEvent()
    class RequestPINAndCAN(val pinCANCallback: (pin: String, can: String) -> Unit) : EIDInteractionEvent()
    class RequestPUK(val pukCallback: (String) -> Unit) : EIDInteractionEvent()
    object ProcessCompletedSuccessfullyWithoutResult : EIDInteractionEvent()
    class ProcessCompletedSuccessfullyWithRedirect(val redirectURL: String) : EIDInteractionEvent()

    object AuthenticationStarted : EIDInteractionEvent()
    class RequestAuthenticationRequestConfirmation(val request: EIDAuthenticationRequest, val confirmationCallback: (Map<IDCardAttribute, Boolean>) -> Unit) : EIDInteractionEvent()
    object AuthenticationSuccessful : EIDInteractionEvent()

    object PINManagementStarted : EIDInteractionEvent()
    class RequestChangedPIN(val attempts: Int?, val pinCallback: (oldPin: String, newPin: String) -> Unit) : EIDInteractionEvent()
    class RequestCANAndChangedPIN(val pinCallback: (oldPin: String, can: String, newPin: String) -> Unit) : EIDInteractionEvent()

    val redacted: RedactedEIDInteractionEvent
        get() = when (this) {
            is RequestCardInsertion -> RedactedEIDInteractionEvent.RequestCardInsertion
            is CardInteractionComplete -> RedactedEIDInteractionEvent.CardInteractionComplete
            is CardRecognized -> RedactedEIDInteractionEvent.CardRecognized
            is CardRemoved -> RedactedEIDInteractionEvent.CardRemoved
            is RequestCAN -> RedactedEIDInteractionEvent.RequestCAN
            is RequestPIN -> RedactedEIDInteractionEvent.RequestPIN
            is RequestPINAndCAN -> RedactedEIDInteractionEvent.RequestPINAndCAN
            is RequestPUK -> RedactedEIDInteractionEvent.RequestPUK
            is ProcessCompletedSuccessfullyWithoutResult -> RedactedEIDInteractionEvent.ProcessCompletedSuccessfullyWithoutResult
            is ProcessCompletedSuccessfullyWithRedirect -> RedactedEIDInteractionEvent.ProcessCompletedSuccessfullyWithRedirect
            is AuthenticationStarted -> RedactedEIDInteractionEvent.AuthenticationStarted
            is RequestAuthenticationRequestConfirmation -> RedactedEIDInteractionEvent.RequestAuthenticationRequestConfirmation
            is AuthenticationSuccessful -> RedactedEIDInteractionEvent.AuthenticationSuccessful
            is PINManagementStarted -> RedactedEIDInteractionEvent.PINManagementStarted
            is RequestChangedPIN -> RedactedEIDInteractionEvent.RequestChangedPIN
            is RequestCANAndChangedPIN -> RedactedEIDInteractionEvent.RequestCANAndChangedPIN
        }
}

sealed class RedactedEIDInteractionEvent : Exception() {
    object RequestCardInsertion : RedactedEIDInteractionEvent()
    object CardInteractionComplete : RedactedEIDInteractionEvent()
    object CardRecognized : RedactedEIDInteractionEvent()
    object CardRemoved : RedactedEIDInteractionEvent()
    object RequestCAN : RedactedEIDInteractionEvent()
    object RequestPIN : RedactedEIDInteractionEvent()
    object RequestPINAndCAN : RedactedEIDInteractionEvent()
    object RequestPUK : RedactedEIDInteractionEvent()
    object ProcessCompletedSuccessfullyWithoutResult : RedactedEIDInteractionEvent()
    object ProcessCompletedSuccessfullyWithRedirect : RedactedEIDInteractionEvent()

    object AuthenticationStarted : RedactedEIDInteractionEvent()
    object RequestAuthenticationRequestConfirmation : RedactedEIDInteractionEvent()
    object AuthenticationSuccessful : RedactedEIDInteractionEvent()

    object PINManagementStarted : RedactedEIDInteractionEvent()
    object RequestChangedPIN : RedactedEIDInteractionEvent()
    object RequestCANAndChangedPIN : RedactedEIDInteractionEvent()
}
