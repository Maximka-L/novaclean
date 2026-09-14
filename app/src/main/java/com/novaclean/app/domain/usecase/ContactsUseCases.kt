package com.novaclean.app.domain.usecase

import com.novaclean.app.domain.model.DuplicateContactGroup
import com.novaclean.app.domain.repository.ContactsRepository

class GetDuplicateContactsUseCase(private val repository: ContactsRepository) {
    suspend operator fun invoke(): List<DuplicateContactGroup> {
        return repository.getDuplicateContacts()
    }
}

class MergeContactsUseCase(private val repository: ContactsRepository) {
    suspend operator fun invoke(groups: List<DuplicateContactGroup>): Int {
        return repository.mergeDuplicateContacts(groups)
    }
}

class DeleteContactUseCase(private val repository: ContactsRepository) {
    suspend operator fun invoke(contactId: Long): Boolean {
        return repository.deleteContact(contactId)
    }
}
