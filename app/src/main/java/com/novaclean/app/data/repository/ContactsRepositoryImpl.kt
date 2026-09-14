package com.novaclean.app.data.repository

import com.novaclean.app.data.datasource.ContactsDataSource
import com.novaclean.app.domain.model.ContactItem
import com.novaclean.app.domain.model.DuplicateContactGroup
import com.novaclean.app.domain.repository.ContactsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ContactsRepositoryImpl(
    private val dataSource: ContactsDataSource
) : ContactsRepository {

    override suspend fun getDuplicateContacts(): List<DuplicateContactGroup> = withContext(Dispatchers.IO) {
        val allContacts = dataSource.queryContacts()
        val groups = mutableListOf<DuplicateContactGroup>()

        val phoneToContacts = mutableMapOf<String, MutableList<ContactItem>>()
        for (contact in allContacts) {
            for (normPhone in contact.normalizedPhones) {
                if (normPhone.length >= 6) {
                    val list = phoneToContacts.getOrPut(normPhone) { mutableListOf() }
                    if (list.none { it.id == contact.id }) {
                        list.add(contact)
                    }
                }
            }
        }

        val handledIds = mutableSetOf<Long>()
        var idx = 1
        for ((normPhone, contacts) in phoneToContacts) {
            if (contacts.size > 1) {
                groups.add(
                    DuplicateContactGroup(
                        id = "phone_${idx++}",
                        matchReason = normPhone,
                        contacts = contacts
                    )
                )
                handledIds.addAll(contacts.map { it.id })
            }
        }

        val remaining = allContacts.filterNot { it.id in handledIds }
        val nameToContacts = remaining.groupBy { it.displayName.trim().lowercase() }
            .filter { it.key.isNotBlank() && it.value.size > 1 }

        for ((name, contacts) in nameToContacts) {
            groups.add(
                DuplicateContactGroup(
                    id = "name_${idx++}",
                    matchReason = contacts.first().displayName,
                    contacts = contacts
                )
            )
        }

        groups
    }

    override suspend fun mergeDuplicateContacts(groups: List<DuplicateContactGroup>): Int = withContext(Dispatchers.IO) {
        var mergedCount = 0
        for (group in groups) {
            // Оставляем первый контакт как основной, удаляем остальные дубликаты
            val duplicates = group.contacts.drop(1)
            for (dupe in duplicates) {
                if (dataSource.deleteContact(dupe.id)) {
                    mergedCount++
                }
            }
        }
        mergedCount
    }

    override suspend fun deleteContact(contactId: Long): Boolean {
        return dataSource.deleteContact(contactId)
    }
}
