package de.digitalService.useID.ui.coordinators

import android.net.Uri
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import de.digitalService.useID.StorageManagerType
import de.digitalService.useID.getLogger
import de.digitalService.useID.models.NfcAvailability
import de.digitalService.useID.ui.navigation.Navigator
import javax.inject.Inject
import javax.inject.Singleton

interface AppCoordinatorType {
    val nfcAvailability: State<NfcAvailability>
    val currentlyHandlingNfcTags: Boolean

    fun offerIdSetup(tcTokenUrl: String?)
    fun homeScreenLaunched()
    fun setNfcAvailability(availability: NfcAvailability)
    fun handleDeepLink(uri: Uri)
}

@Singleton
class AppCoordinator @Inject constructor(
    private val navigator: Navigator,
    private val setupCoordinator: SetupCoordinator,
    private val identificationCoordinator: IdentificationCoordinator,
    private val storageManager: StorageManagerType
) : AppCoordinatorType {
    private val logger by getLogger()

    private var tcTokenUrl: String? = null
    private var coldLaunch: Boolean = true

    override val nfcAvailability: MutableState<NfcAvailability> = mutableStateOf(NfcAvailability.Available)
    override var currentlyHandlingNfcTags: Boolean = false
        private set

    override fun offerIdSetup(tcTokenUrl: String?) {
        navigator.popToRoot()
        setupCoordinator.showSetupIntro(tcTokenUrl)
    }

    override fun homeScreenLaunched() {
        if (coldLaunch &&
            setupCoordinator.stateFlow.value != SubCoordinatorState.Active &&
            identificationCoordinator.stateFlow.value != SubCoordinatorState.Active &&
            storageManager.firstTimeUser
        ) {
            offerIdSetup(null)
        }

        coldLaunch = false
    }

    override fun setNfcAvailability(availability: NfcAvailability) {
        nfcAvailability.value = availability
    }

    override fun handleDeepLink(uri: Uri) {
        logger.debug("Handling deep link.")

        Uri.parse(uri.toString()).getQueryParameter("tcTokenURL")?.let { url ->
            tcTokenUrl = url

            navigator.popToRoot()
            if (storageManager.firstTimeUser) {
                offerIdSetup(url)
            } else {
                identificationCoordinator.startIdentificationProcess(url, false)
            }

            coldLaunch = false
        } ?: run {
            logger.info("URL does not contain tcTokenUrl parameter.")
        }
    }
}
