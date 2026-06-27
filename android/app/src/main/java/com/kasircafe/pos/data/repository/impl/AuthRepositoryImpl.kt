package com.kasircafe.pos.data.repository.impl

import com.kasircafe.pos.data.api.PosApiService
import com.kasircafe.pos.data.api.model.LoginRequest
import com.kasircafe.pos.data.repository.AuthRepository
import com.kasircafe.pos.domain.model.AuthResult
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val api: PosApiService
) : AuthRepository {
    override suspend fun login(username: String, password: String): AuthResult {
        val response = api.login(LoginRequest(username = username, password = password))
        return AuthResult(token = response.token, role = response.role)
    }
}
