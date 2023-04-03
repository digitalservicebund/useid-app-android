package de.digitalService.useID.flows

import de.digitalService.useID.analytics.IssueTrackerManagerType
import de.digitalService.useID.getLogger
import de.digitalService.useID.idCardInterface.IdCardInteractionException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

typealias PinManagementCallback = (String, String) -> Unit

@Singleton
class ChangePinStateMachine(initialState: State, private val issueTrackerManager: IssueTrackerManagerType) {
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

        class OldTransportPinInput(val identificationPending: Boolean) : State()
        object OldPersonalPinInput : State()
        class NewPinIntro(val identificationPending: Boolean, val transportPin: Boolean, val oldPin: String) : State()
        class NewPinInput(val identificationPending: Boolean, val transportPin: Boolean, val oldPin: String) : State()
        class NewPinConfirmation(val identificationPending: Boolean, val transportPin: Boolean, val oldPin: String, val newPin: String) : State()
        class ReadyForScan(val identificationPending: Boolean, val transportPin: Boolean, val oldPin: String, val newPin: String) : State()
        class WaitingForFirstCardAttachment(val identificationPending: Boolean, val transportPin: Boolean, val oldPin: String, val newPin: String) : State()
        class WaitingForCardReAttachment(val identificationPending: Boolean, val transportPin: Boolean, val oldPin: String, val newPin: String) : State()
        class FrameworkReadyForPinInput(val identificationPending: Boolean, val transportPin: Boolean, val oldPin: String, val newPin: String) : State()
        class FrameworkReadyForNewPinInput(val identificationPending: Boolean, val transportPin: Boolean, val oldPin: String, val newPin: String) : State()
        class CanRequested(val identificationPending: Boolean, val transportPin: Boolean, val oldPin: String, val newPin: String, val shortFlow: Boolean) : State()
        object Finished : State()
        object Cancelled : State()
        class OldTransportPinRetry(val identificationPending: Boolean, val newPin: String) : State()
        class OldPersonalPinRetry(val newPin: String) : State()

        object CardDeactivated : State()
        object CardBlocked : State()
        data class ProcessFailed(val identificationPending: Boolean, val transportPin: Boolean, val oldPin: String, val newPin: String, val firstScan: Boolean) : State()
        object UnknownError : State()
    }

    sealed class Event {
        data class StartPinChange(val identificationPending: Boolean, val transportPin: Boolean) : Event()
        data class EnterOldPin(val oldPin: String) : Event()
        object ConfirmNewPinIntro : Event()
        data class EnterNewPin(val newPin: String) : Event()
        data class ConfirmNewPin(val newPin: String) : Event()
        object RetryNewPinConfirmation : Event()
        object RequestCardInsertion : Event()
        object FrameworkRequestsPin : Event()
        object FrameworkRequestsNewPin : Event()
        object FrameworkRequestsCan : Event()
        object Finish : Event()

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
            is Event.StartPinChange -> {
                when (state.value.second) {
                    is State.Invalid -> if (event.transportPin) State.OldTransportPinInput(event.identificationPending) else State.OldPersonalPinInput
                    else -> throw IllegalArgumentException()
                }
            }

            is Event.EnterOldPin -> {
                when (val currentState = state.value.second) {
                    is State.OldTransportPinInput -> State.NewPinIntro(currentState.identificationPending, true, event.oldPin)
                    is State.OldPersonalPinInput -> State.NewPinIntro(false, false, event.oldPin)
                    is State.OldTransportPinRetry -> State.FrameworkReadyForPinInput(currentState.identificationPending, true, event.oldPin, currentState.newPin)
                    is State.OldPersonalPinRetry -> State.FrameworkReadyForPinInput(false, false, event.oldPin, currentState.newPin)
                    else -> throw IllegalArgumentException()
                }
            }

            is Event.ConfirmNewPinIntro -> {
                when (val currentState = state.value.second) {
                    is State.NewPinIntro -> State.NewPinInput(currentState.identificationPending, currentState.transportPin, currentState.oldPin)
                    else -> throw IllegalArgumentException()
                }
            }

            is Event.RetryNewPinConfirmation -> {
                when (val currentState = state.value.second) {
                    is State.NewPinConfirmation -> State.NewPinInput(currentState.identificationPending, currentState.transportPin, currentState.oldPin)
                    else -> throw IllegalArgumentException()
                }
            }

            is Event.EnterNewPin -> {
                when (val currentState = state.value.second) {
                    is State.NewPinInput -> State.NewPinConfirmation(currentState.identificationPending, currentState.transportPin, currentState.oldPin, event.newPin)
                    else -> throw IllegalArgumentException()
                }
            }

            is Event.ConfirmNewPin -> {
                when (val currentState = state.value.second) {
                    is State.NewPinConfirmation -> if (event.newPin == currentState.newPin) State.ReadyForScan(currentState.identificationPending, currentState.transportPin, currentState.oldPin, event.newPin) else throw Error.PinConfirmationFailed
                    else -> throw IllegalArgumentException()
                }
            }

            is Event.RequestCardInsertion -> {
                when (val currentState = state.value.second) {
                    is State.ReadyForScan -> State.WaitingForFirstCardAttachment(currentState.identificationPending, currentState.transportPin, currentState.oldPin, currentState.newPin)
                    is State.FrameworkReadyForPinInput -> State.WaitingForCardReAttachment(currentState.identificationPending, currentState.transportPin, currentState.oldPin, currentState.newPin)
                    is State.FrameworkReadyForNewPinInput -> State.WaitingForCardReAttachment(currentState.identificationPending, currentState.transportPin, currentState.oldPin, currentState.newPin)
                    is State.CanRequested -> State.WaitingForCardReAttachment(currentState.identificationPending, currentState.transportPin, currentState.oldPin, currentState.newPin)
                    is State.WaitingForFirstCardAttachment -> currentState
                    else -> throw IllegalArgumentException()
                }
            }

            is Event.FrameworkRequestsPin -> {
                when (val currentState = state.value.second) {
                    is State.WaitingForFirstCardAttachment -> State.FrameworkReadyForPinInput(currentState.identificationPending, currentState.transportPin, currentState.oldPin, currentState.newPin)
                    is State.FrameworkReadyForPinInput -> if (currentState.transportPin) State.OldTransportPinRetry(currentState.identificationPending, currentState.newPin) else State.OldPersonalPinRetry(currentState.newPin)
                    else -> throw IllegalArgumentException()
                }
            }

            is Event.FrameworkRequestsNewPin -> {
                when (val currentState = state.value.second) {
                    is State.FrameworkReadyForPinInput -> State.FrameworkReadyForNewPinInput(currentState.identificationPending, currentState.transportPin, currentState.oldPin, currentState.newPin)
                    is State.WaitingForCardReAttachment -> State.FrameworkReadyForNewPinInput(currentState.identificationPending, currentState.transportPin, currentState.oldPin, currentState.newPin)
                    else -> throw IllegalArgumentException()
                }
            }

            is Event.FrameworkRequestsCan -> {
                when (val currentState = state.value.second) {
                    is State.WaitingForFirstCardAttachment -> State.CanRequested(currentState.identificationPending, currentState.transportPin, currentState.oldPin, currentState.newPin, true)
                    is State.WaitingForCardReAttachment -> State.CanRequested(currentState.identificationPending, currentState.transportPin, currentState.oldPin, currentState.newPin, false)
//                    is State.FrameworkReadyForPinManagement -> State.CanRequested(currentState.identificationPending, currentState.transportPin, currentState.oldPin, currentState.newPin, false)
                    else -> throw IllegalArgumentException()
                }
            }

            is Event.Finish -> {
                when (state.value.second) {
//                    is State.FrameworkReadyForPinManagement, is State.WaitingForFirstCardAttachment, is State.WaitingForCardReAttachment, is State.CanRequested -> State.Finished
                    is State.FrameworkReadyForNewPinInput -> State.Finished
                    else -> throw IllegalArgumentException()
                }
            }

            is Event.ProceedAfterError -> {
                when (val currentState = state.value.second) {
                    is State.ProcessFailed -> if (currentState.firstScan) State.ReadyForScan(currentState.identificationPending, currentState.transportPin, currentState.oldPin, currentState.newPin) else State.Cancelled
                    else -> throw IllegalArgumentException()
                }
            }

            is Event.Error -> {
                fun nextState(identificationPending: Boolean, transportPin: Boolean, oldPin: String, newPin: String, firstScan: Boolean): State {
                    return when (event.exception) {
                        is IdCardInteractionException.CardDeactivated -> State.CardDeactivated
                        is IdCardInteractionException.CardBlocked -> State.CardBlocked
//                        is IdCardInteractionException.ProcessFailed -> State.ProcessFailed(identificationPending, transportPin, oldPin, newPin, firstScan)
                        else -> State.UnknownError
                    }
                }

                when (val currentState = state.value.second) {
//                    is State.FrameworkReadyForPinManagement -> nextState(currentState.identificationPending, currentState.transportPin, currentState.oldPin, currentState.newPin, true)
                    is State.WaitingForFirstCardAttachment -> nextState(currentState.identificationPending, currentState.transportPin, currentState.oldPin, currentState.newPin, true)
                    is State.WaitingForCardReAttachment -> nextState(currentState.identificationPending, currentState.transportPin, currentState.oldPin, currentState.newPin, false)
                    else -> throw IllegalArgumentException()
                }
            }

            is Event.Back -> {
                when (val currentState = state.value.second) {
                    is State.OldTransportPinInput -> State.Invalid
                    is State.OldPersonalPinInput -> State.Invalid
                    is State.NewPinIntro -> if (currentState.transportPin) State.OldTransportPinInput(currentState.identificationPending) else State.OldPersonalPinInput
                    is State.NewPinInput -> State.NewPinIntro(currentState.identificationPending, currentState.transportPin, currentState.oldPin)
                    is State.NewPinConfirmation -> State.NewPinInput(currentState.identificationPending, currentState.transportPin, currentState.oldPin)
                    is State.WaitingForFirstCardAttachment -> State.NewPinInput(currentState.identificationPending, currentState.transportPin, currentState.oldPin)
                    else -> throw IllegalArgumentException()
                }
            }

            is Event.Invalidate -> State.Invalid
        }
    }
}
