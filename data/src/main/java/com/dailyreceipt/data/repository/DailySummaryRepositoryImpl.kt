package com.dailyreceipt.data.repository

import com.dailyreceipt.data.calendar.CalendarDataSource
import com.dailyreceipt.data.health.HealthDataSource
import com.dailyreceipt.data.local.dao.*
import com.dailyreceipt.data.local.entity.*
import com.dailyreceipt.data.notification.NotificationDataSource
import com.dailyreceipt.data.usage.AppUsageDataSource
import com.dailyreceipt.domain.model.*
import com.dailyreceipt.domain.repository.DailySummaryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

class DailySummaryRepositoryImpl(
    private val appUsageDataSource: AppUsageDataSource,
    private val notificationDataSource: NotificationDataSource,
    private val healthDataSource: HealthDataSource,
    private val calendarDataSource: CalendarDataSource,
    private val appUsageDao: AppUsageDao,
    private val notificationDao: NotificationDao,
    private val calendarEventDao: CalendarEventDao,
    private val financeTransactionDao: FinanceTransactionDao,
    private val dailySummaryDao: DailySummaryDao
) : DailySummaryRepository {

    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    override suspend fun collectDailyData(date: LocalDate): DailySummary {
        val dateStr = date.format(dateFormatter)
        val dateTime = date.atStartOfDay()

        // Collect all data
        val usageStats = appUsageDataSource.getUsageStats(
            date.atStartOfDay().toEpochMilli(),
            date.plusDays(1).atStartOfDay().toEpochMilli()
        )

        val notifications = notificationDataSource.getNotificationsForDate(dateTime)
        val healthData = healthDataSource.getHealthDataForDate(dateTime)
        val calendarEvents = calendarDataSource.getEventsForDay(dateTime)
        val financeTransactions = financeTransactionDao.getByDateSync(dateStr).map { it.toDomain() }

        val totalScreenTime = usageStats.sumOf { it.usageTimeMinutes }

        // Save to database
        appUsageDao.deleteByDate(dateStr)
        appUsageDao.insertAll(usageStats.map { it.toEntity(dateStr) })

        notificationDao.deleteByDate(dateStr)
        notificationDao.insertAll(notifications.map { it.toEntity(dateStr) })

        calendarEventDao.deleteByDate(dateStr)
        calendarEventDao.insertAll(calendarEvents.map { it.toEntity(dateStr) })

        financeTransactionDao.deleteByDate(dateStr)
        financeTransactionDao.insertAll(financeTransactions.map { it.toEntity(dateStr) })

        // Update summary
        val summary = DailySummaryEntity(
            date = dateStr,
            totalScreenTimeMinutes = totalScreenTime,
            totalNotifications = notifications.size,
            totalSteps = healthData.steps,
            totalDistanceMeters = healthData.distanceMeters,
            totalCaloriesBurned = healthData.caloriesBurned,
            totalActiveMinutes = healthData.activeMinutes,
            totalTransactions = financeTransactions.size,
            totalTransactionAmount = financeTransactions.filter { it.transactionType == TransactionType.PAYMENT }.sumOf { it.amount },
            totalCalendarEvents = calendarEvents.size
        )
        dailySummaryDao.insert(summary)

        return DailySummary(
            date = date,
            usageStats = usageStats,
            notifications = notifications,
            healthData = healthData,
            calendarEvents = calendarEvents,
            financeTransactions = financeTransactions,
            totalScreenTimeMinutes = totalScreenTime,
            totalNotifications = notifications.size,
            totalCalendarEvents = calendarEvents.size
        )
    }

    override suspend fun getDailySummary(date: LocalDate): DailySummary? {
        val dateStr = date.format(dateFormatter)
        val summaryEntity = dailySummaryDao.getByDate(dateStr) ?: return null

        val usageStats = appUsageDao.getByDateSync(dateStr).map { it.toDomain() }
        val notifications = notificationDao.getByDateSync(dateStr).map { it.toDomain() }
        val calendarEvents = calendarEventDao.getByDateSync(dateStr).map { it.toDomain() }
        val financeTransactions = financeTransactionDao.getByDateSync(dateStr).map { it.toDomain() }

        return DailySummary(
            date = date,
            usageStats = usageStats,
            notifications = notifications,
            healthData = HealthData(
                steps = summaryEntity.totalSteps,
                distanceMeters = summaryEntity.totalDistanceMeters,
                activeMinutes = summaryEntity.totalActiveMinutes,
                caloriesBurned = summaryEntity.totalCaloriesBurned
            ),
            calendarEvents = calendarEvents,
            financeTransactions = financeTransactions,
            totalScreenTimeMinutes = summaryEntity.totalScreenTimeMinutes,
            totalNotifications = summaryEntity.totalNotifications,
            totalCalendarEvents = summaryEntity.totalCalendarEvents
        )
    }

    override fun observeDailySummary(date: LocalDate): Flow<DailySummary?> {
        val dateStr = date.format(dateFormatter)
        return dailySummaryDao.observeByDate(dateStr).map { entity ->
            entity?.let {
                DailySummary(
                    date = date,
                    totalScreenTimeMinutes = it.totalScreenTimeMinutes,
                    totalNotifications = it.totalNotifications,
                    healthData = HealthData(
                        steps = it.totalSteps,
                        distanceMeters = it.totalDistanceMeters,
                        activeMinutes = it.totalActiveMinutes,
                        caloriesBurned = it.totalCaloriesBurned
                    ),
                    totalCalendarEvents = it.totalCalendarEvents
                )
            }
        }
    }

    override fun getRecentSummaries(limit: Int): Flow<List<DailySummary>> {
        return dailySummaryDao.getRecentSummaries(limit).map { entities ->
            entities.map { entity ->
                DailySummary(
                    date = LocalDate.parse(entity.date, dateFormatter),
                    totalScreenTimeMinutes = entity.totalScreenTimeMinutes,
                    totalNotifications = entity.totalNotifications,
                    healthData = HealthData(
                        steps = entity.totalSteps,
                        distanceMeters = entity.totalDistanceMeters,
                        activeMinutes = entity.totalActiveMinutes,
                        caloriesBurned = entity.totalCaloriesBurned
                    ),
                    totalCalendarEvents = entity.totalCalendarEvents
                )
            }
        }
    }

    override suspend fun deleteOldData(olderThan: LocalDate) {
        val dateStr = olderThan.format(dateFormatter)
        dailySummaryDao.deleteOlderThan(dateStr)
        appUsageDao.deleteOlderThan(dateStr)
        notificationDao.deleteOlderThan(dateStr)
        calendarEventDao.deleteOlderThan(dateStr)
        financeTransactionDao.deleteOlderThan(dateStr)
    }

    // Extension functions for entity conversion
    private fun AppUsage.toEntity(date: String) = AppUsageEntity(
        packageName = packageName,
        appName = appName,
        usageTimeMillis = usageTimeMillis,
        lastUsed = lastUsed?.toString(),
        category = category,
        date = date
    )

    private fun AppUsageEntity.toDomain() = AppUsage(
        packageName = packageName,
        appName = appName,
        usageTimeMillis = usageTimeMillis,
        lastUsed = lastUsed?.let { LocalDateTime.parse(it) },
        category = category
    )

    private fun Notification.toEntity(date: String) = NotificationEntity(
        id = id,
        packageName = packageName,
        appName = appName,
        title = title,
        content = content,
        postedTime = postedTime.toString(),
        category = category,
        date = date
    )

    private fun NotificationEntity.toDomain() = Notification(
        id = id,
        packageName = packageName,
        appName = appName,
        title = title,
        content = content,
        postedTime = LocalDateTime.parse(postedTime),
        category = category
    )

    private fun CalendarEvent.toEntity(date: String) = CalendarEventEntity(
        id = id,
        title = title,
        description = description,
        location = location,
        startTime = startTime.toString(),
        endTime = endTime?.toString(),
        isAllDay = isAllDay,
        calendarId = calendarId,
        date = date
    )

    private fun CalendarEventEntity.toDomain() = CalendarEvent(
        id = id,
        title = title,
        description = description,
        location = location,
        startTime = LocalDateTime.parse(startTime),
        endTime = endTime?.let { LocalDateTime.parse(it) },
        isAllDay = isAllDay,
        calendarId = calendarId
    )

    private fun FinanceTransaction.toEntity(date: String) = FinanceTransactionEntity(
        id = id,
        source = source,
        amount = amount,
        merchantName = merchantName,
        transactionType = transactionType.name,
        timestamp = timestamp.toString(),
        date = date
    )

    private fun FinanceTransactionEntity.toDomain() = FinanceTransaction(
        id = id,
        source = source,
        amount = amount,
        merchantName = merchantName,
        transactionType = TransactionType.valueOf(transactionType),
        timestamp = LocalDateTime.parse(timestamp)
    )

    private fun LocalDateTime.toEpochMilli(): Long =
        atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
}
