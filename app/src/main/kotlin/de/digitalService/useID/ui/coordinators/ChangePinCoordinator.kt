package de.digitalService.useID.ui.coordinators

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import de.digitalService.useID.flows.CanStateMachine
import de.digitalService.useID.flows.ChangePinStateMachine
import de.digitalService.useID.getLogger
import de.digitalService.useID.idCardInterface.EidInteractionEvent
import de.digitalService.useID.idCardInterface.EidInteractionException
import de.digitalService.useID.idCardInterface.EidInteractionManager
import de.digitalService.useID.ui.navigation.Navigator
import de.digitalService.useID.ui.screens.destinations.SetupCardBlockedDestination
import de.digitalService.useID.ui.screens.destinations.SetupCardDeactivatedDestination
import de.digitalService.useID.ui.screens.destinations.SetupCardUnreadableDestination
import de.digitalService.useID.ui.screens.destinations.SetupOtherErrorDestination
import de.digitalService.useID.ui.screens.destinations.SetupPersonalPinConfirmDestination
import de.digitalService.useID.ui.screens.destinations.SetupPersonalPinInputDestination
import de.digitalService.useID.ui.screens.destinations.SetupPersonalPinIntroDestination
import de.digitalService.useID.ui.screens.destinations.SetupScanDestination
import de.digitalService.useID.ui.screens.destinations.SetupTransportPinDestination
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
class ChangePinCoordinator @Inject constructor(
    @ApplicationContext private val context: Context,
    private val canCoordinator: CanCoordinator,
    private val navigator: Navigator,
    private val eidInteractionManager: EidInteractionManager,
    private val flowStateMachine: ChangePinStateMachine,
    private val canStateMachine: CanStateMachine,
    private val coroutineContextProvider: CoroutineContextProviderType
) {
    private val logger by getLogger()

    private var stateMachineCoroutineScope: Job? = null
    private var eIdEventFlowCoroutineScope: Job? = null
    private var canEventFlowCoroutineScope: Job? = null

    private val _scanInProgress: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val scanInProgress: StateFlow<Boolean>
        get() = _scanInProgress

    private val _stateFlow: MutableStateFlow<SubCoordinatorState> = MutableStateFlow(SubCoordinatorState.FINISHED)
    val stateFlow: StateFlow<SubCoordinatorState>
        get() = _stateFlow

    private fun collectStateMachineEvents() {
        if (stateMachineCoroutineScope != null) {
            return
        }

        stateMachineCoroutineScope = CoroutineScope(coroutineContextProvider.Default).launch {
            flowStateMachine.state.collect { eventAndPair ->
                if (eventAndPair.first is ChangePinStateMachine.Event.Back) {
                    navigator.pop()

                    // From Scan screen
                    if (eventAndPair.second is ChangePinStateMachine.State.NewPinInput) {
                        resetCoordinatorState()
                    }

                    // Backing down
                    if (eventAndPair.second is ChangePinStateMachine.State.Invalid) {
                        resetCoordinatorState()
                        _stateFlow.value = SubCoordinatorState.BACKED_DOWN
                    }
                } else {
                    when (val state = eventAndPair.second) {
                        is ChangePinStateMachine.State.OldTransportPinInput -> navigator.navigate(SetupTransportPinDestination(false, state.identificationPending))
                        is ChangePinStateMachine.State.OldPersonalPinInput -> throw NotImplementedError()
                        is ChangePinStateMachine.State.NewPinIntro -> navigator.navigate(SetupPersonalPinIntroDestination)
                        is ChangePinStateMachine.State.NewPinInput -> if (eventAndPair.first is ChangePinStateMachine.Event.RetryNewPinConfirmation) navigator.pop() else navigator.navigate(SetupPersonalPinInputDestination)
                        is ChangePinStateMachine.State.NewPinConfirmation -> navigator.navigate(SetupPersonalPinConfirmDestination)
                        is ChangePinStateMachine.State.StartIdCardInteraction -> {
                            executePinChange()
                            navigator.popUpToOrNavigate(SetupScanDestination(true, state.identificationPending), true)
                        }

                        is ChangePinStateMachine.State.ReadyForSubsequentScan -> {
                            eidInteractionManager.providePin(state.oldPin)
                            navigator.popUpToOrNavigate(SetupScanDestination(false, state.identificationPending), true)
                        }

                        is ChangePinStateMachine.State.FrameworkReadyForPinInput -> eidInteractionManager.providePin(state.oldPin)
                        is ChangePinStateMachine.State.FrameworkReadyForNewPinInput -> eidInteractionManager.provideNewPin(state.newPin)
                        is ChangePinStateMachine.State.CanRequested -> startCanFlow(state.identificationPending, state.oldPin, state.newPin, state.shortFlow)
                        is ChangePinStateMachine.State.OldTransportPinRetry -> navigator.navigate(SetupTransportPinDestination(true, state.identificationPending))
                        is ChangePinStateMachine.State.OldPersonalPinRetry -> throw NotImplementedError()
                        ChangePinStateMachine.State.Finished -> finishPinManagement()
                        ChangePinStateMachine.State.Cancelled -> cancelPinManagement()
                        ChangePinStateMachine.State.CardDeactivated -> navigator.navigate(SetupCardDeactivatedDestination)
                        ChangePinStateMachine.State.CardBlocked -> navigator.navigate(SetupCardBlockedDestination)
                        is ChangePinStateMachine.State.ProcessFailed -> navigator.navigate(SetupCardUnreadableDestination(false))
                        ChangePinStateMachine.State.UnknownError -> navigator.navigate(SetupOtherErrorDestination)
                        ChangePinStateMachine.State.Invalid -> logger.debug("Ignoring transition to state INVALID.")
                    }
                }
            }
        }
    }

    fun startPinChange(identificationPending: Boolean, transportPin: Boolean): Flow<SubCoordinatorState> {
        collectStateMachineEvents()

        canStateMachine.transition(CanStateMachine.Event.Invalidate)
        flowStateMachine.transition(ChangePinStateMachine.Event.StartPinChange(identificationPending, transportPin))
        _stateFlow.value = SubCoordinatorState.ACTIVE
        return stateFlow
    }

    fun onOldPinEntered(oldPin: String) {
        flowStateMachine.transition(ChangePinStateMachine.Event.EnterOldPin(oldPin))
    }

    fun onPersonalPinIntroFinished() {
        flowStateMachine.transition(ChangePinStateMachine.Event.ConfirmNewPinIntro)
    }

    fun onNewPinEntered(newPin: String) {
        flowStateMachine.transition(ChangePinStateMachine.Event.EnterNewPin(newPin))
    }

    fun confirmNewPin(newPin: String): Boolean {
        return try {
            flowStateMachine.transition(ChangePinStateMachine.Event.ConfirmNewPin(newPin))
            true
        } catch (e: ChangePinStateMachine.Error.PinConfirmationFailed) {
            false
        }
    }

    fun onConfirmPinMismatchError() {
        flowStateMachine.transition(ChangePinStateMachine.Event.RetryNewPinConfirmation)
    }

    fun onBack() {
        flowStateMachine.transition(ChangePinStateMachine.Event.Back)
    }

    fun cancelPinManagement() {
        _stateFlow.value = SubCoordinatorState.CANCELLED
        flowStateMachine.transition(ChangePinStateMachine.Event.Invalidate)
        resetCoordinatorState()
    }

    fun confirmCardUnreadableError() {
        flowStateMachine.transition(ChangePinStateMachine.Event.ProceedAfterError)
    }

    private fun startCanFlow(identificationPending: Boolean, oldPin: String, newPin: String, shortFlow: Boolean) {
        if (canCoordinator.stateFlow.value != SubCoordinatorState.ACTIVE) {
            canEventFlowCoroutineScope = CoroutineScope(coroutineContextProvider.IO).launch {
                canCoordinator.startPinChangeCanFlow(identificationPending, oldPin, newPin, shortFlow).collect { state ->
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
        flowStateMachine.transition(ChangePinStateMachine.Event.Invalidate)
        resetCoordinatorState()
    }

    private fun finishPinManagement() {
        _stateFlow.value = SubCoordinatorState.FINISHED
        flowStateMachine.transition(ChangePinStateMachine.Event.Invalidate)
        resetCoordinatorState()
    }

    private fun resetCoordinatorState() {
        _scanInProgress.value = false
        eIdEventFlowCoroutineScope?.cancel()
        canEventFlowCoroutineScope?.cancel()
        eidInteractionManager.cancelTask()
    }

    private fun executePinChange() {
        eIdEventFlowCoroutineScope?.cancel()
        eidInteractionManager.cancelTask()

        eIdEventFlowCoroutineScope = CoroutineScope(coroutineContextProvider.IO).launch {
            eidInteractionManager.eidFlow.catch { exception ->
                logger.error("Error: $exception")
            }.collect { event ->
                when (event) {
                    EidInteractionEvent.CardInsertionRequested -> {
                        logger.debug("Card insertion requested.")
                    }

                    EidInteractionEvent.PinChangeStarted -> {
                        logger.debug("PIN management started.")
                    }

                    EidInteractionEvent.CardRecognized -> {
                        logger.debug("Card recognized.")
                        _scanInProgress.value = true
                    }

                    EidInteractionEvent.CardRemoved -> {
                        logger.debug("Card removed.")
                        _scanInProgress.value = false
                    }

                    EidInteractionEvent.PinChangeSucceeded -> {
                        logger.debug("Process completed successfully.")
                        flowStateMachine.transition(ChangePinStateMachine.Event.Finish)
                    }

                    is EidInteractionEvent.PinRequested -> {
                        logger.debug("Request PIN.")
                        if (canCoordinator.stateFlow.value == SubCoordinatorState.ACTIVE) {
                            logger.debug("Ignoring event because CAN flow is active.")
                            return@collect
                        }
                        flowStateMachine.transition(ChangePinStateMachine.Event.FrameworkRequestsPin)
                    }

                    is EidInteractionEvent.NewPinRequested -> {
                        logger.debug("Request new PIN.")
                        if (canCoordinator.stateFlow.value == SubCoordinatorState.ACTIVE) {
                            logger.debug("Ignoring event because CAN flow is active.")
                            return@collect
                        }
                        flowStateMachine.transition(ChangePinStateMachine.Event.FrameworkRequestsNewPin)
                    }

                    is EidInteractionEvent.CanRequested -> {
                        logger.debug("CAN requested.")
                        if (canCoordinator.stateFlow.value == SubCoordinatorState.ACTIVE) {
                            logger.debug("Ignoring event because CAN flow is active.")
                            return@collect
                        }
                        flowStateMachine.transition(ChangePinStateMachine.Event.FrameworkRequestsCan)
                    }

                    is EidInteractionEvent.PukRequested -> {
                        _scanInProgress.value = false
                        flowStateMachine.transition(ChangePinStateMachine.Event.Error(EidInteractionException.CardBlocked))
                    }

                    is EidInteractionEvent.Error -> {
                        _scanInProgress.value = false
                        flowStateMachine.transition(ChangePinStateMachine.Event.Error(event.exception))
                    }

                    else -> logger.debug("Ignoring event: $event")
                }
            }
        }

        eidInteractionManager.changePin(context)
    }
}
