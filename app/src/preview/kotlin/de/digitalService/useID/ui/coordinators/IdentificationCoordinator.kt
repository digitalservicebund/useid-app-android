package de.digitalService.useID.ui.coordinators

import de.digitalService.useID.getLogger
import de.digitalService.useID.idCardInterface.AuthenticationTerms
import de.digitalService.useID.idCardInterface.EIDAuthenticationRequest
import de.digitalService.useID.idCardInterface.EIDInteractionEvent
import de.digitalService.useID.idCardInterface.IDCardAttribute
import de.digitalService.useID.ui.AppCoordinator
import de.digitalService.useID.ui.composables.screens.destinations.IdentificationAttributeConsentDestination
import de.digitalService.useID.ui.composables.screens.destinations.IdentificationPersonalPINDestination
import de.digitalService.useID.ui.composables.screens.destinations.IdentificationScanDestination
import de.digitalService.useID.ui.composables.screens.destinations.IdentificationSuccessDestination
import de.digitalService.useID.ui.composables.screens.identification.FetchMetadataEvent
import de.digitalService.useID.ui.composables.screens.identification.IdentificationPersonalPIN
import de.digitalService.useID.ui.composables.screens.identification.IdentificationSuccess
import de.digitalService.useID.ui.composables.screens.identification.ScanEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IdentificationCoordinator @Inject constructor(private val appCoordinator: AppCoordinator) {
    private val logger by getLogger()

    private val _fetchMetadataEventFlow: MutableStateFlow<FetchMetadataEvent> = MutableStateFlow(FetchMetadataEvent.Started)
    val fetchMetadataEventFlow: StateFlow<FetchMetadataEvent>
        get() = _fetchMetadataEventFlow

    private val _scanEventFlow: MutableStateFlow<ScanEvent> = MutableStateFlow(ScanEvent.CardRequested)
    val scanEventFlow: StateFlow<ScanEvent>
        get() = _scanEventFlow

    private val provider: String = "Provider"

    fun startIdentificationProcess(tcTokenURL: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val mockedRequest =
                EIDAuthenticationRequest(
                    issuer = "issuer",
                    issuerURL = "issuer_url",
                    subject = "subject",
                    subjectURL = "subject_url",
                    validity = "validity",
                    terms = AuthenticationTerms.Text("terms"),
                    transactionInfo = null,
                    readAttributes = mapOf(Pair(IDCardAttribute.DG01, true))
                )

            CoroutineScope(Dispatchers.Main).launch { appCoordinator.navigate(IdentificationAttributeConsentDestination(mockedRequest)) }
        }
    }

    fun confirmAttributesForIdentification() {
        appCoordinator.navigate(IdentificationPersonalPINDestination)
    }

    fun onPINEntered(pin: String) {
        appCoordinator.navigate(IdentificationScanDestination)
    }

    fun cancelIdentification() {
        appCoordinator.popToRoot()
    }

    fun onIDInteractionFinishedSuccessfully() {
        appCoordinator.navigate(IdentificationSuccessDestination(provider))
    }
}
