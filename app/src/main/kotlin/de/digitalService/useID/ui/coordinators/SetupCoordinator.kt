package de.digitalService.useID.ui.coordinators

import de.digitalService.useID.StorageManagerType
import de.digitalService.useID.getLogger
import de.digitalService.useID.ui.screens.destinations.*
import de.digitalService.useID.util.CoroutineContextProviderType
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
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

    val stateFlow: MutableStateFlow<SubFlowState> = MutableStateFlow(SubFlowState.Idle)

    private var subFlowCoroutineScope: Job? = null
    private var identificationStateCoroutineScope: Job? = null

    fun showSetupIntro(tcTokenUrl: String?) {
        stateFlow.value = SubFlowState.Active
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
                    SubFlowState.Cancelled -> cancelSetup()
                    SubFlowState.Finished -> onSuccessfulPinManagement()
                    else -> logger.debug("Ignoring sub flow event: $event")
                }
            }
        }
    }

    fun setupWithoutPinLetter() {
        navigator.navigate(SetupResetPersonalPinDestination)
    }

    fun onBackClicked() {
        subFlowCoroutineScope?.cancel()
        navigator.pop()
    }

    private fun onSuccessfulPinManagement() {
        logger.debug("Handling successful pin management step.")

        subFlowCoroutineScope?.cancel()
        storageManager.setIsNotFirstTimeUser()
        navigator.navigate(SetupFinishDestination)
    }

    fun skipSetup() {
        finishSetup(true)
    }

    fun finishSetup() {
        finishSetup(false)
    }

    fun cancelSetup() {
        subFlowCoroutineScope?.cancel()
        navigator.popToRoot()
        tcTokenUrl = null

        stateFlow.value = SubFlowState.Cancelled
    }

    private fun finishSetup(skipped: Boolean) {
        identificationStateCoroutineScope?.cancel()

        tcTokenUrl?.let {
            identificationStateCoroutineScope = CoroutineScope(coroutineContextProvider.Default).launch {
                identificationCoordinator.stateFlow.collect { state ->
                    when (state) {
                        SubFlowState.Finished -> {
                            tcTokenUrl = null
                            cancel()
                        }
                        SubFlowState.Cancelled -> cancel()
                        else -> logger.debug("Ignoring sub flow state: $state")
                    }
                }
            }

            identificationCoordinator.startIdentificationProcess(it, skipped)
        } ?: run {
            navigator.popToRoot()
        }

        stateFlow.value = SubFlowState.Finished
    }
}
