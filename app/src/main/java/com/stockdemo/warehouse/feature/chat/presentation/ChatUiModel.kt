package com.stockdemo.warehouse.feature.chat.presentation

import com.stockdemo.warehouse.core.ui.UiText

data class ChatUiModel(
    val text: UiText,
    val isUser: Boolean,
    val isError: Boolean = false
)



