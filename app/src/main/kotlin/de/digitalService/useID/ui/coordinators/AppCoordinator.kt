package de.digitalService.useID.ui

import android.util.Log
import androidx.navigation.NavController
import de.digitalService.useID.getLogger
import de.digitalService.useID.ui.composables.screens.Screen
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppCoordinator @Inject constructor() {
    private val logger by getLogger()

    private lateinit var navController: NavController

    // TODO: The PINs should not live here.
    private var transportPIN: String? = null
    private var personalPIN: String? = null

    fun setNavController(navController: NavController) {
        this.navController = navController
    }

    fun navigate(route: String) = navController.navigate(route)

    // Setup
    fun startSetupIDCard() = navController.navigate(Screen.SetupPINLetter.parameterizedRoute())
    fun setTransportPIN(transportPIN: String) {
        this.transportPIN = transportPIN
    }
    fun setPersonalPIN(personalPIN: String) {
        this.personalPIN = personalPIN
    }
    fun finishPINEntry() {
        val transportPIN = transportPIN
        val personalPIN = personalPIN

        if (transportPIN == null || personalPIN == null) {
            logger.error("PINs not available.")
        } else {
            navController.navigate(Screen.SetupScan.parameterizedRoute(transportPIN, personalPIN))
        }
    }
    fun clearPINs() {
        transportPIN = null
        personalPIN = null
    }
}
