package de.digitalService.useID.ui.coordinators

import android.content.Context
import com.ramcosta.composedestinations.spec.Direction
import dagger.hilt.android.qualifiers.ApplicationContext
import de.digitalService.useID.analytics.IssueTrackerManagerType
import de.digitalService.useID.getLogger
import de.digitalService.useID.idCardInterface.EidInteractionEvent
import de.digitalService.useID.idCardInterface.IdCardInteractionException
import de.digitalService.useID.idCardInterface.IdCardManager
import de.digitalService.useID.ui.navigation.Navigator
import de.digitalService.useID.ui.screens.destinations.*
import de.digitalService.useID.util.CoroutineContextProviderType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

enum class PinStatus {
    TransportPin, PersonalPin
}

@Singleton
class PinManagementCoordinator @Inject constructor(
    @ApplicationContext private val context: Context,
    private val navigator: Navigator,
    private val idCardManager: IdCardManager,
    private val issueTrackerManager: IssueTrackerManagerType,
    private val coroutineContextProvider: CoroutineContextProviderType
) {
    private val logger by getLogger()

    private lateinit var pinStatus: PinStatus
    private var oldPin: String? = null
    private var newPin: String? = null

    private var firstOldPinRequest = true

    private var eIdEventFlowCoroutineScope: Job? = null

    private val _scanInProgress: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val scanInProgress: Flow<Boolean>
        get() = _scanInProgress

    private val stateFlow: MutableStateFlow<SubCoordinatorState> = MutableStateFlow(SubCoordinatorState.Idle)

    fun startPinManagement(pinStatus: PinStatus): Flow<SubCoordinatorState> {
        oldPin = null
        newPin = null
        firstOldPinRequest = true
        eIdEventFlowCoroutineScope?.cancel()

        this.pinStatus = pinStatus

        when (pinStatus) {
            PinStatus.TransportPin -> navigator.navigate(SetupTransportPinDestination(false))
            PinStatus.PersonalPin -> throw NotImplementedError("Pin Management for personal PIN not implemented yet.")
        }

        stateFlow.value = SubCoordinatorState.Active
        return stateFlow
    }

    fun setOldPin(oldPin: String) {
        this.oldPin = oldPin

        if (newPin == null) {
            navigator.navigate(SetupPersonalPinIntroDestination)
        } else {
            executePinManagement()
        }
    }

    fun onPersonalPinIntroFinished() {
        navigator.navigate(SetupPersonalPinInputDestination)
    }

    fun setNewPin(newPin: String) {
        this.newPin = newPin
        navigator.navigate(SetupPersonalPinConfirmDestination)
    }

    fun confirmNewPin(newPin: String): Boolean {
        return if (newPin == this.newPin) {
            executePinManagement()
            true
        } else {
            this.newPin = null
            false
        }
    }

    fun onConfirmPinMismatchError() {
        navigator.pop()
    }

    fun retryPinManagement() {
        executePinManagement()
    }

    fun cancelIdCardManagerTasks() {
        eIdEventFlowCoroutineScope?.cancel()
        idCardManager.cancelTask()
    }

    fun onBack() {
        navigator.pop()
        cancelIdCardManagerTasks()
    }

    private fun cancelPinManagementAndNavigate(destination: Direction) {
        _scanInProgress.value = false
        navigator.navigate(destination)
        cancelIdCardManagerTasks()
        stateFlow.value = SubCoordinatorState.Cancelled
        stateFlow.value = SubCoordinatorState.Idle
    }

    private fun executePinManagement() {
        collectEidEvents()
        idCardManager.changePin(context)
    }

    private fun collectEidEvents() {
        eIdEventFlowCoroutineScope?.cancel()
        eIdEventFlowCoroutineScope = CoroutineScope(coroutineContextProvider.IO).launch {
            idCardManager.eidFlow.catch { exception ->
                logger.error("Error: $exception")
            }.collect { event ->
                when (event) {
                    EidInteractionEvent.RequestCardInsertion -> {
                        logger.debug("Card insertion requested.")
                        navigator.navigatePopping(SetupScanDestination)
                    }
                    EidInteractionEvent.PinManagementStarted -> logger.debug("PIN management started.")
                    EidInteractionEvent.CardRecognized -> {
                        logger.debug("Card recognized.")
                        _scanInProgress.value = true
                    }
                    EidInteractionEvent.CardRemoved -> {
                        logger.debug("Card removed.")
                        _scanInProgress.value = false
                    }
                    EidInteractionEvent.ProcessCompletedSuccessfullyWithoutResult -> {
                        logger.debug("Process completed successfully.")
                        _scanInProgress.value = false
                        stateFlow.emit(SubCoordinatorState.Finished)
                    }
                    is EidInteractionEvent.RequestChangedPin -> {
                        if (firstOldPinRequest) {
                            logger.debug("Changed PIN requested for the first time. Entering old PIN and new PIN")
                            firstOldPinRequest = false

                            val oldPin = oldPin
                            val newPin = newPin

                            if (oldPin == null || newPin == null) {
                                logger.error("Required PIN values not available for PIN management.")
                                idCardManager.cancelTask()
                                cancel()
                                return@collect
                            }

                            event.pinCallback(oldPin, newPin)
                        } else {
                            logger.debug("Old and new PIN requested for a second time. The old PIN seems to be incorrect.")
                            _scanInProgress.value = false
                            navigator.navigate(SetupTransportPinDestination(true))
                            cancelIdCardManagerTasks()
                            firstOldPinRequest = true
                        }
                    }
                    is EidInteractionEvent.RequestCanAndChangedPin -> cancelPinManagementAndNavigate(SetupCardSuspendedDestination)
                    is EidInteractionEvent.RequestPUK -> cancelPinManagementAndNavigate(SetupCardBlockedDestination)
                    is EidInteractionEvent.AuthenticationSuccessful -> cancelPinManagementAndNavigate(SetupOtherErrorDestination)
                    is EidInteractionEvent.Error -> handleEidInteractionEventError(event.exception)
                    else -> logger.debug("Ignoring event: $event")
                }
            }
        }
    }

    private fun handleEidInteractionEventError(exception: IdCardInteractionException) {
        logger.debug("Received exception: $exception")

        when (exception) {
            is IdCardInteractionException.CardDeactivated -> cancelPinManagementAndNavigate(SetupCardDeactivatedDestination)
            is IdCardInteractionException.CardBlocked -> cancelPinManagementAndNavigate(SetupCardBlockedDestination)
            is IdCardInteractionException.ProcessFailed -> {
                (exception as? IdCardInteractionException)?.redacted?.let {
                    issueTrackerManager.capture(it)
                }
                _scanInProgress.value = false
                navigator.navigate(SetupCardUnreadableDestination(false))
                cancelIdCardManagerTasks()
            }
            else -> {
                (exception as? IdCardInteractionException)?.redacted?.let {
                    issueTrackerManager.capture(it)
                }
                cancelPinManagementAndNavigate(SetupOtherErrorDestination)
            }
        }
    }
}
