package de.digitalService.useID.flows

import de.digitalService.useID.getLogger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

typealias PinCanCallback = (String, String) -> Unit
typealias PinManagementCanCallback = (String, String, String) -> Unit

@Singleton
class CanStateMachine(initialState: State) {
    @Inject constructor() : this(State.Invalid)

    private val logger by getLogger()

    private val _state: MutableStateFlow<Pair<Event, State>> = MutableStateFlow(Pair(Event.Invalidate, initialState))
    val state: StateFlow<Pair<Event, State>>
        get() = _state

    sealed class State {
        object Invalid : State()

        sealed class PinManagement(val callback: PinManagementCanCallback, val oldPin: String, val newPin: String) : State() {
            class Intro(callback: PinManagementCanCallback, oldPin: String, newPin: String) : PinManagement(callback, oldPin, newPin)
            class IdAlreadySetup(callback: PinManagementCanCallback, oldPin: String, newPin: String) : PinManagement(callback, oldPin, newPin)
            class PinReset(callback: PinManagementCanCallback, oldPin: String, newPin: String) : PinManagement(callback, oldPin, newPin)
            class CanIntro(callback: PinManagementCanCallback, oldPin: String, newPin: String, val shortFlow: Boolean) : PinManagement(callback, oldPin, newPin)
            class CanInput(callback: PinManagementCanCallback, oldPin: String, newPin: String, val shortFlow: Boolean) : PinManagement(callback, oldPin, newPin)
            class CanInputRetry(callback: PinManagementCanCallback, oldPin: String, newPin: String) : PinManagement(callback, oldPin, newPin)
            class PinInput(callback: PinManagementCanCallback, oldPin: String, val can: String, newPin: String) : PinManagement(callback, oldPin, newPin)
            class CanAndPinEntered(callback: PinManagementCanCallback, oldPin: String, val can: String, newPin: String) : PinManagement(callback, oldPin, newPin)
        }

        sealed class Ident(val callback: PinCanCallback) : State() {
            class Intro(callback: PinCanCallback, val pin: String?) : Ident(callback)
            class PinReset(callback: PinCanCallback, val pin: String?) : Ident(callback)
            class CanIntro(callback: PinCanCallback, val pin: String?) : Ident(callback)
            class CanIntroWithoutFlowIntro(callback: PinCanCallback, val pin: String?) : Ident(callback)
            class CanInput(callback: PinCanCallback, val pin: String?) : Ident(callback)
            class CanInputRetry(callback: PinCanCallback, val pin: String) : Ident(callback)
            class PinInput(callback: PinCanCallback, val can: String) : Ident(callback)
            class CanAndPinEntered(callback: PinCanCallback, val can: String, val pin: String) : Ident(callback)
        }
    }

    sealed class Event {
        object AgreeToThirdAttempt : Event()
        object DenyThirdAttempt : Event()

        object ResetPin : Event()
        object ConfirmCanIntro : Event()

        data class FrameworkRequestsCanForPinManagement(val oldPin: String, val newPin: String, val shortFlow: Boolean, val callback: PinManagementCanCallback) : Event()
        data class FrameworkRequestsCanForIdent(val pin: String?, val callback: PinCanCallback) : Event()

        data class EnterCan(val can: String) : Event()
        data class EnterPin(val pin: String) : Event()

        object Back : Event()
        object Invalidate : Event()
    }

    fun transition(event: Event) {
        val nextState = nextState(event)
        logger.debug("${state.value.second::class.simpleName}  ====${event::class.simpleName}===>  ${nextState::class.simpleName}")
        _state.value = Pair(event, nextState)
    }

    private fun nextState(event: Event): State {
        return when (event) {
            is Event.FrameworkRequestsCanForPinManagement -> {
                when (val currentState = state.value.second) {
                    is State.Invalid -> if (event.shortFlow) State.PinManagement.CanIntro(event.callback, event.oldPin, event.newPin, true) else State.PinManagement.Intro(event.callback, event.oldPin, event.newPin)
                    is State.PinManagement.CanAndPinEntered -> State.PinManagement.CanInputRetry(event.callback, currentState.oldPin, currentState.newPin)
                    else -> throw IllegalArgumentException()
                }
            }

            is Event.FrameworkRequestsCanForIdent -> {
                when (val currentState = state.value.second) {
                    is State.Invalid -> if (event.pin != null) State.Ident.CanIntro(event.callback, event.pin) else State.Ident.Intro(event.callback, null)
                    is State.Ident.CanAndPinEntered -> State.Ident.CanInputRetry(event.callback, currentState.pin)
                    else -> throw IllegalArgumentException()
                }
            }

            is Event.AgreeToThirdAttempt -> {
                when (val currentState = state.value.second) {
                    is State.PinManagement.Intro -> State.PinManagement.CanIntro(currentState.callback, currentState.oldPin, currentState.newPin, false)
                    is State.Ident.Intro -> State.Ident.CanIntro(currentState.callback, currentState.pin)
                    else -> throw IllegalArgumentException()
                }
            }

            is Event.DenyThirdAttempt -> {
                when (val currentState = state.value.second) {
                    is State.PinManagement.Intro -> State.PinManagement.IdAlreadySetup(currentState.callback, currentState.oldPin, currentState.newPin)
                    else -> throw IllegalArgumentException()
                }
            }

            is Event.ResetPin -> {
                when (val currentState = state.value.second) {
                    is State.PinManagement.IdAlreadySetup -> State.PinManagement.PinReset(currentState.callback, currentState.oldPin, currentState.newPin)
                    is State.Ident.Intro -> State.Ident.PinReset(currentState.callback, currentState.pin)
                    else -> throw IllegalArgumentException()
                }
            }

            is Event.ConfirmCanIntro -> {
                when (val currentState = state.value.second) {
                    is State.PinManagement.CanIntro -> State.PinManagement.CanInput(currentState.callback, currentState.oldPin, currentState.newPin, currentState.shortFlow)
                    is State.Ident.CanIntro -> State.Ident.CanInput(currentState.callback, currentState.pin)
                    else -> throw IllegalArgumentException()
                }
            }

            is Event.EnterCan -> {
                when (val currentState = state.value.second) {
                    is State.PinManagement.CanInput -> if (currentState.shortFlow) State.PinManagement.CanAndPinEntered(currentState.callback, currentState.oldPin, event.can, currentState.newPin) else State.PinManagement.PinInput(currentState.callback, currentState.oldPin, event.can, currentState.newPin)
                    is State.PinManagement.CanInputRetry -> State.PinManagement.CanAndPinEntered(currentState.callback, currentState.oldPin, event.can, currentState.newPin)

                    is State.Ident.CanInput -> if (currentState.pin != null) State.Ident.CanAndPinEntered(currentState.callback, event.can, currentState.pin) else State.Ident.PinInput(currentState.callback, event.can)
                    is State.Ident.CanInputRetry -> State.Ident.CanAndPinEntered(currentState.callback, event.can, currentState.pin)
                    else -> throw IllegalArgumentException()
                }
            }

            is Event.EnterPin -> {
                when (val currentState = state.value.second) {
                    is State.PinManagement.PinInput -> State.PinManagement.CanAndPinEntered(currentState.callback, event.pin, currentState.can, currentState.newPin)
                    is State.Ident.PinInput -> State.Ident.CanAndPinEntered(currentState.callback, currentState.can, event.pin)
                    else -> throw IllegalArgumentException()
                }
            }

            is Event.Back -> {
                when (val currentState = state.value.second) {
                    is State.PinManagement.IdAlreadySetup -> State.PinManagement.Intro(currentState.callback, currentState.oldPin, currentState.newPin)
                    is State.PinManagement.PinReset -> State.PinManagement.IdAlreadySetup(currentState.callback, currentState.oldPin, currentState.newPin)
                    is State.PinManagement.CanIntro -> if (!currentState.shortFlow) State.PinManagement.Intro(currentState.callback, currentState.oldPin, currentState.newPin) else throw IllegalArgumentException()
                    is State.PinManagement.CanInput -> State.PinManagement.CanIntro(currentState.callback, currentState.oldPin, currentState.newPin, currentState.shortFlow)
                    is State.PinManagement.CanInputRetry -> State.PinManagement.CanIntro(currentState.callback, currentState.oldPin, currentState.newPin, true)
                    is State.PinManagement.PinInput -> State.PinManagement.CanInput(currentState.callback, currentState.oldPin, currentState.newPin, false)

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
}
