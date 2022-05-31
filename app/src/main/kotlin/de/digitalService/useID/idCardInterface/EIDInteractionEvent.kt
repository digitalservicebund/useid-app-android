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
    object ProcessCompletedSuccessfully : EIDInteractionEvent()

    object AuthenticationStarted : EIDInteractionEvent()
    class RequestAuthenticationRequestConfirmation(val request: EIDAuthenticationRequest, val confirmationCallback: (Map<IDCardAttribute, Boolean>) -> Unit) : EIDInteractionEvent()
    object AuthenticationSuccessful : EIDInteractionEvent()

    object PINManagementStarted : EIDInteractionEvent()
    class RequestChangedPIN(val attempts: Int?, val pinCallback: (oldPin: String, newPin: String) -> Unit) : EIDInteractionEvent()
    class RequestCANAndChangedPIN(val pinCallback: (oldPin: String, can: String, newPin: String) -> Unit) : EIDInteractionEvent()
}
