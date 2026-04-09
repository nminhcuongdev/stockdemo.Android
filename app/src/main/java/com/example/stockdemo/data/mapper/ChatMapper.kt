package com.example.stockdemo.data.mapper

import com.example.stockdemo.data.remote.dto.ChatMessageDto
import com.example.stockdemo.data.remote.dto.ChatRequestDto
import com.example.stockdemo.data.remote.dto.ChatResponseDto
import com.example.stockdemo.domain.model.chat.ChatMessage
import com.example.stockdemo.domain.model.chat.ChatRequest
import com.example.stockdemo.domain.model.chat.ChatResponse

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
