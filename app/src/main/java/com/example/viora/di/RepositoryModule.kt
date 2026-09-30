package com.example.viora.di

import com.example.viora.data.repository.FocusRepositoryImpl
import com.example.viora.data.repository.InsightsRepositoryImpl
import com.example.viora.data.repository.SettingsRepositoryImpl
import com.example.viora.data.repository.UsageRepositoryImpl
import com.example.viora.domain.repository.FocusRepository
import com.example.viora.domain.repository.InsightsRepository
import com.example.viora.domain.repository.SettingsRepository
import com.example.viora.domain.repository.UsageRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindUsageRepository(impl: UsageRepositoryImpl): UsageRepository

    @Binds
    @Singleton
    abstract fun bindFocusRepository(impl: FocusRepositoryImpl): FocusRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository

    @Binds
    @Singleton
    abstract fun bindInsightsRepository(impl: InsightsRepositoryImpl): InsightsRepository
}
