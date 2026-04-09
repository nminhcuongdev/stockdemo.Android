package com.example.stockdemo.ui.screens.chat

data class ChatUIModel(
    val text: String,
    val isUser: Boolean,
    val isError: Boolean = false
)
