package com.example.stockdemo.core.notification

import android.util.Log
import com.example.stockdemo.core.network.model.BaseResponse
import com.example.stockdemo.feature.auth.data.local.UserPreferences
import com.example.stockdemo.feature.stock.data.remote.ApiService
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NotificationTokenManagerTest {

    private val api: ApiService = mockk()
    private val userPreferences: UserPreferences = mockk()

    @Before
    fun setUp() {
        mockkStatic(Log::class)
        every { Log.d(any(), any<String>()) } returns 0
        every { Log.w(any(), any<String>()) } returns 0
    }

    private fun createManager() = NotificationTokenManager(api, userPreferences)

    @Test
    fun `register sends token with the user's current locale and id`() = runTest {
        every { userPreferences.userId } returns flowOf(4)
        every { userPreferences.languageCode } returns flowOf("en")
        val requestSlot = slot<RegisterDeviceTokenRequest>()
        coEvery { api.registerDeviceToken(capture(requestSlot)) } returns
            BaseResponse(success = true, message = "ok", data = Unit)

        createManager().register("fcm-token-123")

        val sent = requestSlot.captured
        assertEquals("fcm-token-123", sent.token)
        assertEquals(4, sent.userId)
        assertEquals("en", sent.locale)
        assertEquals("android", sent.platform)
    }

    @Test
    fun `register defaults to Vietnamese locale`() = runTest {
        every { userPreferences.userId } returns flowOf(9)
        every { userPreferences.languageCode } returns flowOf("vi")
        val requestSlot = slot<RegisterDeviceTokenRequest>()
        coEvery { api.registerDeviceToken(capture(requestSlot)) } returns
            BaseResponse(success = true, message = "ok", data = Unit)

        createManager().register("token")

        assertEquals("vi", requestSlot.captured.locale)
    }

    @Test
    fun `register swallows api errors so it is not fatal`() = runTest {
        every { userPreferences.userId } returns flowOf(4)
        every { userPreferences.languageCode } returns flowOf("vi")
        coEvery { api.registerDeviceToken(any()) } throws IllegalStateException("network down")

        // Should not throw — registration failing (e.g. not logged in yet) must be silent.
        createManager().register("token")

        coVerify(exactly = 1) { api.registerDeviceToken(any()) }
    }
}
