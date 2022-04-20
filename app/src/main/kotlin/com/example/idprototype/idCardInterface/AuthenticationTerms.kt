package com.example.idprototype.idCardInterface

sealed class AuthenticationTerms {
    data class Text(val text: String): AuthenticationTerms()
}