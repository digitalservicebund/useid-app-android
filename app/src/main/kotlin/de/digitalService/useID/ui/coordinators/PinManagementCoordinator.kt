package de.digitalService.useID.ui.coordinators

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import de.digitalService.useID.flows.CanStateMachine
import de.digitalService.useID.flows.PinManagementStateMachine
import de.digitalService.useID.getLogger
import de.digitalService.useID.idCardInterface.EidInteractionEvent
import de.digitalService.useID.idCardInterface.IdCardInteractionException
import de.digitalService.useID.idCardInterface.IdCardManager
import de.digitalService.useID.ui.navigation.Navigator
import de.digitalService.useID.ui.screens.destinations.*
import de.digitalService.useID.util.CoroutineContextProviderType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PinManagementCoordinator @Inject constructor(
    @ApplicationContext private val context: Context,
    private val canCoordinator: CanCoordinator,
    private val navigator: Navigator,
    private val idCardManager: IdCardManager,
    private val flowStateMachine: PinManagementStateMachine,
    private val canStateMachine: CanStateMachine,
    private val coroutineContextProvider: CoroutineContextProviderType
) {
    private val logger by getLogger()

    private var eIdEventFlowCoroutineScope: Job? = null
    private var canEventFlowCoroutineScope: Job? = null

    private val _scanInProgress: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val scanInProgress: StateFlow<Boolean>
        get() = _scanInProgress

    private val _stateFlow: MutableStateFlow<SubCoordinatorState> = MutableStateFlow(SubCoordinatorState.FINISHED)
    val stateFlow: StateFlow<SubCoordinatorState>
        get() = _stateFlow

    init {
        CoroutineScope(coroutineContextProvider.Default).launch {
            flowStateMachine.state.collect { eventAndPair ->
                if (eventAndPair.first is PinManagementStateMachine.Event.Back) {
                    navigator.pop()

                    // From Scan screen
                    if (eventAndPair.second is PinManagementStateMachine.State.NewPinInput) {
                        resetCoordinatorState()
                    }

                    // Backing down
                    if (eventAndPair.second is PinManagementStateMachine.State.Invalid) {
                        resetCoordinatorState()
                        _stateFlow.value = SubCoordinatorState.BACKED_DOWN
                    }
                } else {
                    when (val state = eventAndPair.second) {
                        is PinManagementStateMachine.State.OldTransportPinInput -> navigator.navigate(SetupTransportPinDestination(false, state.identificationPending))
                        is PinManagementStateMachine.State.OldPersonalPinInput -> throw NotImplementedError()
                        is PinManagementStateMachine.State.NewPinIntro -> navigator.navigate(SetupPersonalPinIntroDestination)
                        is PinManagementStateMachine.State.NewPinInput -> if (eventAndPair.first is PinManagementStateMachine.Event.RetryNewPinConfirmation) navigator.pop() else navigator.navigate(SetupPersonalPinInputDestination)
                        is PinManagementStateMachine.State.NewPinConfirmation -> navigator.navigate(SetupPersonalPinConfirmDestination)
                        is PinManagementStateMachine.State.ReadyForScan -> executePinManagement()
                        is PinManagementStateMachine.State.WaitingForFirstCardAttachment -> navigator.popUpToOrNavigate(SetupScanDestination(true, state.identificationPending), true)
                        is PinManagementStateMachine.State.WaitingForCardReAttachment -> navigator.popUpToOrNavigate(SetupScanDestination(false, state.identificationPending), true)
                        is PinManagementStateMachine.State.FrameworkReadyForPinManagement -> state.callback(state.oldPin, state.newPin)
                        is PinManagementStateMachine.State.CanRequested -> startCanFlow(state.identificationPending, state.oldPin, state.newPin, state.shortFlow)
                        is PinManagementStateMachine.State.OldTransportPinRetry -> navigator.navigate(SetupTransportPinDestination(true, state.identificationPending))
                        is PinManagementStateMachine.State.OldPersonalPinRetry -> throw NotImplementedError()
                        PinManagementStateMachine.State.Finished -> finishPinManagement()
                        PinManagementStateMachine.State.Cancelled -> cancelPinManagement()
                        PinManagementStateMachine.State.CardDeactivated -> navigator.navigate(SetupCardDeactivatedDestination)
                        PinManagementStateMachine.State.CardBlocked -> navigator.navigate(SetupCardBlockedDestination)
                        is PinManagementStateMachine.State.ProcessFailed -> navigator.navigate(SetupCardUnreadableDestination(false))
                        PinManagementStateMachine.State.UnknownError -> navigator.navigate(SetupOtherErrorDestination)
                        PinManagementStateMachine.State.Invalid -> logger.debug("Ignoring transition to state INVALID.")
                    }
                }
            }
        }
    }

    fun startPinManagement(identificationPending: Boolean, transportPin: Boolean): Flow<SubCoordinatorState> {
        canStateMachine.transition(CanStateMachine.Event.Invalidate)
        flowStateMachine.transition(PinManagementStateMachine.Event.StartPinManagement(identificationPending, transportPin))
        _stateFlow.value = SubCoordinatorState.ACTIVE
        return stateFlow
    }

    fun onOldPinEntered(oldPin: String) {
        flowStateMachine.transition(PinManagementStateMachine.Event.EnterOldPin(oldPin))
    }

    fun onPersonalPinIntroFinished() {
        flowStateMachine.transition(PinManagementStateMachine.Event.ConfirmNewPinIntro)
    }

    fun onNewPinEntered(newPin: String) {
        flowStateMachine.transition(PinManagementStateMachine.Event.EnterNewPin(newPin))
    }

    fun confirmNewPin(newPin: String): Boolean {
        return try {
            flowStateMachine.transition(PinManagementStateMachine.Event.ConfirmNewPin(newPin))
            true
        } catch (e: PinManagementStateMachine.Error.PinConfirmationFailed) {
            false
        }
    }

    fun onConfirmPinMismatchError() {
        flowStateMachine.transition(PinManagementStateMachine.Event.RetryNewPinConfirmation)
    }

    fun onBack() {
        flowStateMachine.transition(PinManagementStateMachine.Event.Back)
    }

    fun cancelPinManagement() {
        _stateFlow.value = SubCoordinatorState.CANCELLED
        flowStateMachine.transition(PinManagementStateMachine.Event.Invalidate)
        resetCoordinatorState()
    }

    fun confirmCardUnreadableError() {
        flowStateMachine.transition(PinManagementStateMachine.Event.ProceedAfterError)
    }

    private fun startCanFlow(identificationPending: Boolean, oldPin: String, newPin: String, shortFlow: Boolean) {
        if (canCoordinator.stateFlow.value != SubCoordinatorState.ACTIVE) {
            canEventFlowCoroutineScope = CoroutineScope(coroutineContextProvider.IO).launch {
                canCoordinator.startPinManagementCanFlow(identificationPending, oldPin, newPin, shortFlow).collect { state ->
                    when (state) {
                        SubCoordinatorState.CANCELLED -> cancelPinManagement()
                        SubCoordinatorState.SKIPPED -> skipPinManagement()
                        else -> logger.debug("Ignoring sub flow state: $state")
                    }
                }
            }
        } else {
            logger.debug("Don't start CAN flow as it is already active.")
        }
    }

    private fun skipPinManagement() {
        _stateFlow.value = SubCoordinatorState.SKIPPED
        flowStateMachine.transition(PinManagementStateMachine.Event.Invalidate)
        resetCoordinatorState()
    }

    private fun finishPinManagement() {
        _stateFlow.value = SubCoordinatorState.FINISHED
        flowStateMachine.transition(PinManagementStateMachine.Event.Invalidate)
        resetCoordinatorState()
    }

    private fun resetCoordinatorState() {
        _scanInProgress.value = false
        eIdEventFlowCoroutineScope?.cancel()
        canEventFlowCoroutineScope?.cancel()
        idCardManager.cancelTask()
    }

    private fun executePinManagement() {
        eIdEventFlowCoroutineScope?.cancel()
        idCardManager.cancelTask()

        eIdEventFlowCoroutineScope = CoroutineScope(coroutineContextProvider.IO).launch {
            idCardManager.eidFlow.catch { exception ->
                logger.error("Error: $exception")
            }.collect { event ->
                when (event) {
                    EidInteractionEvent.RequestCardInsertion -> {
                        logger.debug("Card insertion requested.")
                        flowStateMachine.transition(PinManagementStateMachine.Event.RequestCardInsertion)
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
                    EidInteractionEvent.CardInteractionComplete -> {
                        logger.debug("Card interaction complete.")
                    }
                    EidInteractionEvent.ProcessCompletedSuccessfullyWithoutResult -> {
                        logger.debug("Process completed successfully.")
                        flowStateMachine.transition(PinManagementStateMachine.Event.Finish)
                    }
                    is EidInteractionEvent.RequestChangedPin -> {
                        logger.debug("Request changed PIN.")
                        flowStateMachine.transition(PinManagementStateMachine.Event.FrameworkRequestsChangedPin(event.pinCallback))
                    }
                    is EidInteractionEvent.RequestCanAndChangedPin -> {
                        logger.debug("PIN and CAN requested.")
                        flowStateMachine.transition(PinManagementStateMachine.Event.FrameworkRequestsCan)
                    }
                    is EidInteractionEvent.RequestPuk -> {
                        _scanInProgress.value = false
                        flowStateMachine.transition(PinManagementStateMachine.Event.Error(IdCardInteractionException.CardBlocked))
                    }
                    is EidInteractionEvent.Error -> {
                        _scanInProgress.value = false
                        flowStateMachine.transition(PinManagementStateMachine.Event.Error(event.exception))
                    }
                    else -> logger.debug("Ignoring event: $event")
                }
            }
        }

        idCardManager.changePin(context)
    }
}
