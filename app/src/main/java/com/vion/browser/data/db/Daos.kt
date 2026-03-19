/*
 * Copyright 2026 Vion Browser Contributors. GPL v3.
 */
package com.vion.browser.data.db

import androidx.lifecycle.LiveData
import androidx.room.*

// ── Bookmarks DAO ──────────────────────────────────────────────────────────

@Dao
interface BookmarkDao {
    @Query("SELECT * FROM bookmarks WHERE parentId = :parentId ORDER BY position ASC, title ASC")
    fun getChildren(parentId: Long = 0): LiveData<List<BookmarkEntity>>

    @Query("SELECT * FROM bookmarks WHERE isPinnedToHome = 1 ORDER BY position ASC")
    fun getHomePinned(): LiveData<List<BookmarkEntity>>

    @Query("SELECT * FROM bookmarks WHERE id = :id")
    suspend fun getById(id: Long): BookmarkEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(bookmark: BookmarkEntity): Long

    @Update
    suspend fun update(bookmark: BookmarkEntity)

    @Delete
    suspend fun delete(bookmark: BookmarkEntity)

    @Query("DELETE FROM bookmarks WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM bookmarks WHERE url = :url LIMIT 1")
    suspend fun findByUrl(url: String): BookmarkEntity?

    @Query("SELECT MAX(position) FROM bookmarks WHERE parentId = :parentId")
    suspend fun maxPositionInFolder(parentId: Long): Int?
}

// ── History DAO ────────────────────────────────────────────────────────────

@Dao
interface HistoryDao {
    @Query("SELECT * FROM history ORDER BY visitedAt DESC")
    fun getAll(): LiveData<List<HistoryEntity>>

    @Query("SELECT * FROM history ORDER BY visitedAt DESC LIMIT :limit")
    fun getRecent(limit: Int = 200): LiveData<List<HistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: HistoryEntity)

    @Delete
    suspend fun delete(entry: HistoryEntity)

    @Query("DELETE FROM history WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM history")
    suspend fun clearAll()
}

// ── Downloads DAO ──────────────────────────────────────────────────────────

@Dao
interface DownloadDao {
    @Query("SELECT * FROM downloads ORDER BY downloadedAt DESC")
    fun getAll(): LiveData<List<DownloadEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(dl: DownloadEntity): Long

    @Update
    suspend fun update(dl: DownloadEntity)

    @Delete
    suspend fun delete(dl: DownloadEntity)

    @Query("DELETE FROM downloads WHERE isComplete = 1")
    suspend fun clearCompleted()
}

// ── Saved Pages DAO ────────────────────────────────────────────────────────

@Dao
interface SavedPageDao {
    @Query("SELECT * FROM saved_pages ORDER BY savedAt DESC")
    fun getAll(): LiveData<List<SavedPageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(page: SavedPageEntity): Long

    @Delete
    suspend fun delete(page: SavedPageEntity)

    @Query("DELETE FROM saved_pages WHERE id = :id")
    suspend fun deleteById(id: Long)
}

// ── Shortcuts DAO ──────────────────────────────────────────────────────────

@Dao
interface ShortcutDao {
    @Query("SELECT * FROM shortcuts ORDER BY position ASC, createdAt ASC")
    fun getAll(): LiveData<List<ShortcutEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(shortcut: ShortcutEntity): Long

    @Update
    suspend fun update(shortcut: ShortcutEntity)

    @Delete
    suspend fun delete(shortcut: ShortcutEntity)

    @Query("DELETE FROM shortcuts WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT COUNT(*) FROM shortcuts")
    suspend fun count(): Int
}
