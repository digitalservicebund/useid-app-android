package de.digitalService.useID.idCardInterface

import kotlin.coroutines.cancellation.CancellationException

sealed class EidInteractionException(message: String? = null) : CancellationException(message) {
    class FrameworkError(message: String? = null) : EidInteractionException(message)
    class UnexpectedReadAttribute(message: String? = null) : EidInteractionException(message)
    object CardBlocked : EidInteractionException()
    object CardDeactivated : EidInteractionException()
    class ProcessFailed(val redirectUrl: String? = null, val resultMinor: String? = null, val resultReason: String? = null) : EidInteractionException()
    object ChangingPinFailed : EidInteractionException()

    val redacted: RedactedIDCardInteractionException?
        get() = when (this) {
            is FrameworkError -> RedactedIDCardInteractionException.FrameworkError
            is UnexpectedReadAttribute -> RedactedIDCardInteractionException.UnexpectedReadAttribute
            is ProcessFailed -> RedactedIDCardInteractionException.ProcessFailed(resultMinor, resultReason)
            else -> null
        }
}

sealed class RedactedIDCardInteractionException(message: String? = null) : Exception(message) {
    object FrameworkError : RedactedIDCardInteractionException()
    object UnexpectedReadAttribute : RedactedIDCardInteractionException()
    class ProcessFailed(resultMinor: String?, resultReason: String?) : RedactedIDCardInteractionException("process failed(resultMinor: $resultMinor, resultReason: $resultReason)")
}
