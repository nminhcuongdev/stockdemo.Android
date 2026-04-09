package com.example.stockdemo.data.remote.dto

data class ValidationErrorResponse(
    val type: String?,
    val title: String?,
    val status: Int?,
    val errors: Map<String, List<String>>?,
    val traceId: String?
)