package com.example.idprototype.idCardInterface

import org.openecard.mobile.activation.ActivationResultCode
import kotlin.coroutines.cancellation.CancellationException

sealed class IDCardInteractionException: CancellationException() {
    object FrameworkError: IDCardInteractionException()
    object UnexpectedReadAttribute: IDCardInteractionException()
    object CardBlocked: IDCardInteractionException()
    object CardDeactivated: IDCardInteractionException()
    class ProcessFailed(val resultCode: ActivationResultCode) : IDCardInteractionException()
}