package com.novaclean.app.domain.usecase

import com.novaclean.app.domain.model.VaultItem
import com.novaclean.app.domain.repository.VaultRepository

class VaultUseCases(
    private val repository: VaultRepository
) {
    fun isPinSet(): Boolean = repository.isPinSet()

    fun verifyPin(pin: String): Boolean = repository.verifyPin(pin)

    fun setPin(pin: String) = repository.setPin(pin)

    suspend fun getItems(): List<VaultItem> = repository.getVaultItems()

    suspend fun addPhoto(uriString: String, fileName: String): Result<VaultItem> =
        repository.addPhotoToVault(uriString, fileName)

    suspend fun addNote(title: String, content: String): Result<VaultItem> =
        repository.addNoteToVault(title, content)

    suspend fun deleteItem(item: VaultItem): Result<Unit> =
        repository.deleteVaultItem(item)

    suspend fun restoreItem(item: VaultItem): Result<String> =
        repository.restoreVaultItem(item)
}
