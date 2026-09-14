package com.novaclean.app

import com.novaclean.app.data.repository.PasswordManagerRepositoryImpl
import com.novaclean.app.domain.model.PasswordCategory
import com.novaclean.app.domain.model.SavedPassword
import com.novaclean.app.domain.usecase.DeletePasswordUseCase
import com.novaclean.app.domain.usecase.GetSavedPasswordsUseCase
import com.novaclean.app.domain.usecase.SavePasswordUseCase
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File
import java.nio.file.Files

class PasswordManagerCryptoTest {

    private lateinit var tempDir: File
    private lateinit var repository: PasswordManagerRepositoryImpl

    @Before
    fun setUp() {
        tempDir = Files.createTempDirectory("vault_test").toFile()
        repository = PasswordManagerRepositoryImpl(tempDir)
    }

    @After
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun testAesEncryptionDecryptionRoundTrip() {
        val plainText = "SuperSecret_P@ssw0rd#2026!"
        val encrypted1 = repository.encryptPassword(plainText)
        val encrypted2 = repository.encryptPassword(plainText)

        // Encrypted is not equal to plain text
        assertNotEquals(plainText, encrypted1)
        assertNotEquals(plainText, encrypted2)

        // Due to randomized IV, two encryptions of the same plaintext produce different ciphertexts
        assertNotEquals(encrypted1, encrypted2)

        // Both decrypt back to the original password
        val decrypted1 = repository.decryptPassword(encrypted1)
        val decrypted2 = repository.decryptPassword(encrypted2)

        assertEquals(plainText, decrypted1)
        assertEquals(plainText, decrypted2)
    }

    @Test
    fun testSaveAndRetrievePassword() = runBlocking {
        assertEquals(0, repository.getAllPasswords().size)

        val pass = SavedPassword(
            id = "test-id-1",
            serviceName = "GitHub",
            login = "developer@example.com",
            password = "SecretPassword123",
            category = PasswordCategory.WORK,
            notes = "Primary dev token"
        )

        val saveResult = repository.savePassword(pass)
        assertTrue(saveResult.isSuccess)

        val all = repository.getAllPasswords()
        assertEquals(1, all.size)
        assertEquals("GitHub", all[0].serviceName)
        assertEquals("developer@example.com", all[0].login)
        assertEquals("SecretPassword123", all[0].password)
        assertEquals(PasswordCategory.WORK, all[0].category)
        assertEquals("Primary dev token", all[0].notes)

        // Verify disk file does NOT store plain password
        val datFile = File(tempDir, "passwords.dat")
        assertTrue(datFile.exists())
        val diskContent = datFile.readText()
        assertFalse(diskContent.contains("SecretPassword123"))
        assertTrue(diskContent.contains("GitHub"))
    }

    @Test
    fun testSearchAndCategoryFilter() = runBlocking {
        val p1 = SavedPassword(
            id = "1",
            serviceName = "Google Account",
            login = "user@gmail.com",
            password = "pass1",
            category = PasswordCategory.MAIL
        )
        val p2 = SavedPassword(
            id = "2",
            serviceName = "Sberbank Online",
            login = "+79991234567",
            password = "pass2",
            category = PasswordCategory.FINANCE
        )
        val p3 = SavedPassword(
            id = "3",
            serviceName = "Telegram Web",
            login = "@my_username",
            password = "pass3",
            category = PasswordCategory.SOCIAL
        )

        repository.savePassword(p1)
        repository.savePassword(p2)
        repository.savePassword(p3)

        // Category filter
        val financeOnly = repository.searchPasswords("", PasswordCategory.FINANCE)
        assertEquals(1, financeOnly.size)
        assertEquals("Sberbank Online", financeOnly[0].serviceName)

        // Query filter
        val searchGoogle = repository.searchPasswords("google", PasswordCategory.ALL)
        assertEquals(1, searchGoogle.size)
        assertEquals("Google Account", searchGoogle[0].serviceName)

        // Query by login
        val searchLogin = repository.searchPasswords("@gmail", PasswordCategory.ALL)
        assertEquals(1, searchLogin.size)
        assertEquals("Google Account", searchLogin[0].serviceName)

        // Empty query with ALL category returns all
        val allItems = repository.searchPasswords("", PasswordCategory.ALL)
        assertEquals(3, allItems.size)
    }

    @Test
    fun testUpdateAndDeletePassword() = runBlocking {
        val pass = SavedPassword(
            id = "item-to-update",
            serviceName = "Netflix",
            login = "watcher@stream.tv",
            password = "oldPassword",
            category = PasswordCategory.OTHER
        )
        repository.savePassword(pass)
        assertEquals("oldPassword", repository.getAllPasswords().first().password)

        // Update password
        val updatedPass = pass.copy(password = "newPassword456", notes = "Updated subscription")
        repository.savePassword(updatedPass)

        val currentList = repository.getAllPasswords()
        assertEquals(1, currentList.size)
        assertEquals("newPassword456", currentList[0].password)
        assertEquals("Updated subscription", currentList[0].notes)

        // Delete password
        val deleteResult = repository.deletePassword("item-to-update")
        assertTrue(deleteResult.isSuccess)
        assertEquals(0, repository.getAllPasswords().size)
    }

    @Test
    fun testPasswordManagerUseCases() = runBlocking {
        val getSaved = GetSavedPasswordsUseCase(repository)
        val save = SavePasswordUseCase(repository)
        val delete = DeletePasswordUseCase(repository)

        assertEquals(0, getSaved().size)

        val item = SavedPassword(
            id = "uc-1",
            serviceName = "Yandex 360",
            login = "admin@company.ru",
            password = "SecureYandexKey",
            category = PasswordCategory.WORK
        )

        save(item)
        val list = getSaved()
        assertEquals(1, list.size)
        assertEquals("Yandex 360", list.first().serviceName)

        delete("uc-1")
        assertEquals(0, getSaved().size)
    }
}
