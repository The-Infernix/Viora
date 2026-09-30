package com.example.viora.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.viora.data.database.dao.AppLimitDao
import com.example.viora.data.database.dao.DailySummaryDao
import com.example.viora.data.database.dao.FocusSessionDao
import com.example.viora.data.database.dao.UsageEventDao
import com.example.viora.data.database.entity.AppLimitEntity
import com.example.viora.data.database.entity.DailySummaryEntity
import com.example.viora.data.database.entity.FocusSessionEntity
import com.example.viora.data.database.entity.UsageEventEntity
import com.example.viora.mindful.data.database.dao.UnlockEventDao
import com.example.viora.mindful.data.database.entity.UnlockEventEntity

@Database(
    entities = [
        UsageEventEntity::class,
        FocusSessionEntity::class,
        DailySummaryEntity::class,
        AppLimitEntity::class,
        UnlockEventEntity::class
    ],
    version = 2,
    exportSchema = true
)
abstract class VioraDatabase : RoomDatabase() {
    abstract fun usageEventDao(): UsageEventDao
    abstract fun focusSessionDao(): FocusSessionDao
    abstract fun dailySummaryDao(): DailySummaryDao
    abstract fun appLimitDao(): AppLimitDao
    abstract fun unlockEventDao(): UnlockEventDao

    companion object {
        const val DATABASE_NAME = "viora.db"
    }
}
