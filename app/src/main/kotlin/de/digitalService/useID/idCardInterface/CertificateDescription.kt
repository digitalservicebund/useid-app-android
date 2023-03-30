package de.digitalService.useID.idCardInterface

import android.net.Uri
import java.util.*

data class CertificateDescription(
    val issuerName: String,
    val issuerUrl: Uri?,
    val purpose: String,
    val subjectName: String,
    val subjectUrl: Uri?,
    val termsOfUsage: String,
    val effectiveDate: Date,
    val expirationDate: Date
)
