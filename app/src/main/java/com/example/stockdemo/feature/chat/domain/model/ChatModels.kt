package com.example.stockdemo.feature.chat.domain.model

data class ChatMessage(
    val content: String,
    val role: String // "user" or "assistant"
)

data class ChatRequest(
    val collectionName: String = "warehouse",
    val conversationHistory: List<ChatMessage>,
    val question: String,
    val topK: Int = 5,
    val useRag: Boolean = true
)

data class ChatResponse(
    val answer: String,
    val sources: List<String>? = null
)

data class HealthResponse(
    val status: String,
    val version: String
)



