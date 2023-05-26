package de.digitalService.useID.idCardInterface

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class IdentificationAttributes(
    val requiredAttributes: List<EidAttribute>,
    val certificateDescription: CertificateDescription
) : Parcelable
