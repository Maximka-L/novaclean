package com.novaclean.app.domain.repository

import com.novaclean.app.domain.model.VaultItem

interface VaultRepository {
    fun isPinSet(): Boolean
    fun verifyPin(pin: String): Boolean
    fun setPin(pin: String)
    suspend fun getVaultItems(): List<VaultItem>
    suspend fun addPhotoToVault(uriString: String, fileName: String): Result<VaultItem>
    suspend fun addNoteToVault(title: String, content: String): Result<VaultItem>
    suspend fun deleteVaultItem(item: VaultItem): Result<Unit>
    suspend fun restoreVaultItem(item: VaultItem): Result<String>
}
