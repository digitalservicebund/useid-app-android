package de.digitalService.useID.idCardInterface

import android.content.Context
import android.nfc.Tag
import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import org.openecard.android.activation.AndroidContextManager
import org.openecard.android.activation.OpeneCard
import org.openecard.mobile.activation.*

class IDCardManager {
    private val logTag = javaClass.canonicalName!!

    private val openECard = OpeneCard.createInstance()
    private var androidContextManager: AndroidContextManager? = null

    private sealed class Task {
        data class EAC(val tokenURL: String) : Task()
        object PINManagement : Task()
    }

    fun handleNFCTag(tag: Tag) = androidContextManager?.onNewIntent(tag) ?: Log.d(logTag, "Ignoring NFC tag because no ID card related process is running.")

    fun identify(context: Context, url: String): Flow<EIDInteractionEvent> = executeTask(context, Task.EAC(url))
    fun changePin(context: Context): Flow<EIDInteractionEvent> = executeTask(context, Task.PINManagement)

    private class ControllerCallbackHandler(private val channel: SendChannel<EIDInteractionEvent>) : ControllerCallback {
        private val logTag = javaClass.canonicalName!!

        override fun onStarted() {
            Log.d(logTag, "Started process.")
            channel.trySendClosingOnError(EIDInteractionEvent.AuthenticationStarted)
        }

        override fun onAuthenticationCompletion(p0: ActivationResult?) {
            Log.d(logTag, "Process completed.")
            if (p0 == null) {
                channel.close(IDCardInteractionException.FrameworkError())
                return
            }

            when (p0.resultCode) {
                ActivationResultCode.OK, ActivationResultCode.REDIRECT -> {
                    channel.trySendClosingOnError(EIDInteractionEvent.ProcessCompletedSuccessfully)
                    channel.close()
                }
                else -> channel.close(IDCardInteractionException.ProcessFailed(p0.resultCode))
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
                    cancel(IDCardInteractionException.FrameworkError())
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
                    "Failure. ${p0?.errorDescription() ?: "n/a"}"
                )
                cancel(IDCardInteractionException.FrameworkError(p0?.errorDescription()))
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

            androidContextManager = null
        }
    }
}
