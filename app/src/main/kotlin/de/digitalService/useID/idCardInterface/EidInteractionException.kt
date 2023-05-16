package de.digitalService.useID.idCardInterface

import kotlin.coroutines.cancellation.CancellationException

sealed class EidInteractionException(message: String? = null) : CancellationException(message) {
    class FrameworkError(message: String? = null) : EidInteractionException(message)
    class UnexpectedReadAttribute(message: String? = null) : EidInteractionException(message)
    object CardBlocked : EidInteractionException()
    object CardDeactivated : EidInteractionException()
    class ProcessFailed(val redirectUrl: String? = null, val resultMinor: String? = null, val resultReason: String? = null) : EidInteractionException()
    object ChangingPinFailed : EidInteractionException()

    val redacted: RedactedEidInteractionException
        get() = when (this) {
            is FrameworkError -> RedactedEidInteractionException.FrameworkError
            is UnexpectedReadAttribute -> RedactedEidInteractionException.UnexpectedReadAttribute
            CardBlocked -> RedactedEidInteractionException.CardBlocked
            CardDeactivated -> RedactedEidInteractionException.CardDeactivated
            is ProcessFailed -> RedactedEidInteractionException.ProcessFailed(resultMinor, resultReason)
            ChangingPinFailed -> RedactedEidInteractionException.ChangingPinFailed
        }
}

sealed class RedactedEidInteractionException(message: String? = null) : Exception(message) {
    object FrameworkError : RedactedEidInteractionException()
    object UnexpectedReadAttribute : RedactedEidInteractionException()
    object CardBlocked : RedactedEidInteractionException()
    object CardDeactivated : RedactedEidInteractionException()
    class ProcessFailed(resultMinor: String?, resultReason: String?) : RedactedEidInteractionException("process failed(resultMinor: $resultMinor, resultReason: $resultReason)")
    object ChangingPinFailed : RedactedEidInteractionException()
}
