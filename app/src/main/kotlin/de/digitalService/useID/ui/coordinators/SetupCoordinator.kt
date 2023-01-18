package de.digitalService.useID.ui.coordinators

import de.digitalService.useID.StorageManagerType
import de.digitalService.useID.getLogger
import de.digitalService.useID.ui.navigation.Navigator
import de.digitalService.useID.ui.screens.destinations.*
import de.digitalService.useID.util.CoroutineContextProviderType
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
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
    private val coroutineContextProvider: CoroutineContextProviderType
) {
    private val logger by getLogger()

    private var tcTokenUrl: String? = null

    val identificationPending: Boolean
        get() = this.tcTokenUrl != null

    private val _stateFlow: MutableStateFlow<SubCoordinatorState> = MutableStateFlow(SubCoordinatorState.Finished)
    val stateFlow: StateFlow<SubCoordinatorState>
        get() = _stateFlow

    private var subFlowCoroutineScope: Job? = null
    private var identificationStateCoroutineScope: Job? = null

    fun showSetupIntro(tcTokenUrl: String?) {
        _stateFlow.value = SubCoordinatorState.Active
        this.tcTokenUrl = tcTokenUrl
        navigator.navigate(SetupIntroDestination(tcTokenUrl != null))
    }

    fun startSetupIdCard() {
        navigator.navigate(SetupPinLetterDestination)
    }

    fun setupWithPinLetter() {
        subFlowCoroutineScope?.cancel()
        subFlowCoroutineScope = CoroutineScope(coroutineContextProvider.IO).launch {
            pinManagementCoordinator.startPinManagement(PinStatus.TransportPin).collect { event ->
                when (event) {
                    SubCoordinatorState.Cancelled -> cancelSetup()
                    SubCoordinatorState.Finished -> onSuccessfulPinManagement()
                    else -> logger.debug("Ignoring sub flow event: $event")
                }
            }
        }
    }

    fun setupWithoutPinLetter() {
        navigator.navigate(ResetPersonalPinDestination)
    }

    fun onBackClicked() {
        subFlowCoroutineScope?.cancel()
        navigator.pop()
    }

    private fun onSuccessfulPinManagement() {
        logger.debug("Handling successful pin management step.")

        subFlowCoroutineScope?.cancel()
        navigator.navigate(SetupFinishDestination)
    }

    fun skipSetup() {
        finishSetup(true)
    }

    fun finishSetup() {
        storageManager.setIsNotFirstTimeUser()
        finishSetup(false)
    }

    fun cancelSetup() {
        subFlowCoroutineScope?.cancel()
        navigator.popToRoot()
        tcTokenUrl = null

        _stateFlow.value = SubCoordinatorState.Cancelled
    }

    private fun finishSetup(skipped: Boolean) {
        // TODO: Cleanup

        identificationStateCoroutineScope?.cancel()

        tcTokenUrl?.let {
            identificationStateCoroutineScope = CoroutineScope(coroutineContextProvider.Default).launch {
                identificationCoordinator.stateFlow.collect { state ->
                    when (state) {
                        SubCoordinatorState.Finished -> {
                            tcTokenUrl = null
                            cancel()
                        }
                        SubCoordinatorState.Cancelled -> cancel()
                        else -> logger.debug("Ignoring sub flow state: $state")
                    }
                }
            }

            identificationCoordinator.startIdentificationProcess(it, skipped)
        } ?: run {
            navigator.popToRoot()
        }

        _stateFlow.value = SubCoordinatorState.Finished
    }
}
