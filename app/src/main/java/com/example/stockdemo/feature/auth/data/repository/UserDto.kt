package com.example.stockdemo.feature.auth.data.repository

data class UserDto(
    val userId: Int,
    val username: String,
    val fullName: String,
    val role: String,
    val isActive: Boolean,
    val createdDate: String,
    val lastLoginDate: String?
)


