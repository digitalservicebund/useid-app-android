package de.digitalService.useID.ui.coordinators

import android.net.Uri
import de.digitalService.useID.StorageManagerType
import de.digitalService.useID.analytics.TrackerManagerType
import de.digitalService.useID.getLogger
import de.digitalService.useID.models.NfcAvailability
import de.digitalService.useID.ui.navigation.Navigator
import de.digitalService.useID.util.CoroutineContextProviderType
import de.digitalService.useID.util.NfcInterfaceManagerType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

interface AppCoordinatorType {
    fun offerIdSetup(tcTokenUrl: String?, widgetSessionId: String?)
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
    private val coroutineContextProvider: CoroutineContextProviderType,
    private val trackerManager: TrackerManagerType
) : AppCoordinatorType {
    private val logger by getLogger()

    private var cachedTcTokenUrl: String? = null

    private val launchedBarrier = Mutex(true)

    private val nfcAvailabilityScope: Job

    init {
        nfcAvailabilityScope = CoroutineScope(coroutineContextProvider.Main).launch {
            nfcInterfaceManager.nfcAvailability.collect { nfcAvailability ->
                cachedTcTokenUrl?.let { cachedTcTokenUrl ->
                    if (nfcAvailability == NfcAvailability.Available) {
                        handleTcTokenUrl(cachedTcTokenUrl, null)
                    }
                }
            }
        }
    }

    override fun offerIdSetup(tcTokenUrl: String?, widgetSessionId: String?) {
        navigator.popToRoot()
        trackerManager.trackEvent("firstTimeUser", "setupIntroOpened", tcTokenUrl?.let { "widget" } ?: "home")
        setupCoordinator.showSetupIntro(tcTokenUrl, widgetSessionId)
    }

    override fun homeScreenLaunched() {
        if (launchedBarrier.isLocked) {
            launchedBarrier.unlock(null)
        }
    }

    override fun handleDeepLink(uri: Uri) {
        Uri.parse(uri.toString()).getQueryParameter("tcTokenURL")?.let { url ->
            val widgetSessionId = Uri.parse(uri.toString()).getQueryParameter("widgetSessionId")
            if (launchedBarrier.isLocked) {
                logger.debug("Wait for app launch to be completed.")
                CoroutineScope(coroutineContextProvider.Default).launch {
                    launchedBarrier.withLock {
                        logger.debug("Handling deep link after waiting.")
                        handleTcTokenUrl(url, widgetSessionId)
                    }
                }
            } else {
                logger.debug("Handling deep link immediately.")
                handleTcTokenUrl(url, widgetSessionId)
            }
        } ?: run {
            logger.info("URL does not contain tcTokenUrl parameter.")
        }
    }

    fun handleTcTokenUrl(url: String, widgetSessionId: String?) {
        navigator.popToRoot()

        if (nfcInterfaceManager.nfcAvailability.value != NfcAvailability.Available) {
            logger.debug("Blocking identification due to unavailable NFC.")
            cachedTcTokenUrl = url
            return
        } else {
            cachedTcTokenUrl = null
        }

        if (storageManager.firstTimeUser) {
            offerIdSetup(url, widgetSessionId)
        } else {
            identificationCoordinator.startIdentificationProcess(url, widgetSessionId, false)
        }

        cachedTcTokenUrl = null
    }
}
