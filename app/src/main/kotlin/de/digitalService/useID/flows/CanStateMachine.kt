package de.digitalService.useID.flows

import de.digitalService.useID.analytics.IssueTrackerManagerType
import de.digitalService.useID.getLogger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CanStateMachine(initialState: State, private val issueTrackerManager: IssueTrackerManagerType) {
    @Inject constructor(issueTrackerManager: IssueTrackerManagerType) : this(State.Invalid, issueTrackerManager)

    private val logger by getLogger()

    private val _state: MutableStateFlow<Pair<Event, State>> = MutableStateFlow(Pair(Event.Invalidate, initialState))
    val state: StateFlow<Pair<Event, State>>
        get() = _state

    sealed class State {
        object Invalid : State()

        sealed class ChangePin(val identificationPending: Boolean, val oldPin: String, val newPin: String) : State() {
            class Intro(identificationPending: Boolean, oldPin: String, newPin: String) : ChangePin(identificationPending, oldPin, newPin)
            class IdAlreadySetup(identificationPending: Boolean,  oldPin: String, newPin: String) : ChangePin(identificationPending, oldPin, newPin)
            class PinReset(identificationPending: Boolean,  oldPin: String, newPin: String) : ChangePin(identificationPending, oldPin, newPin)
            class CanIntro(identificationPending: Boolean,  oldPin: String, newPin: String, val shortFlow: Boolean) : ChangePin(identificationPending, oldPin, newPin)
            class CanInput(identificationPending: Boolean,  oldPin: String, newPin: String, val shortFlow: Boolean) : ChangePin(identificationPending, oldPin, newPin)
            class CanInputRetry(identificationPending: Boolean,  oldPin: String, newPin: String) : ChangePin(identificationPending, oldPin, newPin)
            class PinInput(identificationPending: Boolean,  oldPin: String, val can: String, newPin: String) : ChangePin(identificationPending, oldPin, newPin)
            class CanAndPinEntered(identificationPending: Boolean,  oldPin: String, val can: String, newPin: String) : ChangePin(identificationPending, oldPin, newPin)

            class FrameworkReadyForPinInput(identificationPending: Boolean, val pin: String, newPin: String) : ChangePin(identificationPending, pin, newPin)
            class FrameworkReadyForNewPinInput(identificationPending: Boolean, val pin: String, newPin: String) : ChangePin(identificationPending, pin, newPin)
        }

        sealed class Ident : State() {
            class Intro(val pin: String?) : Ident()
            class PinReset(val pin: String?) : Ident()
            class CanIntro(val pin: String?) : Ident()
            class CanIntroWithoutFlowIntro(val pin: String?) : Ident()
            class CanInput(val pin: String?) : Ident()
            class CanInputRetry(val pin: String) : Ident()
            class PinInput(val can: String) : Ident()
            class CanAndPinEntered(val can: String, val pin: String) : Ident()
            class FrameworkReadyForPinInput(val pin: String): Ident()
        }
    }

    sealed class Event {
        object AgreeToThirdAttempt : Event()
        object DenyThirdAttempt : Event()

        object ResetPin : Event()
        object ConfirmCanIntro : Event()

        data class FrameworkRequestsCanForPinChange(val identificationPending: Boolean, val oldPin: String, val newPin: String, val shortFlow: Boolean) : Event()
        data class FrameworkRequestsCanForIdent(val pin: String?) : Event()

        data class FrameworkRequestsPinForPinChange(val identificationPending: Boolean, val oldPin: String, val newPin: String, val shortFlow: Boolean) : Event()
        data class FrameworkRequestsPinForIdent(val pin: String?) : Event()

        data class FrameworkRequestsNewPin(val identificationPending: Boolean, val oldPin: String, val newPin: String, val shortFlow: Boolean) : Event()

        data class EnterCan(val can: String) : Event()
        data class EnterPin(val pin: String) : Event()

        object Back : Event()
        object Invalidate : Event()
    }

    fun transition(event: Event) {
        val currentStateDescription = state.value.second::class.simpleName
        val eventDescription = event::class.simpleName
        issueTrackerManager.addInfoBreadcrumb("transition", "Transitioning from $currentStateDescription via $eventDescription.")
        logger.debug("$currentStateDescription  ====$eventDescription===>  ???")
        val nextState = nextState(event)
        logger.debug("$currentStateDescription  ====$eventDescription===>  ${nextState::class.simpleName}")
        _state.value = Pair(event, nextState)
    }

    private fun nextState(event: Event): State {
        return when (event) {
            is Event.FrameworkRequestsCanForPinChange -> {
                when (val currentState = state.value.second) {
                    is State.Invalid -> if (event.shortFlow) State.ChangePin.CanIntro(event.identificationPending, event.oldPin, event.newPin, true) else State.ChangePin.Intro(event.identificationPending, event.oldPin, event.newPin)
                    is State.ChangePin.CanAndPinEntered -> State.ChangePin.CanInputRetry(currentState.identificationPending, currentState.oldPin, currentState.newPin)
                    else -> throw IllegalArgumentException()
                }
            }

            is Event.FrameworkRequestsCanForIdent -> {
                when (val currentState = state.value.second) {
                    is State.Invalid -> if (event.pin != null) State.Ident.CanIntro(event.pin) else State.Ident.Intro(null)
                    is State.Ident.CanAndPinEntered -> State.Ident.CanInputRetry(currentState.pin)
                    else -> throw IllegalArgumentException()
                }
            }

            is Event.FrameworkRequestsPinForPinChange -> {
                when(val currentState = state.value.second) {
                    is State.ChangePin.CanAndPinEntered -> State.ChangePin.FrameworkReadyForPinInput(currentState.identificationPending, currentState.oldPin, currentState.newPin)
                    else -> throw IllegalArgumentException()
                }
            }

            is Event.FrameworkRequestsPinForIdent -> {
                when(val currentState = state.value.second) {
                    is State.Ident.CanAndPinEntered -> State.Ident.FrameworkReadyForPinInput(currentState.pin)
                    else -> throw IllegalArgumentException()
                }
            }

            is Event.FrameworkRequestsNewPin -> {
                when(val currentState = state.value.second) {
                    is State.ChangePin.FrameworkReadyForPinInput -> State.ChangePin.FrameworkReadyForNewPinInput(currentState.identificationPending, currentState.oldPin, currentState.newPin)
                    else -> throw IllegalArgumentException()
                }
            }

            is Event.AgreeToThirdAttempt -> {
                when (val currentState = state.value.second) {
                    is State.ChangePin.Intro -> State.ChangePin.CanIntro(currentState.identificationPending, currentState.oldPin, currentState.newPin, false)
                    is State.Ident.Intro -> State.Ident.CanIntro(currentState.pin)
                    else -> throw IllegalArgumentException()
                }
            }

            is Event.DenyThirdAttempt -> {
                when (val currentState = state.value.second) {
                    is State.ChangePin.Intro -> State.ChangePin.IdAlreadySetup(currentState.identificationPending, currentState.oldPin, currentState.newPin)
                    else -> throw IllegalArgumentException()
                }
            }

            is Event.ResetPin -> {
                when (val currentState = state.value.second) {
                    is State.ChangePin.IdAlreadySetup -> State.ChangePin.PinReset(currentState.identificationPending, currentState.oldPin, currentState.newPin)
                    is State.Ident.Intro -> State.Ident.PinReset(currentState.pin)
                    else -> throw IllegalArgumentException()
                }
            }

            is Event.ConfirmCanIntro -> {
                when (val currentState = state.value.second) {
                    is State.ChangePin.CanIntro -> State.ChangePin.CanInput(currentState.identificationPending, currentState.oldPin, currentState.newPin, currentState.shortFlow)
                    is State.Ident.CanIntro -> State.Ident.CanInput(currentState.pin)
                    else -> throw IllegalArgumentException()
                }
            }

            is Event.EnterCan -> {
                when (val currentState = state.value.second) {
                    is State.ChangePin.CanInput -> if (currentState.shortFlow) State.ChangePin.CanAndPinEntered(currentState.identificationPending, currentState.oldPin, event.can, currentState.newPin) else State.ChangePin.PinInput(currentState.identificationPending, currentState.oldPin, event.can, currentState.newPin)
                    is State.ChangePin.CanInputRetry -> State.ChangePin.CanAndPinEntered(currentState.identificationPending, currentState.oldPin, event.can, currentState.newPin)

                    is State.Ident.CanInput -> if (currentState.pin != null) State.Ident.CanAndPinEntered(event.can, currentState.pin) else State.Ident.PinInput(event.can)
                    is State.Ident.CanInputRetry -> State.Ident.CanAndPinEntered(event.can, currentState.pin)
                    else -> throw IllegalArgumentException()
                }
            }

            is Event.EnterPin -> {
                when (val currentState = state.value.second) {
                    is State.ChangePin.PinInput -> State.ChangePin.CanAndPinEntered(currentState.identificationPending, event.pin, currentState.can, currentState.newPin)
                    is State.Ident.PinInput -> State.Ident.CanAndPinEntered(currentState.can, event.pin)
                    else -> throw IllegalArgumentException()
                }
            }

            is Event.Back -> {
                when (val currentState = state.value.second) {
                    is State.ChangePin.IdAlreadySetup -> State.ChangePin.Intro(currentState.identificationPending, currentState.oldPin, currentState.newPin)
                    is State.ChangePin.PinReset -> State.ChangePin.IdAlreadySetup(currentState.identificationPending, currentState.oldPin, currentState.newPin)
                    is State.ChangePin.CanIntro -> if (!currentState.shortFlow) State.ChangePin.Intro(currentState.identificationPending, currentState.oldPin, currentState.newPin) else throw IllegalArgumentException()
                    is State.ChangePin.CanInput -> State.ChangePin.CanIntro(currentState.identificationPending, currentState.oldPin, currentState.newPin, currentState.shortFlow)
                    is State.ChangePin.CanInputRetry -> State.ChangePin.CanIntro(currentState.identificationPending, currentState.oldPin, currentState.newPin, true)
                    is State.ChangePin.PinInput -> State.ChangePin.CanInput(currentState.identificationPending, currentState.oldPin, currentState.newPin, false)

                    is State.Ident.PinReset -> State.Ident.Intro(currentState.pin)
                    is State.Ident.CanIntro -> State.Ident.Intro(currentState.pin)
                    is State.Ident.CanInput -> State.Ident.CanIntro(currentState.pin)
                    is State.Ident.CanInputRetry -> State.Ident.CanIntro(currentState.pin)
                    is State.Ident.PinInput -> State.Ident.CanInput(null)

                    else -> throw IllegalArgumentException()
                }
            }

            is Event.Invalidate -> State.Invalid
        }
    }
}
