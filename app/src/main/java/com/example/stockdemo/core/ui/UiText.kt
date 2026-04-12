package com.example.stockdemo.core.ui

import android.content.Context

sealed class UiText {
    data class DynamicString(val value: String) : UiText()
    data class StringResource(
        val resId: Int,
        val args: List<Any> = emptyList()
    ) : UiText()

    fun asString(context: Context): String {
        return when (this) {
            is DynamicString -> value
            is StringResource -> {
                val resolvedArgs = args.map { arg ->
                    when (arg) {
                        is UiText -> arg.asString(context)
                        else -> arg
                    }
                }.toTypedArray()
                context.getString(resId, *resolvedArgs)
            }
        }
    }
}

fun String?.asUiText(fallbackResId: Int): UiText {
    return if (this.isNullOrBlank()) {
        UiText.StringResource(fallbackResId)
    } else {
        UiText.DynamicString(this)
    }
}
