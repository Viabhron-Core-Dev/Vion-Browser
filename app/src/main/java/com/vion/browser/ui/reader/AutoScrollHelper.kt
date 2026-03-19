/*
 * Copyright 2026 Vion Browser Contributors. GPL v3.
 */
package com.vion.browser.ui.reader

import android.webkit.WebView

/**
 * Auto-scroll helper (Phase 9).
 *
 * Scrolls the page at a configurable speed (pixels per second).
 * Implemented entirely in JS; no Android animation needed.
 */
object AutoScrollHelper {

    var isActive = false
        private set

    /** Speed in px/s — user-configurable; default 60 px/s */
    var speedPxPerSec = 60

    fun toggle(webView: WebView): Boolean {
        isActive = !isActive
        if (isActive) start(webView) else stop(webView)
        return isActive
    }

    private fun start(webView: WebView) {
        val js = """
            (function() {
                if (window._vionAutoScroll) return;
                var speed = $speedPxPerSec;
                var last = null;
                function step(ts) {
                    if (!window._vionAutoScrollActive) return;
                    if (last !== null) {
                        var delta = (ts - last) / 1000 * speed;
                        window.scrollBy(0, delta);
                        if (window.scrollY + window.innerHeight >= document.body.scrollHeight) {
                            window._vionAutoScrollActive = false;
                            return;
                        }
                    }
                    last = ts;
                    window._vionAutoScroll = requestAnimationFrame(step);
                }
                window._vionAutoScrollActive = true;
                window._vionAutoScroll = requestAnimationFrame(step);
            })();
        """.trimIndent()
        webView.evaluateJavascript(js, null)
    }

    private fun stop(webView: WebView) {
        val js = """
            (function() {
                window._vionAutoScrollActive = false;
                if (window._vionAutoScroll) {
                    cancelAnimationFrame(window._vionAutoScroll);
                    window._vionAutoScroll = null;
                }
            })();
        """.trimIndent()
        webView.evaluateJavascript(js, null)
    }
}
