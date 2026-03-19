/*
 * Copyright 2026 Vion Browser Contributors. GPL v3.
 */
package com.vion.browser.ui.scripts

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.vion.browser.data.db.AppDatabase
import com.vion.browser.extension.UserScript
import com.vion.browser.extension.UserScriptEngine
import kotlinx.coroutines.launch

class UserScriptViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getInstance(application).userScriptDao()

    val scripts: LiveData<List<UserScript>> = dao.getAll()

    fun insert(script: UserScript) = viewModelScope.launch {
        dao.insert(script)
        UserScriptEngine.invalidateCache()
    }

    fun update(script: UserScript) = viewModelScope.launch {
        dao.update(script)
        UserScriptEngine.invalidateCache()
    }

    fun delete(script: UserScript) = viewModelScope.launch {
        dao.delete(script)
        UserScriptEngine.invalidateCache()
    }

    fun toggle(script: UserScript) = update(script.copy(enabled = !script.enabled))
}
