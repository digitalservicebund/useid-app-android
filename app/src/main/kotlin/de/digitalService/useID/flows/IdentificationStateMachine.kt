package de.digitalService.useID.flows

import android.net.Uri
import de.digitalService.useID.analytics.IssueTrackerManagerType
import de.digitalService.useID.getLogger
import de.digitalService.useID.idCardInterface.AuthenticationRequest
import de.digitalService.useID.idCardInterface.CertificateDescription
import de.digitalService.useID.idCardInterface.EidInteractionException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IdentificationStateMachine(initialState: State, private val issueTrackerManager: IssueTrackerManagerType) {
    @Inject
    constructor(issueTrackerManager: IssueTrackerManagerType) : this(State.Invalid, issueTrackerManager)

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
        class StartIdentification(val backingDownAllowed: Boolean, val tcTokenUrl: Uri) : State()
        class FetchingMetadata(val backingDownAllowed: Boolean, val tcTokenUrl: Uri) : State()
        class FetchingMetadataFailed(val backingDownAllowed: Boolean, val tcTokenUrl: Uri) : State()
        class RequestCertificate(val backingDownAllowed: Boolean, val request: AuthenticationRequest) : State()
        class CertificateDescriptionReceived(val backingDownAllowed: Boolean, val authenticationRequest: AuthenticationRequest, val certificateDescription: CertificateDescription) : State()
        class PinInput(val backingDownAllowed: Boolean, val authenticationRequest: AuthenticationRequest, val certificateDescription: CertificateDescription) : State()
        object PinInputRetry : State()
        class PinEntered(val pin: String, val firstTime: Boolean) : State()
        class PinRequested(val pin: String) : State()
        class CanRequested(val pin: String?) : State()
        class Finished(val redirectUrl: String) : State()

        object CardDeactivated : State()
        object CardBlocked : State()
        class CardUnreadable(val redirectUrl: String?) : State()

        object Invalid : State()
    }

    sealed class Event {
        data class Initialize(val backingDownAllowed: Boolean, val tcTokenUrl: Uri) : Event()
        object StartedFetchingMetadata : Event()
        data class CertificateDescriptionReceived(val certificateDescription: CertificateDescription) : Event()
        data class FrameworkRequestsAttributeConfirmation(val authenticationRequest: AuthenticationRequest) : Event()
        object ConfirmAttributes : Event()
        data class FrameworkRequestsPin(val firstAttempt: Boolean) : Event()
        data class EnterPin(val pin: String) : Event()
        object FrameworkRequestsCan : Event()
        object RetryAfterError : Event()
        data class Finish(val redirectUrl: String) : Event()

        data class Error(val exception: EidInteractionException) : Event()

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

            is Event.CertificateDescriptionReceived -> {
                when (val currentState = state.value.second) {
                    is State.RequestCertificate -> State.CertificateDescriptionReceived(currentState.backingDownAllowed, currentState.request, event.certificateDescription)
                    else -> throw IllegalArgumentException()
                }
            }

            is Event.FrameworkRequestsAttributeConfirmation -> {
                when (val currentState = state.value.second) {
                    is State.FetchingMetadata -> State.RequestCertificate(currentState.backingDownAllowed, event.authenticationRequest)
                    else -> throw IllegalArgumentException()
                }
            }

            is Event.ConfirmAttributes -> {
                when (val currentState = state.value.second) {
                    is State.CertificateDescriptionReceived -> State.PinInput(currentState.backingDownAllowed, currentState.authenticationRequest, currentState.certificateDescription)
                    else -> throw IllegalArgumentException()
                }
            }

            is Event.FrameworkRequestsPin -> {
                when (val currentState = state.value.second) {
                    is State.PinRequested -> if (!event.firstAttempt) State.PinInputRetry else State.PinRequested(currentState.pin)
                    is State.PinEntered -> State.PinRequested(currentState.pin)
                    else -> throw IllegalArgumentException()
                }
            }

            is Event.EnterPin -> {
                when (val currentState = state.value.second) {
                    is State.PinInput -> State.PinEntered(event.pin, true)
                    is State.PinInputRetry -> State.PinEntered(event.pin, false)
                    else -> throw IllegalArgumentException()
                }
            }

            is Event.FrameworkRequestsCan -> {
                when (val currentState = state.value.second) {
                    is State.PinEntered -> State.CanRequested(currentState.pin.takeIf { currentState.firstTime })
                    is State.PinRequested -> State.CanRequested(currentState.pin)
                    else -> throw IllegalArgumentException()
                }
            }

            is Event.Finish -> {
                when (val currentState = state.value.second) {
                    is State.PinEntered -> State.Finished(event.redirectUrl)
                    is State.CanRequested -> State.Finished(event.redirectUrl)
                    is State.PinRequested -> State.Finished(event.redirectUrl)
                    else -> throw IllegalArgumentException()
                }
            }

            is Event.Error -> {
                fun nextState(): State {
                    return when (val exception = event.exception) {
                        is EidInteractionException.CardDeactivated -> State.CardDeactivated
                        is EidInteractionException.CardBlocked -> State.CardBlocked
                        is EidInteractionException.ProcessFailed -> {
                            when (val currentState = state.value.second) {
                                is State.CardBlocked, is State.CardDeactivated -> currentState
                                else -> State.CardUnreadable(exception.redirectUrl)
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
                    is State.FetchingMetadata, is State.RequestCertificate, is State.CertificateDescriptionReceived -> State.Invalid
                    is State.PinInput -> State.CertificateDescriptionReceived(currentState.backingDownAllowed, currentState.authenticationRequest, currentState.certificateDescription)
                    else -> throw IllegalArgumentException()
                }
            }

            is Event.Invalidate -> State.Invalid
        }
    }
}
