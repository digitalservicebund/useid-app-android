package com.example.idprototype.idCardInterface

import android.content.Context
import android.content.Intent
import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import org.openecard.android.activation.AndroidContextManager
import org.openecard.android.activation.OpeneCard
import org.openecard.mobile.activation.*
import kotlin.coroutines.CoroutineContext

class IDCardManager {
    private val logTag = javaClass.canonicalName!!

    // TODO: Check if we need to create a new instance for every operation. https://github.com/ecsec/open-ecard/blob/v2master/doc/integration/init.rst
    private val openECard = OpeneCard.createInstance()
    private var androidContextManager: AndroidContextManager? = null

    fun handleNFCIntent(intent: Intent) = androidContextManager?.onNewIntent(intent)

    @OptIn(ExperimentalCoroutinesApi::class)
    fun startIdentification(context: Context, tokenURL: String): Flow<Event> = callbackFlow {
        androidContextManager = openECard.context(context)
        var activationController: ActivationController? = null
        androidContextManager?.initializeContext(object: StartServiceHandler {
            override fun onSuccess(p0: ActivationSource?) {
                if (p0 == null) {
                    Log.e(logTag, "onSuccess called without parameter.")
                    cancel(IDCardManagerException.FrameworkError())
                    return
                }

                activationController = p0.eacFactory().create(tokenURL, object: ControllerCallback {
                    override fun onStarted() {
                        Log.d(logTag, "Started EAC identification.")
                    }

                    override fun onAuthenticationCompletion(p0: ActivationResult?) {
                        Log.d(logTag, "EAC identification completed.")
                        if (p0 == null) {
                            cancel(IDCardManagerException.FrameworkError())
                            return
                        }

                        // TODO: Handle each case appropriately
                        when(p0.resultCode) {
                            ActivationResultCode.OK -> trySend(Event.AuthenticationSuccessful)
                                .onClosed { Log.e(logTag, "Tried to send value to closed channel.") }
                                .onFailure { Log.e(logTag, "Sending value to channel failed: ${it?.message}") }
                                .onSuccess { Log.d(logTag, "Value emitted to flow successfully.") }
                            ActivationResultCode.REDIRECT -> trySend(Event.AuthenticationSuccessful)
                                .onClosed { Log.e(logTag, "Tried to send value to closed channel.") }
                                .onFailure { Log.e(logTag, "Sending value to channel failed: ${it?.message}") }
                                .onSuccess { Log.d(logTag, "Value emitted to flow successfully.") }
                            ActivationResultCode.CLIENT_ERROR -> TODO()
                            ActivationResultCode.INTERRUPTED -> TODO()
                            ActivationResultCode.INTERNAL_ERROR -> TODO()
                            ActivationResultCode.DEPENDING_HOST_UNREACHABLE -> TODO()
                            ActivationResultCode.BAD_REQUEST -> TODO()
                        }
                    }
                }, object: EacInteraction {
                    override fun requestCardInsertion() {
                        Log.d(logTag, "Request card insertion.")
                        trySend(Event.RequestCardInsertion)
                            .onClosed { Log.e(logTag, "Tried to send value to closed channel.") }
                            .onFailure { Log.e(logTag, "Sending value to channel failed: ${it?.message}") }
                            .onSuccess { Log.d(logTag, "Value emitted to flow successfully.") }
                    }

                    override fun requestCardInsertion(p0: NFCOverlayMessageHandler?) {
                        TODO("Not yet implemented")
                    }

                    override fun onCardInteractionComplete() {
                        Log.d(logTag, "Card interaction complete.")
                        trySend(Event.CardInteractionComplete)
                            .onClosed { Log.e(logTag, "Tried to send value to closed channel.") }
                            .onFailure { Log.e(logTag, "Sending value to channel failed: ${it?.message}") }
                            .onSuccess { Log.d(logTag, "Value emitted to flow successfully.") }
                    }

                    override fun onCardRecognized() {
                        Log.d(logTag, "Card recognized.")
                        trySend(Event.CardRecognized)
                            .onClosed { Log.e(logTag, "Tried to send value to closed channel.") }
                            .onFailure { Log.e(logTag, "Sending value to channel failed: ${it?.message}") }
                            .onSuccess { Log.d(logTag, "Value emitted to flow successfully.") }
                    }

                    override fun onCardRemoved() {
                        Log.d(logTag, "Card removed.")
                        trySend(Event.CardRemoved)
                            .onClosed { Log.e(logTag, "Tried to send value to closed channel.") }
                            .onFailure { Log.e(logTag, "Sending value to channel failed: ${it?.message}") }
                            .onSuccess { Log.d(logTag, "Value emitted to flow successfully.") }
                    }

                    override fun onCanRequest(p0: ConfirmPasswordOperation?) {
                        TODO("Not yet implemented")
                    }

                    override fun onPinRequest(p0: ConfirmPasswordOperation?) {
                        Log.d(logTag, "Requesting PIN.")

                        if (p0 == null) {
                            cancel(IDCardManagerException.FrameworkError())
                            return
                        }

                        trySend(Event.RequestPIN { p0.confirmPassword(it) })
                            .onClosed { Log.e(logTag, "Tried to send value to closed channel.") }
                            .onFailure { Log.e(logTag, "Sending value to channel failed: ${it?.message}") }
                            .onSuccess { Log.d(logTag, "Value emitted to flow successfully.") }
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
                            cancel(IDCardManagerException.FrameworkError())
                            return
                        }

                        val readAttributes = p0.readAccessAttributes.map { it ->
                            try {
                                Pair(Attribute.valueOf(it.name), it.isRequired)
                            } catch (e: IllegalArgumentException) {
                                throw IDCardManagerException.UnexpectedReadAttribute(e.message)
                            }
                        }.toMap()

                        val eidServerData = EIDAuthenticationRequest(
                            p0.issuer,
                            p0.issuerUrl,
                            p0.subject,
                            p0.subjectUrl,
                            p0.validity,
                            readAttributes
                        )

                        val confirmationCallback: (Map<Attribute, Boolean>) -> Unit = { p2.enterAttributeSelection(it.toSelectableItems(), listOf()) }

                        trySend(Event.RequestAuthenticationRequestConfirmation(eidServerData, confirmationCallback))
                            .onClosed { Log.e(logTag, "Tried to send value to closed channel.") }
                            .onFailure { Log.e(logTag, "Sending value to channel failed: ${it?.message}") }
                            .onSuccess { Log.d(logTag, "Value emitted to flow successfully.") }
                    }

                    override fun onCardAuthenticationSuccessful() {
                        Log.d(logTag, "Card authentication successful.")
                        trySend(Event.AuthenticationSuccessful)
                            .onClosed { Log.e(logTag, "Tried to send value to closed channel.") }
                            .onFailure { Log.e(logTag, "Sending value to channel failed: ${it?.message}") }
                            .onSuccess { Log.d(logTag, "Value emitted to flow successfully.") }
                    }
                })
            }

            override fun onFailure(p0: ServiceErrorResponse?) {
                Log.e(logTag, "Failure. Error code: ${p0?.errorMessage ?: "n/a"}, Error message: ${p0?.errorMessage ?: "n/a"}")
                cancel(CancellationException(p0?.errorDescription()))
            }
        })

        awaitClose {
            Log.d(logTag, "Closing flow channel.")

            activationController?.cancelOngoingAuthentication()
            androidContextManager?.terminateContext(object: StopServiceHandler {
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

// TODO: Add terms
data class EIDAuthenticationRequest(
    val issuer: String,
    val issuerURL: String,
    val subject: String,
    val subjectURL: String,
    val validity: String,
    val readAttributes: Map<Attribute, Boolean>
)

sealed class Event {
    class RequestAuthenticationRequestConfirmation(val request: EIDAuthenticationRequest, val confirmationCallback: (Map<Attribute, Boolean>) -> Unit): Event()
    object RequestCardInsertion: Event()
    object CardInteractionComplete: Event()
    object CardRecognized: Event()
    object CardRemoved: Event()
    class RequestPIN(val pinCallback: (String) -> Unit): Event()
    object AuthenticationSuccessful: Event()
}

sealed class IDCardManagerException(message: String? = null): CancellationException(message) {
    class FrameworkError(message: String? = null): IDCardManagerException(message)
    class UnexpectedReadAttribute(message: String? = null): IDCardManagerException(message)
    class Failure(message: String?) : IDCardManagerException(message)
}

fun ServiceErrorResponse.errorDescription(): String = "${statusCode.ordinal} - ${statusCode.name}: ${errorMessage}"
fun Map<Attribute, Boolean>.toSelectableItems(): List<SelectableItem> = map { object: SelectableItem {
    override fun getName(): String = it.key.name
    override fun getText(): String = ""
    override fun isChecked(): Boolean = it.value
    override fun setChecked(p0: Boolean) { }
    override fun isRequired(): Boolean = false
} }