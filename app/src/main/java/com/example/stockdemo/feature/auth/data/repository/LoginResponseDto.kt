package com.example.stockdemo.feature.auth.data.repository

import com.google.gson.annotations.SerializedName

data class LoginResponseDto(
    @SerializedName(
        value = "accessToken",
        alternate = ["token", "jwt", "access_token"]
    )
    val accessToken: String? = null,
    val expiresAt: String? = null,
    @SerializedName(
        value = "refreshToken",
        alternate = ["refresh_token"]
    )
    val refreshToken: String? = null,
    val user: UserDto? = null,
    val userId: Int? = null,
    val username: String? = null,
    val fullName: String? = null,
    val role: String? = null,
    val isActive: Boolean? = null,
    val createdDate: String? = null,
    val lastLoginDate: String? = null
) {
    fun resolveUser(): UserDto? {
        if (user != null) return user
        if (userId == null || username.isNullOrBlank()) return null

        return UserDto(
            userId = userId,
            username = username,
            fullName = fullName.orEmpty(),
            role = role.orEmpty(),
            isActive = isActive ?: true,
            createdDate = createdDate.orEmpty(),
            lastLoginDate = lastLoginDate
        )
    }
}
