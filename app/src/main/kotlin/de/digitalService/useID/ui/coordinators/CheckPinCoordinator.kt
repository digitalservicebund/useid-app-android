package de.digitalService.useID.ui.coordinators

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import de.digitalService.useID.flows.CanStateMachine
import de.digitalService.useID.flows.CheckPinStateMachine
import de.digitalService.useID.getLogger
import de.digitalService.useID.idCardInterface.EidInteractionEvent
import de.digitalService.useID.idCardInterface.EidInteractionManager
import de.digitalService.useID.idCardInterface.EidInteractionException
import de.digitalService.useID.ui.navigation.Navigator
import de.digitalService.useID.ui.screens.destinations.CheckCardDeactivatedDestination
import de.digitalService.useID.ui.screens.destinations.CheckPersonalPinDestination
import de.digitalService.useID.ui.screens.destinations.CheckResetPersonalPinDestination
import de.digitalService.useID.ui.screens.destinations.CheckScanDestination
import de.digitalService.useID.ui.screens.destinations.CheckSuccessDestination
import de.digitalService.useID.ui.screens.destinations.HomeScreenDestination
import de.digitalService.useID.ui.screens.destinations.ScanSuccessDestination
import de.digitalService.useID.ui.screens.destinations.SetupCardBlockedDestination
import de.digitalService.useID.ui.screens.destinations.SetupCardUnreadableDestination
import de.digitalService.useID.ui.screens.destinations.SetupOtherErrorDestination
import de.digitalService.useID.ui.screens.destinations.WebViewScreenDestination
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
class CheckPinCoordinator @Inject constructor(
    @ApplicationContext private val context: Context,
    private val canCoordinator: CanCoordinator,
    private val navigator: Navigator,
    private val eidInteractionManager: EidInteractionManager,
    private val flowStateMachine: CheckPinStateMachine,
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
                if (eventAndPair.first is CheckPinStateMachine.Event.Back) {
                    navigator.pop()

                    // From Scan screen
                    if (eventAndPair.second is CheckPinStateMachine.State.PinInput) {
                        resetCoordinatorState()
                    }

                    // Backing down
                    if (eventAndPair.second is CheckPinStateMachine.State.Invalid) {
                        resetCoordinatorState()
                        _stateFlow.value = SubCoordinatorState.BACKED_DOWN
                    }
                } else {
                    when (val state = eventAndPair.second) {
                        is CheckPinStateMachine.State.StartIdCardInteraction -> {
                            executePinChange()
                            navigator.navigate(CheckScanDestination(true))
                        }

                        CheckPinStateMachine.State.ScanSuccess -> navigator.popUpToOrNavigate(ScanSuccessDestination, true)
                        CheckPinStateMachine.State.PinInput -> navigator.popUpToOrNavigate(CheckPersonalPinDestination, true)
                        is CheckPinStateMachine.State.ReadyForSubsequentScan -> {
                            eidInteractionManager.providePin(state.pin)
                            navigator.popUpToOrNavigate(CheckScanDestination(false), true)
                        }

                        is CheckPinStateMachine.State.FrameworkReadyForPinInput -> eidInteractionManager.providePin(state.pin)
                        is CheckPinStateMachine.State.FrameworkReadyForNewPinInput -> eidInteractionManager.provideNewPin(state.pin)
                        CheckPinStateMachine.State.PinRetry -> navigator.popUpToOrNavigate(CheckPersonalPinDestination, true)
                        is CheckPinStateMachine.State.CanRequested -> startCanFlow(state.pin, state.pin, state.shortFlow)
                        CheckPinStateMachine.State.Success -> navigator.navigate(CheckSuccessDestination)
                        CheckPinStateMachine.State.Finished -> {
//                            navigator.popUpToOrNavigate(HomeScreenDestination, true)
                            finishPinManagement()
                        }

                        CheckPinStateMachine.State.Cancelled -> cancelPinCheck()
                        CheckPinStateMachine.State.CardDeactivated -> navigator.navigate(CheckCardDeactivatedDestination)
                        CheckPinStateMachine.State.CardBlocked -> navigator.navigate(SetupCardBlockedDestination)
                        is CheckPinStateMachine.State.ProcessFailed -> navigator.navigate(SetupCardUnreadableDestination(false))
                        CheckPinStateMachine.State.UnknownError -> navigator.navigate(SetupOtherErrorDestination)
                        CheckPinStateMachine.State.Invalid -> logger.debug("Ignoring transition to state INVALID.")
                    }
                }
            }
        }
    }

    fun startPinCheck(): Flow<SubCoordinatorState> {
        collectStateMachineEvents()

        canStateMachine.transition(CanStateMachine.Event.Invalidate)
        flowStateMachine.transition(CheckPinStateMachine.Event.StartPinCheck)
        _stateFlow.value = SubCoordinatorState.ACTIVE
        return stateFlow
    }

    fun onPinEntered(oldPin: String) {
        flowStateMachine.transition(CheckPinStateMachine.Event.PinEntered(oldPin))
    }

    fun onContinue() {
        flowStateMachine.transition(CheckPinStateMachine.Event.EnterPin)
    }

    fun onFinish() {
        flowStateMachine.transition(CheckPinStateMachine.Event.Finish)
        navigator.popUpToOrNavigate(HomeScreenDestination, true)
    }

    fun selbstauskunft() {
        flowStateMachine.transition(CheckPinStateMachine.Event.Finish)
        navigator.navigate(WebViewScreenDestination("https://demo.useid.dev.ds4g.net/?view=app"))
    }

    fun flensburg() {
        flowStateMachine.transition(CheckPinStateMachine.Event.Finish)
        navigator.navigate(WebViewScreenDestination("https://www.kba-online.de/registerauskunft/ora/web/?#/faer"))
    }

    fun rente() {
        flowStateMachine.transition(CheckPinStateMachine.Event.Finish)
        navigator.navigate(WebViewScreenDestination("https://www.eservice-drv.de/OnlineDiensteWeb/init.do?npa=true#"))
    }

    fun onBack() {
        flowStateMachine.transition(CheckPinStateMachine.Event.Finish)
        flowStateMachine.transition(CheckPinStateMachine.Event.Back)
    }

    fun cancelPinCheck() {
        navigator.popToRoot()
        _stateFlow.value = SubCoordinatorState.CANCELLED
        flowStateMachine.transition(CheckPinStateMachine.Event.Invalidate)
        resetCoordinatorState()
    }

    fun goToPinbrief() {
        navigator.navigate(CheckResetPersonalPinDestination)
    }

    private fun startCanFlow(oldPin: String, newPin: String, shortFlow: Boolean) {
        if (canCoordinator.stateFlow.value != SubCoordinatorState.ACTIVE) {
            canEventFlowCoroutineScope = CoroutineScope(coroutineContextProvider.IO).launch {
                canCoordinator.startPinChangeCanFlow(false, oldPin, newPin, shortFlow).collect { state ->
                    when (state) {
                        SubCoordinatorState.CANCELLED -> cancelPinCheck()
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
        flowStateMachine.transition(CheckPinStateMachine.Event.Invalidate)
        resetCoordinatorState()
    }

    private fun finishPinManagement() {
        _stateFlow.value = SubCoordinatorState.FINISHED
        flowStateMachine.transition(CheckPinStateMachine.Event.Invalidate)
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

                    EidInteractionEvent.CardDeactivated -> {
                        logger.debug("Card deactivated.")
                        flowStateMachine.transition(CheckPinStateMachine.Event.CardDeactivated)
                    }

                    EidInteractionEvent.PinChangeSucceeded -> {
                        logger.debug("Process completed successfully.")
                        flowStateMachine.transition(CheckPinStateMachine.Event.Success)
                    }

                    is EidInteractionEvent.PinRequested -> {
                        logger.debug("Request PIN.")
                        if (canCoordinator.stateFlow.value == SubCoordinatorState.ACTIVE) {
                            logger.debug("Ignoring event because CAN flow is active.")
                            return@collect
                        }
                        flowStateMachine.transition(CheckPinStateMachine.Event.FrameworkRequestsPin)
                    }

                    is EidInteractionEvent.NewPinRequested -> {
                        logger.debug("Request new PIN.")
                        if (canCoordinator.stateFlow.value == SubCoordinatorState.ACTIVE) {
                            logger.debug("Ignoring event because CAN flow is active.")
                            return@collect
                        }
                        flowStateMachine.transition(CheckPinStateMachine.Event.FrameworkRequestsNewPin)
                    }

                    is EidInteractionEvent.CanRequested -> {
                        logger.debug("CAN requested.")
                        if (canCoordinator.stateFlow.value == SubCoordinatorState.ACTIVE) {
                            logger.debug("Ignoring event because CAN flow is active.")
                            return@collect
                        }
                        flowStateMachine.transition(CheckPinStateMachine.Event.FrameworkRequestsCan)
                    }

                    is EidInteractionEvent.PukRequested -> {
                        _scanInProgress.value = false
                        flowStateMachine.transition(CheckPinStateMachine.Event.Error(EidInteractionException.CardBlocked))
                    }

                    is EidInteractionEvent.Error -> {
                        _scanInProgress.value = false
                        flowStateMachine.transition(CheckPinStateMachine.Event.Error(event.exception))
                    }

                    else -> logger.debug("Ignoring event: $event")
                }
            }
        }

        eidInteractionManager.changePin(context)
    }
}
