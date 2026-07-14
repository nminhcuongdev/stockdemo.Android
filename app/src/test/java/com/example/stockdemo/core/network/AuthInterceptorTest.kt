package com.example.stockdemo.core.network

import com.example.stockdemo.feature.auth.data.local.UserPreferences
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import okhttp3.Interceptor
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AuthInterceptorTest {

    private val baseUrl = "http://localhost:5000/api/"
    private val userPreferences: UserPreferences = mockk()

    private fun chainFor(url: String, existingAuthHeader: String? = null): Pair<Interceptor.Chain, CapturedRequest> {
        val requestBuilder = Request.Builder().url(url)
        if (existingAuthHeader != null) requestBuilder.addHeader("Authorization", existingAuthHeader)
        val request = requestBuilder.build()

        val chain = mockk<Interceptor.Chain>()
        every { chain.request() } returns request
        val proceeded = slot<Request>()
        every { chain.proceed(capture(proceeded)) } answers {
            Response.Builder()
                .request(proceeded.captured)
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body("{}".toResponseBody())
                .build()
        }
        return chain to CapturedRequest(proceeded)
    }

    private class CapturedRequest(val slot: io.mockk.CapturingSlot<Request>) {
        val authHeader: String? get() = slot.captured.header("Authorization")
    }

    @Test
    fun `attaches bearer token to protected request`() {
        coEvery { userPreferences.getSavedAccessToken() } returns "abc123"
        val (chain, captured) = chainFor(baseUrl + "Stocks")

        AuthInterceptor(userPreferences, baseUrl).intercept(chain)

        assertEquals("Bearer abc123", captured.authHeader)
    }

    @Test
    fun `does not attach token to non-protected url`() {
        val (chain, captured) = chainFor("http://other-host.com/api/Stocks")

        AuthInterceptor(userPreferences, baseUrl).intercept(chain)

        assertNull(captured.authHeader)
    }

    @Test
    fun `does not attach token to the login endpoint`() {
        val (chain, captured) = chainFor(baseUrl + "Users/login")

        AuthInterceptor(userPreferences, baseUrl).intercept(chain)

        assertNull(captured.authHeader)
    }

    @Test
    fun `keeps an already present authorization header`() {
        val (chain, captured) = chainFor(baseUrl + "Stocks", existingAuthHeader = "Bearer preset")

        AuthInterceptor(userPreferences, baseUrl).intercept(chain)

        assertEquals("Bearer preset", captured.authHeader)
    }

    @Test
    fun `does not attach header when no token is stored`() {
        coEvery { userPreferences.getSavedAccessToken() } returns null
        val (chain, captured) = chainFor(baseUrl + "Stocks")

        AuthInterceptor(userPreferences, baseUrl).intercept(chain)

        assertNull(captured.authHeader)
    }

    @Test
    fun `does not attach header when stored token is blank`() {
        coEvery { userPreferences.getSavedAccessToken() } returns "   "
        val (chain, captured) = chainFor(baseUrl + "Stocks")

        AuthInterceptor(userPreferences, baseUrl).intercept(chain)

        assertNull(captured.authHeader)
    }
}
