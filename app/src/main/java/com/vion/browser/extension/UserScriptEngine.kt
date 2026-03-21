/*
 * Copyright 2026 Vion Browser Contributors. GPL v3.
 */
package com.vion.browser.extension

import android.content.Context
import android.webkit.WebView
import com.vion.browser.data.db.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Manages userscript injection into the WebView.
 *
 * Called from PrivacyWebViewClient:
 *   - injectAtDocumentEnd(view, url)  → inject "document_end" scripts
 *
 * Called from MainWebViewActivity:
 *   - reload(context)                 → refresh the cached script list from DB
 */
object UserScriptEngine {

    @Volatile private var cachedScripts: List<UserScript> = emptyList()
    private val engineScope = CoroutineScope(Dispatchers.IO)

    /** Called once on startup and after script edits. */
    fun reload(context: Context) {
        engineScope.launch {
            val db = AppDatabase.getInstance(context)
            cachedScripts = db.userScriptDao().getEnabled()
        }
    }

    /** Invalidate in-memory cache (forces a reload on next injection). */
    fun invalidateCache() { cachedScripts = emptyList() }

    /**
     * Inject all enabled scripts whose [runAt] == "document_end" and
     * whose [matchPattern] matches [url]. Runs on the main thread via [WebView.post].
     */
    fun injectAtDocumentEnd(view: WebView, url: String) {
        inject(view, url, "document_end")
    }

    /**
     * Inject all enabled scripts whose [runAt] == "document_start" and
     * whose [matchPattern] matches [url].
     */
    fun injectAtDocumentStart(view: WebView, url: String) {
        inject(view, url, "document_start")
    }

    private fun inject(webView: WebView, url: String, runAt: String) {
        val scripts = cachedScripts
        if (scripts.isEmpty()) return
        val matching = scripts.filter {
            it.runAt == runAt && urlMatchesPattern(url, it.matchPattern)
        }
        for (script in matching) {
            webView.post {
                webView.evaluateJavascript("(function() { ${script.code} })();", null)
            }
        }
    }

    /**
     * Simple glob matcher: supports * and ? wildcards in URL patterns.
     * Follows WebExtension match_pattern semantics for common cases.
     */
    fun urlMatchesPattern(url: String, pattern: String): Boolean {
        if (pattern == "*://*/*" || pattern == "<all_urls>") return true
        val regex = pattern
            .replace(".", "\\.")
            .replace("?", ".")
            .replace("*", ".*")
        return Regex("^$regex$").containsMatchIn(url)
    }
}
