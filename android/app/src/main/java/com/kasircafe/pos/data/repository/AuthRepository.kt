package com.kasircafe.pos.data.repository

import com.kasircafe.pos.domain.model.AuthResult

interface AuthRepository {
    suspend fun login(username: String, password: String): AuthResult
}
