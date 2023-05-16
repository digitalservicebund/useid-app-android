package de.digitalService.useID.idCardInterface

data class IdentificationRequest(
    val requiredAttributes: List<EidAttribute>,
    val transactionInfo: String?
)
