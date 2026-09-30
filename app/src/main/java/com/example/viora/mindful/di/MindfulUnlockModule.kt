package com.example.viora.mindful.di

import com.example.viora.mindful.data.database.dao.UnlockEventDao
import com.example.viora.mindful.data.repository.MindfulUnlockRepositoryImpl
import com.example.viora.mindful.domain.repository.MindfulUnlockRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class MindfulUnlockModule {

    @Binds
    @Singleton
    abstract fun bindMindfulUnlockRepository(
        impl: MindfulUnlockRepositoryImpl
    ): MindfulUnlockRepository
}

@Module
@InstallIn(SingletonComponent::class)
object MindfulUnlockDaoModule {

    @Provides
    @Singleton
    fun provideUnlockEventDao(
        db: com.example.viora.data.database.VioraDatabase
    ): UnlockEventDao = db.unlockEventDao()
}
