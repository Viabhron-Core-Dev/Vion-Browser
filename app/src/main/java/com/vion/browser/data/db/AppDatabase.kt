/*
 * Copyright 2026 Vion Browser Contributors. GPL v3.
 */
package com.vion.browser.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.vion.browser.extension.UserScript
import com.vion.browser.extension.UserScriptDao

@Database(
    entities = [BookmarkEntity::class, HistoryEntity::class, DownloadEntity::class,
                SavedPageEntity::class, ShortcutEntity::class, UserScript::class],
    version = 3,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun bookmarkDao(): BookmarkDao
    abstract fun historyDao(): HistoryDao
    abstract fun downloadDao(): DownloadDao
    abstract fun savedPageDao(): SavedPageDao
    abstract fun shortcutDao(): ShortcutDao
    abstract fun userScriptDao(): UserScriptDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "vion_browser.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
    }
}
