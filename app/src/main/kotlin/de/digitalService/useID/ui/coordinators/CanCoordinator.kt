package de.digitalService.useID.ui.coordinators

import androidx.compose.foundation.isSystemInDarkTheme
import de.digitalService.useID.getLogger
import de.digitalService.useID.ui.screens.destinations.IdentificationCanInputDestination
import de.digitalService.useID.ui.screens.destinations.IdentificationCanIntroDestination
import de.digitalService.useID.ui.screens.destinations.IdentificationCanPinInputDestination
import de.digitalService.useID.ui.screens.destinations.SetupResetPersonalPinDestination
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CanCoordinator @Inject constructor(
    val identificationCoordinator: IdentificationCoordinator, // TODO: Refactor to general interface
    val appCoordinator: AppCoordinatorType
) {
    private val logger by getLogger()

    private var can: String? = null
    private var pin: String? = null

    fun onCancelIdentification() {
        identificationCoordinator.cancelIdentification()
    }

    fun onNewPin() {
//        appCoordinator.navigate(SetupResetPersonalPinDestination)
    }

    fun startCanFlow() {
//        appCoordinator.navigate(IdentificationCanIntroDestination)
    }

    fun continueAfterIntro() {
//        appCoordinator.navigate(IdentificationCanInputDestination)
    }

    fun onCanEntered(can: String) {
        this.can = can
//        appCoordinator.navigate(IdentificationCanPinInputDestination)
    }

    fun onPinEntered(pin: String) {
        val can = can ?: run {
            logger.error("CAN not set.")
            return
        }

        this.pin = pin

//        identificationCoordinator.onCanPinEntered(pin, can)
    }

    fun onBack() {
//        appCoordinator.pop()
    }
}
