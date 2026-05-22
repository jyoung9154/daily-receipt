package com.dailyreceipt.data.usage

import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import com.dailyreceipt.domain.model.AppUsage
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

/**
 * DataSource for app usage statistics using UsageStatsManager.
 * Requires PACKAGE_USAGE_STATS permission.
 */
@Singleton
class AppUsageDataSource @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val usageStatsManager: UsageStatsManager by lazy {
        context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
    }

    private val packageManager: PackageManager = context.packageManager

    /**
     * Check if the app has permission to access usage stats.
     */
    fun hasPermission(): Boolean {
        val time = System.currentTimeMillis()
        val stats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            time - 1000 * 60,
            time
        )
        return stats != null && stats.isNotEmpty()
    }

    /**
     * Get usage stats for a specific date range.
     */
    suspend fun getUsageStats(startTime: Long, endTime: Long): List<AppUsage> =
        withContext(Dispatchers.IO) {
            val stats = usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY,
                startTime,
                endTime
            )

            stats
                .filter { it.totalTimeInForeground > 0 }
                .mapNotNull { stat ->
                    try {
                        val appInfo = packageManager.getApplicationInfo(stat.packageName, 0)
                        val appName = packageManager.getApplicationLabel(appInfo).toString()
                        val category = getAppCategory(appInfo)

                        AppUsage(
                            packageName = stat.packageName,
                            appName = appName,
                            usageTimeMillis = stat.totalTimeInForeground,
                            lastUsed = stat.lastTimeUsed.let {
                                if (it > 0) {
                                    LocalDateTime.ofInstant(
                                        java.time.Instant.ofEpochMilli(it),
                                        ZoneId.systemDefault()
                                    )
                                } else null
                            },
                            category = category
                        )
                    } catch (e: PackageManager.NameNotFoundException) {
                        null
                    }
                }
                .sortedByDescending { it.usageTimeMillis }
        }

    /**
     * Get usage stats for today.
     */
    suspend fun getTodayUsageStats(): List<AppUsage> {
        val now = System.currentTimeMillis()
        val startOfDay = getStartOfDayMillis(now)
        return getUsageStats(startOfDay, now)
    }

    /**
     * Get total screen time for today in minutes.
     */
    suspend fun getTodayScreenTimeMinutes(): Long {
        val stats = getTodayUsageStats()
        return stats.sumOf { it.usageTimeMinutes }
    }

    private fun getStartOfDayMillis(timestamp: Long): Long {
        val localDate = java.time.Instant.ofEpochMilli(timestamp)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
        return localDate.atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    }

    private fun getAppCategory(appInfo: ApplicationInfo): String {
        return when {
            isSystemApp(appInfo) -> "System"
            hasCategory(appInfo, "android.intent.category.LAUNCHER") -> "Launcher"
            hasCategory(appInfo, "android.intent.category.GAME") -> "Game"
            isCommunicationApp(appInfo.packageName) -> "Communication"
            isSocialMediaApp(appInfo.packageName) -> "Social Media"
            isMediaApp(appInfo.packageName) -> "Media"
            isProductivityApp(appInfo.packageName) -> "Productivity"
            else -> "Other"
        }
    }

    private fun isSystemApp(appInfo: ApplicationInfo): Boolean {
        return (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0 &&
                (appInfo.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == 0
    }

    private fun hasCategory(appInfo: ApplicationInfo, category: String): Boolean {
        return try {
            val intent = packageManager.getLaunchIntentForPackage(appInfo.packageName)
            intent?.categories?.contains(category) == true
        } catch (e: Exception) {
            false
        }
    }

    private fun isCommunicationApp(packageName: String): Boolean {
        val commPackages = setOf(
            "com.kakao.talk",           // KakaoTalk
            "com.google.android.apps.messaging", // Google Messages
            "com.samsung.android.messaging",    // Samsung Messages
            "org.telegram.messenger",   // Telegram
            "com.whatsapp",             // WhatsApp
            "com.facebook.orca",        // Facebook Messenger
            "com.slack",                // Slack
            "org.zwanoo.android.signal" // Signal
        )
        return packageName in commPackages
    }

    private fun isSocialMediaApp(packageName: String): Boolean {
        val socialPackages = setOf(
            "com.instagram.android",
            "com.facebook.katana",
            "com.twitter.android",
            "com.snapchat.android",
            "com.zhiliaoapp.musically",  // TikTok
            "com.reddit.frontpage"
        )
        return packageName in socialPackages
    }

    private fun isMediaApp(packageName: String): Boolean {
        val mediaPackages = setOf(
            "com.google.android.youtube",
            "com.netflix.mediaclient",
            "com.spotify.music",
            "com.apple.android.music",
            "tv.twitch.android.app"
        )
        return packageName in mediaPackages
    }

    private fun isProductivityApp(packageName: String): Boolean {
        val productivityPackages = setOf(
            "com.google.android.apps.docs",
            "com.google.android.apps.calendar",
            "com.microsoft.office.word",
            "com.microsoft.office.excel",
            "com.microsoft.office.powerpoint",
            "com.dropbox.android"
        )
        return packageName in productivityPackages
    }
}
