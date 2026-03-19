/*
 * Copyright 2026 Vion Browser Contributors. GPL v3.
 */
package com.vion.browser.ui.reader

import android.webkit.WebView

/**
 * Reader Mode (Phase 9).
 *
 * Injects a lightweight Readability-style extractor written in pure JS
 * (no external network call, fully on-device).  After extraction the
 * WebView's DOM is replaced with a clean reader view rendered with the
 * user's preferred font size and background.
 *
 * Usage:
 *   ReaderModeHelper.toggle(webView) { isActive -> … }
 */
object ReaderModeHelper {

    var isActive = false
        private set

    /** Toggle reader mode on the given WebView. */
    fun toggle(webView: WebView, onResult: (active: Boolean) -> Unit) {
        if (isActive) {
            exit(webView); onResult(false)
        } else {
            enter(webView) { success -> if (success) onResult(true) else onResult(false) }
        }
    }

    // ── Enter reader mode ─────────────────────────────────────────────────

    private fun enter(webView: WebView, callback: (Boolean) -> Unit) {
        val js = buildExtractorJs()
        webView.evaluateJavascript(js) { result ->
            if (result != null && result != "null" && result.length > 10) {
                isActive = true
                renderReaderView(webView, result)
                callback(true)
            } else {
                callback(false)
            }
        }
    }

    private fun renderReaderView(webView: WebView, jsonEncoded: String) {
        val renderJs = """
            (function() {
                try {
                    var data = JSON.parse($jsonEncoded);
                    var title   = data.title   || document.title || '';
                    var byline  = data.byline  || '';
                    var content = data.content || '<p>Could not extract article content.</p>';

                    var html = '<!DOCTYPE html><html><head>'
                        + '<meta charset="UTF-8">'
                        + '<meta name="viewport" content="width=device-width,initial-scale=1">'
                        + '<style>'
                        + 'body{margin:0;padding:20px;max-width:700px;margin-left:auto;margin-right:auto;'
                        + 'background:#1A1A2E;color:#E8E8FF;font-family:Georgia,serif;font-size:18px;line-height:1.8;}'
                        + 'h1{color:#9D97FF;font-size:1.6em;margin-bottom:8px;}'
                        + '.byline{color:#707088;font-size:0.85em;margin-bottom:24px;}'
                        + 'img{max-width:100%;height:auto;border-radius:4px;}'
                        + 'a{color:#6C63FF;}pre,code{background:#2A2A3E;padding:4px 8px;border-radius:4px;}'
                        + 'blockquote{border-left:3px solid #6C63FF;margin-left:0;padding-left:16px;color:#B0B0C8;}'
                        + '#vion-reader-exit{position:fixed;top:12px;right:12px;background:#6C63FF;color:#fff;'
                        + 'border:none;border-radius:20px;padding:8px 16px;font-size:14px;cursor:pointer;z-index:9999;}'
                        + '</style></head><body>'
                        + '<button id="vion-reader-exit" onclick="window.VionReader.exit()">✕ Exit reader</button>'
                        + '<h1>' + title + '</h1>'
                        + (byline ? '<div class="byline">' + byline + '</div>' : '')
                        + content
                        + '</body></html>';

                    document.open();
                    document.write(html);
                    document.close();
                } catch(e) {}
            })();
        """.trimIndent()
        webView.evaluateJavascript(renderJs, null)
    }

    // ── Exit reader mode ─────────────────────────────────────────────────

    fun exit(webView: WebView) {
        isActive = false
        webView.reload()
    }

    // ── Article extractor (pure JS, no network) ───────────────────────────

    /**
     * Minimal Readability-inspired extractor.
     * Returns a JSON string with {title, byline, content} or null.
     */
    private fun buildExtractorJs(): String = """
        (function() {
            try {
                function scoreNode(node) {
                    var tag = node.tagName ? node.tagName.toLowerCase() : '';
                    var positiveRe = /article|post|content|entry|main|story|body|text/i;
                    var negativeRe = /nav|sidebar|ad|comment|footer|header|widget|menu/i;
                    var score = 0;
                    if (positiveRe.test(node.className + ' ' + node.id)) score += 25;
                    if (negativeRe.test(node.className + ' ' + node.id)) score -= 25;
                    if (tag === 'article') score += 30;
                    if (tag === 'section')  score += 10;
                    if (tag === 'div')      score +=  5;
                    var pCount = node.querySelectorAll ? node.querySelectorAll('p').length : 0;
                    score += pCount * 3;
                    var textLen = (node.innerText || node.textContent || '').trim().length;
                    score += Math.min(textLen / 200, 30);
                    return score;
                }

                var candidates = document.querySelectorAll('article, [role="main"], main, .post, .entry, .content, .article, div');
                var best = null, bestScore = 0;
                for (var i = 0; i < candidates.length; i++) {
                    var s = scoreNode(candidates[i]);
                    if (s > bestScore) { bestScore = s; best = candidates[i]; }
                }

                if (!best || bestScore < 20) return null;

                // Clean the content
                var clone = best.cloneNode(true);
                ['script','style','noscript','iframe','form','nav','aside','footer','header',
                 '.ad','.ads','.advertisement','.sidebar','.comment','.social'].forEach(function(sel) {
                    try { clone.querySelectorAll(sel).forEach(function(el){ el.remove(); }); } catch(e){}
                });

                var title = document.title || '';
                var h1 = document.querySelector('h1');
                if (h1) title = h1.innerText || h1.textContent || title;

                var byline = '';
                var authorEl = document.querySelector('[rel="author"], .author, .byline, [itemprop="author"]');
                if (authorEl) byline = authorEl.innerText || authorEl.textContent || '';

                return JSON.stringify({
                    title:   title.trim(),
                    byline:  byline.trim(),
                    content: clone.innerHTML
                });
            } catch(e) { return null; }
        })();
    """.trimIndent()
}
