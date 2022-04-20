package com.example.idprototype.idCardInterface

import android.content.Context
import android.content.Intent
import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import org.openecard.android.activation.AndroidContextManager
import org.openecard.android.activation.OpeneCard
import org.openecard.mobile.activation.*

sealed class IDCardManagerException: CancellationException() {
    object FrameworkError: IDCardManagerException()
    object UnexpectedReadAttribute: IDCardManagerException()
    class ProcessFailed(val resultCode: ActivationResultCode) : IDCardManagerException()
}

class IDCardManager {
    private val logTag = javaClass.canonicalName!!

    private val openECard = OpeneCard.createInstance()
    private var androidContextManager: AndroidContextManager? = null

    private sealed class Task {
        data class EAC(val tokenURL: String): Task()
        object PINManagement: Task()
    }

    // TODO: As we might need to handle more intents in the future switch to androidContectManager.onNewIntent(Tag) instead
    fun handleNFCIntent(intent: Intent) = androidContextManager?.onNewIntent(intent)

    fun identify(context: Context, tokenURL: String): Flow<EIDInteractionEvent> = executeTask(context, Task.EAC(tokenURL))
    fun changePin(context: Context): Flow<EIDInteractionEvent> = executeTask(context, Task.PINManagement)

    private class ControllerCallbackHandler(private val channel: SendChannel<EIDInteractionEvent>): ControllerCallback {
        private val logTag = javaClass.canonicalName!!

        override fun onStarted() {
            Log.d(logTag, "Started process.")
            channel.trySendClosingOnError(EIDInteractionEvent.AuthenticationStarted)
        }

        override fun onAuthenticationCompletion(p0: ActivationResult?) {
            Log.d(logTag, "Process completed.")
            if (p0 == null) {
                channel.close(IDCardManagerException.FrameworkError)
                return
            }

            when(p0.resultCode) {
                ActivationResultCode.OK -> {
                    channel.trySendClosingOnError(EIDInteractionEvent.ProcessCompletedSuccessfully)
                    channel.close()
                }
                ActivationResultCode.REDIRECT -> {
                    channel.trySendClosingOnError(EIDInteractionEvent.ProcessCompletedSuccessfully)
                    channel.close()
                }
                else -> channel.close(IDCardManagerException.ProcessFailed(p0.resultCode))
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun executeTask(context: Context, task: Task): Flow<EIDInteractionEvent> = callbackFlow {
        androidContextManager = openECard.context(context)
        var activationController: ActivationController? = null

        androidContextManager?.initializeContext(object : StartServiceHandler {
            override fun onSuccess(p0: ActivationSource?) {
                if (p0 == null) {
                    Log.e(logTag, "onSuccess called without parameter.")
                    cancel(IDCardManagerException.FrameworkError)
                    return
                }

                val controllerCallback = ControllerCallbackHandler(channel)
                activationController = when (task) {
                    is Task.EAC -> p0.eacFactory().create(task.tokenURL, controllerCallback, EACInteractionHandler(channel))
                    is Task.PINManagement -> p0.pinManagementFactory().create(controllerCallback, PINManagementInteractionHandler(channel))
                }
            }

            override fun onFailure(p0: ServiceErrorResponse?) {
                Log.e(
                    logTag,
                    "Failure. Error code: ${p0?.errorMessage ?: "n/a"}, Error message: ${p0?.errorMessage ?: "n/a"}"
                )
                cancel(CancellationException(p0?.errorDescription()))
            }
        })

        awaitClose {
            Log.d(logTag, "Closing flow channel.")

            activationController?.cancelOngoingAuthentication()
            androidContextManager?.terminateContext(object : StopServiceHandler {
                override fun onSuccess() {
                    Log.d(logTag, "Terminated context successfully.")
                }

                override fun onFailure(p0: ServiceErrorResponse?) {
                    Log.e(logTag, "Failed to terminate context: ${p0?.errorDescription()}")
                }
            })
        }
    }
}