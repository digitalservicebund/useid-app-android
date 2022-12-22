package de.digitalService.useID.ui.coordinators

import android.net.Uri
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.navigation.NavController
import com.ramcosta.composedestinations.navigation.navigate
import com.ramcosta.composedestinations.spec.Direction
import de.digitalService.useID.StorageManagerType
import de.digitalService.useID.getLogger
import de.digitalService.useID.models.NfcAvailability
import de.digitalService.useID.ui.screens.destinations.Destination
import de.digitalService.useID.ui.screens.destinations.HomeScreenDestination
import de.digitalService.useID.ui.screens.destinations.IdentificationFetchMetadataDestination
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

interface AppCoordinatorType: NavigatorDelegate {
    val nfcAvailability: State<NfcAvailability>
    val currentlyHandlingNfcTags: Boolean

    fun setNavController(navController: NavController)
    fun offerIdSetup(tcTokenURL: String?)
    fun startIdentification(tcTokenURL: String, didSetup: Boolean)
    fun homeScreenLaunched()
    fun setNfcAvailability(availability: NfcAvailability)
    fun setIsNotFirstTimeUser()
    fun handleDeepLink(uri: Uri)
    fun popUpTo(direction: Destination)
}

interface NavigatorDelegate {
    fun navigate(route: Direction)
    fun navigatePopping(route: Direction)
    fun pop()
    fun popToRoot()

    // Will be eliminated after switch to AA2
    fun startNfcTagHandling()
    fun stopNfcTagHandling()
}

@Singleton
class AppCoordinator @Inject constructor(
    private val setupCoordinator: SetupCoordinator,
    private val storageManager: StorageManagerType
) : AppCoordinatorType {
    private val logger by getLogger()

    private lateinit var navController: NavController

    private var tcTokenURL: String? = null
    private var coldLaunch: Boolean = true

    override val nfcAvailability: MutableState<NfcAvailability> = mutableStateOf(NfcAvailability.Available)
    override var currentlyHandlingNfcTags: Boolean = false
        private set

    override fun setNavController(navController: NavController) {
        this.navController = navController
    }

    override fun navigate(route: Direction) {
        CoroutineScope(Dispatchers.Main).launch { navController.navigate(route) }
    }

    override fun navigatePopping(route: Direction) {
        CoroutineScope(Dispatchers.Main).launch {
            navController.navigate(route) {
                pop()
            }
        }
    }

    override fun pop() {
        navController.popBackStack()
    }

    override fun popUpTo(direction: Destination) {
        navController.popBackStack(route = direction.route, inclusive = false)
    }

    override fun popToRoot() {
        navController.popBackStack(route = HomeScreenDestination.route, inclusive = false)
    }

    override fun offerIdSetup(tcTokenURL: String?) {
        popToRoot()
        setupCoordinator.showSetupIntro()
    }

    override fun startIdentification(tcTokenURL: String, didSetup: Boolean) {
        if (nfcAvailability.value != NfcAvailability.Available) {
            logger.warn("Do not start identification because NFC is not available.")
            return
        }

        navController.navigate(IdentificationFetchMetadataDestination(tcTokenURL, didSetup))
    }

    override fun homeScreenLaunched() {
        if (storageManager.getIsFirstTimeUser()) {
            if (coldLaunch || tcTokenURL != null) {
                offerIdSetup(tcTokenURL)
            }
        } else {
            tcTokenURL?.let { startIdentification(it, false) }
        }

        coldLaunch = false
        tcTokenURL = null
    }

    override fun setNfcAvailability(availability: NfcAvailability) {
        nfcAvailability.value = availability
    }

    override fun setIsNotFirstTimeUser() {
        storageManager.setIsNotFirstTimeUser()
    }

    override fun handleDeepLink(uri: Uri) {
        Uri.parse(uri.toString()).getQueryParameter("tcTokenURL")?.let { url ->
            tcTokenURL = url

            if (!coldLaunch) {
                if (navController.previousBackStackEntry != null) {
                    popToRoot()
                } else {
                    homeScreenLaunched()
                }
            }
        } ?: run {
            logger.info("URL does not contain tcTokenURL parameter.")
        }
    }

    override fun startNfcTagHandling() {
        currentlyHandlingNfcTags = true
    }

    override fun stopNfcTagHandling() {
        currentlyHandlingNfcTags = false
    }
}
