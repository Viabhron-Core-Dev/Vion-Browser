/*
 * Copyright 2026 Vion Browser Contributors. GPL v3.
 */
package com.vion.browser.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.vion.browser.data.db.AppDatabase
import com.vion.browser.data.db.ShortcutEntity
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val shortcutDao = db.shortcutDao()

    val shortcuts: LiveData<List<ShortcutEntity>> = shortcutDao.getAll()

    fun addShortcut(label: String, url: String) {
        viewModelScope.launch {
            val pos = shortcutDao.count()
            shortcutDao.insert(ShortcutEntity(label = label, url = url, position = pos))
        }
    }

    fun deleteShortcut(shortcut: ShortcutEntity) {
        viewModelScope.launch { shortcutDao.delete(shortcut) }
    }

    fun updateShortcut(shortcut: ShortcutEntity) {
        viewModelScope.launch { shortcutDao.update(shortcut) }
    }
}
