package de.digitalService.useID.idCardInterface

import android.content.Context
import android.nfc.Tag
import android.util.Log
import de.digitalService.useID.getLogger
import io.sentry.Sentry
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import org.openecard.android.activation.AndroidContextManager
import org.openecard.android.activation.OpeneCard
import org.openecard.mobile.activation.*

class IdCardManager {
    private val logTag = javaClass.canonicalName!!

    private val openECard = OpeneCard.createInstance()
    private var androidContextManager: AndroidContextManager? = null
    private var activationController: ActivationController? = null
    private var stopHandler: StopServiceHandler = StopServiceHandlerImplementation()

    private sealed class Task {
        data class EAC(val tokenURL: String) : Task()
        object PinManagement : Task()
    }

    private val _eidFlow: MutableStateFlow<EidInteractionEvent> = MutableStateFlow(EidInteractionEvent.Idle)
    val eidFlow: Flow<EidInteractionEvent>
        get() = _eidFlow

    fun handleNfcTag(tag: Tag) = androidContextManager?.onNewIntent(tag) ?: Log.d(logTag, "Ignoring NFC tag because no ID card related process is running.")

    fun identify(context: Context, url: String) = executeTask(context, Task.EAC(url))
    fun changePin(context: Context) = executeTask(context, Task.PinManagement)

    private class ControllerCallbackHandler(private val eidFlow: MutableStateFlow<EidInteractionEvent>, private val completion: () -> Unit) : ControllerCallback {
        private val logger by getLogger()

        private val logTag = javaClass.canonicalName!!

        override fun onStarted() {
            Log.d(logTag, "Started process.")
            CoroutineScope(Dispatchers.IO).launch {
                eidFlow.emit(EidInteractionEvent.AuthenticationStarted)
            }
        }

        override fun onAuthenticationCompletion(p0: ActivationResult?) {
            CoroutineScope(Dispatchers.IO).launch {
                Log.d(logTag, "Process completed.")
                if (p0 == null) {
                    eidFlow.emit(EidInteractionEvent.Error(IdCardInteractionException.FrameworkError()))
                    return@launch
                }

                when (p0.resultCode) {
                    ActivationResultCode.OK -> {
                        eidFlow.emit(EidInteractionEvent.ProcessCompletedSuccessfullyWithoutResult)
                    }
                    ActivationResultCode.REDIRECT -> {
                        if (p0.processResultMinor != null) {
                            eidFlow.emit(EidInteractionEvent.Error(IdCardInteractionException.ProcessFailed(p0.resultCode, p0.redirectUrl, p0.processResultMinor)))
                        } else {
                            eidFlow.emit(EidInteractionEvent.ProcessCompletedSuccessfullyWithRedirect(p0.redirectUrl))
                        }
                    }
                    ActivationResultCode.INTERRUPTED -> {
                        logger.debug("INTERRUPTED. Process has probably been cancelled. Resetting to idle state.")
                        eidFlow.emit(EidInteractionEvent.Idle)
                    }
                    else -> eidFlow.emit(EidInteractionEvent.Error(IdCardInteractionException.ProcessFailed(p0.resultCode, p0.redirectUrl, p0.processResultMinor)))
                }

                completion()
            }
        }
    }

    private class StopServiceHandlerImplementation : StopServiceHandler {
        private val logger by getLogger()

        override fun onSuccess() {
            logger.debug("Terminated context successfully.")
        }

        override fun onFailure(p0: ServiceErrorResponse?) {
            class ServiceErrorResponseError(message: String) : Exception(message)
            Sentry.captureException(ServiceErrorResponseError("Status code: ${p0?.statusCode}"))
            logger.error("Failed to terminate context: ${p0?.errorDescription()}")
        }
    }

    private fun executeTask(context: Context, task: Task) {
        androidContextManager = openECard.context(context)

        androidContextManager?.initializeContext(object : StartServiceHandler {
            override fun onSuccess(p0: ActivationSource?) {
                if (p0 == null) {
                    Log.e(logTag, "onSuccess called without parameter.")
                    CoroutineScope(Dispatchers.IO).launch {
                        _eidFlow.emit(EidInteractionEvent.Error(IdCardInteractionException.FrameworkError()))
                    }
                    return
                }

                val controllerCallback = ControllerCallbackHandler(_eidFlow, this@IdCardManager::cancelTask)
                activationController = when (task) {
                    is Task.EAC -> p0.eacFactory().create(task.tokenURL, controllerCallback, EacInteractionHandler(_eidFlow))
                    is Task.PinManagement -> p0.pinManagementFactory().create(controllerCallback, PinManagementInteractionHandler(_eidFlow))
                }
            }

            override fun onFailure(p0: ServiceErrorResponse?) {
                Log.e(
                    logTag,
                    "Failure. ${p0?.errorDescription() ?: "n/a"}"
                )
                CoroutineScope(Dispatchers.IO).launch {
                    _eidFlow.emit(EidInteractionEvent.Error(IdCardInteractionException.FrameworkError(p0?.errorDescription())))
                }
            }
        })
    }

    fun cancelTask() {
        CoroutineScope(Dispatchers.IO).launch {
            _eidFlow.emit(EidInteractionEvent.Idle)
        }

        activationController?.cancelOngoingAuthentication()
        androidContextManager?.terminateContext(stopHandler)
        androidContextManager = null
    }
}
