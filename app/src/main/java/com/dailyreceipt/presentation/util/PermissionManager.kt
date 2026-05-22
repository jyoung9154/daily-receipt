package com.dailyreceipt.presentation.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings

/**
 * Android 권한 관리 유틸리티
 * 
 * 개별 권한 요청과 설정 화면 이동 처리
 */
object PermissionManager {

    /**
     * 권한 유형
     */
    enum class PermissionType {
        USAGE_STATS,      // 앱 사용량
        NOTIFICATION,     // 알림 접근
        ACTIVITY,         // 신체 활동 (Google Fit)
        CALENDAR,         // 캘린더
        POST_NOTIFICATIONS // POST_NOTIFICATIONS (Android 13+)
    }

    /**
     * 특정 권한의 설정 화면으로 이동
     */
    fun openPermissionSettings(context: Context, type: PermissionType) {
        val intent = when (type) {
            PermissionType.USAGE_STATS -> {
                Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
            }
            PermissionType.NOTIFICATION -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                        putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                    }
                } else {
                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = android.net.Uri.parse("package:${context.packageName}")
                    }
                }
            }
            PermissionType.ACTIVITY -> {
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = android.net.Uri.parse("package:${context.packageName}")
                }
            }
            PermissionType.CALENDAR -> {
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = android.net.Uri.parse("package:${context.packageName}")
                }
            }
            PermissionType.POST_NOTIFICATIONS -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                        putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                    }
                } else {
                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = android.net.Uri.parse("package:${context.packageName}")
                    }
                }
            }
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            // fallback: 일반 앱 설정
            val fallback = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = android.net.Uri.parse("package:${context.packageName}")
            }
            context.startActivity(fallback)
        }
    }

    /**
     * 해당 권한이 이미 허용되어 있는지 확인
     */
    fun isPermissionGranted(context: Context, type: PermissionType): Boolean {
        return when (type) {
            PermissionType.USAGE_STATS -> isUsageStatsAllowed(context)
            PermissionType.NOTIFICATION -> isNotificationListenerEnabled(context)
            PermissionType.ACTIVITY -> true // Google Fit 연결 상태는 별도 확인 필요
            PermissionType.CALENDAR -> true // READ_CALENDAR는 runtime permission
            PermissionType.POST_NOTIFICATIONS -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) == 
                        android.content.pm.PackageManager.PERMISSION_GRANTED
                } else true
            }
        }
    }

    /**
     * Usage Stats 권한 확인
     */
    private fun isUsageStatsAllowed(context: Context): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as android.app.AppOpsManager
        val mode = appOps.checkOpNoThrow(
            android.app.AppOpsManager.OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(),
            context.packageName
        )
        return mode == android.app.AppOpsManager.MODE_ALLOWED
    }

    /**
     * Notification Listener 권한 확인
     */
    private fun isNotificationListenerEnabled(context: Context): Boolean {
        val packageName = android.content.ComponentName(
            context, 
            "com.dailyreceipt.data.notification.NotificationListenerServiceImpl"
        ).flattenToString()
        val enabledListeners = Settings.Secure.getString(
            context.contentResolver,
            "enabled_notification_listeners"
        )
        return enabledListeners?.contains(packageName) == true
    }
}

/**
 * 권한 요청 콜백
 */
interface PermissionCallback {
    fun onPermissionResult(type: PermissionManager.PermissionType, granted: Boolean)
}
