package com.novaclean.app.data.repository

import android.content.Context
import java.util.Base64
import com.novaclean.app.domain.model.PasswordCategory
import com.novaclean.app.domain.model.SavedPassword
import com.novaclean.app.domain.repository.PasswordManagerRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class PasswordManagerRepositoryImpl(
    private val storageDir: File
) : PasswordManagerRepository {

    constructor(context: Context) : this(File(context.filesDir, "vault"))

    private val storageFile: File
        get() {
            storageDir.mkdirs()
            return File(storageDir, "passwords.dat")
        }

    private val secretKey: SecretKeySpec by lazy {
        val digest = MessageDigest.getInstance("SHA-256")
        val keyBytes = digest.digest("NovaClean_PassVault_MasterKey_#2026".toByteArray(Charsets.UTF_8))
        SecretKeySpec(keyBytes, "AES")
    }

    override suspend fun getAllPasswords(): List<SavedPassword> = withContext(Dispatchers.IO) {
        loadEntries()
    }

    override suspend fun searchPasswords(
        query: String,
        category: PasswordCategory
    ): List<SavedPassword> = withContext(Dispatchers.IO) {
        val all = loadEntries()
        all.filter { item ->
            val matchCategory = category == PasswordCategory.ALL || item.category == category
            val matchQuery = query.isBlank() ||
                    item.serviceName.contains(query, ignoreCase = true) ||
                    item.login.contains(query, ignoreCase = true) ||
                    (item.notes?.contains(query, ignoreCase = true) == true)
            matchCategory && matchQuery
        }
    }

    override suspend fun savePassword(password: SavedPassword): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val list = loadEntries().toMutableList()
            val index = list.indexOfFirst { it.id == password.id }
            if (index >= 0) {
                list[index] = password
            } else {
                list.add(0, password)
            }
            saveEntries(list)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deletePassword(id: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val list = loadEntries().toMutableList()
            val removed = list.removeAll { it.id == id }
            if (removed) {
                saveEntries(list)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun loadEntries(): List<SavedPassword> {
        val file = storageFile
        if (!file.exists()) return emptyList()

        val result = mutableListOf<SavedPassword>()
        try {
            val content = file.readText(Charsets.UTF_8)
            if (content.isBlank()) return emptyList()

            val jsonArray = JSONArray(content)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val id = obj.getString("id")
                val serviceName = obj.getString("serviceName")
                val login = obj.getString("login")
                val encryptedPass = obj.getString("encryptedPass")
                val catString = obj.optString("category", PasswordCategory.OTHER.name)
                val category = try { PasswordCategory.valueOf(catString) } catch (e: Exception) { PasswordCategory.OTHER }
                val createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                val notes = if (obj.has("notes") && !obj.isNull("notes")) obj.getString("notes") else null

                val decryptedPass = decryptPassword(encryptedPass)
                result.add(
                    SavedPassword(
                        id = id,
                        serviceName = serviceName,
                        login = login,
                        password = decryptedPass,
                        category = category,
                        createdAt = createdAt,
                        notes = notes
                    )
                )
            }
        } catch (e: Exception) {
            // Safe fallback
        }
        return result.sortedByDescending { it.createdAt }
    }

    private fun saveEntries(list: List<SavedPassword>) {
        val jsonArray = JSONArray()
        for (item in list) {
            val obj = JSONObject().apply {
                put("id", item.id)
                put("serviceName", item.serviceName)
                put("login", item.login)
                put("encryptedPass", encryptPassword(item.password))
                put("category", item.category.name)
                put("createdAt", item.createdAt)
                if (item.notes != null) put("notes", item.notes)
            }
            jsonArray.put(obj)
        }
        storageFile.writeText(jsonArray.toString(), Charsets.UTF_8)
    }

    internal fun encryptPassword(plainText: String): String {
        return try {
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            val iv = ByteArray(16)
            SecureRandom().nextBytes(iv)
            val ivSpec = IvParameterSpec(iv)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec)
            val cipherText = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
            val combined = ByteArray(iv.size + cipherText.size)
            System.arraycopy(iv, 0, combined, 0, iv.size)
            System.arraycopy(cipherText, 0, combined, iv.size, cipherText.size)
            Base64.getEncoder().encodeToString(combined)
        } catch (e: Exception) {
            plainText
        }
    }

    internal fun decryptPassword(encoded: String): String {
        return try {
            val combined = Base64.getDecoder().decode(encoded)
            if (combined.size <= 16) return ""
            val iv = ByteArray(16)
            System.arraycopy(combined, 0, iv, 0, 16)
            val cipherText = ByteArray(combined.size - 16)
            System.arraycopy(combined, 16, cipherText, 0, cipherText.size)
            val ivSpec = IvParameterSpec(iv)
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec)
            val plainBytes = cipher.doFinal(cipherText)
            String(plainBytes, Charsets.UTF_8)
        } catch (e: Exception) {
            encoded
        }
    }
}
