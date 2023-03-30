package de.digitalService.useID.idCardInterface

import de.governikus.ausweisapp2.sdkwrapper.card.core.AccessRight

data class AuthenticationRequest(
    val requiredAttributes: List<AccessRight>,
    val transactionInfo: String?
)
