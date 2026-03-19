/*
 * Copyright 2026 Vion Browser Contributors. GPL v3.
 */
package com.vion.browser.extension

import android.content.Context
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import java.io.ByteArrayInputStream

/**
 * Lightweight ad/tracker blocking engine (Phase 11).
 *
 * Filter lists are bundled in assets/adblock/ and loaded once at startup.
 * Format: one domain or URL pattern per line (EasyList subset).
 * Lines starting with '!' are comments; '@@' lines are allowlist rules.
 *
 * The engine is intentionally simple: it blocks requests whose URL contains
 * any blocked domain/pattern.  Full ABP/uBO filter syntax is out of scope
 * for this phase but the architecture supports upgrading later.
 */
object AdBlockEngine {

    private val blockedPatterns = mutableListOf<String>()
    private val allowedPatterns = mutableListOf<String>()
    private var isLoaded = false

    /** Load bundled filter lists (call once from Application or Activity). */
    fun load(context: Context) {
        if (isLoaded) return
        try {
            val lists = context.assets.list("adblock") ?: return
            for (file in lists) {
                context.assets.open("adblock/$file").bufferedReader().forEachLine { line ->
                    val trimmed = line.trim()
                    when {
                        trimmed.isEmpty() || trimmed.startsWith('!') || trimmed.startsWith('#') -> { /* skip */ }
                        trimmed.startsWith("@@") -> allowedPatterns.add(trimmed.removePrefix("@@").trim())
                        else -> blockedPatterns.add(trimmed)
                    }
                }
            }
            isLoaded = true
        } catch (_: Exception) { /* assets/adblock/ may not exist in dev builds */ }
    }

    /** Returns a blocked empty response if the request should be blocked, null otherwise. */
    fun shouldBlock(request: WebResourceRequest): WebResourceResponse? {
        if (!isLoaded || blockedPatterns.isEmpty()) return null
        val url = request.url.toString()

        // Check allow-list first
        if (allowedPatterns.any { url.contains(it, ignoreCase = true) }) return null

        // Check block list
        if (blockedPatterns.any { url.contains(it, ignoreCase = true) }) {
            return WebResourceResponse(
                "text/plain", "utf-8", 403,
                "Blocked by Vion AdBlock",
                mapOf("Cache-Control" to "no-store"),
                ByteArrayInputStream(ByteArray(0))
            )
        }
        return null
    }

    fun isEnabled(context: Context): Boolean {
        val prefs = androidx.preference.PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getBoolean("ad_blocking_enabled", false)
    }
}
