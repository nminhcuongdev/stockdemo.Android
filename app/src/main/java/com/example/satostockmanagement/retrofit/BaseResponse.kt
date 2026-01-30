package com.example.satostockmanagement.retrofit

data class BaseResponse<T>(
    val success: Boolean,
    val message: String,
    val data: T?,
    val errors: List<String>?
)
