package de.digitalService.useID.ui.coordinators

import de.digitalService.useID.StorageManagerType
import de.digitalService.useID.flows.SetupStateMachine
import de.digitalService.useID.getLogger
import de.digitalService.useID.ui.navigation.Navigator
import de.digitalService.useID.ui.screens.destinations.*
import de.digitalService.useID.util.CoroutineContextProviderType
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SetupCoordinator @Inject constructor(
    private val navigator: Navigator,
    private val pinManagementCoordinator: PinManagementCoordinator,
    private val identificationCoordinator: IdentificationCoordinator,
    private val storageManager: StorageManagerType,
    private val flowStateMachine: SetupStateMachine,
    private val coroutineContextProvider: CoroutineContextProviderType
) {
    private val logger by getLogger()

    private val _stateFlow: MutableStateFlow<SubCoordinatorState> = MutableStateFlow(SubCoordinatorState.FINISHED)
    val stateFlow: StateFlow<SubCoordinatorState>
        get() = _stateFlow

    private var stateMachineCoroutineScope: Job? = null
    private var subFlowCoroutineScope: Job? = null

    private fun collectStateMachineEvents() {
        if (stateMachineCoroutineScope != null) {
            return
        }
        stateMachineCoroutineScope = CoroutineScope(coroutineContextProvider.Default).launch {
            flowStateMachine.state.collect { eventAndPair ->
                when (eventAndPair.first) {
                    is SetupStateMachine.Event.Back -> navigator.pop()
                    is SetupStateMachine.Event.SubsequentFlowBackedDown -> return@collect
                    else -> {
                        when (val state = eventAndPair.second) {
                            is SetupStateMachine.State.Intro -> navigator.navigate(SetupIntroDestination(state.tcTokenUrl != null))
                            is SetupStateMachine.State.PinManagement -> startPinManagement(state.tcTokenUrl != null)
                            is SetupStateMachine.State.SkippingToIdentRequested -> identificationCoordinator.startIdentificationProcess(state.tcTokenUrl, true)
                            is SetupStateMachine.State.StartSetup -> navigator.navigate(SetupPinLetterDestination)
                            is SetupStateMachine.State.PinReset -> navigator.navigate(SetupResetPersonalPinDestination)
                            is SetupStateMachine.State.PinManagementFinished -> {
                                storageManager.setIsNotFirstTimeUser()
                                navigator.navigate(SetupFinishDestination(state.tcTokenUrl != null))
                            }
                            is SetupStateMachine.State.SetupFinished -> finishSetup()
                            is SetupStateMachine.State.IdentAfterFinishedSetupRequested -> identificationCoordinator.startIdentificationProcess(state.tcTokenUrl, false)

                            SetupStateMachine.State.Invalid -> logger.debug("Ignoring transition to state INVALID.")
                        }
                    }
                }
            }
        }
    }

    fun showSetupIntro(tcTokenUrl: String?) {
        collectStateMachineEvents()

        _stateFlow.value = SubCoordinatorState.ACTIVE
        flowStateMachine.transition(SetupStateMachine.Event.OfferSetup(tcTokenUrl))
    }

    fun startSetupIdCard() {
        flowStateMachine.transition(SetupStateMachine.Event.StartSetup)
    }

    fun setupWithPinLetter() {
        flowStateMachine.transition(SetupStateMachine.Event.StartPinManagement)
    }

    private fun startPinManagement(identificationPending: Boolean) {
        subFlowCoroutineScope?.cancel()
        subFlowCoroutineScope = CoroutineScope(coroutineContextProvider.IO).launch {
            pinManagementCoordinator.startPinManagement(identificationPending, true).collect { event ->
                when (event) {
                    SubCoordinatorState.CANCELLED -> cancelSetup()
                    SubCoordinatorState.BACKED_DOWN -> flowStateMachine.transition(SetupStateMachine.Event.SubsequentFlowBackedDown)
                    SubCoordinatorState.FINISHED -> flowStateMachine.transition(SetupStateMachine.Event.FinishPinManagement)
                    SubCoordinatorState.SKIPPED -> finishSetup()
                    else -> logger.debug("Ignoring sub flow event: $event")
                }
            }
        }
    }

    fun setupWithoutPinLetter() {
        flowStateMachine.transition(SetupStateMachine.Event.ResetPin)
    }

    fun onBackClicked() {
        subFlowCoroutineScope?.cancel()
        flowStateMachine.transition(SetupStateMachine.Event.Back)
    }

    fun skipSetup() {
        flowStateMachine.transition(SetupStateMachine.Event.SkipSetup)
    }

    fun onSetupFinishConfirmed() {
        flowStateMachine.transition(SetupStateMachine.Event.ConfirmFinish)
    }

    private fun finishSetup() {
        navigator.popToRoot()
        _stateFlow.value = SubCoordinatorState.FINISHED
        resetCoordinatorState()
    }

    fun cancelSetup() {
        navigator.popToRoot()
        _stateFlow.value = SubCoordinatorState.CANCELLED
        resetCoordinatorState()
    }

    private fun resetCoordinatorState() {
        subFlowCoroutineScope?.cancel()
        flowStateMachine.transition(SetupStateMachine.Event.Invalidate)
    }
}
