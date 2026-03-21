/*
 * Copyright 2026 Vion Browser Contributors. GPL v3.
 */
package com.vion.browser.security

import android.net.Uri
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import com.vion.browser.extension.AdBlockEngine
import com.vion.browser.extension.UserScriptEngine

/**
 * Privacy-hardened WebViewClient (Phases 7, 8, 11).
 *
 * Features:
 *  1. Strip tracking parameters (utm_*, fbclid, gclid, etc.)
 *  2. Ad-block via AdBlockEngine (Phase 11)
 *  3. User-script injection via UserScriptEngine (Phase 11)
 *  4. Page Visibility spoofing JS snippets (injected by activity after load)
 *  5. Canvas fingerprint noise JS snippets (injected by activity after load)
 */
open class PrivacyWebViewClient : WebViewClient() {

    // Whether ad-blocking is active — toggled by MainWebViewActivity
    var adBlockEnabled: Boolean = false

    // ── Tracking parameter names to strip from URLs ────────────────────────
    private val TRACKING_PARAMS = setOf(
        "utm_source", "utm_medium", "utm_campaign", "utm_term", "utm_content",
        "fbclid", "gclid", "gclsrc", "dclid", "msclkid",
        "_ga", "_gl", "mc_cid", "mc_eid",
        "ref", "referrer", "source", "affiliate_id"
    )

    /**
     * Strip tracking parameters before loading the URL.
     */
    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
        val originalUri = request.url
        val cleanUri = stripTrackingParams(originalUri)

        if (cleanUri != originalUri) {
            view.loadUrl(cleanUri.toString())
            return true
        }
        // Let the WebView handle it normally (stays inside the WebView)
        return false
    }

    /**
     * Intercept sub-resource requests for ad-blocking (Phase 11).
     * Main-frame navigations are not blocked here.
     */
    override fun shouldInterceptRequest(
        view: WebView,
        request: WebResourceRequest
    ): WebResourceResponse? {
        // Only block sub-resources, not main-frame navigation
        if (adBlockEnabled && !request.isForMainFrame) {
            val blocked = AdBlockEngine.shouldBlock(request)
            if (blocked != null) return blocked
        }
        return null
    }

    /**
     * Inject user scripts that run at document_end after page load.
     * Call from onPageFinished in the host activity.
     */
    fun injectDocumentEndScripts(view: WebView, url: String) {
        UserScriptEngine.injectAtDocumentEnd(view, url)
    }

    // ── Helpers ─────────────────────────────────────────────────────────────

    private fun stripTrackingParams(uri: Uri): Uri {
        val query = uri.queryParameterNames
        if (query.none { it in TRACKING_PARAMS }) return uri

        val builder = uri.buildUpon().clearQuery()
        for (param in query) {
            if (param !in TRACKING_PARAMS) {
                uri.getQueryParameters(param).forEach { value ->
                    builder.appendQueryParameter(param, value)
                }
            }
        }
        return builder.build()
    }

    // ── JS snippets (injected by the activity via evaluateJavascript) ────────

    /** Page Visibility API spoof — always reports document as visible. */
    val pageVisibilitySpoofJs: String = """
        (function() {
            try {
                Object.defineProperty(document, 'hidden', { get: () => false });
                Object.defineProperty(document, 'visibilityState', { get: () => 'visible' });
                document.addEventListener = (function(origAddEventListener) {
                    return function(type, listener, options) {
                        if (type === 'visibilitychange' || type === 'pagehide') return;
                        return origAddEventListener.call(this, type, listener, options);
                    };
                })(document.addEventListener);
            } catch(e) {}
        })();
    """.trimIndent()

    /** Canvas fingerprint noise + hardware normalisation. */
    val fingerprintResistanceJs: String = """
        (function() {
            try {
                const origToDataURL = HTMLCanvasElement.prototype.toDataURL;
                HTMLCanvasElement.prototype.toDataURL = function(type) {
                    const ctx = this.getContext('2d');
                    if (ctx) {
                        const imgData = ctx.getImageData(0, 0, this.width, this.height);
                        const data = imgData.data;
                        for (let i = 0; i < data.length; i += 4) {
                            data[i]   = data[i]   ^ (Math.random() * 2 | 0);
                            data[i+1] = data[i+1] ^ (Math.random() * 2 | 0);
                        }
                        ctx.putImageData(imgData, 0, 0);
                    }
                    return origToDataURL.apply(this, arguments);
                };
                Object.defineProperty(navigator, 'hardwareConcurrency', { get: () => 2 });
                if ('deviceMemory' in navigator)
                    Object.defineProperty(navigator, 'deviceMemory', { get: () => 2 });
            } catch(e) {}
        })();
    """.trimIndent()
}
