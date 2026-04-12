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
    every { context.getString(any(), *anyVararg()) } answers {
        val id = firstArg<Int>()
        val template = strings[id] ?: "string-$id"
        val args = args.drop(1).toTypedArray()
        String.format(template, *args)
    }
    return context
}
