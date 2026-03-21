/*
 * Copyright 2026 Vion Browser Contributors.
 * Based on Privacy Browser by Soren Stoutner (GPL v3).
 *
 * This file is part of Vion Browser (GPL v3).
 */

package com.vion.browser.util

import android.util.Patterns
import java.net.URLEncoder

/**
 * URL helpers — validates, formats, and builds search URLs.
 */
object UrlUtils {

    fun isUrl(input: String): Boolean {
        val trimmed = input.trim()
        return Patterns.WEB_URL.matcher(trimmed).matches() ||
               trimmed.startsWith("http://") ||
               trimmed.startsWith("https://") ||
               trimmed.startsWith("file://") ||
               trimmed.startsWith("about:")
    }

    /**
     * Given raw text from the URL bar, return either:
     *  - the URL as-is (if it looks like a URL)
     *  - a search query URL using the supplied base search URL
     */
    fun formatInput(input: String, searchUrl: String): String {
        val trimmed = input.trim()
        if (trimmed.isBlank()) return ""
        return if (isUrl(trimmed)) {
            // Prepend https if no scheme
            if (!trimmed.contains("://")) "https://$trimmed" else trimmed
        } else {
            searchUrl + URLEncoder.encode(trimmed, "UTF-8")
        }
    }
}
