package com.novaclean.app.domain.repository

import com.novaclean.app.domain.model.PasswordCategory
import com.novaclean.app.domain.model.SavedPassword

interface PasswordManagerRepository {
    suspend fun getAllPasswords(): List<SavedPassword>
    suspend fun searchPasswords(query: String, category: PasswordCategory): List<SavedPassword>
    suspend fun savePassword(password: SavedPassword): Result<Unit>
    suspend fun deletePassword(id: String): Result<Unit>
}
