package de.digitalService.useID.ui.coordinators

import com.ramcosta.composedestinations.spec.Direction
import de.digitalService.useID.StorageManagerType
import de.digitalService.useID.getLogger
import de.digitalService.useID.ui.screens.destinations.*
import de.digitalService.useID.util.CoroutineContextProviderType
import kotlinx.coroutines.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SetupCoordinator @Inject constructor(
    private val navigator: NavigatorDelegate,
    private val pinManagementCoordinator: PinManagementCoordinator,
    private val storageManager: StorageManagerType,
    private val coroutineContextProvider: CoroutineContextProviderType
) {
    private val logger by getLogger()

    private var tcTokenURL: String? = null

    val identificationPending: Boolean
        get() = this.tcTokenURL != null

    private var subFlowCoroutineScope: Job? = null

    fun setTCTokenURL(tcTokenURL: String) {
        this.tcTokenURL = tcTokenURL
    }

    fun showSetupIntro() {
        navigator.navigate(SetupIntroDestination(tcTokenURL))
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

    fun cancelSetup() {
        subFlowCoroutineScope?.cancel()
        navigator.popToRoot()
        tcTokenURL = null
    }

    fun finishSetup() {
        tcTokenURL?.let {
            // TODO: Relink
//            navigator.startIdentification(it, true)
            tcTokenURL = null
        } ?: run {
            navigator.popToRoot()
        }
    }
}
