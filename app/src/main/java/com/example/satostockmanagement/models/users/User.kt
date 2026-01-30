package com.example.satostockmanagement.models.users

data class User(
    val createdDate: String,
    val fullName: String,
    val isActive: Boolean,
    val lastLoginDate: Any,
    val role: String,
    val userId: Int,
    val username: String
)