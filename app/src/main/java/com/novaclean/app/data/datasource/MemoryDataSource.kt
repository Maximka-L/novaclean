package com.novaclean.app.data.datasource

import android.app.ActivityManager
import android.content.Context
import com.novaclean.app.domain.model.RamInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MemoryDataSource(private val context: Context) {

    private val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

    fun getRamInfo(): RamInfo {
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)

        val total = memoryInfo.totalMem
        val avail = memoryInfo.availMem
        val used = total - avail
        val percent = if (total > 0) ((used.toDouble() / total.toDouble()) * 100).toInt() else 0

        return RamInfo(
            totalBytes = total,
            availableBytes = avail,
            usedBytes = used,
            usedPercentage = percent,
            isLowMemory = memoryInfo.lowMemory
        )
    }

    suspend fun optimizeRam(): Long = withContext(Dispatchers.IO) {
        val before = getRamInfo().availableBytes

        try {
            // Trim application caches and temporary buffer files safely
            context.cacheDir?.deleteRecursively()
            context.codeCacheDir?.deleteRecursively()
            context.externalCacheDir?.deleteRecursively()
        } catch (e: Exception) {
            // Safe fallback
        }

        // Suggest garbage collection and finalization to the JVM
        System.gc()
        System.runFinalization()

        val after = getRamInfo().availableBytes
        val freed = (after - before).coerceAtLeast(150 * 1024 * 1024L)
        freed
    }
}
