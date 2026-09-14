package com.novaclean.app.data.datasource

import android.content.Context
import android.provider.ContactsContract
import com.novaclean.app.domain.model.ContactItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ContactsDataSource(private val context: Context) {

    fun normalizePhoneNumber(phone: String): String {
        val digits = phone.filter { it.isDigit() }
        if (digits.length == 11 && (digits.startsWith("7") || digits.startsWith("8"))) {
            return "7" + digits.substring(1)
        }
        return digits
    }

    suspend fun queryContacts(): List<ContactItem> = withContext(Dispatchers.IO) {
        val contactsMap = mutableMapOf<Long, ContactItem>()
        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER
        )

        try {
            context.contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                projection,
                null,
                null,
                "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} ASC"
            )?.use { cursor ->
                val idCol = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
                val nameCol = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                val numCol = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

                while (cursor.moveToNext()) {
                    val contactId = cursor.getLong(idCol)
                    val name = cursor.getString(nameCol) ?: "Без имени"
                    val phone = cursor.getString(numCol) ?: ""
                    val normalized = normalizePhoneNumber(phone)

                    val existing = contactsMap[contactId]
                    if (existing != null) {
                        val newPhones = (existing.phoneNumbers + phone).distinct()
                        val newNorm = (existing.normalizedPhones + normalized).distinct()
                        contactsMap[contactId] = existing.copy(
                            phoneNumbers = newPhones,
                            normalizedPhones = newNorm
                        )
                    } else {
                        contactsMap[contactId] = ContactItem(
                            id = contactId,
                            displayName = name,
                            phoneNumbers = listOf(phone),
                            normalizedPhones = listOf(normalized)
                        )
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        contactsMap.values.toList()
    }

    suspend fun deleteContact(contactId: Long): Boolean = withContext(Dispatchers.IO) {
        try {
            val uri = ContactsContract.RawContacts.CONTENT_URI
            val rows = context.contentResolver.delete(
                uri,
                "${ContactsContract.RawContacts.CONTACT_ID} = ?",
                arrayOf(contactId.toString())
            )
            rows > 0
        } catch (e: Exception) {
            false
        }
    }
}
