package de.digitalService.useID.flows

import de.digitalService.useID.getLogger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

typealias PinCallback = (String, String) -> Unit
typealias PinManagementCallback = (String, String, String) -> Unit

@Singleton
class CanStateMachine(initialState: State) {
    @Inject constructor() : this(State.Invalid)

    private val logger by getLogger()

    private val _state: MutableStateFlow<Pair<Event, State>> = MutableStateFlow(Pair(Event.Invalidate, initialState))
    val state: StateFlow<Pair<Event, State>>
        get() = _state

    sealed class State {
        object Invalid : State()

        sealed class PinManagement(val oldPin: String, val callback: PinManagementCallback): State() {
            class Intro(oldPin: String, callback: PinManagementCallback, val newPin: String?) : PinManagement(oldPin, callback)
            class IdAlreadySetup(oldPin: String, callback: PinManagementCallback, val newPin: String?) : PinManagement(oldPin, callback)
            class PinReset(oldPin: String, callback: PinManagementCallback, val newPin: String?) : PinManagement(oldPin, callback)
            class CanIntro(oldPin: String, callback: PinManagementCallback, val newPin: String?) : PinManagement(oldPin, callback)
            class CanInput(oldPin: String, callback: PinManagementCallback, val newPin: String?) : PinManagement(oldPin, callback)
            class CanInputRetry(oldPin: String, callback: PinManagementCallback, val newPin: String) : PinManagement(oldPin, callback)
            class PinInput(oldPin: String, callback: PinManagementCallback, val can: String) : PinManagement(oldPin, callback)
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

        data class InitializeCanForPinManagement(val oldPin: String, val newPin: String?, val callback: PinManagementCallback): Event()
        data class InitializeCanForIdent(val pin: String?, val callback: PinCallback): Event()

        data class EnterCan(val can: String): Event()
        data class EnterPin(val pin: String): Event()

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
                    is State.Invalid -> if (event.newPin != null) State.PinManagement.CanIntro(event.oldPin, event.callback, event.newPin) else State.PinManagement.Intro(event.oldPin, event.callback, null)
                    is State.PinManagement.CanAndPinEntered -> State.PinManagement.CanInputRetry(currentState.oldPin, event.callback, currentState.newPin)
                    else -> throw IllegalArgumentException()
                }
            }

            is Event.InitializeCanForIdent -> {
                when (val currentState = state.value.second) {
                    is State.Invalid -> if (event.pin != null) State.Ident.CanIntro(event.callback, event.pin) else State.Ident.Intro(event.callback, null)
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
                    is State.PinManagement.CanIntro -> State.PinManagement.CanInput(currentState.oldPin, currentState.callback, currentState.newPin)
                    is State.Ident.CanIntro -> State.Ident.CanInput(currentState.callback, currentState.pin)
                    else -> throw IllegalArgumentException()
                }
            }

            is Event.EnterCan -> {
                when (val currentState = state.value.second) {
                    is State.PinManagement.CanInput -> if (currentState.newPin != null) State.PinManagement.CanAndPinEntered(currentState.oldPin, currentState.callback, event.can, currentState.newPin) else State.PinManagement.PinInput(currentState.oldPin, currentState.callback, event.can)
                    is State.PinManagement.CanInputRetry -> State.PinManagement.CanAndPinEntered(currentState.oldPin, currentState.callback, event.can, currentState.newPin)

                    is State.Ident.CanInput -> if (currentState.pin != null) State.Ident.CanAndPinEntered(currentState.callback, event.can, currentState.pin) else State.Ident.PinInput(currentState.callback, event.can)
                    is State.Ident.CanInputRetry -> State.Ident.CanAndPinEntered(currentState.callback, event.can, currentState.pin)
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
