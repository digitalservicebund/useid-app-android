package de.digitalService.useID.ui.coordinators

import de.digitalService.useID.flows.CanStateMachine
import de.digitalService.useID.getLogger
import de.digitalService.useID.idCardInterface.EidInteractionEvent
import de.digitalService.useID.idCardInterface.EidInteractionManager
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
class CanCoordinator @Inject constructor(
    private val navigator: Navigator,
    private val eidInteractionManager: EidInteractionManager,
    private val flowStateMachine: CanStateMachine,
    private val coroutineContextProvider: CoroutineContextProviderType
) {
    private val logger by getLogger()

    private var stateMachineCoroutineScope: Job? = null
    private var eIdEventFlowCoroutineScope: Job? = null

    private val _stateFlow: MutableStateFlow<SubCoordinatorState> = MutableStateFlow(SubCoordinatorState.FINISHED)
    val stateFlow: StateFlow<SubCoordinatorState>
        get() = _stateFlow

    private fun collectStateMachineEvents() {
        if (stateMachineCoroutineScope != null) {
            return
        }
        stateMachineCoroutineScope = CoroutineScope(coroutineContextProvider.Default).launch {
            flowStateMachine.state.collect { eventAndPair ->
                if (eventAndPair.first is CanStateMachine.Event.Back) {
                    navigator.pop()
                } else {
                    when (val state = eventAndPair.second) {
                        is CanStateMachine.State.ChangePin.Intro -> navigator.navigate(SetupCanConfirmTransportPinDestination(state.oldPin, state.identificationPending))
                        is CanStateMachine.State.ChangePin.IdAlreadySetup -> navigator.navigate(SetupCanAlreadySetupDestination(state.identificationPending))
                        is CanStateMachine.State.ChangePin.PinReset, is CanStateMachine.State.Ident.PinReset -> navigator.navigate(CanResetPersonalPinDestination)
                        is CanStateMachine.State.ChangePin.CanIntro -> navigator.navigate(SetupCanIntroDestination(!state.shortFlow, state.identificationPending))
                        is CanStateMachine.State.ChangePin.CanInput -> navigator.navigate(CanInputDestination(false))
                        is CanStateMachine.State.ChangePin.CanInputRetry -> {
                            navigator.navigate(SetupCanIntroDestination(false, state.identificationPending))
                            navigator.navigate(CanInputDestination(true))
                        }
                        is CanStateMachine.State.ChangePin.PinInput -> navigator.navigate(SetupCanTransportPinDestination(state.identificationPending))
                        is CanStateMachine.State.ChangePin.CanAndPinEntered -> {
                            navigator.popUpToOrNavigate(SetupScanDestination(false, state.identificationPending), true)
                            eidInteractionManager.provideCan(state.can)
                        }
                        is CanStateMachine.State.ChangePin.FrameworkReadyForPinInput -> eidInteractionManager.providePin(state.pin)
                        is CanStateMachine.State.ChangePin.FrameworkReadyForNewPinInput -> eidInteractionManager.provideNewPin(state.newPin)

                        is CanStateMachine.State.Ident.Intro -> navigator.navigate(IdentificationCanPinForgottenDestination)
                        is CanStateMachine.State.Ident.CanIntro -> navigator.navigate(IdentificationCanIntroDestination(!state.shortFlow))
                        is CanStateMachine.State.Ident.CanInput -> navigator.navigate(CanInputDestination(false))
                        is CanStateMachine.State.Ident.CanInputRetry -> {
                            navigator.navigate(IdentificationCanPinForgottenDestination)
                            navigator.navigate(IdentificationCanIntroDestination(!state.shortFlow))
                            navigator.navigate(CanInputDestination(true))
                        }
                        is CanStateMachine.State.Ident.PinInput -> navigator.navigate(IdentificationCanPinInputDestination)
                        is CanStateMachine.State.Ident.CanAndPinEntered -> {
                            navigator.popUpToOrNavigate(IdentificationScanDestination, true)
                            eidInteractionManager.provideCan(state.can)
                        }
                        is CanStateMachine.State.Ident.FrameworkReadyForPinInput -> eidInteractionManager.providePin(state.pin)
                        CanStateMachine.State.Invalid -> logger.debug("Ignoring transition to state INVALID.")
                    }
                }
            }
        }
    }

    fun startPinChangeCanFlow(identificationPending: Boolean, oldPin: String, newPin: String, shortFlow: Boolean): Flow<SubCoordinatorState> {
        collectStateMachineEvents()

        _stateFlow.value = SubCoordinatorState.ACTIVE
        handleEidEventsForPinChange(identificationPending, oldPin, newPin, shortFlow)
        return stateFlow
    }

    fun startIdentCanFlow(pin: String?): Flow<SubCoordinatorState> {
        collectStateMachineEvents()

        _stateFlow.value = SubCoordinatorState.ACTIVE
        handleEidEventsForIdent(pin)
        return stateFlow
    }

    fun onResetPin() {
        flowStateMachine.transition(CanStateMachine.Event.ResetPin)
    }

    fun onConfirmedPinInput() {
        flowStateMachine.transition(CanStateMachine.Event.DenyThirdAttempt)
    }

    fun proceedWithThirdAttempt() {
        flowStateMachine.transition(CanStateMachine.Event.AgreeToThirdAttempt)
    }

    fun finishIntro() {
        flowStateMachine.transition(CanStateMachine.Event.ConfirmCanIntro)
    }

    fun onCanEntered(can: String) {
        flowStateMachine.transition(CanStateMachine.Event.EnterCan(can))
    }

    fun onPinEntered(pin: String) {
        flowStateMachine.transition(CanStateMachine.Event.EnterPin(pin))
    }

    fun onBack() {
        flowStateMachine.transition(CanStateMachine.Event.Back)
    }

    fun cancelCanFlow() {
        logger.debug("cancel CAN")
        _stateFlow.value = SubCoordinatorState.CANCELLED
        resetCoordinatorState()
    }

    private fun finishCanFlow() {
        logger.debug("finish CAN")
        _stateFlow.value = SubCoordinatorState.FINISHED
        resetCoordinatorState()
    }

    fun skipCanFlow() {
        logger.debug("skip CAN")
        _stateFlow.value = SubCoordinatorState.SKIPPED
        resetCoordinatorState()
    }

    private fun resetCoordinatorState() {
        eIdEventFlowCoroutineScope?.cancel()
        flowStateMachine.transition(CanStateMachine.Event.Invalidate)
    }

    private fun handleEidEventsForIdent(pin: String?) {
        eIdEventFlowCoroutineScope?.cancel()
        eIdEventFlowCoroutineScope = CoroutineScope(coroutineContextProvider.IO).launch {
            eidInteractionManager.eidFlow.catch { exception ->
                logger.error("Error: $exception")
            }.collect { event ->
                when (event) {
                    is EidInteractionEvent.CanRequested -> flowStateMachine.transition(CanStateMachine.Event.FrameworkRequestsCanForIdent(pin))
                    is EidInteractionEvent.PinRequested -> flowStateMachine.transition(CanStateMachine.Event.FrameworkRequestsPinForIdent(pin))
                    is EidInteractionEvent.AuthenticationSucceededWithRedirect -> finishCanFlow()
                    is EidInteractionEvent.Error -> finishCanFlow()
                    else -> logger.debug("Ignoring event: $event")
                }
            }
        }
    }

    private fun handleEidEventsForPinChange(identificationPending: Boolean, pin: String, newPin: String, shortFlow: Boolean) {
        eIdEventFlowCoroutineScope?.cancel()
        eIdEventFlowCoroutineScope = CoroutineScope(coroutineContextProvider.IO).launch {
            eidInteractionManager.eidFlow.catch { exception ->
                logger.error("Error: $exception")
            }.collect { event ->
                when (event) {
                    is EidInteractionEvent.CanRequested -> flowStateMachine.transition(CanStateMachine.Event.FrameworkRequestsCanForPinChange(identificationPending, pin, newPin, shortFlow))
                    is EidInteractionEvent.PinRequested -> flowStateMachine.transition(CanStateMachine.Event.FrameworkRequestsPinForPinChange(identificationPending, pin, newPin, shortFlow))
                    is EidInteractionEvent.NewPinRequested -> flowStateMachine.transition(CanStateMachine.Event.FrameworkRequestsNewPin(identificationPending, pin, newPin, shortFlow))
                    is EidInteractionEvent.PinChangeSucceeded -> finishCanFlow()
                    is EidInteractionEvent.Error -> finishCanFlow()
                    else -> logger.debug("Ignoring event: $event")
                }
            }
        }
    }
}
