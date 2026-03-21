/*
 * Copyright 2026 Vion Browser Contributors.
 * Based on Privacy Browser by Soren Stoutner (GPL v3).
 *
 * This file is part of Vion Browser (GPL v3).
 */

package com.vion.browser.model

/**
 * Represents a single browser tab.
 */
data class BrowserTab(
    val id: Int,
    var title: String = "New tab",
    var url: String = "",
    var favicon: android.graphics.Bitmap? = null,
    var isLoading: Boolean = false,
    var progress: Int = 0
)
