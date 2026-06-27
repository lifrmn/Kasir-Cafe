package com.kasircafe.pos.domain.model

data class AuthResult(
    val token: String,
    val role: String
)
