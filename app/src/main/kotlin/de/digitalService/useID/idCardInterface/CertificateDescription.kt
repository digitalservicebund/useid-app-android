package de.digitalService.useID.idCardInterface

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CertificateDescription(
    val issuerName: String,
    val issuerUrl: String?,
    val purpose: String,
    val subjectName: String,
    val subjectUrl: String?,
    val termsOfUsage: String
) : Parcelable
