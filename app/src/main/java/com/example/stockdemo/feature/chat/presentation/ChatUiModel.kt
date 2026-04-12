package com.example.stockdemo.feature.chat.presentation

import com.example.stockdemo.core.ui.UiText

data class ChatUiModel(
    val text: UiText,
    val isUser: Boolean,
    val isError: Boolean = false
)



