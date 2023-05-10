package de.digitalService.useID.idCardInterface

import kotlin.coroutines.cancellation.CancellationException

sealed class EidInteractionException(message: String? = null) : CancellationException(message) {
    class FrameworkError(message: String? = null) : EidInteractionException(message)
    class UnexpectedReadAttribute(message: String? = null) : EidInteractionException(message)
    object CardBlocked : EidInteractionException()
    object CardDeactivated : EidInteractionException()
    class ProcessFailed(val redirectUrl: String? = null) : EidInteractionException()
    object ChangingPinFailed : EidInteractionException()

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
