package com.novaclean.app.domain.model

enum class PasswordCategory {
    ALL,
    SOCIAL,
    MAIL,
    FINANCE,
    WORK,
    OTHER
}

data class SavedPassword(
    val id: String,
    val serviceName: String,
    val login: String,
    val password: String,
    val category: PasswordCategory = PasswordCategory.OTHER,
    val createdAt: Long = System.currentTimeMillis(),
    val notes: String? = null
)
