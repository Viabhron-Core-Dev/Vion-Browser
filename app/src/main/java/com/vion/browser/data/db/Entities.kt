/*
 * Copyright 2026 Vion Browser Contributors. GPL v3.
 */
package com.vion.browser.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

// ── Bookmarks ──────────────────────────────────────────────────────────────

@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val url: String,
    val parentId: Long = 0,          // 0 = root
    val isFolder: Boolean = false,
    val position: Int = 0,
    val isPinnedToHome: Boolean = false,
    val faviconPath: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

// ── History ────────────────────────────────────────────────────────────────

@Entity(tableName = "history")
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val url: String,
    val visitedAt: Long = System.currentTimeMillis()
)

// ── Downloads ──────────────────────────────────────────────────────────────

@Entity(tableName = "downloads")
data class DownloadEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val url: String,
    val filePath: String,
    val mimeType: String = "",
    val fileSizeBytes: Long = 0L,
    val downloadedAt: Long = System.currentTimeMillis(),
    val isComplete: Boolean = false
)

// ── Saved Pages ────────────────────────────────────────────────────────────

@Entity(tableName = "saved_pages")
data class SavedPageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val url: String,
    val filePath: String,
    val format: String,              // "MHT" | "PDF" | "TEXT" | "SCREENSHOT"
    val fileSizeBytes: Long = 0L,
    val savedAt: Long = System.currentTimeMillis()
)

// ── Home Shortcuts ─────────────────────────────────────────────────────────

@Entity(tableName = "shortcuts")
data class ShortcutEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val label: String,
    val url: String,
    val iconBase64: String? = null,   // base-64 encoded favicon PNG
    val position: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)
