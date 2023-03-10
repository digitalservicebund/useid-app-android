package de.digitalService.useID.ui.coordinators

import de.digitalService.useID.flows.CanStateMachine
import de.digitalService.useID.getLogger
import de.digitalService.useID.idCardInterface.EidInteractionEvent
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
class CanCoordinator @Inject constructor(
    private val navigator: Navigator,
    private val idCardManager: IdCardManager,
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
        CoroutineScope(coroutineContextProvider.Default).launch {
            flowStateMachine.state.collect { eventAndPair ->
                if (eventAndPair.first is CanStateMachine.Event.Back) {
                    navigator.pop()
                } else {
                    when (val state = eventAndPair.second) {
                        is CanStateMachine.State.PinManagement.Intro -> navigator.navigate(SetupCanConfirmTransportPinDestination(state.oldPin, state.identificationPending))
                        is CanStateMachine.State.PinManagement.IdAlreadySetup -> navigator.navigate(SetupCanAlreadySetupDestination(state.identificationPending))
                        is CanStateMachine.State.PinManagement.PinReset, is CanStateMachine.State.Ident.PinReset -> navigator.navigate(CanResetPersonalPinDestination)
                        is CanStateMachine.State.PinManagement.CanIntro -> navigator.navigate(SetupCanIntroDestination(!state.shortFlow, state.identificationPending))
                        is CanStateMachine.State.PinManagement.CanInput -> navigator.navigate(CanInputDestination(false))
                        is CanStateMachine.State.PinManagement.CanInputRetry -> {
                            navigator.navigate(SetupCanIntroDestination(false, state.identificationPending))
                            navigator.navigate(CanInputDestination(true))
                        }
                        is CanStateMachine.State.PinManagement.PinInput -> navigator.navigate(SetupCanTransportPinDestination(state.identificationPending))
                        is CanStateMachine.State.PinManagement.CanAndPinEntered -> state.callback(state.oldPin, state.can, state.newPin)

                        is CanStateMachine.State.Ident.Intro -> navigator.navigate(IdentificationCanPinForgottenDestination)
                        is CanStateMachine.State.Ident.CanIntro -> navigator.navigate(IdentificationCanIntroDestination(state.pin == null))
                        is CanStateMachine.State.Ident.CanIntroWithoutFlowIntro -> navigator.navigate(IdentificationCanIntroDestination(false))
                        is CanStateMachine.State.Ident.CanInput -> navigator.navigate(CanInputDestination(false))
                        is CanStateMachine.State.Ident.CanInputRetry -> {
                            navigator.navigate(IdentificationCanPinForgottenDestination)
                            navigator.navigate(IdentificationCanIntroDestination(true))
                            navigator.navigate(CanInputDestination(true))
                        }
                        is CanStateMachine.State.Ident.PinInput -> navigator.navigate(IdentificationCanPinInputDestination)
                        is CanStateMachine.State.Ident.CanAndPinEntered -> state.callback(state.pin, state.can)

                        CanStateMachine.State.Invalid -> logger.debug("Ignoring transition to state INVALID.")
                    }
                }
            }
        }
    }

    fun startPinManagementCanFlow(identificationPending: Boolean, oldPin: String, newPin: String, shortFlow: Boolean): Flow<SubCoordinatorState> {
        collectStateMachineEvents()

        _stateFlow.value = SubCoordinatorState.ACTIVE
        handleEidEventsForPinManagement(identificationPending, oldPin, newPin, shortFlow)
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
        _stateFlow.value = SubCoordinatorState.CANCELLED
        resetCoordinatorState()
    }

    private fun finishCanFlow() {
        _stateFlow.value = SubCoordinatorState.FINISHED
        resetCoordinatorState()
    }

    fun skipCanFlow() {
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
            idCardManager.eidFlow.catch { exception ->
                logger.error("Error: $exception")
            }.collect { event ->
                when (event) {
                    is EidInteractionEvent.RequestPinAndCan -> {
                        flowStateMachine.transition(CanStateMachine.Event.FrameworkRequestsCanForIdent(pin, event.pinCanCallback))
                    }
                    is EidInteractionEvent.AuthenticationSuccessful, EidInteractionEvent.ProcessCompletedSuccessfullyWithoutResult, is EidInteractionEvent.ProcessCompletedSuccessfullyWithRedirect -> finishCanFlow()
                    is EidInteractionEvent.Error -> finishCanFlow()
                    else -> logger.debug("Ignoring event: $event")
                }
            }
        }
    }

    private fun handleEidEventsForPinManagement(identificationPending: Boolean, pin: String, newPin: String, shortFlow: Boolean) {
        eIdEventFlowCoroutineScope?.cancel()
        eIdEventFlowCoroutineScope = CoroutineScope(coroutineContextProvider.IO).launch {
            idCardManager.eidFlow.catch { exception ->
                logger.error("Error: $exception")
            }.collect { event ->
                when (event) {
                    is EidInteractionEvent.RequestCanAndChangedPin -> {
                        flowStateMachine.transition(CanStateMachine.Event.FrameworkRequestsCanForPinManagement(identificationPending, pin, newPin, shortFlow, event.pinCallback))
                    }
                    is EidInteractionEvent.AuthenticationSuccessful, EidInteractionEvent.ProcessCompletedSuccessfullyWithoutResult, is EidInteractionEvent.ProcessCompletedSuccessfullyWithRedirect -> finishCanFlow()
                    is EidInteractionEvent.Error -> finishCanFlow()
                    else -> logger.debug("Ignoring event: $event")
                }
            }
        }
    }
}
