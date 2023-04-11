package de.digitalService.useID.idCardInterface

import kotlinx.serialization.Serializable

@Serializable
data class IdentificationAttributes(
    val requiredAttributes: List<IdCardAttribute>,
    val certificateDescription: CertificateDescription
)
