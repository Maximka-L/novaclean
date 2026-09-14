package com.novaclean.app.domain.usecase

import com.novaclean.app.domain.model.PasswordCategory
import com.novaclean.app.domain.model.SavedPassword
import com.novaclean.app.domain.repository.PasswordManagerRepository

class GetSavedPasswordsUseCase(
    private val repository: PasswordManagerRepository
) {
    suspend operator fun invoke(
        query: String = "",
        category: PasswordCategory = PasswordCategory.ALL
    ): List<SavedPassword> = execute(query, category)

    suspend fun execute(
        query: String = "",
        category: PasswordCategory = PasswordCategory.ALL
    ): List<SavedPassword> {
        return repository.searchPasswords(query, category)
    }
}

class SavePasswordUseCase(
    private val repository: PasswordManagerRepository
) {
    suspend operator fun invoke(password: SavedPassword): Result<Unit> = execute(password)

    suspend fun execute(password: SavedPassword): Result<Unit> {
        if (password.serviceName.isBlank() || password.password.isBlank()) {
            return Result.failure(IllegalArgumentException("Service name and password cannot be empty"))
        }
        return repository.savePassword(password)
    }
}

class DeletePasswordUseCase(
    private val repository: PasswordManagerRepository
) {
    suspend operator fun invoke(id: String): Result<Unit> = execute(id)

    suspend fun execute(id: String): Result<Unit> {
        return repository.deletePassword(id)
    }
}
