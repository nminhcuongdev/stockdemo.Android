package com.example.stockdemo.feature.chat.data.mapper

import com.example.stockdemo.feature.chat.data.remote.ChatMessageDto
import com.example.stockdemo.feature.chat.data.remote.ChatRequestDto
import com.example.stockdemo.feature.chat.data.remote.ChatResponseDto
import com.example.stockdemo.feature.chat.domain.model.ChatMessage
import com.example.stockdemo.feature.chat.domain.model.ChatRequest
import com.example.stockdemo.feature.chat.domain.model.ChatResponse

fun ChatMessageDto.toDomain(): ChatMessage {
    return ChatMessage(
        content = content,
        role = role
    )
}

fun ChatMessage.toDto(): ChatMessageDto {
    return ChatMessageDto(
        content = content,
        role = role
    )
}

fun ChatRequest.toDto(): ChatRequestDto {
    return ChatRequestDto(
        collectionName = collectionName,
        conversationHistory = conversationHistory.map { it.toDto() },
        question = question,
        topK = topK,
        useRag = useRag
    )
}

fun ChatResponseDto.toDomain(): ChatResponse {
    return ChatResponse(
        answer = answer,
        sources = sources
    )
}



