package com.novaclean.app.data.repository

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.novaclean.app.domain.model.VaultFileType
import com.novaclean.app.domain.model.VaultItem
import com.novaclean.app.domain.repository.VaultRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.MessageDigest
import java.util.UUID

class VaultRepositoryImpl(private val context: Context) : VaultRepository {

    private val prefs = context.getSharedPreferences("novaclean_vault_prefs", Context.MODE_PRIVATE)
    private val vaultDir: File get() = File(context.filesDir, "vault").apply { mkdirs() }
    private val photosDir: File get() = File(vaultDir, "photos").apply { mkdirs() }
    private val notesDir: File get() = File(vaultDir, "notes").apply { mkdirs() }

    override fun isPinSet(): Boolean {
        return prefs.getString(KEY_PIN_HASH, null) != null
    }

    override fun verifyPin(pin: String): Boolean {
        val savedHash = prefs.getString(KEY_PIN_HASH, null) ?: return false
        return hashPin(pin) == savedHash
    }

    override fun setPin(pin: String) {
        prefs.edit().putString(KEY_PIN_HASH, hashPin(pin)).apply()
    }

    override suspend fun getVaultItems(): List<VaultItem> = withContext(Dispatchers.IO) {
        val items = mutableListOf<VaultItem>()

        // 1. Photos
        photosDir.listFiles()?.forEach { file ->
            if (file.isFile) {
                items.add(
                    VaultItem(
                        id = file.nameWithoutExtension,
                        title = file.name,
                        internalPath = file.absolutePath,
                        fileType = VaultFileType.PHOTO,
                        sizeBytes = file.length(),
                        timestamp = file.lastModified()
                    )
                )
            }
        }

        // 2. Notes
        notesDir.listFiles()?.forEach { file ->
            if (file.isFile) {
                val content = try { file.readText() } catch (e: Exception) { "" }
                items.add(
                    VaultItem(
                        id = file.nameWithoutExtension,
                        title = file.nameWithoutExtension,
                        internalPath = file.absolutePath,
                        fileType = VaultFileType.NOTE,
                        sizeBytes = file.length(),
                        timestamp = file.lastModified(),
                        noteContent = content
                    )
                )
            }
        }

        items.sortedByDescending { it.timestamp }
    }

    override suspend fun addPhotoToVault(uriString: String, fileName: String): Result<VaultItem> = withContext(Dispatchers.IO) {
        try {
            val uri = Uri.parse(uriString)
            val id = UUID.randomUUID().toString()
            val targetFile = File(photosDir, "$id.jpg")

            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(targetFile).use { output ->
                    input.copyTo(output)
                }
            } ?: return@withContext Result.failure(IllegalStateException("Failed to read image stream"))

            Result.success(
                VaultItem(
                    id = id,
                    title = fileName.ifEmpty { "Photo_$id" },
                    internalPath = targetFile.absolutePath,
                    fileType = VaultFileType.PHOTO,
                    sizeBytes = targetFile.length(),
                    timestamp = System.currentTimeMillis()
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun addNoteToVault(title: String, content: String): Result<VaultItem> = withContext(Dispatchers.IO) {
        try {
            val id = UUID.randomUUID().toString()
            val safeTitle = title.ifBlank { "Note_${System.currentTimeMillis()}" }
            val noteFile = File(notesDir, "$safeTitle.note")
            noteFile.writeText(content)

            Result.success(
                VaultItem(
                    id = id,
                    title = safeTitle,
                    internalPath = noteFile.absolutePath,
                    fileType = VaultFileType.NOTE,
                    sizeBytes = noteFile.length(),
                    timestamp = System.currentTimeMillis(),
                    noteContent = content
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteVaultItem(item: VaultItem): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val file = File(item.internalPath)
            if (file.exists()) {
                file.delete()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun restoreVaultItem(item: VaultItem): Result<String> = withContext(Dispatchers.IO) {
        try {
            val sourceFile = File(item.internalPath)
            if (!sourceFile.exists()) return@withContext Result.failure(IllegalStateException("File not found"))

            if (item.fileType == VaultFileType.PHOTO) {
                val values = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, "Restored_${item.title}")
                    put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/NovaClean_Restored")
                        put(MediaStore.Images.Media.IS_PENDING, 1)
                    }
                }

                val targetUri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                    ?: return@withContext Result.failure(IllegalStateException("Cannot insert into MediaStore"))

                context.contentResolver.openOutputStream(targetUri)?.use { out ->
                    FileInputStream(sourceFile).use { input ->
                        input.copyTo(out)
                    }
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    values.clear()
                    values.put(MediaStore.Images.Media.IS_PENDING, 0)
                    context.contentResolver.update(targetUri, values, null, null)
                }

                sourceFile.delete()
                Result.success(targetUri.toString())
            } else {
                Result.success(item.noteContent ?: "")
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun hashPin(pin: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val salted = "NovaClean_Salt_#2026:$pin"
        val bytes = md.digest(salted.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    companion object {
        private const val KEY_PIN_HASH = "vault_pin_hash_v1"
    }
}
