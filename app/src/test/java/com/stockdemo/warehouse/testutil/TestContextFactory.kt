package com.stockdemo.warehouse.testutil

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
        val formatArgs = args.drop(1).flatMap { arg ->
            if (arg is Array<*>) arg.asList() else listOf(arg)
        }.toTypedArray()
        String.format(template, *formatArgs)
    }
    return context
}
