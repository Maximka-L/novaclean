package com.novaclean.app.domain.model

data class ContactItem(
    val id: Long,
    val displayName: String,
    val phoneNumbers: List<String>,
    val normalizedPhones: List<String>,
    val emails: List<String> = emptyList(),
    val isSelected: Boolean = true
)

data class DuplicateContactGroup(
    val id: String,
    val matchReason: String,
    val contacts: List<ContactItem>,
    val isSelected: Boolean = true
)
