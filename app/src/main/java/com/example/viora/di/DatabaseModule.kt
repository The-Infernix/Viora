package com.example.viora.di

import android.content.Context
import androidx.room.Room
import com.example.viora.data.database.VioraDatabase
import com.example.viora.data.database.dao.AppLimitDao
import com.example.viora.data.database.dao.DailySummaryDao
import com.example.viora.data.database.dao.FocusSessionDao
import com.example.viora.data.database.dao.UsageEventDao
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
    fun provideDatabase(@ApplicationContext context: Context): VioraDatabase {
        return Room.databaseBuilder(
            context,
            VioraDatabase::class.java,
            VioraDatabase.DATABASE_NAME
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideUsageEventDao(db: VioraDatabase): UsageEventDao = db.usageEventDao()

    @Provides
    fun provideFocusSessionDao(db: VioraDatabase): FocusSessionDao = db.focusSessionDao()

    @Provides
    fun provideDailySummaryDao(db: VioraDatabase): DailySummaryDao = db.dailySummaryDao()

    @Provides
    fun provideAppLimitDao(db: VioraDatabase): AppLimitDao = db.appLimitDao()
}
