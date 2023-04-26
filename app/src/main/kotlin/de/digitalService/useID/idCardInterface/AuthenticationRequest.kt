package de.digitalService.useID.idCardInterface

data class AuthenticationRequest(
    val requiredAttributes: List<EidAttribute>,
    val transactionInfo: String?
)
