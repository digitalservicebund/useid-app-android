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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

interface AppCoordinatorType {
    val nfcAvailability: State<NfcAvailability>
    val currentlyHandlingNfcTags: Boolean

    fun offerIdSetup(tcTokenUrl: String?)
//    fun startIdentification(tcTokenUrl: String, didSetup: Boolean)
    fun homeScreenLaunched()
    fun setNfcAvailability(availability: NfcAvailability)
    fun setIsNotFirstTimeUser()
    fun handleDeepLink(uri: Uri)
}

interface Navigator {
    val isAtRoot: Boolean

    fun setNavController(navController: NavController)

    fun navigate(route: Direction)
    fun navigatePopping(route: Direction)
    fun popUpTo(direction: Destination)
    fun pop()
    fun popToRoot()

    // Will be eliminated after switch to AA2
//    fun startNfcTagHandling()
//    fun stopNfcTagHandling()
}

@Singleton
class AppNavigator @Inject constructor(): Navigator {
    private lateinit var navController: NavController

    override val isAtRoot: Boolean
        get() = navController.previousBackStackEntry == null

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
        CoroutineScope(Dispatchers.Main).launch { navController.popBackStack(route = direction.route, inclusive = false) }
    }

    override fun popToRoot() {
        CoroutineScope(Dispatchers.Main).launch { navController.popBackStack(route = HomeScreenDestination.route, inclusive = false) }
    }
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
        logger.debug("Home screen launched. Setup coordinator: ${setupCoordinator.stateFlow.value}, identification coordinator: ${identificationCoordinator.stateFlow.value}")

        if (coldLaunch &&
            setupCoordinator.stateFlow.value != SubFlowState.Active &&
            identificationCoordinator.stateFlow.value != SubFlowState.Active &&
            storageManager.firstTimeUser
        ) {
            offerIdSetup(null)
        }

        coldLaunch = false

//        if (storageManager.firstTimeUser) {
//            if (coldLaunch || tcTokenUrl != null) {
//                offerIdSetup(tcTokenUrl)
//            }
//        } else {
//            tcTokenUrl?.let { tcTokenUrl ->
//                if (nfcAvailability.value != NfcAvailability.Available) {
//                    logger.warn("Do not start identification because NFC is not available.")
//                    return
//                }
//
//                identificationCoordinator.startIdentificationProcess(tcTokenUrl, false)
//            }
//        }
//
//        coldLaunch = false
//        tcTokenUrl = null
    }

    override fun setNfcAvailability(availability: NfcAvailability) {
        nfcAvailability.value = availability
    }

    override fun setIsNotFirstTimeUser() {
        storageManager.setIsNotFirstTimeUser()
    }

    override fun handleDeepLink(uri: Uri) {
        logger.debug("Handling deep link.")

        Uri.parse(uri.toString()).getQueryParameter("tcTokenURL")?.let { url ->
            tcTokenUrl = url

//            if (!coldLaunch) {
//                if (navigator.isAtRoot) {
//                    navigator.popToRoot()
//                } else {
//                    homeScreenLaunched()
//                }
//            }

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

//    override fun startNfcTagHandling() {
//        currentlyHandlingNfcTags = true
//    }
//
//    override fun stopNfcTagHandling() {
//        currentlyHandlingNfcTags = false
//    }
}
