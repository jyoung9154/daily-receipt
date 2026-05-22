package com.dailyreceipt.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * DatabaseModule — Room DB 설정 (v1.0에서 실제 구현 예정)
 * 
 * v1.0 MVP에서는:
 * - UsageStatsManager (앱 사용 시간)
 * - NotificationListenerService (알림)
 * - Google Fit API (건강)
 * - CalendarContract (일정)
 * 위 데이터 소스들을 직접 사용하고, Room은 데이터 누적 저장 용도로 이후 추가
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    // TODO(v1.0): Room Database 추가 예정
    // @Provides
    // @Singleton
    // fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase { ... }
}
