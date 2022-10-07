package de.digitalService.useID.idCardInterface

import org.openecard.mobile.activation.ActivationResultCode
import kotlin.coroutines.cancellation.CancellationException

sealed class IDCardInteractionException(message: String? = null) : CancellationException(message) {
    class FrameworkError(message: String? = null) : IDCardInteractionException(message)
    class UnexpectedReadAttribute(message: String? = null) : IDCardInteractionException(message)
    object CardBlocked : IDCardInteractionException()
    object CardDeactivated : IDCardInteractionException()
    class ProcessFailed(val resultCode: ActivationResultCode, val redirectUrl: String?, val resultMinor: String?) : IDCardInteractionException()

    val redacted: RedactedIDCardInteractionException?
        get() = when (this) {
            is FrameworkError -> RedactedIDCardInteractionException.FrameworkError
            is UnexpectedReadAttribute -> RedactedIDCardInteractionException.UnexpectedReadAttribute
            is ProcessFailed -> RedactedIDCardInteractionException.ProcessFailed(this.resultCode, this.resultMinor)
            else -> null
        }
}

sealed class RedactedIDCardInteractionException(message: String? = null) : Exception(message) {
    object FrameworkError : RedactedIDCardInteractionException()
    object UnexpectedReadAttribute : RedactedIDCardInteractionException()
    class ProcessFailed(resultCode: ActivationResultCode, resultMinor: String?) : RedactedIDCardInteractionException("processFailed(resultCode: ${resultCode.name}, resultMinor: $resultMinor")
}
