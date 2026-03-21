/*
 * Copyright 2026 Vion Browser Contributors. GPL v3.
 */
package com.vion.browser.ui.panel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.vion.browser.data.db.*
import kotlinx.coroutines.launch

class PanelViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)

    // ── Bookmarks ──────────────────────────────────────────────────────────
    val rootBookmarks: LiveData<List<BookmarkEntity>> = db.bookmarkDao().getChildren(0)
    val pinnedBookmarks: LiveData<List<BookmarkEntity>> = db.bookmarkDao().getHomePinned()

    fun addBookmark(title: String, url: String, pinToHome: Boolean = false) {
        viewModelScope.launch {
            val pos = db.bookmarkDao().maxPositionInFolder(0) ?: 0
            db.bookmarkDao().insert(
                BookmarkEntity(title = title, url = url, position = pos + 1,
                    isPinnedToHome = pinToHome)
            )
        }
    }

    fun deleteBookmark(bookmark: BookmarkEntity) {
        viewModelScope.launch { db.bookmarkDao().delete(bookmark) }
    }

    fun toggleBookmarkPin(bookmark: BookmarkEntity) {
        viewModelScope.launch {
            db.bookmarkDao().update(bookmark.copy(isPinnedToHome = !bookmark.isPinnedToHome))
        }
    }

    // ── History ────────────────────────────────────────────────────────────
    val history: LiveData<List<HistoryEntity>> = db.historyDao().getRecent(500)

    fun addHistory(title: String, url: String) {
        viewModelScope.launch {
            db.historyDao().insert(HistoryEntity(title = title, url = url))
        }
    }

    fun deleteHistory(entry: HistoryEntity) {
        viewModelScope.launch { db.historyDao().delete(entry) }
    }

    fun clearHistory() {
        viewModelScope.launch { db.historyDao().clearAll() }
    }

    // ── Downloads ──────────────────────────────────────────────────────────
    val downloads: LiveData<List<DownloadEntity>> = db.downloadDao().getAll()

    fun addDownload(title: String, url: String, filePath: String,
                    mimeType: String = "", bytes: Long = 0) {
        viewModelScope.launch {
            db.downloadDao().insert(
                DownloadEntity(title = title, url = url, filePath = filePath,
                    mimeType = mimeType, fileSizeBytes = bytes)
            )
        }
    }

    fun deleteDownload(dl: DownloadEntity) {
        viewModelScope.launch { db.downloadDao().delete(dl) }
    }

    fun clearCompletedDownloads() {
        viewModelScope.launch { db.downloadDao().clearCompleted() }
    }

    // ── Saved Pages ────────────────────────────────────────────────────────
    val savedPages: LiveData<List<SavedPageEntity>> = db.savedPageDao().getAll()

    fun deleteSavedPage(page: SavedPageEntity) {
        viewModelScope.launch { db.savedPageDao().delete(page) }
    }
}
