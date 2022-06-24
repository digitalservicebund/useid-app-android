package de.digitalService.useID.ui.coordinators

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import de.digitalService.useID.getLogger
import de.digitalService.useID.idCardInterface.EIDInteractionEvent
import de.digitalService.useID.idCardInterface.IDCardAttribute
import de.digitalService.useID.idCardInterface.IDCardManager
import de.digitalService.useID.ui.AppCoordinator
import de.digitalService.useID.ui.composables.screens.Screen
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
    val logger by getLogger()

    private var confirmationCallback: ((Map<IDCardAttribute, Boolean>) -> Unit)? = null

    fun startIdentificationProcess() {
        val demoURL =
            "http://127.0.0.1:24727/eID-Client?tcTokenURL=https%3A%2F%2Ftest.governikus-eid.de%2FAutent-DemoApplication%2FRequestServlet%3Fprovider%3Ddemo_epa_20%26redirect%3Dtrue"

        CoroutineScope(Dispatchers.IO).launch {
            idCardManager.identify(context, demoURL).collect { event ->
                when (event) {
                    EIDInteractionEvent.AuthenticationStarted -> logger.debug("Authentication started")
                    is EIDInteractionEvent.RequestAuthenticationRequestConfirmation -> {
                        logger.debug(
                            "Requesting authentication confirmation:\n" +
                                "${event.request.subject}\n" +
                                "Read attributes: ${event.request.readAttributes.keys}"
                        )

                        confirmationCallback = event.confirmationCallback

                        val subject = event.request.subject
                        val requiredReadAttributes = event.request.readAttributes.filterValues { it }.keys

                        CoroutineScope(Dispatchers.Main).launch { appCoordinator.navigate(Screen.IdentificationAttributeConsent.parameterizedRoute(subject, event.request)) }
                    }
                    else -> logger.debug("Unhandled authentication event: $event")
                }
            }
        }
    }
}
