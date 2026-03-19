/*
 * Copyright 2026 Vion Browser Contributors.
 * Based on Privacy Browser by Soren Stoutner (GPL v3).
 *
 * This file is part of Vion Browser (GPL v3).
 */

package com.vion.browser.model

/**
 * Items that appear in the floating 2x5 menu grid.
 */
data class FloatingMenuItem(
    val id: String,
    val labelRes: Int,
    val iconRes: Int,
    var isEnabled: Boolean = true,
    var page: Int = 0,       // which grid page (0-based)
    var position: Int = 0    // slot within page (0-9)
)
