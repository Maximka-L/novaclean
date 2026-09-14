package com.novaclean.app.data.datasource

import android.content.Context
import android.os.Environment
import com.novaclean.app.domain.model.JunkCategory
import com.novaclean.app.domain.model.JunkItem
import com.novaclean.app.domain.model.JunkScanResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class FileSystemDataSource(private val context: Context) {

    suspend fun scanJunk(): JunkScanResult = withContext(Dispatchers.IO) {
        val junkList = mutableListOf<JunkItem>()

        val searchDirs = listOfNotNull(
            context.cacheDir,
            context.externalCacheDir,
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        )

        for (dir in searchDirs) {
            if (dir.exists() && dir.canRead()) {
                scanDirectoryRecursively(dir, junkList, maxDepth = 4, currentDepth = 0)
            }
        }

        val totalSize = junkList.sumOf { it.size }
        val categoryCount = junkList.groupBy { it.category }.mapValues { it.value.size }
        val categorySizes = junkList.groupBy { it.category }.mapValues { it.value.sumOf { item -> item.size } }

        JunkScanResult(
            items = junkList,
            totalSize = totalSize,
            categoryCount = categoryCount,
            categorySizes = categorySizes
        )
    }

    private fun scanDirectoryRecursively(
        directory: File,
        results: MutableList<JunkItem>,
        maxDepth: Int,
        currentDepth: Int
    ) {
        if (currentDepth > maxDepth) return
        val files = directory.listFiles() ?: return

        for (file in files) {
            if (file.isDirectory) {
                val subFiles = file.listFiles()
                if (subFiles != null && subFiles.isEmpty()) {
                    results.add(
                        JunkItem(
                            id = file.absolutePath,
                            name = file.name,
                            path = file.absolutePath,
                            size = 0L,
                            category = JunkCategory.EMPTY_FOLDERS
                        )
                    )
                } else {
                    scanDirectoryRecursively(file, results, maxDepth, currentDepth + 1)
                }
            } else if (file.isFile) {
                val nameLower = file.name.lowercase()
                val size = file.length()

                when {
                    size == 0L && !file.name.startsWith(".nomedia") -> {
                        results.add(
                            JunkItem(
                                id = file.absolutePath,
                                name = file.name,
                                path = file.absolutePath,
                                size = 0L,
                                category = JunkCategory.ZERO_BYTE_FILES
                            )
                        )
                    }
                    nameLower.endsWith(".apk") -> {
                        results.add(
                            JunkItem(
                                id = file.absolutePath,
                                name = file.name,
                                path = file.absolutePath,
                                size = size,
                                category = JunkCategory.OLD_APKS
                            )
                        )
                    }
                    nameLower.endsWith(".tmp") || nameLower.endsWith(".log") ||
                    nameLower.endsWith(".bak") || nameLower.endsWith(".crdownload") -> {
                        results.add(
                            JunkItem(
                                id = file.absolutePath,
                                name = file.name,
                                path = file.absolutePath,
                                size = size,
                                category = JunkCategory.TEMP_FILES
                            )
                        )
                    }
                    nameLower.endsWith(".thumb") || file.parent?.contains("cache", ignoreCase = true) == true -> {
                        results.add(
                            JunkItem(
                                id = file.absolutePath,
                                name = file.name,
                                path = file.absolutePath,
                                size = size,
                                category = JunkCategory.APP_CACHE
                            )
                        )
                    }
                }
            }
        }
    }

    suspend fun cleanFiles(items: List<JunkItem>): Pair<Int, Long> = withContext(Dispatchers.IO) {
        var deletedCount = 0
        var freedBytes = 0L

        for (item in items) {
            try {
                val file = File(item.path)
                val fileSize = file.length()
                if (file.exists() && file.delete()) {
                    deletedCount++
                    freedBytes += fileSize
                }
            } catch (e: Exception) {
                // Ignore
            }
        }
        Pair(deletedCount, freedBytes)
    }
}
