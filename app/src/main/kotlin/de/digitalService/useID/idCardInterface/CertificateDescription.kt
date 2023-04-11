package de.digitalService.useID.idCardInterface

import kotlinx.serialization.Serializable

@Serializable
data class CertificateDescription(
    val issuerName: String,
    val issuerUrl: String?,
    val purpose: String,
    val subjectName: String,
    val subjectUrl: String?,
    val termsOfUsage: String
)
