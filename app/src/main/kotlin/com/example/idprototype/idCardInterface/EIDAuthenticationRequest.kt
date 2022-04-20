package com.example.idprototype.idCardInterface

data class EIDAuthenticationRequest(
    val issuer: String,
    val issuerURL: String,
    val subject: String,
    val subjectURL: String,
    val validity: String,
    val terms: AuthenticationTerms,
    val readAttributes: Map<IDCardAttribute, Boolean>
)