package com.stockdemo.warehouse.core.network

import com.stockdemo.warehouse.core.session.SessionManager
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Watches responses from the protected backend and signals the [SessionManager]
 * whenever the server returns 401 Unauthorized (expired/invalid token), so the
 * app can force a logout. The login endpoint is ignored because a 401 there just
 * means wrong credentials, not an expired session.
 */
class UnauthorizedInterceptor(
    private val sessionManager: SessionManager,
    private val protectedBaseUrl: String
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)

        val isProtected = request.url.toString().startsWith(protectedBaseUrl)
        val isLogin = request.url.encodedPath.endsWith("/Users/login")

        if (response.code == HTTP_UNAUTHORIZED && isProtected && !isLogin) {
            sessionManager.notifySessionExpired()
        }

        return response
    }

    private companion object {
        const val HTTP_UNAUTHORIZED = 401
    }
}
