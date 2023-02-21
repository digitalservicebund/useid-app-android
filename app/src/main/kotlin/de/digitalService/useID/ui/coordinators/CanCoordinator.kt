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
        // TODO: Refactor: Introduce base class for PinManagement and Ident states
        // TODO: Refactor: Get rid of nullable callbacks by set initial state later in id card framework callback

        object Invalid : State()

        class IntroForPinManagement(val oldPin: String, val newPin: String?, val callback: PinManagementCallback?) : State()
        class IdAlreadySetup(val oldPin: String, val newPin: String?, val callback: PinManagementCallback) : State()
        class PinResetForPinManagement(val oldPin: String, val newPin: String?, val callback: PinManagementCallback) : State()
        class CanIntroForPinManagement(val oldPin: String, val newPin: String?, val callback: PinManagementCallback?) : State()
        class CanInputForPinManagement(val oldPin: String, val newPin: String?, val callback: PinManagementCallback) : State()
        class CanInputForPinManagementRetry(val oldPin: String, val newPin: String, val callback: PinManagementCallback) : State()
        class PinInputForPinManagement(val oldPin: String, val can: String, val callback: PinManagementCallback) : State()
        class CanAndPinEnteredForPinManagement(val can: String, val oldPin: String, val newPin: String, val callback: PinManagementCallback) : State()

        class IntroForIdent(val pin: String?, val callback: PinCallback?) : State()
        class PinResetForIdent(val pin: String?, val callback: PinCallback) : State()
        class CanIntroForIdent(val pin: String?, val callback: PinCallback?) : State()
        class CanIntroWithoutFlowIntroForIdent(val pin: String, val callback: PinCallback?) : State()
        class CanInputForIdent(val pin: String?, val callback: PinCallback) : State()
        class CanInputForIdentRetry(val pin: String, val callback: PinCallback) : State()
        class PinInputForIdent(val can: String, val callback: PinCallback) : State()
        class CanAndPinEnteredForIdent(val can: String, val pin: String, val callback: PinCallback) : State()
    }

    sealed class Event {
        class InitializeForPinManagement(val shortFlow: Boolean, val oldPin: String, val newPin: String): Event()
        class InitializeForIdent(val pin: String?): Event()

        object AgreeToThirdAttempt: Event()
        object DenyThirdAttempt: Event()

        object ResetPin: Event()
        object ConfirmCanIntro: Event()

        class RegisterPinManagementCallback(val callback: PinManagementCallback): Event()
        class RegisterPinCallback(val callback: PinCallback): Event()

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
            is Event.InitializeForPinManagement -> if (event.shortFlow) State.CanIntroForPinManagement(event.oldPin, event.newPin, null) else State.IntroForPinManagement(event.oldPin, null, null)
            is Event.InitializeForIdent -> if (event.pin != null) State.CanIntroWithoutFlowIntroForIdent(event.pin, null) else State.IntroForIdent(null, null)

            is Event.RegisterPinManagementCallback -> {
                when (val currentState = state.value.second) {
                    is State.IntroForPinManagement -> State.IntroForPinManagement(currentState.oldPin, currentState.newPin, event.callback)
                    is State.CanIntroForPinManagement -> State.CanIntroForPinManagement(currentState.oldPin, currentState.newPin, event.callback)
                    is State.CanAndPinEnteredForPinManagement -> State.CanInputForPinManagementRetry(currentState.oldPin, currentState.newPin, event.callback)
                    else -> throw IllegalArgumentException()
                }
            }

            is Event.RegisterPinCallback -> {
                when (val currentState = state.value.second) {
                    is State.IntroForIdent -> State.IntroForIdent(currentState.pin, event.callback)
                    is State.CanIntroWithoutFlowIntroForIdent -> State.CanIntroWithoutFlowIntroForIdent(currentState.pin, event.callback)
                    is State.CanAndPinEnteredForIdent -> State.CanInputForIdentRetry(currentState.pin, event.callback)
                    else -> throw IllegalArgumentException()
                }
            }

            is Event.AgreeToThirdAttempt -> {
                when (val currentState = state.value.second) {
                    is State.IntroForPinManagement -> State.CanIntroForPinManagement(currentState.oldPin, currentState.newPin, currentState.callback)
                    is State.IntroForIdent -> if (currentState.callback != null) State.CanIntroForIdent(currentState.pin, currentState.callback) else throw IllegalArgumentException()
                    else -> throw IllegalArgumentException()
                }
            }

            is Event.DenyThirdAttempt -> {
                when (val currentState = state.value.second) {
                    is State.IntroForPinManagement -> if (currentState.callback != null) State.IdAlreadySetup(currentState.oldPin, currentState.newPin, currentState.callback) else throw IllegalArgumentException()
                    else -> throw IllegalArgumentException()
                }
            }

            is Event.ResetPin -> {
                when (val currentState = state.value.second) {
                    is State.IdAlreadySetup -> State.PinResetForPinManagement(currentState.oldPin, currentState.newPin, currentState.callback)
                    is State.IntroForIdent -> if (currentState.callback != null) State.PinResetForIdent(currentState.pin, currentState.callback) else throw IllegalArgumentException()
                    else -> throw IllegalArgumentException()
                }
            }

            is Event.ConfirmCanIntro -> {
                when (val currentState = state.value.second) {
                    is State.CanIntroForPinManagement -> if (currentState.callback != null) State.CanInputForPinManagement(currentState.oldPin, currentState.newPin, currentState.callback) else throw IllegalArgumentException()
                    is State.CanIntroForIdent -> if (currentState.callback != null) State.CanInputForIdent(currentState.pin, currentState.callback) else throw IllegalArgumentException()
                    else -> throw IllegalArgumentException()
                }
            }

            is Event.EnterCan -> {
                when (val currentState = state.value.second) {
                    is State.CanInputForPinManagement -> if (currentState.newPin != null) State.CanAndPinEnteredForPinManagement(event.can, currentState.oldPin, currentState.newPin, currentState.callback) else State.PinInputForPinManagement(currentState.oldPin, event.can, currentState.callback)
                    is State.CanAndPinEnteredForPinManagement -> State.CanAndPinEnteredForPinManagement(event.can, currentState.oldPin, currentState.newPin, currentState.callback)

                    is State.CanInputForIdent -> if (currentState.pin != null) State.CanAndPinEnteredForIdent(event.can, currentState.pin, currentState.callback) else State.PinInputForIdent(event.can, currentState.callback)
                    is State.CanAndPinEnteredForIdent -> State.CanAndPinEnteredForIdent(event.can, currentState.pin, currentState.callback)
                    else -> throw IllegalArgumentException()
                }
            }

            is Event.EnterPin -> {
                when (val currentState = state.value.second) {
                    is State.PinInputForPinManagement -> State.CanAndPinEnteredForPinManagement(currentState.can, currentState.oldPin, event.pin, currentState.callback)
                    is State.PinInputForIdent -> State.CanAndPinEnteredForIdent(currentState.can, event.pin, currentState.callback)
                    else -> throw IllegalArgumentException()
                }
            }

            is Event.Back -> {
                when (val currentState = state.value.second) {
                    is State.IdAlreadySetup -> State.IntroForPinManagement(currentState.oldPin, currentState.newPin, currentState.callback)
                    is State.PinResetForPinManagement -> State.IdAlreadySetup(currentState.oldPin, currentState.newPin, currentState.callback)
                    is State.CanIntroForPinManagement -> if (currentState.newPin == null) State.IntroForPinManagement(currentState.oldPin, null, currentState.callback) else throw IllegalArgumentException()
                    is State.CanInputForPinManagement -> State.CanIntroForPinManagement(currentState.oldPin, currentState.newPin, currentState.callback)
                    is State.CanInputForPinManagementRetry -> State.CanIntroForPinManagement(currentState.oldPin, currentState.newPin, currentState.callback)
                    is State.PinInputForPinManagement -> State.CanInputForPinManagement(currentState.oldPin, null, currentState.callback)

                    is State.PinResetForIdent -> State.IntroForIdent(currentState.pin, currentState.callback)
                    is State.CanIntroForIdent -> State.IntroForIdent(currentState.pin, currentState.callback)
                    is State.CanInputForIdent -> State.CanIntroForIdent(currentState.pin, currentState.callback)
                    is State.CanInputForIdentRetry -> State.CanIntroForIdent(currentState.pin, currentState.callback)
                    is State.PinInputForIdent -> State.CanInputForIdent(null, currentState.callback)

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
                        is CanFsm.State.IntroForPinManagement -> navigator.navigate(SetupCanConfirmTransportPinDestination(state.oldPin))
                        is CanFsm.State.IdAlreadySetup -> navigator.navigate(SetupCanAlreadySetupDestination)
                        is CanFsm.State.PinResetForPinManagement, is CanFsm.State.PinResetForIdent -> navigator.navigate(CanResetPersonalPinDestination)
                        is CanFsm.State.CanIntroForPinManagement -> navigator.navigate(SetupCanIntroDestination(true))
                        is CanFsm.State.CanInputForPinManagement -> navigator.navigate(CanInputDestination(false))
                        is CanFsm.State.CanInputForPinManagementRetry -> {
                            navigator.navigate(SetupCanIntroDestination(false))
                            navigator.navigate(CanInputDestination(true))
                        }
                        is CanFsm.State.PinInputForPinManagement -> navigator.navigate(SetupCanTransportPinDestination)
                        is CanFsm.State.CanAndPinEnteredForPinManagement -> state.callback(state.oldPin, state.can, state.newPin)

                        is CanFsm.State.IntroForIdent -> navigator.navigate(IdentificationCanPinForgottenDestination)
                        is CanFsm.State.CanIntroForIdent -> navigator.navigate(IdentificationCanIntroDestination(true))
                        is CanFsm.State.CanIntroWithoutFlowIntroForIdent -> navigator.navigate(IdentificationCanIntroDestination(false))
                        is CanFsm.State.CanInputForIdent -> navigator.navigate(CanInputDestination(false))
                        is CanFsm.State.CanInputForIdentRetry -> {
                            navigator.navigate(IdentificationCanPinForgottenDestination)
                            navigator.navigate(IdentificationCanIntroDestination(true))
                            navigator.navigate(CanInputDestination(true))
                        }
                        is CanFsm.State.PinInputForIdent -> navigator.navigate(IdentificationCanPinInputDestination)
                        is CanFsm.State.CanAndPinEnteredForIdent -> state.callback(state.pin, state.can)

                        CanFsm.State.Invalid -> logger.debug("Ignoring transition to invalid state.")
                    }
                }
            }
        }
    }

    fun startPinManagementCanFlow(shortFlow: Boolean, oldPin: String, newPin: String): Flow<SubCoordinatorState> {
        flowStateMachine.transition(CanFsm.Event.InitializeForPinManagement(shortFlow, oldPin, newPin))
        return startCanFlow()
    }

    fun startIdentCanFlow(pin: String?): Flow<SubCoordinatorState> {
        flowStateMachine.transition(CanFsm.Event.InitializeForIdent(pin))
        return startCanFlow()
    }

    private fun startCanFlow(): Flow<SubCoordinatorState> {
        _stateFlow.value = SubCoordinatorState.ACTIVE
        collectEidEvents()
        return stateFlow
    }

    fun onResetPin() {
//        navigator.navigate(ResetPersonalPinDestination)
        flowStateMachine.transition(CanFsm.Event.ResetPin)
    }

    fun confirmPinInput() {
//        navigator.navigate(SetupCanAlreadySetupDestination)
        flowStateMachine.transition(CanFsm.Event.DenyThirdAttempt)
    }

    fun proceedWithThirdAttempt() {
        flowStateMachine.transition(CanFsm.Event.AgreeToThirdAttempt)

//        when (flowStateMachine.state) {
//            is CanFsm.State.CanInputForPinManagement -> navigator.navigate(SetupCanIntroDestination(true))
//            is CanFsm.State.CanIntroForIdent, is CanFsm.State.CanAndPinEnteredForIdent -> navigator.navigate(IdentificationCanIntroDestination(true))
//            else -> logger.error("Requested to proceed with third PIN attempt unexpected in state ${flowStateMachine.state}")
//        }
    }

    fun finishIntro() {
//        navigator.navigate(CanInputDestination(false))
        flowStateMachine.transition(CanFsm.Event.ConfirmCanIntro)
    }

    fun onCanEntered(can: String) {
        flowStateMachine.transition(CanFsm.Event.EnterCan(can))

//        when (flowStateMachine.transition(CanFsm.Event.EnterCan(can))) {
//            is CanFsm.State.PinInputForPinManagement -> navigator.navigate(SetupCanTransportPinDestination)
//            is CanFsm.State.PinInputForIdent -> navigator.navigate(IdentificationCanPinInputDestination)
//            is CanFsm.State.CanAndPinEnteredForPinManagement, is CanFsm.State.CanAndPinEnteredForIdent -> executeCanStep()
//            else -> throw IllegalStateException()
//        }
    }

    fun onPinEntered(pin: String) {
        flowStateMachine.transition(CanFsm.Event.EnterPin(pin))
//        executeCanStep()
    }

//    private fun executeCanStep() {
//        when (val currentState = flowStateMachine.state) {
////            is CanFsm.State.CanAndPinEnteredForPinManagement -> currentState.callback(currentState.pin, currentState.can, currentState.newPin)
//            is CanFsm.State.CanAndPinEnteredForIdent -> currentState.callback(currentState.pin, currentState.can)
//            else -> throw IllegalStateException()
//        }
//    }

    fun onBack() {
        flowStateMachine.transition(CanFsm.Event.Back)
//        navigator.pop()
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
                        flowStateMachine.transition(CanFsm.Event.RegisterPinCallback(event.pinCanCallback))

//                        when (val newState = flowStateMachine.transition(CanFsm.Event.RegisterPinCallback(event.pinCanCallback))) {
//                            is CanFsm.State.CanIntroForIdent -> {
//                                if (newState.pin == null) {
//                                    navigator.navigate(IdentificationCanPinForgottenDestination)
//                                } else {
//                                    navigator.navigate(IdentificationCanIntroDestination(false))
//                                }
//                            }
//
//                            is CanFsm.State.CanAndPinEnteredForIdent -> {
//                                navigator.navigate(IdentificationCanPinForgottenDestination)
//                                navigator.navigate(IdentificationCanIntroDestination(true))
//                                navigator.navigate(CanInputDestination(true))
//                            }
//
//                            else -> throw IllegalStateException()
//                        }
                    }
                    is EidInteractionEvent.RequestCanAndChangedPin -> {
                        flowStateMachine.transition(CanFsm.Event.RegisterPinManagementCallback(event.pinCallback))

//                        when (val currentState = flowStateMachine.state) {
//                            is CanFsm.State.InitializedForPinManagement -> {
//                                if (currentState.shortFlow) {
//                                    navigator.navigate(SetupCanIntroDestination(false))
//                                } else {
//                                    navigator.navigate(SetupCanConfirmTransportPinDestination(currentState.oldPin))
//                                }
//                            }
//                            is CanFsm.State.CanAndPinEnteredForPinManagement -> {
//                                navigator.navigate(SetupCanIntroDestination(false))
//                                navigator.navigate(CanInputDestination(true))
//                            }
//                            else -> throw IllegalStateException()
//                        }
//
//                        flowStateMachine.transition(CanFsm.Event.RegisterPinManagementCallback(event.pinCallback))
                    }
                    is EidInteractionEvent.AuthenticationSuccessful, EidInteractionEvent.ProcessCompletedSuccessfullyWithoutResult, is EidInteractionEvent.ProcessCompletedSuccessfullyWithRedirect -> finishCanFlow()
                    is EidInteractionEvent.Error -> finishCanFlow()
                    else -> logger.debug("Ignoring event: $event")
                }
            }
        }
    }
}
