package com.dailyreceipt.data.di

import android.content.Context
import androidx.room.Room
import com.dailyreceipt.data.local.AppDatabase
import com.dailyreceipt.data.local.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "daily_receipt_db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideDailySummaryDao(database: AppDatabase): DailySummaryDao {
        return database.dailySummaryDao()
    }

    @Provides
    fun provideAppUsageDao(database: AppDatabase): AppUsageDao {
        return database.appUsageDao()
    }

    @Provides
    fun provideNotificationDao(database: AppDatabase): NotificationDao {
        return database.notificationDao()
    }

    @Provides
    fun provideCalendarEventDao(database: AppDatabase): CalendarEventDao {
        return database.calendarEventDao()
    }

    @Provides
    fun provideFinanceTransactionDao(database: AppDatabase): FinanceTransactionDao {
        return database.financeTransactionDao()
    }
}
