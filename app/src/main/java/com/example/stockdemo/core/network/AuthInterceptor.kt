package com.example.stockdemo.core.network

import com.example.stockdemo.feature.auth.data.local.UserPreferences
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
    private val userPreferences: UserPreferences,
    private val protectedBaseUrl: String
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val requestUrl = request.url.toString()

        if (!requestUrl.startsWith(protectedBaseUrl) || request.url.encodedPath.endsWith("/Users/login")) {
            return chain.proceed(request)
        }

        if (request.header(AUTHORIZATION_HEADER) != null) {
            return chain.proceed(request)
        }

        val token = runBlocking { userPreferences.getSavedAccessToken() }
        if (token.isNullOrBlank()) {
            return chain.proceed(request)
        }

        val authenticatedRequest = request.newBuilder()
            .addHeader(AUTHORIZATION_HEADER, "$BEARER_PREFIX $token")
            .build()

        return chain.proceed(authenticatedRequest)
    }

    private companion object {
        const val AUTHORIZATION_HEADER = "Authorization"
        const val BEARER_PREFIX = "Bearer"
    }
}
