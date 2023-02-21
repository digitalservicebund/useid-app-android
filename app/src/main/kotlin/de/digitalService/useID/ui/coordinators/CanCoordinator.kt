package de.digitalService.useID.ui.coordinators

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

private typealias PinCallback = (String, String) -> Unit
private typealias PinManagementCallback = (String, String, String) -> Unit

@Singleton
class CanFsm @Inject constructor() {
    var state: State = State.Invalid
        private set

    sealed class State {
        object Invalid : State()
        class InitializedForPinManagement(val shortFlow: Boolean, val oldPin: String, val newPin: String) : State()
        class InitializedForIdent(val pin: String?) : State()
        class RequestedCanAndPinForPinManagement(val oldPin: String?, val newPin: String, val callback: PinManagementCallback) : State()
        class RequestedCanAndPinForIdent(val pin: String?, val callback: PinCallback) : State()
        class CanEnteredForPinManagement(val can: String, val newPin: String, val callback: PinManagementCallback) : State()
        class CanEnteredForIdent(val can: String, val callback: PinCallback) : State()
        class CanAndPinEnteredForPinManagement(var can: String, val pin: String, val newPin: String, var callback: PinManagementCallback) : State()
        class CanAndPinEnteredForIdent(var can: String, val pin: String, var callback: PinCallback) : State()
    }

    sealed class Event {
        class InitializeForPinManagement(val shortFlow: Boolean, val oldPin: String, val newPin: String): Event()
        class InitializeForIdent(val pin: String?): Event()

        class RegisterPinManagementCallback(val callback: PinManagementCallback): Event()
        class RegisterPinCallback(val callback: PinCallback): Event()

        class EnterCan(val can: String): Event()
        class EnterPin(val pin: String): Event()

        object Back: Event()
        object Invalidate: Event()
    }

    fun transition(event: Event): State {
        when (event) {
            is Event.InitializeForPinManagement -> state = State.InitializedForPinManagement(event.shortFlow, event.oldPin, event.newPin)
            is Event.InitializeForIdent -> state = State.InitializedForIdent(event.pin)

            is Event.RegisterPinManagementCallback -> {
                state = when (val currentState = state) {
                    is State.InitializedForPinManagement -> State.RequestedCanAndPinForPinManagement(currentState.oldPin.takeIf { currentState.shortFlow }, currentState.newPin, event.callback)
                    is State.CanAndPinEnteredForPinManagement -> State.CanAndPinEnteredForPinManagement(currentState.can, currentState.pin, currentState.newPin, event.callback)
                    else -> throw IllegalArgumentException()
                }
            }

            is Event.RegisterPinCallback -> {
                state = when (val currentState = state) {
                    is State.InitializedForIdent -> State.RequestedCanAndPinForIdent(currentState.pin, event.callback)
                    is State.CanAndPinEnteredForIdent -> State.CanAndPinEnteredForIdent(currentState.can, currentState.pin, event.callback)
                    else -> throw IllegalArgumentException()
                }
            }

            is Event.EnterCan -> {
                state = when (val currentState = state) {
                    is State.RequestedCanAndPinForPinManagement -> if (currentState.oldPin != null) State.CanAndPinEnteredForPinManagement(event.can, currentState.oldPin, currentState.newPin, currentState.callback) else State.CanEnteredForPinManagement(event.can, currentState.newPin, currentState.callback)
                    is State.CanAndPinEnteredForPinManagement -> State.CanAndPinEnteredForPinManagement(event.can, currentState.pin, currentState.newPin, currentState.callback)

                    is State.RequestedCanAndPinForIdent -> if (currentState.pin != null) State.CanAndPinEnteredForIdent(event.can, currentState.pin, currentState.callback) else State.CanEnteredForIdent(event.can, currentState.callback)
                    is State.CanAndPinEnteredForIdent -> State.CanAndPinEnteredForIdent(event.can, currentState.pin, currentState.callback)
                    else -> throw IllegalArgumentException()
                }
            }

            is Event.EnterPin -> {
                state = when (val currentState = state) {
                    is State.CanEnteredForPinManagement -> State.CanAndPinEnteredForPinManagement(currentState.can, event.pin, currentState.newPin, currentState.callback)
                    is State.CanEnteredForIdent -> State.CanAndPinEnteredForIdent(currentState.can, event.pin, currentState.callback)
                    else -> throw IllegalArgumentException()
                }
            }

            is Event.Back -> {
                state = when (val currentState = state) {
                    is State.CanEnteredForPinManagement -> State.RequestedCanAndPinForPinManagement(null, currentState.newPin, currentState.callback)
                    is State.CanEnteredForIdent -> State.RequestedCanAndPinForIdent(null, currentState.callback)
                    else -> throw IllegalArgumentException()
                }
            }

            is Event.Invalidate -> state = State.Invalid
        }

        return state
    }
}

@Singleton
class CanCoordinator @Inject constructor(
    private val navigator: Navigator,
    private val idCardManager: IdCardManager,
    private val flowStateMachine: CanFsm,
    private val coroutineContextProvider: CoroutineContextProviderType
) {
    private val logger by getLogger()

    private var eIdEventFlowCoroutineScope: Job? = null

    private val _stateFlow: MutableStateFlow<SubCoordinatorState> = MutableStateFlow(SubCoordinatorState.FINISHED)
    val stateFlow: StateFlow<SubCoordinatorState>
        get() = _stateFlow

    fun startPinManagementCanFlow(shortFlow: Boolean, oldPin: String, newPin: String): Flow<SubCoordinatorState> {
        flowStateMachine.transition(CanFsm.Event.InitializeForPinManagement(shortFlow, oldPin, newPin))
        return startCanFlow()
    }

    fun startIdentCanFlow(pin: String?): Flow<SubCoordinatorState> {
        flowStateMachine.transition(CanFsm.Event.InitializeForIdent(pin))
        return startCanFlow()
    }

    private fun startCanFlow(): Flow<SubCoordinatorState> {
        collectEidEvents()

        _stateFlow.value = SubCoordinatorState.ACTIVE
        return stateFlow
    }

    fun onResetPin() {
        navigator.navigate(ResetPersonalPinDestination)
    }

    fun confirmPinInput() {
        navigator.navigate(SetupCanAlreadySetupDestination)
    }

    fun proceedWithThirdAttempt() {
        when (flowStateMachine.state) {
            is CanFsm.State.RequestedCanAndPinForPinManagement -> navigator.navigate(SetupCanIntroDestination(true))
            is CanFsm.State.RequestedCanAndPinForIdent, is CanFsm.State.CanAndPinEnteredForIdent -> navigator.navigate(IdentificationCanIntroDestination(true))
            else -> logger.error("Requested to proceed with third PIN attempt unexpected in state ${flowStateMachine.state}")
        }
    }

    fun finishIntro() {
        navigator.navigate(CanInputDestination(false))
    }

    fun onCanEntered(can: String) {
        when (flowStateMachine.transition(CanFsm.Event.EnterCan(can))) {
            is CanFsm.State.CanEnteredForPinManagement -> navigator.navigate(SetupCanTransportPinDestination)
            is CanFsm.State.CanEnteredForIdent -> navigator.navigate(IdentificationCanPinInputDestination)
            is CanFsm.State.CanAndPinEnteredForPinManagement, is CanFsm.State.CanAndPinEnteredForIdent -> executeCanStep()
            else -> throw IllegalStateException()
        }
    }

    fun onPinEntered(pin: String) {
        flowStateMachine.transition(CanFsm.Event.EnterPin(pin))
        executeCanStep()
    }

    private fun executeCanStep() {
        when (val currentState = flowStateMachine.state) {
            is CanFsm.State.CanAndPinEnteredForPinManagement -> currentState.callback(currentState.pin, currentState.can, currentState.newPin)
            is CanFsm.State.CanAndPinEnteredForIdent -> currentState.callback(currentState.pin, currentState.can)
            else -> throw IllegalStateException()
        }
    }

    fun onBack() {
        flowStateMachine.transition(CanFsm.Event.Back)
        navigator.pop()
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
        flowStateMachine.transition(CanFsm.Event.Invalidate)
    }

    private fun collectEidEvents() {
        eIdEventFlowCoroutineScope?.cancel()
        eIdEventFlowCoroutineScope = CoroutineScope(coroutineContextProvider.IO).launch {
            idCardManager.eidFlow.catch { exception ->
                logger.error("Error: $exception")
            }.collect { event ->
                when (event) {
                    is EidInteractionEvent.RequestPinAndCan -> {
                        when (val newState = flowStateMachine.transition(CanFsm.Event.RegisterPinCallback(event.pinCanCallback))) {
                            is CanFsm.State.RequestedCanAndPinForIdent -> {
                                if (newState.pin == null) {
                                    navigator.navigate(IdentificationCanPinForgottenDestination)
                                } else {
                                    navigator.navigate(IdentificationCanIntroDestination(false))
                                }
                            }

                            is CanFsm.State.CanAndPinEnteredForIdent -> {
                                navigator.navigate(IdentificationCanPinForgottenDestination)
                                navigator.navigate(IdentificationCanIntroDestination(true))
                                navigator.navigate(CanInputDestination(true))
                            }

                            else -> throw IllegalStateException()
                        }
                    }
                    is EidInteractionEvent.RequestCanAndChangedPin -> {
                        when (val currentState = flowStateMachine.state) {
                            is CanFsm.State.InitializedForPinManagement -> {
                                if (currentState.shortFlow) {
                                    navigator.navigate(SetupCanIntroDestination(false))
                                } else {
                                    navigator.navigate(SetupCanConfirmTransportPinDestination(currentState.oldPin))
                                }
                            }
                            is CanFsm.State.CanAndPinEnteredForPinManagement -> {
                                navigator.navigate(SetupCanIntroDestination(false))
                                navigator.navigate(CanInputDestination(true))
                            }
                            else -> throw IllegalStateException()
                        }

                        flowStateMachine.transition(CanFsm.Event.RegisterPinManagementCallback(event.pinCallback))
                    }
                    is EidInteractionEvent.AuthenticationSuccessful, EidInteractionEvent.ProcessCompletedSuccessfullyWithoutResult, is EidInteractionEvent.ProcessCompletedSuccessfullyWithRedirect -> finishCanFlow()
                    is EidInteractionEvent.Error -> finishCanFlow()
                    else -> logger.debug("Ignoring event: $event")
                }
            }
        }
    }
}
