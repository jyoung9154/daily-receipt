package com.dailyreceipt.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.dailyreceipt.data.local.dao.AppUsageDao
import com.dailyreceipt.data.local.dao.CalendarEventDao
import com.dailyreceipt.data.local.dao.DailySummaryDao
import com.dailyreceipt.data.local.dao.FinanceTransactionDao
import com.dailyreceipt.data.local.dao.NotificationDao
import com.dailyreceipt.data.local.entity.AppUsageEntity
import com.dailyreceipt.data.local.entity.CalendarEventEntity
import com.dailyreceipt.data.local.entity.DailySummaryEntity
import com.dailyreceipt.data.local.entity.FinanceTransactionEntity
import com.dailyreceipt.data.local.entity.NotificationEntity

@Database(
    entities = [
        DailySummaryEntity::class,
        AppUsageEntity::class,
        NotificationEntity::class,
        CalendarEventEntity::class,
        FinanceTransactionEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dailySummaryDao(): DailySummaryDao
    abstract fun appUsageDao(): AppUsageDao
    abstract fun notificationDao(): NotificationDao
    abstract fun calendarEventDao(): CalendarEventDao
    abstract fun financeTransactionDao(): FinanceTransactionDao
}
