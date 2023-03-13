package de.digitalService.useID.flows

import de.digitalService.useID.getLogger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SetupStateMachine(initialState: State) {
    @Inject constructor() : this(State.Invalid)

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

        class Intro(val tcTokenUrl: String?) : State()
        class SkippingToIdentRequested(val tcTokenUrl: String) : State()
        class StartSetup(val tcTokenUrl: String?) : State()
        class PinReset(val tcTokenUrl: String?) : State()
        class PinManagement(val tcTokenUrl: String?) : State()
        class PinManagementFinished(val tcTokenUrl: String?) : State()
        class IdentAfterFinishedSetupRequested(val tcTokenUrl: String) : State()
        object SetupFinished : State()
    }

    sealed class Event {
        data class OfferSetup(val tcTokenUrl: String?) : Event()
        object SkipSetup : Event()
        object StartSetup : Event()
        object ResetPin : Event()
        object StartPinManagement : Event()
        object FinishPinManagement : Event()
        object ConfirmFinish : Event()

        object SubsequentFlowBackedDown : Event()
        object Back : Event()
        object Invalidate : Event()
    }

    fun transition(event: Event) {
        logger.debug("${state.value.second::class.simpleName}  ====${event::class.simpleName}===>  ???")
        val nextState = nextState(event)
        logger.debug("${state.value.second::class.simpleName}  ====${event::class.simpleName}===>  ${nextState::class.simpleName}")
        _state.value = Pair(event, nextState)
    }

    private fun nextState(event: Event): State {
        return when (event) {
            is Event.OfferSetup -> {
                when (state.value.second) {
                    is State.Invalid -> State.Intro(event.tcTokenUrl)
                    else -> throw IllegalArgumentException()
                }
            }

            is Event.SkipSetup -> {
                when (val currentState = state.value.second) {
                    is State.Intro -> if (currentState.tcTokenUrl != null) State.SkippingToIdentRequested(currentState.tcTokenUrl) else State.SetupFinished
                    is State.SkippingToIdentRequested -> State.SkippingToIdentRequested(currentState.tcTokenUrl)
                    else -> throw IllegalArgumentException()
                }
            }

            is Event.StartSetup -> {
                when (val currentState = state.value.second) {
                    is State.Intro -> State.StartSetup(currentState.tcTokenUrl)
                    is State.SkippingToIdentRequested -> State.StartSetup(currentState.tcTokenUrl)
                    else -> throw IllegalArgumentException()
                }
            }

            is Event.ResetPin -> {
                when (val currentState = state.value.second) {
                    is State.StartSetup -> State.PinReset(currentState.tcTokenUrl)
                    else -> throw IllegalArgumentException()
                }
            }

            is Event.StartPinManagement -> {
                when (val currentState = state.value.second) {
                    is State.StartSetup -> State.PinManagement(currentState.tcTokenUrl)
                    else -> throw IllegalArgumentException()
                }
            }

            is Event.FinishPinManagement -> {
                when (val currentState = state.value.second) {
                    is State.PinManagement -> State.PinManagementFinished(currentState.tcTokenUrl)
                    else -> throw IllegalArgumentException()
                }
            }

            is Event.ConfirmFinish -> {
                when (val currentState = state.value.second) {
                    is State.PinManagementFinished -> if (currentState.tcTokenUrl != null) State.IdentAfterFinishedSetupRequested(currentState.tcTokenUrl) else State.SetupFinished
                    else -> throw IllegalArgumentException()
                }
            }

            is Event.SubsequentFlowBackedDown -> {
                when (val currentState = state.value.second) {
                    is State.PinManagement -> State.StartSetup(currentState.tcTokenUrl)
                    else -> throw IllegalArgumentException()
                }
            }

            is Event.Back -> {
                when (val currentState = state.value.second) {
                    is State.StartSetup -> State.Intro(currentState.tcTokenUrl)
                    is State.PinReset -> State.StartSetup(currentState.tcTokenUrl)
                    else -> throw IllegalArgumentException()
                }
            }

            is Event.Invalidate -> State.Invalid
        }
    }
}
