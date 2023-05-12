package de.digitalService.useID.ui.coordinators

import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Base64
import androidx.core.content.ContextCompat
import androidx.credentials.CreatePublicKeyCredentialRequest
import androidx.credentials.CredentialManager
import androidx.credentials.GetPasswordOption
import com.google.android.gms.fido.fido2.Fido2ApiClient
import com.google.android.gms.fido.fido2.api.common.PublicKeyCredentialCreationOptions
import com.google.android.gms.fido.fido2.api.common.PublicKeyCredentialParameters
import com.google.android.gms.fido.fido2.api.common.PublicKeyCredentialRpEntity
import com.google.android.gms.fido.fido2.api.common.PublicKeyCredentialUserEntity
import com.google.android.gms.fido.u2f.api.common.RegisterRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import de.digitalService.useID.StorageManagerType
import de.digitalService.useID.analytics.TrackerManagerType
import de.digitalService.useID.flows.CanStateMachine
import de.digitalService.useID.flows.IdentificationStateMachine
import de.digitalService.useID.getLogger
import de.digitalService.useID.idCardInterface.EidInteractionEvent
import de.digitalService.useID.idCardInterface.IdCardInteractionException
import de.digitalService.useID.idCardInterface.IdCardManager
import de.digitalService.useID.ui.navigation.Navigator
import de.digitalService.useID.ui.screens.destinations.*
import de.digitalService.useID.ui.screens.identification.IdentificationDeviceSelection
import de.digitalService.useID.util.CoroutineContextProviderType
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.serialization.*
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okio.ByteString.Companion.decodeBase64
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IdentificationCoordinator @Inject constructor(
    @ApplicationContext private val context: Context,
    private val canCoordinator: CanCoordinator,
    private val navigator: Navigator,
    private val idCardManager: IdCardManager,
    private val storageManager: StorageManagerType,
    private val trackerManager: TrackerManagerType,
    private val flowStateMachine: IdentificationStateMachine,
    private val canStateMachine: CanStateMachine,
    private val backendClient: OkHttpClient,
    private val coroutineContextProvider: CoroutineContextProviderType
) {
    private val logger by getLogger()

    private val _scanInProgress: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val scanInProgress: Flow<Boolean>
        get() = _scanInProgress

    private var stateMachineCoroutineScope: Job? = null
    private var eIdEventFlowCoroutineScope: Job? = null
    private var canEventFlowCoroutineScope: Job? = null

    private val _stateFlow: MutableStateFlow<SubCoordinatorState> = MutableStateFlow(SubCoordinatorState.FINISHED)
    val stateFlow: StateFlow<SubCoordinatorState>
        get() = _stateFlow

    private var widgetSessionId: String? = null
    val credentialRequestFlow: MutableStateFlow<PendingIntent?> = MutableStateFlow(null)
    lateinit var activity: Activity

    private fun collectStateMachineEvents() {
        if (stateMachineCoroutineScope != null) {
            return
        }

        stateMachineCoroutineScope = CoroutineScope(coroutineContextProvider.Default).launch {
            flowStateMachine.state.collect { eventAndPair ->
                if (eventAndPair.first is IdentificationStateMachine.Event.Back) {
                    navigator.pop()
                    if (eventAndPair.second == IdentificationStateMachine.State.Invalid) {
                        resetCoordinatorState()
                    }
                } else {
                    when (val state = eventAndPair.second) {
                        is IdentificationStateMachine.State.StartIdentification -> executeIdentification(state.tcTokenUrl)
                        is IdentificationStateMachine.State.FetchingMetadata -> navigator.popUpToOrNavigate(IdentificationFetchMetadataDestination(state.backingDownAllowed), false)
                        is IdentificationStateMachine.State.FetchingMetadataFailed -> navigator.navigate(IdentificationOtherErrorDestination)
                        is IdentificationStateMachine.State.RequestAttributeConfirmation -> navigator.navigatePopping(IdentificationAttributeConsentDestination(state.request, state.backingDownAllowed))
                        is IdentificationStateMachine.State.SubmitAttributeConfirmation -> state.confirmationCallback(state.request.readAttributes.filterValues { it })
                        is IdentificationStateMachine.State.PinInput -> navigator.navigate(IdentificationPersonalPinDestination(false))
                        is IdentificationStateMachine.State.PinInputRetry -> navigator.navigate(IdentificationPersonalPinDestination(true))
                        is IdentificationStateMachine.State.RevisitAttributes -> navigator.pop()
                        is IdentificationStateMachine.State.PinEntered -> state.callback(state.pin)
                        is IdentificationStateMachine.State.CanRequested -> startCanFlow(state.pin)
                        is IdentificationStateMachine.State.WaitingForCardAttachment -> navigator.popUpToOrNavigate(IdentificationScanDestination, false)
                        is IdentificationStateMachine.State.AskForDeviceSwitch -> navigator.navigate(IdentificationDeviceSelectionDestination)
                        is IdentificationStateMachine.State.FinishOnSameDevice -> finishOnSameDevice(state.redirectUrl)
                        is IdentificationStateMachine.State.ExecuteDeviceSwitch -> handoverToDifferentDevice(state.redirectUrl)
                        is IdentificationStateMachine.State.FinishedOnSameDevice -> navigator.navigate(IdentificationSuccessDestination)
//                        is IdentificationStateMachine.State.Finished -> finishIdentification()

                        is IdentificationStateMachine.State.CardDeactivated -> navigator.navigate(IdentificationCardDeactivatedDestination)
                        is IdentificationStateMachine.State.CardBlocked -> navigator.navigate(IdentificationCardBlockedDestination)
                        is IdentificationStateMachine.State.CardUnreadable -> navigator.navigate(IdentificationCardUnreadableDestination(true, state.redirectUrl))

                        IdentificationStateMachine.State.Invalid -> logger.debug("Ignoring transition to state INVALID.")
                    }
                }
            }
        }
    }

    fun startIdentificationProcess(tcTokenUrl: String, widgetSessionId: String?, setupSkipped: Boolean) {
        collectStateMachineEvents()
        _stateFlow.value = SubCoordinatorState.ACTIVE

        this.widgetSessionId = widgetSessionId

        val normalizedTcTokenUrl = Uri
            .Builder()
            .scheme("http")
            .encodedAuthority("127.0.0.1:24727")
            .appendPath("eID-Client")
            .appendQueryParameter("tcTokenURL", tcTokenUrl)
            .build()
            .toString()

        flowStateMachine.transition(IdentificationStateMachine.Event.Initialize(setupSkipped, normalizedTcTokenUrl))
        canStateMachine.transition(CanStateMachine.Event.Invalidate)
    }

    fun confirmAttributesForIdentification() {
        flowStateMachine.transition(IdentificationStateMachine.Event.ConfirmAttributes)
    }

    fun setPin(pin: String) {
        flowStateMachine.transition(IdentificationStateMachine.Event.EnterPin(pin))
    }

    private fun startCanFlow(pin: String?) {
        if (canCoordinator.stateFlow.value != SubCoordinatorState.ACTIVE) {
            canEventFlowCoroutineScope = CoroutineScope(coroutineContextProvider.IO).launch {
                canCoordinator.startIdentCanFlow(pin).collect { state ->
                    when (state) {
                        SubCoordinatorState.CANCELLED -> cancelIdentification()
                        else -> logger.debug("Ignoring sub flow state: $state")
                    }
                }
            }
        } else {
            logger.debug("Don't start CAN flow as it is already active.")
        }
    }

    fun onBack() {
        flowStateMachine.transition(IdentificationStateMachine.Event.Back)
    }

    fun retryIdentification() {
        flowStateMachine.transition(IdentificationStateMachine.Event.RetryAfterError)
    }

    fun cancelIdentification() {
        logger.debug("Cancel identification process.")

        navigator.popToRoot()
        _stateFlow.value = SubCoordinatorState.CANCELLED
        resetCoordinatorState()
    }

    fun redirectOnSameDevice() {
        flowStateMachine.transition(IdentificationStateMachine.Event.FinishOnSameDevice)
    }

    fun redirectOnDifferentDevice() {
        flowStateMachine.transition(IdentificationStateMachine.Event.DeviceSwitchRequested)
    }

    fun confirmFinish() {
//        flowStateMachine.transition(IdentificationStateMachine.Event.Finish)
        finishIdentification()
    }

    private fun finishOnSameDevice(redirectUrl: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(redirectUrl))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        ContextCompat.startActivity(context, intent, null)

        flowStateMachine.transition(IdentificationStateMachine.Event.RedirectedOnSameDevice)
    }

    private fun handoverToDifferentDevice(redirectUrl: String) {
        CoroutineScope(coroutineContextProvider.IO).launch {
            logger.debug("Requesting credentials")

            // CREDENTIALS REQUEST
            val credentialsRequest = CredentialsRequest(widgetSessionId!!, redirectUrl)
            val requestBody = Json.encodeToString(credentialsRequest).toRequestBody("application/json; charset=utf-8".toMediaType())

            val request = Request.Builder()
                .url("https://useid.dev.ds4g.net/api/v1/credentials")
                .post(requestBody)
                .build()

            val response = backendClient.newCall(request).execute()
            val responseJson = Json.parseToJsonElement(response.body!!.string()).jsonObject

            val credentialId = responseJson.get("credentialId")!!.jsonPrimitive.content
            val pkcCreationOptionsString = responseJson.get("pkcCreationOptions")!!.jsonPrimitive.content
            val pkcCreationOptions: PkcCreationOptions = Json.decodeFromString(pkcCreationOptionsString)

            val unwrappedOptions = Json.parseToJsonElement(pkcCreationOptionsString).jsonObject.get("publicKey")!!.jsonObject.toString()

            val challenge = Base64.decode(pkcCreationOptions.publicKey.challenge, Base64.DEFAULT)
            val parameters = pkcCreationOptions.publicKey.pubKeyCredParams.map { PublicKeyCredentialParameters(it.type, it.alg) }

//            logger.debug("Credentials response: $credentialsResponse")

            // WEBAUTHN

            val credentialManager = CredentialManager.create(context)

            logger.debug("Create request...")
            val createPublicKeyCredentialRequest = CreatePublicKeyCredentialRequest(unwrappedOptions)

            logger.debug("Create credentials...")
            val result = credentialManager.createCredential(createPublicKeyCredentialRequest, activity)

            logger.debug("Registration result: $result")

            // SEND RESPONSE TO BACKEND
            val resultRequestBody = ""
            val resultRequest = Request.Builder()
                .url("https://useid.dev.ds4g.net/api/v1/credentials/$credentialId")
                .put(resultRequestBody.toRequestBody("application/json; charset=utf-8".toMediaType()))
                .build()

            val resultResponse = backendClient.newCall(resultRequest).execute()

//            val client = Fido2ApiClient(context)
//
//            val options = PublicKeyCredentialCreationOptions.Builder()
//                .setChallenge(challenge)
//                .setRp(
//                    PublicKeyCredentialRpEntity(
//                        pkcCreationOptions.publicKey.rp.id,
//                        pkcCreationOptions.publicKey.rp.name,
//                        null
//                    )
//                )
//                .setUser(PublicKeyCredentialUserEntity(Base64.decode(pkcCreationOptions.publicKey.user.id, Base64.NO_CLOSE), pkcCreationOptions.publicKey.user.name, "", pkcCreationOptions.publicKey.user.displayName))
//                .setParameters(parameters)
//                .setTimeoutSeconds(60.toDouble())
//                .build()
//
//            val result = client.getRegisterPendingIntent(options)
//                .addOnCanceledListener {
//                    logger.debug("onCancel")
//                }
//                .addOnCompleteListener {
//                    logger.debug("onComplete")
//                }
//                .addOnFailureListener {
//                    logger.error("onFailure: $it")
//                }
//                .addOnSuccessListener {
//                    logger.debug("onSuccess")
//                    CoroutineScope(coroutineContextProvider.Default).launch {
//                        credentialRequestFlow.emit(it)
//                    }
//                }
        }
    }

    private fun finishIdentification() {
        logger.debug("Finish identification process.")

        navigator.popToRoot()
        _stateFlow.value = SubCoordinatorState.FINISHED

        storageManager.setIsNotFirstTimeUser()
        trackerManager.trackEvent(category = "identification", action = "success", name = "")

        resetCoordinatorState()
    }

    private fun resetCoordinatorState() {
        _scanInProgress.value = false
        eIdEventFlowCoroutineScope?.cancel()
        canEventFlowCoroutineScope?.cancel()
        idCardManager.cancelTask()
        flowStateMachine.transition(IdentificationStateMachine.Event.Invalidate)
    }

    private fun executeIdentification(tcTokenUrl: String) {
        eIdEventFlowCoroutineScope?.cancel()
        idCardManager.cancelTask()
        eIdEventFlowCoroutineScope = CoroutineScope(coroutineContextProvider.IO).launch {
            idCardManager.eidFlow.catch { exception ->
                logger.error("Error: $exception")
                _scanInProgress.value = false
                idCardManager.cancelTask()
                navigator.navigate(IdentificationOtherErrorDestination)
            }.collect { event ->
                when (event) {
                    is EidInteractionEvent.AuthenticationStarted -> {
                        logger.debug("Authentication started.")
                        flowStateMachine.transition(IdentificationStateMachine.Event.StartedFetchingMetadata)
                    }
                    is EidInteractionEvent.RequestAuthenticationRequestConfirmation -> {
                        logger.debug("Requesting authentication confirmation")
                        flowStateMachine.transition(IdentificationStateMachine.Event.FrameworkRequestsAttributeConfirmation(event.request, event.confirmationCallback))
                    }
                    is EidInteractionEvent.RequestPin -> {
                        logger.debug("Requesting PIN")
                        flowStateMachine.transition(IdentificationStateMachine.Event.FrameworkRequestsPin(event.pinCallback))
                    }
                    EidInteractionEvent.RequestCardInsertion -> {
                        logger.debug("Card insertion requested.")
                        flowStateMachine.transition(IdentificationStateMachine.Event.RequestCardInsertion)
                    }
                    EidInteractionEvent.CardRecognized -> {
                        logger.debug("Card recognized.")
                        _scanInProgress.value = true
                    }
                    EidInteractionEvent.CardRemoved -> {
                        logger.debug("Card removed.")
                        _scanInProgress.value = false
                    }
                    is EidInteractionEvent.ProcessCompletedSuccessfullyWithRedirect -> {
                        logger.debug("Process completed successfully.")
                        flowStateMachine.transition(IdentificationStateMachine.Event.HandleIdentificationResult(event.redirectURL))
                    }
                    is EidInteractionEvent.RequestPinAndCan -> {
                        logger.debug("PIN and CAN requested.")
                        flowStateMachine.transition(IdentificationStateMachine.Event.FrameworkRequestsCan)
                    }
                    is EidInteractionEvent.RequestPuk -> {
                        logger.debug("PUK requested.")
                        flowStateMachine.transition(IdentificationStateMachine.Event.Error(IdCardInteractionException.CardBlocked))
                    }
                    is EidInteractionEvent.Error -> {
                        logger.error("Identification error: ${event.exception}")
                        flowStateMachine.transition(IdentificationStateMachine.Event.Error(event.exception))
                    }
                    else -> logger.debug("Ignoring event: $event")
                }
            }
        }

        idCardManager.identify(context, tcTokenUrl)
    }
}

@kotlinx.serialization.Serializable
data class CredentialsRequest(
    val widgetSessionId: String,
    val refreshAddress: String
)

//@OptIn(ExperimentalSerializationApi::class)
//@Serializer(forClass = CredentialsResponseSerializer::class)
//object CredentialsResponseSerializer: KSerializer<CredentialsResponse> {
//    override val descriptor: SerialDescriptor
//    get() = buildClassSerialDescriptor("CredentialsResponse") {
//        element<String>("credentialId")
//        element<PkcCreationOptions>("pkcCreationOptions")
//    }
//
//    override fun deserialize(decoder: Decoder): CredentialsResponse {
//        TODO("Not yet implemented")
//    }
//
//    override fun serialize(encoder: Encoder, value: CredentialsResponse) {
//        TODO("Not yet implemented")
//    }
//
//}

@kotlinx.serialization.Serializable
data class CredentialsResponse(
    val credentialId: String,
    val pkcCreationOptions: PkcCreationOptions
)

@kotlinx.serialization.Serializable
data class PkcCreationOptions(
    val publicKey: CredentialsPublicKey
)

@kotlinx.serialization.Serializable
data class CredentialsPublicKey(
    val challenge: String,
    val rp: RelyingParty,
    val user: PkcUser,
    val pubKeyCredParams: Array<PubKeyCredParams>,
    val excludeCredentials: Array<String>,
    val authenticatorSelection: AuthenticatorSelection,
    val attestation: String,
    val extensions: CredExtensions
)

@kotlinx.serialization.Serializable
data class RelyingParty(
    val name: String,
    val id: String
)

@kotlinx.serialization.Serializable
data class PubKeyCredParams(
    val alg: Int,
    val type: String
)

@kotlinx.serialization.Serializable
data class CredExtensions(
    val credProps: Boolean
)

@kotlinx.serialization.Serializable
data class AuthenticatorSelection(
    val requireResidentKey: Boolean,
    val residentKey: String
)

@kotlinx.serialization.Serializable
data class PkcUser(
    val name: String,
    val displayName: String,
    val id: String
)

//const publicKeyCredentialCreationOptions = {
//    challenge: Uint8Array.from(
//    randomStringFromServer, c => c.charCodeAt(0)),
//    rp: {
//        name: "Duo Security",
//        id: "duosecurity.com",
//    },
//    user: {
//        id: Uint8Array.from(
//        "UZSL85T9AFC", c => c.charCodeAt(0)),
//        name: "lee@webauthn.guide",
//        displayName: "Lee",
//    },
//    pubKeyCredParams: [{alg: -7, type: "public-key"}],
//    authenticatorSelection: {
//        authenticatorAttachment: "cross-platform",
//    },
//    timeout: 60000,
//    attestation: "direct"
//};
//
//const credential = await navigator.credentials.create({
//    publicKey: publicKeyCredentialCreationOptions
//});
