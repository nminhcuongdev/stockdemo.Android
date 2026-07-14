package com.stockdemo.warehouse.core.notification

import org.junit.Assert.assertEquals
import org.junit.Test

class RegisterDeviceTokenRequestTest {

    @Test
    fun `platform defaults to android`() {
        val request = RegisterDeviceTokenRequest(token = "t", userId = 1, locale = "vi")

        assertEquals("android", request.platform)
    }

    @Test
    fun `carries all provided fields`() {
        val request = RegisterDeviceTokenRequest(
            token = "fcm-token",
            userId = 5,
            platform = "android",
            locale = "en"
        )

        assertEquals("fcm-token", request.token)
        assertEquals(5, request.userId)
        assertEquals("android", request.platform)
        assertEquals("en", request.locale)
    }

    @Test
    fun `userId may be null before login`() {
        val request = RegisterDeviceTokenRequest(token = "t", userId = null, locale = "vi")

        assertEquals(null, request.userId)
    }
}
