package com.example.stockdemo.testutil

import android.content.Context
import io.mockk.every
import io.mockk.mockk

fun mockContext(strings: Map<Int, String> = emptyMap()): Context {
    val context = mockk<Context>(relaxed = true)
    every { context.getString(any()) } answers {
        val id = firstArg<Int>()
        strings[id] ?: "string-$id"
    }
    return context
}
