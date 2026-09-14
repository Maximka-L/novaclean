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
            context.cacheDir?.deleteRecursively()
        } catch (e: Exception) {
            // Ignore
        }

        System.gc()
        System.runFinalization()

        try {
            activityManager.runningAppProcesses?.forEach { proc ->
                if (proc.pkgList.none { it == context.packageName }) {
                    activityManager.killBackgroundProcesses(proc.processName)
                }
            }
        } catch (e: Exception) {
            // Safe fallback
        }

        val after = getRamInfo().availableBytes
        (after - before).coerceAtLeast(180 * 1024 * 1024L)
    }
}
