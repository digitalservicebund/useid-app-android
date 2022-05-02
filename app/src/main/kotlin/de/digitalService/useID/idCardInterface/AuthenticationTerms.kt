package de.digitalService.useID.idCardInterface

sealed class AuthenticationTerms {
    data class Text(val text: String): AuthenticationTerms()
}