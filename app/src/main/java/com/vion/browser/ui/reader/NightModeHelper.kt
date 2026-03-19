/*
 * Copyright 2026 Vion Browser Contributors. GPL v3.
 */
package com.vion.browser.ui.reader

import android.webkit.WebView
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewFeature

/**
 * Night / Dark mode helper (Phase 9).
 *
 * Prefers the native AndroidX WebKit dark-mode API (API 29+).
 * Falls back to a CSS inversion filter injected via JS for older devices.
 */
object NightModeHelper {

    var isActive = false
        private set

    fun toggle(webView: WebView): Boolean {
        isActive = !isActive
        apply(webView, isActive)
        return isActive
    }

    fun apply(webView: WebView, enabled: Boolean) {
        isActive = enabled
        if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
            @Suppress("DEPRECATION")
            WebSettingsCompat.setForceDark(
                webView.settings,
                if (enabled) WebSettingsCompat.FORCE_DARK_ON
                else         WebSettingsCompat.FORCE_DARK_OFF
            )
        } else {
            // JS fallback: CSS invert filter
            val js = if (enabled) """
                (function() {
                    if (!document.getElementById('_vion_night')) {
                        var s = document.createElement('style');
                        s.id = '_vion_night';
                        s.textContent = 'html{filter:invert(1) hue-rotate(180deg)} img,video,canvas{filter:invert(1) hue-rotate(180deg)}';
                        document.head.appendChild(s);
                    }
                })();
            """.trimIndent() else """
                (function() {
                    var s = document.getElementById('_vion_night');
                    if (s) s.remove();
                })();
            """.trimIndent()
            webView.evaluateJavascript(js, null)
        }
    }
}
