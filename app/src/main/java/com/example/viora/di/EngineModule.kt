package com.example.viora.di

import com.example.viora.monitoring.blocking.BlockEngine
import com.example.viora.monitoring.blocking.BlockEngineImpl
import com.example.viora.monitoring.notification.NotificationEngine
import com.example.viora.monitoring.notification.NotificationEngineImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class EngineModule {

    @Binds
    @Singleton
    abstract fun bindBlockEngine(impl: BlockEngineImpl): BlockEngine

    @Binds
    @Singleton
    abstract fun bindNotificationEngine(impl: NotificationEngineImpl): NotificationEngine
}
