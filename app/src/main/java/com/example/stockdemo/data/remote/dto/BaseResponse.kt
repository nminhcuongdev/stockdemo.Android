package com.example.stockdemo.data.remote.dto

data class BaseResponse<T>(
    val success: Boolean,
    val message: String,
    val data: T? = null,
    val errors: List<String>? = null
)