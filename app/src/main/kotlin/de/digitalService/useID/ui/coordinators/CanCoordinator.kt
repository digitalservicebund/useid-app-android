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
private typealias ChangePinCallback = (String, String, String) -> Unit

@Singleton
class CanFsm @Inject constructor() {
    var state: State = State.Invalid

    sealed class State {
        object Invalid : State()
        class InitializedForPinManagement(val shortFlow: Boolean, val oldPin: String, private val newPin: String) : State() {
            fun advance(callback: ChangePinCallback): State = RequestedCanAndPinForPinManagement(oldPin.takeIf { shortFlow }, newPin, callback)
        }
        class InitializedForIdent(val pin: String?) : State() {
            fun advance(callback: PinCallback): State = RequestedCanAndPinForIdent(pin, callback)
        }
        class RequestedCanAndPinForPinManagement(val oldPin: String?, private val newPin: String, private val callback: ChangePinCallback) : State() {
            fun advance(can: String): State = if (oldPin != null) CanAndPinEnteredForPinManagement(can, oldPin, newPin, callback) else CanEnteredForPinManagement(can, newPin, callback)
        }
        class RequestedCanAndPinForIdent(val pin: String?, private val callback: PinCallback) : State() {
            fun advance(can: String): State = if (pin != null) CanAndPinEnteredForIdent(can, pin, callback) else CanEnteredForIdent(can, callback)
        }
        class CanEnteredForPinManagement(private val can: String, private val newPin: String, private val callback: ChangePinCallback) : State(), CanEntered {
            override fun back(): State = RequestedCanAndPinForPinManagement(null, newPin, callback)
            override fun advance(pin: String): State = CanAndPinEnteredForPinManagement(can, pin, newPin, callback)
        }
        class CanEnteredForIdent(private val can: String, private val callback: PinCallback) : State(), CanEntered {
            override fun back(): State = RequestedCanAndPinForIdent(null, callback)
            override fun advance(pin: String): State = CanAndPinEnteredForIdent(can, pin, callback)
        }
        class CanAndPinEnteredForPinManagement(private var can: String, private val pin: String, private val newPin: String, private var callback: ChangePinCallback) : State(), CanAndPinEntered {
            fun setNewCallback(callback: ChangePinCallback) { this.callback = callback }
            override fun setNewCan(can: String) {
                this.can = can
            }
            override fun executeCanStep() = callback(pin, can, newPin)
        }
        class CanAndPinEnteredForIdent(private var can: String, private val pin: String, private var callback: PinCallback) : State(), CanAndPinEntered {
            fun setNewCallback(callback: PinCallback) { this.callback = callback }
            override fun setNewCan(can: String) {
                this.can = can
            }
            override fun executeCanStep() = callback(pin, can)
        }

        interface CanEntered {
            fun back(): State
            fun advance(pin: String): State
        }
        interface CanAndPinEntered {
            fun setNewCan(can: String)
            fun executeCanStep()
        }
    }
}

@Singleton
class CanCoordinator @Inject constructor(
    private val navigator: Navigator,
    private val idCardManager: IdCardManager,
    private val flowStateMachine: CanFsm,
    private val coroutineContextProvider: CoroutineContextProviderType
) {
    private val logger by getLogger()

//    private var state: CanFsm.State = CanFsm.State.Invalid

    private var eIdEventFlowCoroutineScope: Job? = null

    private val _stateFlow: MutableStateFlow<SubCoordinatorState> = MutableStateFlow(SubCoordinatorState.FINISHED)
    val stateFlow: StateFlow<SubCoordinatorState>
        get() = _stateFlow

    fun startIdentCanFlow(pin: String?): Flow<SubCoordinatorState> {
        flowStateMachine.state = CanFsm.State.InitializedForIdent(pin)
        return startCanFlow()
    }

    fun startPinManagementCanFlow(shortFlow: Boolean, oldPin: String, newPin: String): Flow<SubCoordinatorState> {
        flowStateMachine.state = CanFsm.State.InitializedForPinManagement(shortFlow, oldPin, newPin)
        return startCanFlow()
    }

    private fun startCanFlow(): Flow<SubCoordinatorState> {
        collectEidEvents()

        _stateFlow.value = SubCoordinatorState.ACTIVE
        return stateFlow
    }

    fun onResetPin() {
        navigator.navigate(ResetPersonalPinDestination)
    }

    fun confirmPinInput() {
        navigator.navigate(SetupCanAlreadySetupDestination)
    }

    fun proceedWithThirdAttempt() {
        when (flowStateMachine.state) {
            is CanFsm.State.RequestedCanAndPinForPinManagement -> navigator.navigate(SetupCanIntroDestination(true))
            is CanFsm.State.RequestedCanAndPinForIdent, is CanFsm.State.CanAndPinEnteredForIdent -> navigator.navigate(IdentificationCanIntroDestination(true))
            else -> logger.error("Requested to proceed with third PIN attempt unexpected in state ${flowStateMachine.state}")
        }
    }

    fun finishIntro() {
        navigator.navigate(CanInputDestination(false))
    }

    fun onCanEntered(can: String) {
        when (val currentState = flowStateMachine.state) {
            is CanFsm.State.RequestedCanAndPinForPinManagement -> {
                flowStateMachine.state = currentState.advance(can)
                if (flowStateMachine.state is CanFsm.State.CanAndPinEnteredForPinManagement) {
                    executeCanStep()
                } else {
                    navigator.navigate(SetupCanTransportPinDestination)
                }
            }
            is CanFsm.State.RequestedCanAndPinForIdent -> {
                flowStateMachine.state = currentState.advance(can)
                if (flowStateMachine.state is CanFsm.State.CanAndPinEnteredForIdent) {
                    executeCanStep()
                } else {
                    navigator.navigate(IdentificationCanPinInputDestination)
                }
            }
            is CanFsm.State.CanAndPinEntered -> {
                currentState.setNewCan(can)
                executeCanStep()
            }
            else -> logger.error("Request for PIN and CAN unexpected in state ${flowStateMachine.state}")
        }
    }

    fun onPinEntered(pin: String) {
        when (val currentState = flowStateMachine.state) {
            is CanFsm.State.CanEntered -> {
                flowStateMachine.state = currentState.advance(pin)
                executeCanStep()
            }
            else -> logger.error("Entered PIN unexpected in state ${flowStateMachine.state}")
        }
    }

    private fun executeCanStep() {
        (flowStateMachine.state as? CanFsm.State.CanAndPinEntered)?.executeCanStep()
            ?: run { logger.error("Cannot execute can step in state ${flowStateMachine.state}") }
    }

    fun onBack() {
        (flowStateMachine.state as? CanFsm.State.CanEntered)?.let { currentState ->
            flowStateMachine.state = currentState.back()
        }

        navigator.pop()
    }

    fun cancelCanFlow() {
        _stateFlow.value = SubCoordinatorState.CANCELLED
        resetCoordinatorState()
    }

    fun finishCanFlow() {
        _stateFlow.value = SubCoordinatorState.FINISHED
        resetCoordinatorState()
    }

    fun skipCanFlow() {
        _stateFlow.value = SubCoordinatorState.SKIPPED
        resetCoordinatorState()
    }

    private fun resetCoordinatorState() {
        eIdEventFlowCoroutineScope?.cancel()
        flowStateMachine.state = CanFsm.State.Invalid
    }

    private fun collectEidEvents() {
        eIdEventFlowCoroutineScope?.cancel()
        eIdEventFlowCoroutineScope = CoroutineScope(coroutineContextProvider.IO).launch {
            idCardManager.eidFlow.catch { exception ->
                logger.error("Error: $exception")
            }.collect { event ->
                when (event) {
                    is EidInteractionEvent.RequestPinAndCan -> {
                        when (val currentState = flowStateMachine.state) {
                            is CanFsm.State.InitializedForIdent -> {
                                flowStateMachine.state = currentState.advance(event.pinCanCallback)

                                if (currentState.pin == null) {
                                    navigator.navigate(IdentificationCanPinForgottenDestination)
                                } else {
                                    navigator.navigate(IdentificationCanIntroDestination(false))
                                }
                            }
                            is CanFsm.State.CanAndPinEnteredForIdent -> {
                                currentState.setNewCallback(event.pinCanCallback)
                                navigator.navigate(IdentificationCanPinForgottenDestination)
                                navigator.navigate(IdentificationCanIntroDestination(true))
                                navigator.navigate(CanInputDestination(true))
                            }
                            else -> logger.error("Request for PIN and CAN unexpected in state ${flowStateMachine.state}")
                        }
                    }
                    is EidInteractionEvent.RequestCanAndChangedPin -> {
                        when (val currentState = flowStateMachine.state) {
                            is CanFsm.State.InitializedForPinManagement -> {
                                flowStateMachine.state = currentState.advance(event.pinCallback)

                                if (currentState.shortFlow) {
                                    navigator.navigate(SetupCanIntroDestination(false))
                                } else {
                                    navigator.navigate(SetupCanConfirmTransportPinDestination(currentState.oldPin))
                                }
                            }
                            is CanFsm.State.CanAndPinEnteredForPinManagement -> {
                                currentState.setNewCallback(event.pinCallback)
                                navigator.navigate(SetupCanIntroDestination(false))
                                navigator.navigate(CanInputDestination(true))
                            }
                            else -> logger.error("Request for PIN and CAN unexpected in state ${flowStateMachine.state}")
                        }
                    }
                    is EidInteractionEvent.AuthenticationSuccessful, EidInteractionEvent.ProcessCompletedSuccessfullyWithoutResult, is EidInteractionEvent.ProcessCompletedSuccessfullyWithRedirect -> finishCanFlow()
                    is EidInteractionEvent.Error -> finishCanFlow()
                    else -> logger.debug("Ignoring event: $event")
                }
            }
        }
    }
}
