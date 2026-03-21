/*
 * Copyright 2026 Vion Browser Contributors.
 * Based on Privacy Browser by Soren Stoutner (GPL v3).
 *
 * This file is part of Vion Browser (GPL v3).
 */

package com.vion.browser.ui.tabs

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.vion.browser.model.BrowserTab

/**
 * ViewModel that owns the list of open tabs and the currently active tab index.
 * Survives configuration changes. MainWebViewActivity observes it.
 */
class TabManagerViewModel : ViewModel() {

    private val _tabs = MutableLiveData<MutableList<BrowserTab>>(mutableListOf())
    val tabs: LiveData<MutableList<BrowserTab>> = _tabs

    private val _activeIndex = MutableLiveData(0)
    val activeIndex: LiveData<Int> = _activeIndex

    private var nextId = 1

    val activeTab: BrowserTab?
        get() = _tabs.value?.getOrNull(_activeIndex.value ?: 0)

    val tabCount: Int
        get() = _tabs.value?.size ?: 0

    /** Opens a new tab. Returns the index of the new tab. */
    fun openTab(url: String = "", switchTo: Boolean = true): Int {
        val list = _tabs.value ?: mutableListOf()
        val tab = BrowserTab(id = nextId++, url = url, title = if (url.isBlank()) "New tab" else url)
        list.add(tab)
        _tabs.value = list
        val index = list.size - 1
        if (switchTo) _activeIndex.value = index
        return index
    }

    /** Closes the tab at [index]. Adjusts active index accordingly. */
    fun closeTab(index: Int) {
        val list = _tabs.value ?: return
        if (list.size <= 1) {
            // Keep at least one tab — just reset it
            list[0] = BrowserTab(id = nextId++, title = "New tab", url = "")
            _tabs.value = list
            _activeIndex.value = 0
            return
        }
        list.removeAt(index)
        _tabs.value = list
        val current = _activeIndex.value ?: 0
        _activeIndex.value = when {
            index < current       -> current - 1
            current >= list.size  -> list.size - 1
            else                  -> current
        }
    }

    /** Closes all tabs except the one at [keepIndex]. */
    fun closeOtherTabs(keepIndex: Int) {
        val list = _tabs.value ?: return
        val kept = list.getOrNull(keepIndex) ?: return
        _tabs.value = mutableListOf(kept)
        _activeIndex.value = 0
    }

    /** Closes every tab and opens one blank tab. */
    fun closeAllTabs() {
        _tabs.value = mutableListOf(BrowserTab(id = nextId++, title = "New tab", url = ""))
        _activeIndex.value = 0
    }

    /** Duplicates the tab at [index]. */
    fun duplicateTab(index: Int) {
        val list = _tabs.value ?: return
        val original = list.getOrNull(index) ?: return
        val copy = original.copy(id = nextId++)
        list.add(index + 1, copy)
        _tabs.value = list
        _activeIndex.value = index + 1
    }

    /** Switches to the tab at [index]. */
    fun switchTo(index: Int) {
        val list = _tabs.value ?: return
        if (index in list.indices) _activeIndex.value = index
    }

    /** Moves a tab from [fromIndex] to [toIndex] (drag-to-reorder). */
    fun moveTab(fromIndex: Int, toIndex: Int) {
        val list = _tabs.value ?: return
        if (fromIndex !in list.indices || toIndex !in list.indices) return
        val tab = list.removeAt(fromIndex)
        list.add(toIndex, tab)
        _tabs.value = list
        // Keep active tab tracking correct after move
        val current = _activeIndex.value ?: 0
        _activeIndex.value = when (current) {
            fromIndex -> toIndex
            in (minOf(fromIndex, toIndex)..maxOf(fromIndex, toIndex)) ->
                if (fromIndex < toIndex) current - 1 else current + 1
            else -> current
        }
    }

    /** Updates url/title/loading state for the active tab. */
    fun updateActiveTab(
        url: String? = null,
        title: String? = null,
        isLoading: Boolean? = null,
        progress: Int? = null
    ) {
        val list = _tabs.value ?: return
        val idx = _activeIndex.value ?: return
        val tab = list.getOrNull(idx) ?: return
        url?.let { tab.url = it }
        title?.let { tab.title = it }
        isLoading?.let { tab.isLoading = it }
        progress?.let { tab.progress = it }
        _tabs.value = list
    }

    /** Ensures at least one tab exists on first launch. */
    fun ensureAtLeastOneTab(homepage: String) {
        if (_tabs.value.isNullOrEmpty()) {
            openTab(homepage)
        }
    }
}
