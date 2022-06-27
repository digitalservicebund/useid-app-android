package de.digitalService.useID.ui.coordinators

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import de.digitalService.useID.getLogger
import de.digitalService.useID.idCardInterface.EIDAuthenticationRequest
import de.digitalService.useID.idCardInterface.EIDInteractionEvent
import de.digitalService.useID.idCardInterface.IDCardAttribute
import de.digitalService.useID.idCardInterface.IDCardManager
import de.digitalService.useID.ui.AppCoordinator
import de.digitalService.useID.ui.composables.screens.destinations.IdentificationAttributeConsentDestination
import de.digitalService.useID.ui.composables.screens.destinations.IdentificationPersonalPINDestination
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

class IdentificationCoordinator @Inject constructor(
    @ApplicationContext private val context: Context,
    val appCoordinator: AppCoordinator,
    val idCardManager: IDCardManager
) {
    private val logger by getLogger()

    private var requestAuthenticationEvent: EIDInteractionEvent.RequestAuthenticationRequestConfirmation? = null

    fun startIdentificationProcess() {
        startIdentification()
    }

    fun confirmAttributesForIdentification() {
        val requestAuthenticationEvent = requestAuthenticationEvent ?: run {
            logger.error("Cannot confirm attributes because there isn't any authentication confirmation request event saved.")
            return
        }

        val requiredAttributes = requestAuthenticationEvent.request.readAttributes.filterValues { it }
        requestAuthenticationEvent.confirmationCallback(requiredAttributes)
    }

    fun onPINEntered() {
        // TODO: Implement
    }

    private fun startIdentification() {
        val demoURL =
            "http://127.0.0.1:24727/eID-Client?tcTokenURL=https%3A%2F%2Ftest.governikus-eid.de%2FAutent-DemoApplication%2FRequestServlet%3Fprovider%3Ddemo_epa_20%26redirect%3Dtrue"

        CoroutineScope(Dispatchers.IO).launch {
            idCardManager.identify(context, demoURL).catch {
                logger.error("Error: $it")
            }.collect { event ->
                when (event) {
                    EIDInteractionEvent.AuthenticationStarted -> logger.debug("Authentication started")
                    is EIDInteractionEvent.RequestAuthenticationRequestConfirmation -> {
                        logger.debug(
                            "Requesting authentication confirmation:\n" +
                                "${event.request.subject}\n" +
                                "Read attributes: ${event.request.readAttributes.keys}"
                        )

                        requestAuthenticationEvent = event

                        CoroutineScope(Dispatchers.Main).launch { appCoordinator.navigate(IdentificationAttributeConsentDestination(event.request)) }
                    }
                    is EIDInteractionEvent.RequestPIN -> {
                        logger.debug("Requesting PIN")

                        CoroutineScope(Dispatchers.Main).launch { appCoordinator.navigate(IdentificationPersonalPINDestination(event.attempts, false)) }
                    }
                    else -> logger.debug("Unhandled authentication event: $event")
                }
            }
        }
    }
}
