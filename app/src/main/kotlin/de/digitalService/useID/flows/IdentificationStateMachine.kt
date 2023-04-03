package de.digitalService.useID.flows

import de.digitalService.useID.analytics.IssueTrackerManagerType
import de.digitalService.useID.getLogger
import de.digitalService.useID.idCardInterface.EidAuthenticationRequest
import de.digitalService.useID.idCardInterface.IdCardAttribute
import de.digitalService.useID.idCardInterface.IdCardInteractionException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

typealias AttributeConfirmationCallback = (Map<IdCardAttribute, Boolean>) -> Unit
typealias PinCallback = (String) -> Unit

@Singleton
class IdentificationStateMachine(initialState: State, private val issueTrackerManager: IssueTrackerManagerType) {
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
        class StartIdentification(val backingDownAllowed: Boolean, val tcTokenUrl: String) : State()
        class FetchingMetadata(val backingDownAllowed: Boolean, val tcTokenUrl: String) : State()
        class FetchingMetadataFailed(val backingDownAllowed: Boolean, val tcTokenUrl: String) : State()
        class RequestAttributeConfirmation(val backingDownAllowed: Boolean, val request: EidAuthenticationRequest, val confirmationCallback: AttributeConfirmationCallback) : State()
        class SubmitAttributeConfirmation(val backingDownAllowed: Boolean, val request: EidAuthenticationRequest, val confirmationCallback: AttributeConfirmationCallback) : State()
        class RevisitAttributes(val backingDownAllowed: Boolean, val request: EidAuthenticationRequest, val pinCallback: PinCallback) : State()
        class PinInput(val backingDownAllowed: Boolean, val request: EidAuthenticationRequest, val callback: PinCallback) : State()
        class PinInputRetry(val callback: PinCallback) : State()
        class PinEntered(val pin: String, val secondTime: Boolean, val callback: PinCallback) : State()
        class CanRequested(val pin: String?) : State()
        class WaitingForCardAttachment(val pin: String?) : State()
        class Finished(val redirectUrl: String) : State()

        object CardDeactivated : State()
        object CardBlocked : State()
        class CardUnreadable(val redirectUrl: String?) : State()

        object Invalid : State()
    }

    sealed class Event {
        data class Initialize(val backingDownAllowed: Boolean, val tcTokenUrl: String) : Event()
        object StartedFetchingMetadata : Event()
        data class FrameworkRequestsAttributeConfirmation(val request: EidAuthenticationRequest, val confirmationCallback: AttributeConfirmationCallback) : Event()
        object ConfirmAttributes : Event()
        data class FrameworkRequestsPin(val callback: PinCallback) : Event()
        data class EnterPin(val pin: String) : Event()
        object FrameworkRequestsCan : Event()
        object RequestCardInsertion : Event()
        object RetryAfterError : Event()
        data class Finish(val redirectUrl: String) : Event()

        data class Error(val exception: IdCardInteractionException) : Event()

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
            is Event.Initialize -> {
                State.StartIdentification(event.backingDownAllowed, event.tcTokenUrl)
            }

            is Event.StartedFetchingMetadata -> {
                when (val currentState = state.value.second) {
                    is State.StartIdentification -> State.FetchingMetadata(currentState.backingDownAllowed, currentState.tcTokenUrl)
                    else -> throw IllegalArgumentException()
                }
            }

            is Event.FrameworkRequestsAttributeConfirmation -> {
                when (val currentState = state.value.second) {
                    is State.FetchingMetadata -> State.RequestAttributeConfirmation(currentState.backingDownAllowed, event.request, event.confirmationCallback)
                    else -> throw IllegalArgumentException()
                }
            }

            is Event.ConfirmAttributes -> {
                when (val currentState = state.value.second) {
                    is State.RequestAttributeConfirmation -> State.SubmitAttributeConfirmation(currentState.backingDownAllowed, currentState.request, currentState.confirmationCallback)
                    is State.RevisitAttributes -> State.PinInput(currentState.backingDownAllowed, currentState.request, currentState.pinCallback)
                    else -> throw IllegalArgumentException()
                }
            }

            is Event.FrameworkRequestsPin -> {
                when (val currentState = state.value.second) {
                    is State.SubmitAttributeConfirmation -> State.PinInput(currentState.backingDownAllowed, currentState.request, event.callback)
                    is State.WaitingForCardAttachment -> State.PinInputRetry(event.callback)
                    else -> throw IllegalArgumentException()
                }
            }

            is Event.EnterPin -> {
                when (val currentState = state.value.second) {
                    is State.PinInput -> State.PinEntered(event.pin, false, currentState.callback)
                    is State.PinInputRetry -> State.PinEntered(event.pin, true, currentState.callback)
                    else -> throw IllegalArgumentException()
                }
            }

            is Event.FrameworkRequestsCan -> {
                when (val currentState = state.value.second) {
                    is State.WaitingForCardAttachment -> State.CanRequested(currentState.pin)
                    else -> throw IllegalArgumentException()
                }
            }

            is Event.RequestCardInsertion -> {
                when (val currentState = state.value.second) {
                    is State.PinEntered -> State.WaitingForCardAttachment(currentState.pin.takeIf { !currentState.secondTime })
                    is State.CanRequested -> State.WaitingForCardAttachment(currentState.pin)
                    else -> throw IllegalArgumentException()
                }
            }

            is Event.Finish -> {
                when (val currentState = state.value.second) {
                    is State.WaitingForCardAttachment -> State.Finished(event.redirectUrl)
                    is State.CanRequested -> State.Finished(event.redirectUrl)
                    else -> throw IllegalArgumentException()
                }
            }

            is Event.Error -> {
                fun nextState(): IdentificationStateMachine.State {
                    return when (event.exception) {
                        is IdCardInteractionException.CardDeactivated -> State.CardDeactivated
                        is IdCardInteractionException.CardBlocked -> State.CardBlocked
                        is IdCardInteractionException.ProcessFailed -> {
                            when (val currentState = state.value.second) {
                                is State.CardBlocked, is State.CardDeactivated -> currentState
                                else -> State.CardUnreadable(null)
                            }
                        }
                        else -> throw IllegalArgumentException()
                    }
                }

                when (val currentState = state.value.second) {
                    is State.FetchingMetadata -> State.FetchingMetadataFailed(currentState.backingDownAllowed, currentState.tcTokenUrl)
                    else -> nextState()
                }
            }

            is Event.RetryAfterError -> {
                when (val currentState = state.value.second) {
                    is State.FetchingMetadataFailed -> State.StartIdentification(currentState.backingDownAllowed, currentState.tcTokenUrl)
                    else -> throw IllegalArgumentException()
                }
            }

            is Event.Back -> {
                when (val currentState = state.value.second) {
                    is State.FetchingMetadata, is State.RequestAttributeConfirmation -> State.Invalid
                    is State.PinInput -> State.RevisitAttributes(currentState.backingDownAllowed, currentState.request, currentState.callback)
                    else -> throw IllegalArgumentException()
                }
            }

            is Event.Invalidate -> State.Invalid
        }
    }
}
