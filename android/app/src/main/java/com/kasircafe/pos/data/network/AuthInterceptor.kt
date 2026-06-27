package com.kasircafe.pos.data.network

import com.kasircafe.pos.data.local.SessionDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthInterceptor @Inject constructor(
    private val sessionDataStore: SessionDataStore
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        if (original.url.encodedPath == "/login") {
            return chain.proceed(original)
        }

        val token = runBlocking { sessionDataStore.tokenFlow.first() }
        if (token.isBlank()) {
            return chain.proceed(original)
        }

        val request = original.newBuilder()
            .addHeader("Authorization", "Bearer $token")
            .build()

        return chain.proceed(request)
    }
}
