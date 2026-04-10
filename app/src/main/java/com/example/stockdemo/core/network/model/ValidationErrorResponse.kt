package com.example.stockdemo.core.network.model

data class ValidationErrorResponse(
    val type: String?,
    val title: String?,
    val status: Int?,
    val errors: Map<String, List<String>>?,
    val traceId: String?
)


