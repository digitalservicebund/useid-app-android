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

fun SendChannel<EIDInteractionEvent>.trySendAbortingOnError(event: EIDInteractionEvent) = trySend(event)
    .onClosed { Log.e("Channel", "Tried to send value to closed channel.") }
    .onFailure { Log.e("Channel", "Sending value to channel failed: ${it?.message}"); close(it) }

class IDCardManager {
    private val logTag = javaClass.canonicalName!!

    private val openECard = OpeneCard.createInstance()
    private var androidContextManager: AndroidContextManager? = null

    class ControllerCallbackHandler(private val channel: SendChannel<EIDInteractionEvent>): ControllerCallback {
        private val logTag = javaClass.canonicalName!!

        override fun onStarted() {
            Log.d(logTag, "Started process.")
            channel.trySendAbortingOnError(EIDInteractionEvent.AuthenticationStarted)
        }

        override fun onAuthenticationCompletion(p0: ActivationResult?) {
            Log.d(logTag, "Process completed.")
            if (p0 == null) {
                channel.close(IDCardManagerException.FrameworkError)
                return
            }

            when(p0.resultCode) {
                ActivationResultCode.OK -> {
                    channel.trySendAbortingOnError(EIDInteractionEvent.ProcessCompletedSuccessfully)
                    channel.close()
                }
                ActivationResultCode.REDIRECT -> {
                    channel.trySendAbortingOnError(EIDInteractionEvent.ProcessCompletedSuccessfully)
                    channel.close()
                }
                else -> channel.close(IDCardManagerException.ProcessFailed(p0.resultCode))
            }
        }
    }

    class EACInteractionHandler(private val channel: SendChannel<EIDInteractionEvent>): EacInteraction {
        private val logTag = javaClass.canonicalName!!

        override fun requestCardInsertion() {
            Log.d(logTag, "Request card insertion.")
            channel.trySendAbortingOnError(EIDInteractionEvent.RequestCardInsertion)
        }

        override fun requestCardInsertion(p0: NFCOverlayMessageHandler?) {
            TODO("Not yet implemented")
        }

        override fun onCardInteractionComplete() {
            Log.d(logTag, "Card interaction complete.")
            channel.trySendAbortingOnError(EIDInteractionEvent.CardInteractionComplete)
        }

        override fun onCardRecognized() {
            Log.d(logTag, "Card recognized.")
            channel.trySendAbortingOnError(EIDInteractionEvent.CardRecognized)
        }

        override fun onCardRemoved() {
            Log.d(logTag, "Card removed.")
            channel.trySendAbortingOnError(EIDInteractionEvent.CardRemoved)
        }

        override fun onCanRequest(p0: ConfirmPasswordOperation?) {
            TODO("Not yet implemented")
        }

        override fun onPinRequest(p0: ConfirmPasswordOperation?) {
            Log.d(logTag, "Requesting PIN.")

            if (p0 == null) {
                channel.close(IDCardManagerException.FrameworkError)
                return
            }

            channel.trySendAbortingOnError(EIDInteractionEvent.RequestPIN { p0.confirmPassword(it) })
        }

        override fun onPinRequest(p0: Int, p1: ConfirmPasswordOperation?) {
            TODO("Not yet implemented")
        }

        override fun onPinCanRequest(p0: ConfirmPinCanOperation?) {
            TODO("Not yet implemented")
        }

        override fun onCardBlocked() {
            TODO("Not yet implemented")
        }

        override fun onCardDeactivated() {
            TODO("Not yet implemented")
        }

        override fun onServerData(
            p0: ServerData?,
            p1: String?,
            p2: ConfirmAttributeSelectionOperation?
        ) {
            if (p0 == null || p1 == null || p2 == null) {
                channel.close(IDCardManagerException.FrameworkError)
                return
            }

            val readAttributes = p0.readAccessAttributes.map { it ->
                try {
                    Pair(Attribute.valueOf(it.name), it.isRequired)
                } catch (e: IllegalArgumentException) {
                    throw IDCardManagerException.UnexpectedReadAttribute(e)
                }
            }.toMap()

            val eidServerData = EIDAuthenticationRequest(
                p0.issuer,
                p0.issuerUrl,
                p0.subject,
                p0.subjectUrl,
                p0.validity,
                AuthenticationTerms.Text(p0.termsOfUsage.dataString),
                readAttributes
            )

            val confirmationCallback: (Map<Attribute, Boolean>) -> Unit = { p2.enterAttributeSelection(it.toSelectableItems(), listOf()) }

            channel.trySendAbortingOnError(EIDInteractionEvent.RequestAuthenticationRequestConfirmation(eidServerData, confirmationCallback))
        }

        override fun onCardAuthenticationSuccessful() {
            Log.d(logTag, "Card authentication successful.")
            channel.trySendAbortingOnError(EIDInteractionEvent.AuthenticationSuccessful)
        }
    }

    class PINManagementInteractionHandler(private val channel: SendChannel<EIDInteractionEvent>): PinManagementInteraction {
        private val logTag = javaClass.canonicalName!!

        override fun requestCardInsertion() {
            Log.d(logTag, "Request card insertion.")
            channel.trySendAbortingOnError(EIDInteractionEvent.RequestCardInsertion)
        }

        override fun requestCardInsertion(p0: NFCOverlayMessageHandler?) {
            TODO("Not yet implemented")
        }

        override fun onCardInteractionComplete() {
            Log.d(logTag, "Card interaction complete.")
            channel.trySendAbortingOnError(EIDInteractionEvent.CardInteractionComplete)
        }

        override fun onCardRecognized() {
            Log.d(logTag, "Card recognized.")
            channel.trySendAbortingOnError(EIDInteractionEvent.CardRecognized)
        }

        override fun onCardRemoved() {
            Log.d(logTag, "Card removed.")
            channel.trySendAbortingOnError(EIDInteractionEvent.CardRemoved)
        }

        override fun onPinChangeable(p0: ConfirmOldSetNewPasswordOperation?) {
            TODO("Not yet implemented")
        }

        override fun onPinChangeable(p0: Int, p1: ConfirmOldSetNewPasswordOperation?) {
            Log.d(logTag, "Request old and new PIN.")
            if (p1 == null) {
                channel.close(IDCardManagerException.FrameworkError)
                return
            }

            val pinCallback: (oldPin: String, newPin: String) -> Unit = { oldPin, newPin ->
                p1.confirmPassword(oldPin, newPin)
            }

            channel.trySendAbortingOnError(EIDInteractionEvent.RequestChangedPIN(p0, pinCallback))
        }

        override fun onPinCanNewPinRequired(p0: ConfirmPinCanNewPinOperation?) {
            TODO("Not yet implemented")
        }

        override fun onPinBlocked(p0: ConfirmPasswordOperation?) {
            TODO("Not yet implemented")
        }

        override fun onCardPukBlocked() {
            TODO("Not yet implemented")
        }

        override fun onCardDeactivated() {
            TODO("Not yet implemented")
        }
    }

    fun handleNFCIntent(intent: Intent) = androidContextManager?.onNewIntent(intent)

    private sealed class Task {
        data class EAC(val tokenURL: String): Task()
        object PINManagement: Task()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun generalTask(context: Context, task: Task): Flow<EIDInteractionEvent> = callbackFlow {
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

    fun identify(context: Context, tokenURL: String): Flow<EIDInteractionEvent> = generalTask(context, Task.EAC(tokenURL))
    fun changePin(context: Context): Flow<EIDInteractionEvent> = generalTask(context, Task.PINManagement)
}

// TR-03110 (Part 4), Section 2.2.3
enum class Attribute {
    DG01,
    DG02,
    DG03,
    DG04,
    DG05,
    DG06,
    DG07,
    DG08,
    DG09,
    DG10,
    DG13,
    DG17,
    DG19,
    RESTRICTED_IDENTIFICATION,
    AGE_VERIFICATION
}

sealed class AuthenticationTerms {
    data class Text(val text: String): AuthenticationTerms()
}

data class EIDAuthenticationRequest(
    val issuer: String,
    val issuerURL: String,
    val subject: String,
    val subjectURL: String,
    val validity: String,
    val terms: AuthenticationTerms,
    val readAttributes: Map<Attribute, Boolean>
)

sealed class EIDInteractionEvent {
    object RequestCardInsertion: EIDInteractionEvent()
    object CardInteractionComplete: EIDInteractionEvent()
    object CardRecognized: EIDInteractionEvent()
    object CardRemoved: EIDInteractionEvent()
    object ProcessCompletedSuccessfully: EIDInteractionEvent()

    object AuthenticationStarted: EIDInteractionEvent()
    class RequestAuthenticationRequestConfirmation(val request: EIDAuthenticationRequest, val confirmationCallback: (Map<Attribute, Boolean>) -> Unit): EIDInteractionEvent()
    class RequestPIN(val pinCallback: (String) -> Unit): EIDInteractionEvent()
    object AuthenticationSuccessful: EIDInteractionEvent()

    object PINManagementStarted: EIDInteractionEvent()
    class RequestChangedPIN(val attempts: Int, val pinCallback: (oldPin: String, newPin: String) -> Unit): EIDInteractionEvent()
}

sealed class IDCardManagerException(e: Throwable? = null): CancellationException() {
    object FrameworkError: IDCardManagerException()
    class UnexpectedReadAttribute(e: Throwable?): IDCardManagerException(e)
    class ProcessFailed(val resultCode: ActivationResultCode) : IDCardManagerException()
}

fun ServiceErrorResponse.errorDescription(): String = "${statusCode.ordinal} - ${statusCode.name}: ${errorMessage}"
fun Map<Attribute, Boolean>.toSelectableItems(): List<SelectableItem> = map { object: SelectableItem {
    override fun getName(): String = it.key.name
    override fun getText(): String = ""
    override fun isChecked(): Boolean = it.value
    override fun setChecked(p0: Boolean) { }
    override fun isRequired(): Boolean = false
} }