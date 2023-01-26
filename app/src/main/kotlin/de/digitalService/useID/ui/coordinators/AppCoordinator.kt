package de.digitalService.useID.ui.coordinators

import android.net.Uri
import de.digitalService.useID.StorageManagerType
import de.digitalService.useID.getLogger
import de.digitalService.useID.models.NfcAvailability
import de.digitalService.useID.ui.navigation.Navigator
import de.digitalService.useID.util.CoroutineContextProvider
import de.digitalService.useID.util.NfcInterfaceManagerType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

interface AppCoordinatorType {
    fun offerIdSetup(tcTokenUrl: String?)
    fun homeScreenLaunched()
    fun handleDeepLink(uri: Uri)
}

@Singleton
class AppCoordinator @Inject constructor(
    private val nfcInterfaceManager: NfcInterfaceManagerType,
    private val navigator: Navigator,
    private val setupCoordinator: SetupCoordinator,
    private val identificationCoordinator: IdentificationCoordinator,
    private val storageManager: StorageManagerType,
    private val coroutineContextProvider: CoroutineContextProvider
) : AppCoordinatorType {
    private val logger by getLogger()

    private var cachedTcTokenUrl: String? = null
    private var coldLaunch: Boolean = true

    private val launchedBarrier = Mutex(true)

    private val nfcAvailabilityScope: Job

    init {
        nfcAvailabilityScope = CoroutineScope(coroutineContextProvider.Main).launch {
            nfcInterfaceManager.nfcAvailability.collect { nfcAvailability ->
                cachedTcTokenUrl?.let { cachedTcTokenUrl ->
                    if (nfcAvailability == NfcAvailability.Available) {
                        handleTcTokenUrl(cachedTcTokenUrl)
                    }
                }
            }
        }
    }

    override fun offerIdSetup(tcTokenUrl: String?) {
        navigator.popToRoot()
        setupCoordinator.showSetupIntro(tcTokenUrl)
    }

    override fun homeScreenLaunched() {
        if (coldLaunch &&
            setupCoordinator.stateFlow.value != SubCoordinatorState.ACTIVE &&
            identificationCoordinator.stateFlow.value != SubCoordinatorState.ACTIVE &&
            storageManager.firstTimeUser
        ) {
            offerIdSetup(null)
        }

        coldLaunch = false

        if (launchedBarrier.isLocked) {
            launchedBarrier.unlock(null)
        }
    }

    override fun handleDeepLink(uri: Uri) {
        Uri.parse(uri.toString()).getQueryParameter("tcTokenURL")?.let { url ->
            if (launchedBarrier.isLocked) {
                logger.debug("Wait for app launch to be completed.")
                CoroutineScope(coroutineContextProvider.Default).launch {
                    launchedBarrier.withLock {
                        logger.debug("Handling deep link after waiting.")
                        handleTcTokenUrl(url)
                    }
                }
            } else {
                logger.debug("Handling deep link immediately.")
                handleTcTokenUrl(url)
            }
        } ?: run {
            logger.info("URL does not contain tcTokenUrl parameter.")
        }
    }

    private fun handleTcTokenUrl(url: String) {
        navigator.popToRoot()

        if (nfcInterfaceManager.nfcAvailability.value != NfcAvailability.Available) {
            logger.debug("Blocking identification due to unavailable NFC.")
            cachedTcTokenUrl = url
            return
        } else {
            cachedTcTokenUrl = null
        }

        if (storageManager.firstTimeUser) {
            offerIdSetup(url)
        } else {
            identificationCoordinator.startIdentificationProcess(url, false)
        }

        cachedTcTokenUrl = null
        coldLaunch = false
    }
}
