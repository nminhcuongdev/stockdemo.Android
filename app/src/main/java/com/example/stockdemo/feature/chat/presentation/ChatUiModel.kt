package com.example.stockdemo.feature.chat.presentation

data class ChatUiModel(
    val text: String,
    val isUser: Boolean,
    val isError: Boolean = false
)



