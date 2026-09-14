package com.novaclean.app.domain.model

enum class VaultFileType {
    PHOTO,
    NOTE
}

data class VaultItem(
    val id: String,
    val title: String,
    val internalPath: String,
    val fileType: VaultFileType,
    val sizeBytes: Long,
    val timestamp: Long,
    val noteContent: String? = null
)
