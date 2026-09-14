package com.novaclean.app

import com.novaclean.app.domain.model.VaultFileType
import com.novaclean.app.domain.model.VaultItem
import com.novaclean.app.domain.repository.VaultRepository
import com.novaclean.app.domain.usecase.VaultUseCases
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.security.MessageDigest

class VaultLogicTest {

    private class FakeVaultRepository : VaultRepository {
        var pinHash: String? = null
        val items = mutableListOf<VaultItem>()

        override fun isPinSet(): Boolean = pinHash != null

        override fun verifyPin(pin: String): Boolean {
            return pinHash != null && pinHash == hashPin(pin)
        }

        override fun setPin(pin: String) {
            pinHash = hashPin(pin)
        }

        override suspend fun getVaultItems(): List<VaultItem> = items

        override suspend fun addPhotoToVault(uriString: String, fileName: String): Result<VaultItem> {
            val item = VaultItem("1", fileName, "/internal/$fileName", VaultFileType.PHOTO, 1024L, System.currentTimeMillis())
            items.add(item)
            return Result.success(item)
        }

        override suspend fun addNoteToVault(title: String, content: String): Result<VaultItem> {
            val item = VaultItem("2", title, "/internal/$title", VaultFileType.NOTE, content.length.toLong(), System.currentTimeMillis(), content)
            items.add(item)
            return Result.success(item)
        }

        override suspend fun deleteVaultItem(item: VaultItem): Result<Unit> {
            items.remove(item)
            return Result.success(Unit)
        }

        override suspend fun restoreVaultItem(item: VaultItem): Result<String> {
            return Result.success("restored")
        }

        private fun hashPin(pin: String): String {
            val md = MessageDigest.getInstance("SHA-256")
            val salted = "NovaClean_Salt_#2026:$pin"
            val bytes = md.digest(salted.toByteArray())
            return bytes.joinToString("") { "%02x".format(it) }
        }
    }

    @Test
    fun testPinLifecycle() {
        val repo = FakeVaultRepository()
        val useCases = VaultUseCases(repo)

        assertFalse(useCases.isPinSet())
        assertFalse(useCases.verifyPin("1234"))

        useCases.setPin("4321")
        assertTrue(useCases.isPinSet())
        assertFalse(useCases.verifyPin("1234"))
        assertTrue(useCases.verifyPin("4321"))
    }

    @Test
    fun testVaultItemManagement() = runBlocking {
        val repo = FakeVaultRepository()
        val useCases = VaultUseCases(repo)

        assertEquals(0, useCases.getItems().size)

        val photoResult = useCases.addPhoto("content://fake", "secret.jpg")
        assertTrue(photoResult.isSuccess)
        assertEquals(1, useCases.getItems().size)

        val noteResult = useCases.addNote("Bank PINs", "Secret note content")
        assertTrue(noteResult.isSuccess)
        assertEquals(2, useCases.getItems().size)

        val itemToDelete = useCases.getItems().first()
        useCases.deleteItem(itemToDelete)
        assertEquals(1, useCases.getItems().size)
    }
}
