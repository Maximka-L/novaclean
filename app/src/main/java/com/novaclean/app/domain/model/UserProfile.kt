package com.novaclean.app.domain.model

data class UserProfile(
    val displayName: String = "Пользователь",
    val email: String? = null,
    val userId: String = "local_user",
    val isPro: Boolean = false,
    val isRegistered: Boolean = false,
    val totalCleanedBytes: Long = 0L,
    val notificationsEnabled: Boolean = true
)
