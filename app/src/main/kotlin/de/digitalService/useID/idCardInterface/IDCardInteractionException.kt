package de.digitalService.useID.idCardInterface

import org.openecard.mobile.activation.ActivationResultCode
import kotlin.coroutines.cancellation.CancellationException

sealed class IDCardInteractionException(message: String? = null): CancellationException(message) {
    class FrameworkError(message: String? = null): IDCardInteractionException(message)
    class UnexpectedReadAttribute(message: String? = null): IDCardInteractionException(message)
    object CardBlocked: IDCardInteractionException()
    object CardDeactivated: IDCardInteractionException()
    class ProcessFailed(val resultCode: ActivationResultCode) : IDCardInteractionException()
}