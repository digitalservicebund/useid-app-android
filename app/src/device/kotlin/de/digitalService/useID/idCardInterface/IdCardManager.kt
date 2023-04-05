package de.digitalService.useID.idCardInterface

import android.content.Context
import android.nfc.Tag
import dagger.hilt.android.qualifiers.ApplicationContext
import de.digitalService.useID.getLogger
import de.digitalService.useID.util.CoroutineContextProvider
import de.governikus.ausweisapp2.sdkwrapper.SDKWrapper.workflowController
import de.governikus.ausweisapp2.sdkwrapper.card.core.*
import de.governikus.ausweisapp2.sdkwrapper.card.core.CertificateDescription
//import de.governikus.ausweisapp2.sdkwrapper.card.core.WorkflowController
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject
import javax.inject.Singleton

//import org.openecard.android.activation.AndroidContextManager
//import org.openecard.android.activation.OpeneCard
//import org.openecard.mobile.activation.*

//class IdCardManager {
//    private val logTag = javaClass.canonicalName!!
//
//    private val openECard = OpeneCard.createInstance()
//    private var androidContextManager: AndroidContextManager? = null
//    private var activationController: ActivationController? = null
//    private var stopHandler: StopServiceHandler = StopServiceHandlerImplementation()
//
//    private sealed class Task {
//        data class EAC(val tokenURL: String) : Task()
//        object PinManagement : Task()
//    }
//
//    private val _eidFlow: MutableStateFlow<EidInteractionEvent> = MutableStateFlow(EidInteractionEvent.Idle)
//    val eidFlow: Flow<EidInteractionEvent>
//        get() = _eidFlow
//
//    fun handleNfcTag(tag: Tag) = androidContextManager?.onNewIntent(tag) ?: Log.d(logTag, "Ignoring NFC tag because no ID card related process is running.")
//
//    fun identify(context: Context, url: String) = executeTask(context, Task.EAC(url))
//    fun changePin(context: Context) = executeTask(context, Task.PinManagement)
//
//    private class ControllerCallbackHandler(private val eidFlow: MutableStateFlow<EidInteractionEvent>, private val completion: () -> Unit) : ControllerCallback {
//        private val logger by getLogger()
//
//        private val logTag = javaClass.canonicalName!!
//
//        override fun onStarted() {
//            Log.d(logTag, "Started process.")
//            CoroutineScope(Dispatchers.IO).launch {
//                eidFlow.emit(EidInteractionEvent.AuthenticationStarted)
//            }
//        }
//
//        override fun onAuthenticationCompletion(p0: ActivationResult?) {
//            CoroutineScope(Dispatchers.IO).launch {
//                Log.d(logTag, "Process completed.")
//                if (p0 == null) {
//                    eidFlow.emit(EidInteractionEvent.Error(IdCardInteractionException.FrameworkError()))
//                    return@launch
//                }
//
//                when (p0.resultCode) {
//                    ActivationResultCode.OK -> {
//                        eidFlow.emit(EidInteractionEvent.ProcessCompletedSuccessfullyWithoutResult)
//                    }
//                    ActivationResultCode.REDIRECT -> {
//                        if (p0.processResultMinor != null) {
//                            eidFlow.emit(EidInteractionEvent.Error(IdCardInteractionException.ProcessFailed(p0.resultCode, p0.redirectUrl, p0.processResultMinor)))
//                        } else {
//                            eidFlow.emit(EidInteractionEvent.ProcessCompletedSuccessfullyWithRedirect(p0.redirectUrl))
//                        }
//                    }
//                    ActivationResultCode.INTERRUPTED -> {
//                        logger.debug("INTERRUPTED. Process has probably been cancelled. Resetting to idle state.")
//                        eidFlow.emit(EidInteractionEvent.Idle)
//                    }
//                    else -> eidFlow.emit(EidInteractionEvent.Error(IdCardInteractionException.ProcessFailed(p0.resultCode, p0.redirectUrl, p0.processResultMinor)))
//                }
//
//                completion()
//            }
//        }
//    }
//
//    private class StopServiceHandlerImplementation : StopServiceHandler {
//        private val logger by getLogger()
//
//        override fun onSuccess() {
//            logger.debug("Terminated context successfully.")
//        }
//
//        override fun onFailure(p0: ServiceErrorResponse?) {
//            class ServiceErrorResponseError(message: String) : Exception(message)
//            Sentry.captureException(ServiceErrorResponseError("Status code: ${p0?.statusCode}"))
//            logger.error("Failed to terminate context: ${p0?.errorDescription()}")
//        }
//    }
//
//    private fun executeTask(context: Context, task: Task) {
//        androidContextManager = openECard.context(context)
//
//        androidContextManager?.initializeContext(object : StartServiceHandler {
//            override fun onSuccess(p0: ActivationSource?) {
//                if (p0 == null) {
//                    Log.e(logTag, "onSuccess called without parameter.")
//                    CoroutineScope(Dispatchers.IO).launch {
//                        _eidFlow.emit(EidInteractionEvent.Error(IdCardInteractionException.FrameworkError()))
//                    }
//                    return
//                }
//
//                val controllerCallback = ControllerCallbackHandler(_eidFlow, this@IdCardManager::cancelTask)
//                activationController = when (task) {
//                    is Task.EAC -> p0.eacFactory().create(task.tokenURL, controllerCallback, EacInteractionHandler(_eidFlow))
//                    is Task.PinManagement -> p0.pinManagementFactory().create(controllerCallback, PinManagementInteractionHandler(_eidFlow))
//                }
//            }
//
//            override fun onFailure(p0: ServiceErrorResponse?) {
//                Log.e(
//                    logTag,
//                    "Failure. ${p0?.errorDescription() ?: "n/a"}"
//                )
//                CoroutineScope(Dispatchers.IO).launch {
//                    _eidFlow.emit(EidInteractionEvent.Error(IdCardInteractionException.FrameworkError(p0?.errorDescription())))
//                }
//            }
//        })
//    }
//
//    fun cancelTask() {
//        CoroutineScope(Dispatchers.IO).launch {
//            _eidFlow.emit(EidInteractionEvent.Idle)
//        }
//
//        activationController?.cancelOngoingAuthentication()
//        androidContextManager?.terminateContext(stopHandler)
//        androidContextManager = null
//    }
//}

@Singleton
class IdCardManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val coroutineContextProvider: CoroutineContextProvider
){
    private val logger by getLogger()

    private val _eidFlow: MutableStateFlow<EidInteractionEvent> = MutableStateFlow(EidInteractionEvent.Idle)
    val eidFlow: Flow<EidInteractionEvent>
        get() = _eidFlow

    private val workflowControllerStarted: MutableStateFlow<Boolean> = MutableStateFlow(false)

    private val workflowCallbacks = object : WorkflowCallbacks {
        override fun onAccessRights(error: String?, accessRights: AccessRights?) {
            logger.trace("onAccessRights called")

            error?.let { logger.error(it) }
            if (accessRights == null) {
                logger.error("Access rights missing.")
                _eidFlow.value = EidInteractionEvent.Error(IdCardInteractionException.FrameworkError("Access rights missing. ${error ?: "n/a"}"))
                return
            }

            if (accessRights.effectiveRights == accessRights.requiredRights) {
                val authenticationRequest = AuthenticationRequest(accessRights.requiredRights, accessRights.transactionInfo)
                _eidFlow.value = EidInteractionEvent.AuthenticationRequestConfirmationRequested(authenticationRequest)
            } else {
                workflowController.setAccessRights(listOf())
            }
        }

        override fun onApiLevel(error: String?, apiLevel: ApiLevel?) {
            TODO("Not yet implemented")
        }

        override fun onAuthenticationCompleted(authResult: AuthResult) {
            TODO("Not yet implemented")
        }

        override fun onAuthenticationStartFailed(error: String) {
            _eidFlow.value = EidInteractionEvent.Error(IdCardInteractionException.FrameworkError(error))
        }

        override fun onAuthenticationStarted() {
            _eidFlow.value = EidInteractionEvent.AuthenticationStarted
        }

        override fun onBadState(error: String) {
            _eidFlow.value = EidInteractionEvent.Error(IdCardInteractionException.FrameworkError("Bad state: $error"))
        }

        override fun onCertificate(certificateDescription: CertificateDescription) {
            _eidFlow.value = EidInteractionEvent.CertificateDescriptionReceived(
                CertificateDescription(
                    certificateDescription.issuerName,
                    certificateDescription.issuerUrl,
                    certificateDescription.purpose,
                    certificateDescription.subjectName,
                    certificateDescription.subjectUrl,
                    certificateDescription.termsOfUsage,
                    certificateDescription.validity.effectiveDate,
                    certificateDescription.validity.expirationDate
                )
            )
        }

        override fun onChangePinCompleted(changePinResult: ChangePinResult) {
            if (changePinResult.success) {
                logger.debug("New PIN has been set successfully.")
                _eidFlow.value = EidInteractionEvent.PinChangeSucceeded
            } else {
                logger.error("Changing PIN failed.")
                _eidFlow.value = EidInteractionEvent.Error(IdCardInteractionException.ChangingPinFailed)
            }
        }

        override fun onChangePinStarted() {
            _eidFlow.value = EidInteractionEvent.PinChangeStarted
        }

        override fun onEnterCan(error: String?, reader: Reader) {
            error?.let { logger.error(it) }
            _eidFlow.value = EidInteractionEvent.CanRequested
        }

        override fun onEnterNewPin(error: String?, reader: Reader) {
            error?.let { logger.error(it) }
            _eidFlow.value = EidInteractionEvent.NewPinRequested(reader.card?.pinRetryCounter)
        }

        override fun onEnterPin(error: String?, reader: Reader) {
            error?.let { logger.error(it) }
            logger.debug("pin retry counter: ${reader.card?.pinRetryCounter}")
            _eidFlow.value = EidInteractionEvent.PinRequested(reader.card?.pinRetryCounter)
        }

        override fun onEnterPuk(error: String?, reader: Reader) {
            error?.let { logger.error(it) }
            _eidFlow.value = EidInteractionEvent.PukRequested
        }

        override fun onInfo(versionInfo: VersionInfo) {
            TODO("Not yet implemented")
        }

        override fun onInsertCard(error: String?) {
            error?.let { logger.error(it) }
            _eidFlow.value = EidInteractionEvent.CardInsertionRequested
        }

        override fun onInternalError(error: String) {
            logger.error(error)
            _eidFlow.value = EidInteractionEvent.Error(IdCardInteractionException.FrameworkError(error))
        }

        override fun onReader(reader: Reader?) {
            logger.trace("onReader")
            if (reader == null) {
                logger.error("Unknown reader.")
                _eidFlow.value = EidInteractionEvent.Error(IdCardInteractionException.UnknownReader)
                return
            }

            if (reader.card == null) {
                _eidFlow.value = EidInteractionEvent.CardRemoved
            } else {
                _eidFlow.value = EidInteractionEvent.CardRecognized
            }
        }

        override fun onReaderList(readers: List<Reader>?) {
            TODO("Not yet implemented")
        }

        override fun onStarted() {
            logger.trace("onStarted")
            workflowControllerStarted.value = true
        }

        override fun onStatus(workflowProgress: WorkflowProgress) {
            logger.trace("onStatus with state ${ workflowProgress.state } and progress ${ workflowProgress.progress }")
        }

        override fun onWrapperError(error: WrapperError) {
            logger.error("${error.error} - ${error.msg}")
            _eidFlow.value = EidInteractionEvent.Error(IdCardInteractionException.FrameworkError(error.msg))
        }
    }

    fun handleNfcTag(tag: Tag)  {
        if (!workflowController.isStarted) {
            logger.error("No task running.")
            return
        }

        workflowController.onNfcTagDetected(tag)
    }

    fun identify(context: Context, url: String) { }
    fun changePin(context: Context) {
        logger.debug("Starting workflow controller.")
        workflowController.registerCallbacks(workflowCallbacks)

        CoroutineScope(coroutineContextProvider.Default).launch {
            workflowControllerStarted.collect { started ->
                if (started) {
                    logger.debug("Start PIN management")
                    workflowController.startChangePin()
                    cancel()
                }
            }
        }

        workflowController.start(context)
    }

    fun cancelTask() {
        logger.debug("Stopping workflow controller.")
        workflowController.unregisterCallbacks(workflowCallbacks)
        workflowController.stop()
        workflowControllerStarted.value = false
        _eidFlow.value = EidInteractionEvent.Idle
    }

    fun providePin(pin: String) {
        if (!workflowController.isStarted) {
            logger.error("No task running.")
            return
        }

        workflowController.setPin(pin)
    }

    fun provideNewPin(newPin: String) {
        if (!workflowController.isStarted) {
            logger.error("No task running.")
            return
        }

        workflowController.setNewPin(newPin)
    }

    fun provideCan(can: String) {
        if (!workflowController.isStarted) {
            logger.error("No task running.")
            return
        }

        workflowController.setCan(can)
    }
}
