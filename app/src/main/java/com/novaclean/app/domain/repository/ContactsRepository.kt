package com.novaclean.app.domain.repository

import com.novaclean.app.domain.model.DuplicateContactGroup

interface ContactsRepository {
    suspend fun getDuplicateContacts(): List<DuplicateContactGroup>
    suspend fun mergeDuplicateContacts(groups: List<DuplicateContactGroup>): Int
    suspend fun deleteContact(contactId: Long): Boolean
}
