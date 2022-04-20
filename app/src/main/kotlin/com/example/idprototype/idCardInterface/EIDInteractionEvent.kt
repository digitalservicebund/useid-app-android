package com.example.idprototype.idCardInterface

sealed class EIDInteractionEvent {
    object RequestCardInsertion: EIDInteractionEvent()
    object CardInteractionComplete: EIDInteractionEvent()
    object CardRecognized: EIDInteractionEvent()
    object CardRemoved: EIDInteractionEvent()
    object ProcessCompletedSuccessfully: EIDInteractionEvent()

    object AuthenticationStarted: EIDInteractionEvent()
    class RequestAuthenticationRequestConfirmation(val request: EIDAuthenticationRequest, val confirmationCallback: (Map<IDCardAttribute, Boolean>) -> Unit): EIDInteractionEvent()
    class RequestPIN(val pinCallback: (String) -> Unit): EIDInteractionEvent()
    object AuthenticationSuccessful: EIDInteractionEvent()

    object PINManagementStarted: EIDInteractionEvent()
    class RequestChangedPIN(val attempts: Int, val pinCallback: (oldPin: String, newPin: String) -> Unit): EIDInteractionEvent()
}