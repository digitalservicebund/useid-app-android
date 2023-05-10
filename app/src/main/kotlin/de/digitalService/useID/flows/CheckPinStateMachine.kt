package de.digitalService.useID.flows

import de.digitalService.useID.analytics.IssueTrackerManagerType
import de.digitalService.useID.getLogger
import de.digitalService.useID.idCardInterface.IdCardInteractionException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CheckPinStateMachine(initialState: State, private val issueTrackerManager: IssueTrackerManagerType) {
    @Inject constructor(issueTrackerManager: IssueTrackerManagerType) : this(State.Invalid, issueTrackerManager)

    private val logger by getLogger()

    private val _state: MutableStateFlow<Pair<Event, State>> = MutableStateFlow(
        Pair(
            Event.Invalidate,
            initialState
        )
    )
    val state: StateFlow<Pair<Event, State>>
        get() = _state

    sealed class State {
        object Invalid : State()

        object StartIdCardInteraction : State()
        object ScanSuccess: State()
        object PinInput : State()
        class ReadyForSubsequentScan(val pin: String) : State()
        class FrameworkReadyForPinInput(val pin: String) : State()
        class FrameworkReadyForNewPinInput(val pin: String) : State()
        class CanRequested(val pin: String, val shortFlow: Boolean) : State()
        object Success: State()
        object Finished : State()
        object Cancelled : State()
        object PinRetry : State()

        object CardDeactivated : State()
        object CardBlocked : State()
        data class ProcessFailed(val pin: String?, val firstScan: Boolean) : State()
        object UnknownError : State()
    }

    sealed class Event {
        object StartPinCheck : Event()
        object CardDeactivated: Event()
        object EnterPin : Event()
        data class PinEntered(val pin: String): Event()
        object FrameworkRequestsPin : Event()
        object FrameworkRequestsNewPin : Event()
        object FrameworkRequestsCan : Event()
        object Success : Event()
        object Finish: Event()

        data class Error(val exception: IdCardInteractionException) : Event()
        object ProceedAfterError : Event()

        object Back : Event()
        object Invalidate : Event()
    }

    sealed class Error : kotlin.Error() {
        object PinConfirmationFailed : Error()
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
            is Event.StartPinCheck -> {
                when (state.value.second) {
                    is State.Invalid -> State.StartIdCardInteraction
                    else -> throw IllegalArgumentException()
                }
            }

            is Event.FrameworkRequestsPin -> {
                when (val currentState = state.value.second) {
                    is State.StartIdCardInteraction -> State.ScanSuccess
                    is State.FrameworkReadyForPinInput -> State.FrameworkReadyForPinInput(currentState.pin)
                    is State.ReadyForSubsequentScan -> State.FrameworkReadyForPinInput(currentState.pin)
                    else -> throw IllegalArgumentException()
                }
            }

            is Event.CardDeactivated -> {
                when (val currentState = state.value.second) {
                    is State.StartIdCardInteraction -> State.CardDeactivated
                    else -> throw IllegalArgumentException()
                }
            }

            is Event.EnterPin -> {
                when (val currentState = state.value.second) {
                    is State.ScanSuccess -> State.PinInput
                    else -> throw IllegalArgumentException()
                }
            }

            is Event.PinEntered -> {
                when (val currentState = state.value.second) {
                    is State.PinInput -> State.ReadyForSubsequentScan(event.pin)
                    else -> throw IllegalArgumentException()
                }
            }

            is Event.FrameworkRequestsNewPin -> {
                when (val currentState = state.value.second) {
                    is State.FrameworkReadyForPinInput -> State.FrameworkReadyForNewPinInput(currentState.pin)
                    is State.ReadyForSubsequentScan -> State.FrameworkReadyForNewPinInput(currentState.pin)
                    else -> throw IllegalArgumentException()
                }
            }

            is Event.FrameworkRequestsCan -> {
                when (val currentState = state.value.second) {
                    is State.ReadyForSubsequentScan -> State.CanRequested(currentState.pin, false)
                    is State.FrameworkReadyForPinInput -> State.CanRequested(currentState.pin, false)
                    else -> throw IllegalArgumentException()
                }
            }

            is Event.Success -> {
                when (state.value.second) {
                    is State.StartIdCardInteraction, is State.ReadyForSubsequentScan, is State.CanRequested, is State.FrameworkReadyForNewPinInput -> State.Success
                    else -> throw IllegalArgumentException()
                }
            }

            is Event.Finish -> {
                when (state.value.second) {
                    is State.Success -> State.Finished
                    else -> throw IllegalArgumentException()
                }
            }

            is Event.ProceedAfterError -> {
                when (val currentState = state.value.second) {
                    is State.ProcessFailed -> if (currentState.firstScan) State.StartIdCardInteraction else State.Cancelled
                    else -> throw IllegalArgumentException()
                }
            }

            is Event.Error -> {
                fun nextState(pin: String?, firstScan: Boolean): State {
                    return when (event.exception) {
                        is IdCardInteractionException.CardDeactivated -> State.CardDeactivated
                        is IdCardInteractionException.CardBlocked -> State.CardBlocked
                        is IdCardInteractionException.ProcessFailed -> State.ProcessFailed(pin, firstScan)
                        else -> State.UnknownError
                    }
                }

                when (val currentState = state.value.second) {
                    is State.FrameworkReadyForPinInput -> nextState(currentState.pin, true)
                    is State.CanRequested -> nextState(currentState.pin, false)
                    is State.StartIdCardInteraction -> nextState(null, true)
                    is State.ReadyForSubsequentScan -> nextState(currentState.pin, false)
                    else -> throw IllegalArgumentException()
                }
            }

            is Event.Back -> {
                when (val currentState = state.value.second) {
                    is State.StartIdCardInteraction -> State.Invalid
                    is State.ReadyForSubsequentScan -> State.PinInput
                    is State.PinInput -> State.ScanSuccess
//                    is State.FrameworkReadyForPinInput ->
                    else -> throw IllegalArgumentException()
                }
            }

            is Event.Invalidate -> State.Invalid
        }
    }
}
