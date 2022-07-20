package de.digitalService.useID.ui.coordinators

import de.digitalService.useID.getLogger
import de.digitalService.useID.idCardInterface.AuthenticationTerms
import de.digitalService.useID.idCardInterface.EIDAuthenticationRequest
import de.digitalService.useID.idCardInterface.EIDInteractionEvent
import de.digitalService.useID.idCardInterface.IDCardAttribute
import de.digitalService.useID.ui.AppCoordinator
import de.digitalService.useID.ui.composables.screens.destinations.IdentificationAttributeConsentDestination
import de.digitalService.useID.ui.composables.screens.destinations.IdentificationPersonalPINDestination
import de.digitalService.useID.ui.composables.screens.identification.IdentificationPersonalPIN
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class IdentificationCoordinator @Inject constructor(private val appCoordinator: AppCoordinator) {
    private val logger by getLogger()

    fun startIdentificationProcess() {
        CoroutineScope(Dispatchers.IO).launch {

            logger.debug("Simulate fetching provider metadata.")
            delay(2000L)
            logger.debug("Fetching provider metadata done.")

            val mockedRequest =
                EIDAuthenticationRequest(
                    issuer = "issuer",
                    issuerURL = "issuer_url",
                    subject = "subject",
                    subjectURL = "subject_url",
                    validity = "validity",
                    terms = AuthenticationTerms.Text("terms"),
                    readAttributes = mapOf(Pair(IDCardAttribute.DG01, true))
                )

            CoroutineScope(Dispatchers.Main).launch { appCoordinator.navigate(IdentificationAttributeConsentDestination(mockedRequest)) }
        }
    }

    fun confirmAttributesForIdentification() {
        appCoordinator.navigate(IdentificationPersonalPINDestination(null, false))
    }

    fun onPINEntered() {

    }
}
