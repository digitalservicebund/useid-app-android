package de.digitalService.useID.idCardInterface

import kotlin.coroutines.cancellation.CancellationException

sealed class IdCardInteractionException(message: String? = null) : CancellationException(message) {
    class FrameworkError(message: String? = null) : IdCardInteractionException(message)
    class UnexpectedReadAttribute(message: String? = null) : IdCardInteractionException(message)
    object CardBlocked : IdCardInteractionException()
    object CardDeactivated : IdCardInteractionException()
    class ProcessFailed(val redirectUrl: String? = null) : IdCardInteractionException()
    object ChangingPinFailed : IdCardInteractionException()

    val redacted: RedactedIDCardInteractionException?
        get() = when (this) {
            is FrameworkError -> RedactedIDCardInteractionException.FrameworkError
            is UnexpectedReadAttribute -> RedactedIDCardInteractionException.UnexpectedReadAttribute
            is ProcessFailed -> RedactedIDCardInteractionException.ProcessFailed
            else -> null
        }
}

sealed class RedactedIDCardInteractionException(message: String? = null) : Exception(message) {
    object FrameworkError : RedactedIDCardInteractionException()
    object UnexpectedReadAttribute : RedactedIDCardInteractionException()
    object ProcessFailed : RedactedIDCardInteractionException()
}
