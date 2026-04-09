package com.example.stockdemo.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ChatMessageDto(
    @SerializedName("content") val content: String,
    @SerializedName("role") val role: String
)

data class ChatRequestDto(
    @SerializedName("collection_name") val collectionName: String,
    @SerializedName("conversation_history") val conversationHistory: List<ChatMessageDto>,
    @SerializedName("question") val question: String,
    @SerializedName("top_k") val topK: Int = 5,
    @SerializedName("use_rag") val useRag: Boolean = true
)

data class ChatResponseDto(
    @SerializedName("answer") val answer: String,
    @SerializedName("sources") val sources: List<String>? = null
)

data class HealthResponseDto(
    @SerializedName("status") val status: String,
    @SerializedName("version") val version: String
)
