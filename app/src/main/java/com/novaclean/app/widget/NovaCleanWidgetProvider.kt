package com.novaclean.app.widget

import android.app.ActivityManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Environment
import android.os.StatFs
import android.widget.RemoteViews
import android.widget.Toast
import com.novaclean.app.MainActivity
import com.novaclean.app.R

class NovaCleanWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (widgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, widgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_QUICK_CLEAN) {
            // Quick cache and RAM optimization
            try {
                context.cacheDir?.deleteRecursively()
                context.codeCacheDir?.deleteRecursively()
                context.externalCacheDir?.deleteRecursively()
            } catch (e: Exception) {
                // Safe fallback
            }
            System.gc()
            System.runFinalization()

            Toast.makeText(context, context.getString(R.string.widget_cleaned_toast), Toast.LENGTH_SHORT).show()

            // Update all widgets
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(ComponentName(context, NovaCleanWidgetProvider::class.java))
            for (id in ids) {
                updateWidget(context, manager, id)
            }
        }
    }

    private fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, widgetId: Int) {
        val views = RemoteViews(context.packageName, R.layout.widget_novaclean)

        // Calculate RAM percentage
        val ramPercent = try {
            val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val memInfo = ActivityManager.MemoryInfo()
            am.getMemoryInfo(memInfo)
            val used = memInfo.totalMem - memInfo.availMem
            if (memInfo.totalMem > 0) ((used.toDouble() / memInfo.totalMem.toDouble()) * 100).toInt() else 0
        } catch (e: Exception) {
            50
        }

        // Calculate Storage percentage
        val storagePercent = try {
            val stat = StatFs(Environment.getDataDirectory().path)
            val total = stat.totalBytes
            val available = stat.availableBytes
            val used = total - available
            if (total > 0) ((used.toDouble() / total.toDouble()) * 100).toInt() else 0
        } catch (e: Exception) {
            60
        }

        views.setTextViewText(R.id.widget_ram_value, "$ramPercent%")
        views.setTextViewText(R.id.widget_storage_value, "$storagePercent%")

        // Click on widget opens app
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            context,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_root, openAppPendingIntent)

        // Click on Clean button triggers ACTION_QUICK_CLEAN
        val cleanIntent = Intent(context, NovaCleanWidgetProvider::class.java).apply {
            action = ACTION_QUICK_CLEAN
        }
        val cleanPendingIntent = PendingIntent.getBroadcast(
            context,
            1,
            cleanIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_btn_clean, cleanPendingIntent)

        appWidgetManager.updateAppWidget(widgetId, views)
    }

    companion object {
        const val ACTION_QUICK_CLEAN = "com.novaclean.app.ACTION_QUICK_CLEAN"
    }
}
