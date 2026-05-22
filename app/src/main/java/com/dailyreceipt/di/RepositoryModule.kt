package com.dailyreceipt.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * RepositoryModule — Repository DI 설정
 * 
 * v1.0 MVP에서는 데이터 소스들을 ViewModel에서 직접 사용
 * Repository 패턴은 Cloud Sync (v1.1) 때 도입 예정
 */
@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    // TODO(v1.1): Repository Bindings 추가 예정
}
