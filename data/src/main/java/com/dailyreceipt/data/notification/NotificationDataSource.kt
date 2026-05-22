package com.dailyreceipt.data.notification

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import com.dailyreceipt.domain.model.FinanceTransaction
import com.dailyreceipt.domain.model.Notification
import com.dailyreceipt.domain.model.TransactionType
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * DataSource for notification data.
 * Uses NotificationListenerService to capture notifications.
 * Also parses finance notifications for transaction data.
 */
@Singleton
class NotificationDataSource @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val _notifications = MutableStateFlow<List<Notification>>(emptyList())
    val notifications: StateFlow<List<Notification>> = _notifications.asStateFlow()

    private val _financeTransactions = MutableStateFlow<List<FinanceTransaction>>(emptyList())
    val financeTransactions: StateFlow<List<FinanceTransaction>> = _financeTransactions.asStateFlow()

    /**
     * Check if notification listener permission is granted.
     */
    fun hasPermission(): Boolean {
        val packageName = context.packageName
        val enabledListeners = Settings.Secure.getString(
            context.contentResolver,
            "enabled_notification_listeners"
        )
        return enabledListeners?.contains(packageName) == true
    }

    /**
     * Open system settings to grant notification listener permission.
     */
    fun getPermissionSettingsIntent(): Intent {
        return Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
    }

    /**
     * Add a captured notification to the flow.
     * Called by NotificationListenerServiceImpl.
     */
    fun addNotification(notification: Notification) {
        val current = _notifications.value.toMutableList()
        current.add(0, notification)
        _notifications.value = current.take(1000) // Keep last 1000

        // Try to parse as finance transaction
        parseFinanceTransaction(notification)?.let { transaction ->
            val transactions = _financeTransactions.value.toMutableList()
            transactions.add(0, transaction)
            _financeTransactions.value = transactions.take(500)
        }
    }

    /**
     * Clear all notifications (e.g., when day changes).
     */
    fun clearNotifications() {
        _notifications.value = emptyList()
        _financeTransactions.value = emptyList()
    }

    /**
     * Get notifications for a specific date.
     */
    fun getNotificationsForDate(date: LocalDateTime): List<Notification> {
        return _notifications.value.filter {
            it.postedTime.toLocalDate() == date.toLocalDate()
        }
    }

    /**
     * Parse finance-related notifications to extract transaction data.
     */
    private fun parseFinanceTransaction(notification: Notification): FinanceTransaction? {
        val packageName = notification.packageName
        val content = notification.content ?: return null

        val (source, amount, merchant, type) = when {
            // KakaoPay
            packageName == "com.kakaopay.service" -> {
                parseKakaoPayNotification(content)
            }
            // Samsung Pay
            packageName == "com.samsung.android.spay" -> {
                parseSamsungPayNotification(content)
            }
            // Naver Pay
            packageName == "com.nhn.android.search" -> {
                parseNaverPayNotification(content)
            }
            // Toss
            packageName == "com.vivarepublic.toss" -> {
                parseTossNotification(content)
            }
            // Standard banking notifications
            isBankingPackage(packageName) -> {
                parseBankingNotification(content)
            }
            else -> return null
        }

        return FinanceTransaction(
            id = UUID.randomUUID().toString(),
            source = source,
            amount = amount,
            merchantName = merchant,
            transactionType = type,
            timestamp = notification.postedTime
        )
    }

    private fun parseKakaoPayNotification(content: String): Quad<String, Long, String?, TransactionType> {
        // KakaoPay patterns: "12,500원 결제", "45,000원 송금"
        val paymentPattern = "([\\d,]+)원\\s*(?:결제|구매)".toRegex()
        val transferPattern = "([\\d,]+)원\\s*송금".toRegex()
        val depositPattern = "([\\d,]+)원\\s*(?:입금|받음)".toRegex()

        paymentPattern.find(content)?.let { match ->
            val amount = match.groupValues[1].replace(",", "").toLongOrNull() ?: 0
            return Quad("KakaoPay", amount, extractMerchant(content), TransactionType.PAYMENT)
        }

        transferPattern.find(content)?.let { match ->
            val amount = match.groupValues[1].replace(",", "").toLongOrNull() ?: 0
            return Quad("KakaoPay", amount, extractRecipient(content), TransactionType.TRANSFER)
        }

        depositPattern.find(content)?.let { match ->
            val amount = match.groupValues[1].replace(",", "").toLongOrNull() ?: 0
            return Quad("KakaoPay", amount, extractSender(content), TransactionType.DEPOSIT)
        }

        return Quad("KakaoPay", 0, null, TransactionType.PAYMENT)
    }

    private fun parseSamsungPayNotification(content: String): Quad<String, Long, String?, TransactionType> {
        // Samsung Pay patterns: "12,500원 사용", "45,000원 충전"
        val paymentPattern = "([\\d,]+)원\\s*(?:사용|결제)".toRegex()

        paymentPattern.find(content)?.let { match ->
            val amount = match.groupValues[1].replace(",", "").toLongOrNull() ?: 0
            return Quad("Samsung Pay", amount, extractMerchant(content), TransactionType.PAYMENT)
        }

        return Quad("Samsung Pay", 0, null, TransactionType.PAYMENT)
    }

    private fun parseNaverPayNotification(content: String): Quad<String, Long, String?, TransactionType> {
        val paymentPattern = "([\\d,]+)원\\s*(?:결제|구매)".toRegex()

        paymentPattern.find(content)?.let { match ->
            val amount = match.groupValues[1].replace(",", "").toLongOrNull() ?: 0
            return Quad("Naver Pay", amount, extractMerchant(content), TransactionType.PAYMENT)
        }

        return Quad("Naver Pay", 0, null, TransactionType.PAYMENT)
    }

    private fun parseTossNotification(content: String): Quad<String, Long, String?, TransactionType> {
        val paymentPattern = "([\\d,]+)원\\s*(?:결제|지출)".toRegex()

        paymentPattern.find(content)?.let { match ->
            val amount = match.groupValues[1].replace(",", "").toLongOrNull() ?: 0
            return Quad("Toss", amount, extractMerchant(content), TransactionType.PAYMENT)
        }

        return Quad("Toss", 0, null, TransactionType.PAYMENT)
    }

    private fun parseBankingNotification(content: String): Quad<String, Long, String?, TransactionType> {
        val amountPattern = "([\\d,]+)원".toRegex()
        val amounts = amountPattern.findAll(content).map {
            it.groupValues[1].replace(",", "").toLongOrNull() ?: 0
        }.toList()

        val amount = amounts.firstOrNull() ?: 0
        val type = when {
            content.contains("출금") || content.contains("이체") -> TransactionType.WITHDRAWAL
            content.contains("입금") -> TransactionType.DEPOSIT
            else -> TransactionType.PAYMENT
        }

        return Quad("Banking", amount, null, type)
    }

    private fun isBankingPackage(packageName: String): Boolean {
        val bankingPackages = setOf(
            "com.kakao.bank",           // Kakao Bank
            "com.shinhan.sbanking",     // Shinhan Bank
            "com.kbstar.reboot",        // KB Bank
            "com.nonghyup.bank",        // NongHyup Bank
            "com.ibk.wooribank",        // Woori Bank
            "com.hanmi.bank",           // Hanmi Bank
            "com.sc.b抵当",              // Standard Chartered
            "com.citibank.online"       // Citibank
        )
        return packageName in bankingPackages
    }

    private fun extractMerchant(content: String): String? {
        // Try to extract merchant name from various patterns
        val patterns = listOf(
            "(.+?)\\s*(?:에|에서)",  // "...에" or "...에서"
            "(?:결제|사용)\\s*(.+?)(?:\\s|$)",  // "결제 ... " or "사용 ... "
            "\\[(.+?)]"  // "[Merchant]"
        )

        for (pattern in patterns) {
            pattern.toRegex().find(content)?.let { match ->
                val merchant = match.groupValues[1].trim()
                if (merchant.isNotEmpty() && merchant.length < 50) {
                    return merchant
                }
            }
        }
        return null
    }

    private fun extractRecipient(content: String): String? {
        val pattern = "(.+?)\\s*님\\s*에게".toRegex()
        return pattern.find(content)?.groupValues?.get(1)?.trim()
    }

    private fun extractSender(content: String): String? {
        val pattern = "(.+?)\\s*님\\s*으로부터".toRegex()
        return pattern.find(content)?.groupValues?.get(1)?.trim()
    }

    // Simple 4-tuple data class
    data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
}
