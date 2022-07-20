package de.digitalService.useID.idCardInterface

import kotlinx.serialization.Serializable

@Serializable
sealed class AuthenticationTerms {
    @Serializable
    class Text(val text: String) : AuthenticationTerms()
}
