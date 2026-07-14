package com.stockdemo.warehouse.core.common

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ResourceTest {

    @Test
    fun `success carries data and no message`() {
        val resource = Resource.Success(listOf(1, 2, 3))

        assertEquals(listOf(1, 2, 3), resource.data)
        assertNull(resource.message)
    }

    @Test
    fun `success may carry null data`() {
        val resource = Resource.Success<String>(null)

        assertNull(resource.data)
    }

    @Test
    fun `error carries a message and optional fallback data`() {
        val resource = Resource.Error(message = "boom", data = 42)

        assertEquals("boom", resource.message)
        assertEquals(42, resource.data)
    }

    @Test
    fun `error without data has null data`() {
        val resource = Resource.Error<Int>("boom")

        assertEquals("boom", resource.message)
        assertNull(resource.data)
    }

    @Test
    fun `loading defaults to isLoading true and null data`() {
        val resource = Resource.Loading<Int>()

        assertTrue(resource.isLoading)
        assertNull(resource.data)
    }

    @Test
    fun `loading can represent finished state`() {
        val resource = Resource.Loading<Int>(isLoading = false)

        assertFalse(resource.isLoading)
    }
}
