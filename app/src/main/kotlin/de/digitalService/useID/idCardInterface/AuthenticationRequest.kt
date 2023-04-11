package de.digitalService.useID.idCardInterface

data class AuthenticationRequest(
    val requiredAttributes: List<IdCardAttribute>,
    val transactionInfo: String?
)
