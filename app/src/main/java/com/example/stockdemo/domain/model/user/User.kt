package com.example.stockdemo.domain.model.user

data class User(
    val userId: Int,
    val username: String,
    val fullName: String,
    val role: String,
    val isActive: Boolean,
    val createdDate: String? = null,
    val lastLoginDate: String? = null
)