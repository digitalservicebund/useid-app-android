package de.digitalService.useID.idCardInterface

import android.net.Uri
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class EidAuthenticationRequest(
    val issuer: String,
    val issuerURL: String,
    val subject: String,
    val subjectURL: String,
    val validity: String,
    val terms: AuthenticationTerms,
    val transactionInfo: String?,
    val readAttributes: Map<IdCardAttribute, Boolean>
)
