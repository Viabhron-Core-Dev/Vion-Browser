/*
 * Copyright 2026 Vion Browser Contributors. GPL v3.
 */
package com.vion.browser.extension

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A user-defined JavaScript snippet injected into matching pages.
 *
 * @param matchPattern  glob-style URL pattern (e.g. "https://example.com/*", "*://*/*")
 * @param runAt         "document_start" | "document_end" (default)
 * @param enabled       whether the script is active
 */
@Entity(tableName = "user_scripts")
data class UserScript(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String = "",
    val matchPattern: String = "*://*/*",
    val code: String,
    val runAt: String = "document_end",
    val enabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
