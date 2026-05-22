package com.dailyreceipt.data.notification

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.dailyreceipt.domain.model.Notification
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * NotificationListenerService implementation to capture notifications.
 * Registered in AndroidManifest.xml.
 */
@AndroidEntryPoint
class NotificationListenerServiceImpl : NotificationListenerService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private var notificationDataSource: NotificationDataSource? = null

    override fun onCreate() {
        super.onCreate()
        // DataSource will be injected via Hilt entry point
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        sbn ?: return

        // Skip our own notifications
        if (sbn.packageName == packageName) return

        try {
            val notification = buildNotification(sbn)
            notificationDataSource?.addNotification(notification)
        } catch (e: Exception) {
            // Log error but don't crash
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        // We don't need to track removed notifications for this use case
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        _isConnected.value = true
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        _isConnected.value = false
    }

    private fun buildNotification(sbn: StatusBarNotification): Notification {
        val extras = sbn.notification.extras

        val title = extras.getCharSequence("android.title")?.toString()
        val content = extras.getCharSequence("android.text")?.toString()

        val postedTime = LocalDateTime.ofInstant(
            Instant.ofEpochMilli(sbn.postTime),
            ZoneId.systemDefault()
        )

        return Notification(
            id = sbn.key,
            packageName = sbn.packageName,
            appName = getAppName(sbn.packageName),
            title = title,
            content = content,
            postedTime = postedTime,
            category = sbn.notification.category
        )
    }

    private fun getAppName(packageName: String): String {
        return try {
            val appInfo = packageManager.getApplicationInfo(packageName, 0)
            packageManager.getApplicationLabel(appInfo).toString()
        } catch (e: Exception) {
            packageName
        }
    }

    /**
     * Entry point for Hilt injection.
     */
    fun setDataSource(dataSource: NotificationDataSource) {
        this.notificationDataSource = dataSource
    }
}

/**
 * Hilt EntryPoint for accessing NotificationListenerServiceImpl.
 */
@dagger.hilt.EntryPoint
@dagger.hilt.InstallIn(
    dagger.hilt.components.SingletonComponent::class,
    scope = dagger.hilt.android.components.ServiceComponent::class
)
interface NotificationListenerEntryPoint {
    fun notificationDataSource(): NotificationDataSource
}
