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
class CanCoordinator @Inject constructor(
    private val navigator: Navigator,
    private val idCardManager: IdCardManager,
    private val coroutineContextProvider: CoroutineContextProviderType,
) {
    private val logger by getLogger()

    private sealed class CanFlowState {
        object Invalid: CanFlowState()
        class InitializedForSetup(private val newPin: String): CanFlowState() {
            fun advance(callback: ChangePinCallback): CanFlowState = RequestedCanAndPinForSetup(newPin, callback)
        }
        object InitializedForIdent: CanFlowState() {
            fun advance(callback: PinCallback): CanFlowState = RequestedCanAndPinForIdent(callback)
        }
        class RequestedCanAndPinForSetup(private val newPin: String, private val callback: ChangePinCallback): CanFlowState(), RequestedCanAndPin {
            override fun advance(can: String): CanFlowState = CanEnteredForSetup(can, newPin, callback)
        }
        class RequestedCanAndPinForIdent(private val callback: PinCallback): CanFlowState(), RequestedCanAndPin {
            override fun advance(can: String): CanFlowState = CanEnteredForIdent(can, callback)
        }
        class CanEnteredForSetup(private val can: String, private val newPin: String, private val callback: ChangePinCallback): CanFlowState(), CanEntered {
            override fun back(): CanFlowState = RequestedCanAndPinForSetup(newPin, callback)
            override fun advance(pin: String): CanFlowState = CanAndPinEnteredForSetup(can, pin, newPin, callback)
        }
        class CanEnteredForIdent(private val can: String, private val callback: PinCallback): CanFlowState(), CanEntered {
            override fun back(): CanFlowState = RequestedCanAndPinForIdent(callback)
            override fun advance(pin: String): CanFlowState = CanAndPinEnteredForIdent(can, pin, callback)
        }
        class CanAndPinEnteredForSetup(private var can: String, private val pin: String, private val newPin: String, private var callback: ChangePinCallback): CanFlowState(), CanAndPinEntered {
            fun setNewCallback(callback: ChangePinCallback) { this.callback = callback }
            override fun setNewCan(can: String) { this.can = can }
            override fun executeCanStep() = callback(pin, can, newPin)
        }
        class CanAndPinEnteredForIdent(private var can: String, private val pin: String, private var callback: PinCallback): CanFlowState(), CanAndPinEntered {
            fun setNewCallback(callback: PinCallback) { this.callback = callback }
            override fun setNewCan(can: String) { this.can = can }
            override fun executeCanStep() = callback(pin, can)
        }

        interface RequestedCanAndPin {
            fun advance(can: String): CanFlowState
        }
        interface CanEntered {
            fun back(): CanFlowState
            fun advance(pin: String): CanFlowState
        }
        interface CanAndPinEntered {
            fun setNewCan(can: String)
            fun executeCanStep()
        }
    }

    private var state: CanFlowState = CanFlowState.Invalid

    private var eIdEventFlowCoroutineScope: Job? = null

    private val _stateFlow: MutableStateFlow<SubCoordinatorState> = MutableStateFlow(SubCoordinatorState.Idle)
    val stateFlow: StateFlow<SubCoordinatorState>
        get() = _stateFlow

    fun startCanFlow(newPin: String? = null): Flow<SubCoordinatorState> {
        state = if (newPin != null) CanFlowState.InitializedForSetup(newPin) else CanFlowState.InitializedForIdent
        collectEidEvents()

        _stateFlow.value = SubCoordinatorState.Active
        return stateFlow
    }

    fun onResetPin() {
        navigator.navigate(ResetPersonalPinDestination)
    }

    fun proceedWithThirdAttempt() {
        navigator.navigate(IdentificationCanIntroDestination)
    }

    fun finishIntro() {
        navigator.navigate(IdentificationCanInputDestination(false))
    }

    fun onCanEntered(can: String) {
        when (val currentState = state) {
            is CanFlowState.RequestedCanAndPin -> {
                state = currentState.advance(can)
                navigator.navigate(IdentificationCanPinInputDestination)
            }
            is CanFlowState.CanAndPinEntered -> {
                currentState.setNewCan(can)
                executeCanStep()
            }
            else -> logger.error("Request for PIN and CAN unexpected in state $state")
        }
    }

    fun onPinEntered(pin: String) {
        when (val currentState = state) {
            is CanFlowState.CanEntered -> {
                state = currentState.advance(pin)
                executeCanStep()
            }
            else -> logger.error("Entered PIN unexpected in state $state")
        }
    }

    private fun executeCanStep() {
        (state as? CanFlowState.CanAndPinEntered)?.executeCanStep()
            ?: run { logger.error("Cannot execute can step in state $state") }
    }

    fun onBack() {
        (state as? CanFlowState.CanEntered)?.let { currentState ->
            state = currentState.back()
        }

        navigator.pop()
    }

    fun cancelCanFlow() {
        _stateFlow.value = SubCoordinatorState.Cancelled
        resetCoordinatorState()
    }

    private fun finishCanFlow() {
        _stateFlow.value = SubCoordinatorState.Finished
        _stateFlow.value = SubCoordinatorState.Idle
        resetCoordinatorState()
    }

    private fun resetCoordinatorState() {
        eIdEventFlowCoroutineScope?.cancel()
        state = CanFlowState.Invalid
    }

    private fun collectEidEvents() {
        eIdEventFlowCoroutineScope?.cancel()
        eIdEventFlowCoroutineScope = CoroutineScope(coroutineContextProvider.IO).launch {
            idCardManager.eidFlow.catch { exception ->
                logger.error("Error: $exception")
            }.collect { event ->
                when (event) {
                    is EidInteractionEvent.RequestPinAndCan -> {
                        when (val currentState = state) {
                            is CanFlowState.InitializedForIdent -> {
                                state = currentState.advance(event.pinCanCallback)
                                navigator.navigate(IdentificationCanPinForgottenDestination)
                            }
                            is CanFlowState.CanAndPinEnteredForIdent -> {
                                currentState.setNewCallback(event.pinCanCallback)
                                navigator.navigate(IdentificationCanPinForgottenDestination)
                                navigator.navigate(IdentificationCanIntroDestination)
                                navigator.navigate(IdentificationCanInputDestination(true))
                            }
                            else -> logger.error("Request for PIN and CAN unexpected in state $state")
                        }
                    }
                    is EidInteractionEvent.RequestCanAndChangedPin -> {
                        // TODO: Implement
                    }
                    is EidInteractionEvent.AuthenticationSuccessful, EidInteractionEvent.ProcessCompletedSuccessfullyWithoutResult, is EidInteractionEvent.ProcessCompletedSuccessfullyWithRedirect -> finishCanFlow()
                    is EidInteractionEvent.Error -> cancelCanFlow()
                    else -> logger.debug("Ignoring event: $event")
                }
            }
        }
    }
}
