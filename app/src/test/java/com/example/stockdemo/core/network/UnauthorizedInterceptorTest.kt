package com.example.stockdemo.core.network

import com.example.stockdemo.core.session.SessionManager
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import okhttp3.Interceptor
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Test

class UnauthorizedInterceptorTest {

    private val baseUrl = "http://localhost:5000/api/"
    private val sessionManager: SessionManager = mockk(relaxed = true)

    private fun chainReturning(url: String, code: Int): Interceptor.Chain {
        val request = Request.Builder().url(url).build()
        val chain = mockk<Interceptor.Chain>()
        every { chain.request() } returns request
        every { chain.proceed(request) } returns Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(code)
            .message(if (code == 401) "Unauthorized" else "OK")
            .body("{}".toResponseBody())
            .build()
        return chain
    }

    @Test
    fun `notifies session expired on 401 from a protected endpoint`() {
        val chain = chainReturning(baseUrl + "Stocks", 401)

        UnauthorizedInterceptor(sessionManager, baseUrl).intercept(chain)

        verify(exactly = 1) { sessionManager.notifySessionExpired() }
    }

    @Test
    fun `ignores 401 from the login endpoint`() {
        // A 401 at login means wrong credentials, not an expired session.
        val chain = chainReturning(baseUrl + "Users/login", 401)

        UnauthorizedInterceptor(sessionManager, baseUrl).intercept(chain)

        verify(exactly = 0) { sessionManager.notifySessionExpired() }
    }

    @Test
    fun `ignores 401 from a non-protected host`() {
        val chain = chainReturning("http://other-host.com/api/Stocks", 401)

        UnauthorizedInterceptor(sessionManager, baseUrl).intercept(chain)

        verify(exactly = 0) { sessionManager.notifySessionExpired() }
    }

    @Test
    fun `does not notify on a successful response`() {
        val chain = chainReturning(baseUrl + "Stocks", 200)

        UnauthorizedInterceptor(sessionManager, baseUrl).intercept(chain)

        verify(exactly = 0) { sessionManager.notifySessionExpired() }
    }
}
