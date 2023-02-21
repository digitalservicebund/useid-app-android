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
    private val logger by getLogger()

    private val _state: MutableStateFlow<Pair<Event, State>> = MutableStateFlow(Pair(Event.Invalidate, State.Invalid))
    val state: StateFlow<Pair<Event, State>>
        get() = _state

    sealed class State {
        object Invalid : State()

        sealed class PinManagement(val oldPin: String, val callback: PinManagementCallback): State() {
            class Intro(oldPin: String, callback: PinManagementCallback, val newPin: String?) : PinManagement(oldPin, callback)
            class IdAlreadySetup(oldPin: String, callback: PinManagementCallback, val newPin: String?, ) : PinManagement(oldPin, callback)
            class PinReset(oldPin: String, callback: PinManagementCallback, val newPin: String?, ) : PinManagement(oldPin, callback)
            class CanIntro(oldPin: String, callback: PinManagementCallback, val newPin: String?) : PinManagement(oldPin, callback)
            class CanInput(oldPin: String, callback: PinManagementCallback, val newPin: String?, ) : PinManagement(oldPin, callback)
            class CanInputRetry(oldPin: String, callback: PinManagementCallback, val newPin: String, ) : PinManagement(oldPin, callback)
            class PinInput(oldPin: String, callback: PinManagementCallback, val can: String, ) : PinManagement(oldPin, callback)
            class CanAndPinEntered(oldPin: String, callback: PinManagementCallback, val can: String, val newPin: String) : PinManagement(oldPin, callback)
        }

        sealed class Ident(val callback: PinCallback): State() {
            class Intro(callback: PinCallback, val pin: String?) : Ident(callback)
            class PinReset(callback: PinCallback, val pin: String?) : Ident(callback)
            class CanIntro(callback: PinCallback, val pin: String?) : Ident(callback)
            class CanIntroWithoutFlowIntro(callback: PinCallback, val pin: String?) : Ident(callback)
            class CanInput(callback: PinCallback, val pin: String?) : Ident(callback)
            class CanInputRetry(callback: PinCallback, val pin: String) : Ident(callback)
            class PinInput(callback: PinCallback, val can: String) : Ident(callback)
            class CanAndPinEntered(callback: PinCallback, val can: String, val pin: String) : Ident(callback)
        }
    }

    sealed class Event {
        object AgreeToThirdAttempt: Event()
        object DenyThirdAttempt: Event()

        object ResetPin: Event()
        object ConfirmCanIntro: Event()

        class InitializeCanForPinManagement(val oldPin: String, val newPin: String?, val callback: PinManagementCallback): Event()
        class InitializeCanForIdent(val pin: String?, val callback: PinCallback): Event()

        class EnterCan(val can: String): Event()
        class EnterPin(val pin: String): Event()

        object Back: Event()
        object Invalidate: Event()
    }

    fun transition(event: Event) {
        val nextState = nextState(event)
        logger.debug("${state.value.second::class.simpleName}  ====${event::class.simpleName}===>  ${nextState::class.simpleName}")
        _state.value = Pair(event, nextState)
    }

    private fun nextState(event: Event): State {
        return when (event) {
            is Event.InitializeCanForPinManagement -> {
                when (val currentState = state.value.second) {
                    is State.Invalid -> State.PinManagement.Intro(event.oldPin, event.callback, event.newPin)
                    is State.PinManagement.CanIntro -> State.PinManagement.CanIntro(currentState.oldPin, event.callback, currentState.newPin)
                    is State.PinManagement.CanAndPinEntered -> State.PinManagement.CanInputRetry(currentState.oldPin, event.callback, currentState.newPin)
                    else -> throw IllegalArgumentException()
                }
            }

            is Event.InitializeCanForIdent -> {
                when (val currentState = state.value.second) {
                    is State.Invalid -> if (event.pin != null) State.Ident.CanIntro(event.callback, event.pin) else State.Ident.Intro(event.callback, null)
                    is State.Ident.CanIntroWithoutFlowIntro -> State.Ident.CanIntroWithoutFlowIntro(event.callback, currentState.pin)
                    is State.Ident.CanAndPinEntered -> State.Ident.CanInputRetry(event.callback, currentState.pin)
                    else -> throw IllegalArgumentException()
                }
            }

            is Event.AgreeToThirdAttempt -> {
                when (val currentState = state.value.second) {
                    is State.PinManagement.Intro -> State.PinManagement.CanIntro(currentState.oldPin, currentState.callback, currentState.newPin)
                    is State.Ident.Intro -> State.Ident.CanIntro(currentState.callback, currentState.pin)
                    else -> throw IllegalArgumentException()
                }
            }

            is Event.DenyThirdAttempt -> {
                when (val currentState = state.value.second) {
                    is State.PinManagement.Intro -> State.PinManagement.IdAlreadySetup(currentState.oldPin, currentState.callback, currentState.newPin)
                    else -> throw IllegalArgumentException()
                }
            }

            is Event.ResetPin -> {
                when (val currentState = state.value.second) {
                    is State.PinManagement.IdAlreadySetup -> State.PinManagement.PinReset(currentState.oldPin, currentState.callback, currentState.newPin)
                    is State.Ident.Intro -> State.Ident.PinReset(currentState.callback, currentState.pin)
                    else -> throw IllegalArgumentException()
                }
            }

            is Event.ConfirmCanIntro -> {
                when (val currentState = state.value.second) {
                    is State.PinManagement.CanIntro ->State.PinManagement.CanInput(currentState.oldPin, currentState.callback, currentState.newPin)
                    is State.Ident.CanIntro -> State.Ident.CanInput(currentState.callback, currentState.pin)
                    else -> throw IllegalArgumentException()
                }
            }

            is Event.EnterCan -> {
                when (val currentState = state.value.second) {
                    is State.PinManagement.CanInput -> if (currentState.newPin != null) State.PinManagement.CanAndPinEntered(currentState.oldPin, currentState.callback, event.can, currentState.newPin) else State.PinManagement.PinInput(event.can, currentState.callback, currentState.oldPin)
                    is State.PinManagement.CanInputRetry -> State.PinManagement.CanAndPinEntered(currentState.oldPin, currentState.callback, event.can, currentState.newPin)
                    is State.PinManagement.CanAndPinEntered -> State.PinManagement.CanAndPinEntered(currentState.oldPin, currentState.callback, event.can, currentState.newPin)

                    is State.Ident.CanInput -> if (currentState.pin != null) State.Ident.CanAndPinEntered(currentState.callback, event.can, currentState.pin) else State.Ident.PinInput(currentState.callback, event.can)
                    is State.Ident.CanInputRetry -> State.Ident.CanAndPinEntered(currentState.callback, event.can, currentState.pin)
                    is State.Ident.CanAndPinEntered -> State.Ident.CanAndPinEntered(currentState.callback, event.can, currentState.pin)
                    else -> throw IllegalArgumentException()
                }
            }

            is Event.EnterPin -> {
                when (val currentState = state.value.second) {
                    is State.PinManagement.PinInput -> State.PinManagement.CanAndPinEntered(currentState.oldPin, currentState.callback, currentState.can, event.pin)
                    is State.Ident.PinInput -> State.Ident.CanAndPinEntered(currentState.callback, currentState.can, event.pin)
                    else -> throw IllegalArgumentException()
                }
            }

            is Event.Back -> {
                when (val currentState = state.value.second) {
                    is State.PinManagement.IdAlreadySetup -> State.PinManagement.Intro(currentState.oldPin, currentState.callback, currentState.newPin)
                    is State.PinManagement.PinReset -> State.PinManagement.IdAlreadySetup(currentState.oldPin, currentState.callback, currentState.newPin)
                    is State.PinManagement.CanIntro -> if (currentState.newPin == null) State.PinManagement.Intro(currentState.oldPin, currentState.callback, null) else throw IllegalArgumentException()
                    is State.PinManagement.CanInput -> State.PinManagement.CanIntro(currentState.oldPin, currentState.callback, currentState.newPin)
                    is State.PinManagement.CanInputRetry -> State.PinManagement.CanIntro(currentState.oldPin, currentState.callback, currentState.newPin)
                    is State.PinManagement.PinInput -> State.PinManagement.CanInput(currentState.oldPin, currentState.callback, null)

                    is State.Ident.PinReset -> State.Ident.Intro(currentState.callback, currentState.pin)
                    is State.Ident.CanIntro -> State.Ident.Intro(currentState.callback, currentState.pin)
                    is State.Ident.CanInput -> State.Ident.CanIntro(currentState.callback, currentState.pin)
                    is State.Ident.CanInputRetry -> State.Ident.CanIntro(currentState.callback, currentState.pin)
                    is State.Ident.PinInput -> State.Ident.CanInput(currentState.callback, null)

                    else -> throw IllegalArgumentException()
                }
            }

            is Event.Invalidate -> State.Invalid
        }
    }

    val backAllowed: Boolean
        get() = try { nextState(Event.Back); true } catch (e: IllegalArgumentException) { false }
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

    init {
        CoroutineScope(coroutineContextProvider.Default).launch {
            flowStateMachine.state.collect { eventAndPair ->
                if (eventAndPair.first is CanFsm.Event.Back) {
                    navigator.pop()
                } else {
                    when (val state = eventAndPair.second) {
                        is CanFsm.State.PinManagement.Intro -> navigator.navigate(SetupCanConfirmTransportPinDestination(state.oldPin))
                        is CanFsm.State.PinManagement.IdAlreadySetup -> navigator.navigate(SetupCanAlreadySetupDestination)
                        is CanFsm.State.PinManagement.PinReset, is CanFsm.State.Ident.PinReset -> navigator.navigate(CanResetPersonalPinDestination)
                        is CanFsm.State.PinManagement.CanIntro -> navigator.navigate(SetupCanIntroDestination(true))
                        is CanFsm.State.PinManagement.CanInput -> navigator.navigate(CanInputDestination(false))
                        is CanFsm.State.PinManagement.CanInputRetry -> {
                            navigator.navigate(SetupCanIntroDestination(false))
                            navigator.navigate(CanInputDestination(true))
                        }
                        is CanFsm.State.PinManagement.PinInput -> navigator.navigate(SetupCanTransportPinDestination)
                        is CanFsm.State.PinManagement.CanAndPinEntered -> state.callback(state.oldPin, state.can, state.newPin)

                        is CanFsm.State.Ident.Intro -> navigator.navigate(IdentificationCanPinForgottenDestination)
                        is CanFsm.State.Ident.CanIntro -> navigator.navigate(IdentificationCanIntroDestination(state.pin == null))
                        is CanFsm.State.Ident.CanIntroWithoutFlowIntro -> navigator.navigate(IdentificationCanIntroDestination(false))
                        is CanFsm.State.Ident.CanInput -> navigator.navigate(CanInputDestination(false))
                        is CanFsm.State.Ident.CanInputRetry -> {
                            navigator.navigate(IdentificationCanPinForgottenDestination)
                            navigator.navigate(IdentificationCanIntroDestination(true))
                            navigator.navigate(CanInputDestination(true))
                        }
                        is CanFsm.State.Ident.PinInput -> navigator.navigate(IdentificationCanPinInputDestination)
                        is CanFsm.State.Ident.CanAndPinEntered -> state.callback(state.pin, state.can)

                        CanFsm.State.Invalid -> logger.debug("Ignoring transition to invalid state.")
                    }
                }
            }
        }
    }

    fun startPinManagementCanFlow(shortFlow: Boolean, oldPin: String, newPin: String): Flow<SubCoordinatorState> {
        _stateFlow.value = SubCoordinatorState.ACTIVE
        handleEidEvents(shortFlow, oldPin, newPin)
        return stateFlow
    }

    fun startIdentCanFlow(pin: String?): Flow<SubCoordinatorState> {
        _stateFlow.value = SubCoordinatorState.ACTIVE
        handleEidEvents(null, pin, null)
        return stateFlow
    }

    fun onResetPin() {
        flowStateMachine.transition(CanFsm.Event.ResetPin)
    }

    fun confirmPinInput() {
        flowStateMachine.transition(CanFsm.Event.DenyThirdAttempt)
    }

    fun proceedWithThirdAttempt() {
        flowStateMachine.transition(CanFsm.Event.AgreeToThirdAttempt)
    }

    fun finishIntro() {
        flowStateMachine.transition(CanFsm.Event.ConfirmCanIntro)
    }

    fun onCanEntered(can: String) {
        flowStateMachine.transition(CanFsm.Event.EnterCan(can))
    }

    fun onPinEntered(pin: String) {
        flowStateMachine.transition(CanFsm.Event.EnterPin(pin))
    }

    fun onBack() {
        flowStateMachine.transition(CanFsm.Event.Back)
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

    private fun handleEidEvents(shortFlow: Boolean?, pin: String?, newPin: String?) {
        logger.debug("Handle EID events.")

        eIdEventFlowCoroutineScope?.cancel()
        eIdEventFlowCoroutineScope = CoroutineScope(coroutineContextProvider.IO).launch {
            idCardManager.eidFlow.catch { exception ->
                logger.error("Error: $exception")
            }.collect { event ->
                logger.debug("Handling event: $event")

                when (event) {
                    is EidInteractionEvent.RequestPinAndCan -> {
                        flowStateMachine.transition(CanFsm.Event.InitializeCanForIdent(pin, event.pinCanCallback))
                    }
                    is EidInteractionEvent.RequestCanAndChangedPin -> {
                        flowStateMachine.transition(CanFsm.Event.InitializeCanForPinManagement(pin!!, newPin, event.pinCallback))
                    }
                    is EidInteractionEvent.AuthenticationSuccessful, EidInteractionEvent.ProcessCompletedSuccessfullyWithoutResult, is EidInteractionEvent.ProcessCompletedSuccessfullyWithRedirect -> finishCanFlow()
                    is EidInteractionEvent.Error -> finishCanFlow()
                    else -> logger.debug("Ignoring event: $event")
                }
            }
        }
    }
}
