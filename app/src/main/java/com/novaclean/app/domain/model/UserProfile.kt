package com.novaclean.app.domain.model

data class UserProfile(
    val displayName: String,
    val userId: String,
    val isPro: Boolean,
    val totalCleanedBytes: Long,
    val notificationsEnabled: Boolean
)
