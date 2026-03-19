/*
 * Copyright 2026 Vion Browser Contributors. GPL v3.
 */
package com.vion.browser.security

/**
 * DNS-over-HTTPS provider definitions (Phase 8).
 *
 * The actual DoH enforcement is done at the WebView network layer through
 * [androidx.webkit.WebViewCompat.setServiceWorkerClient] and — for Android 9+
 * — via [android.net.ssl.SSLCertificateSocketFactory] or the system private
 * DNS setting (which Vion can prompt the user to enable).
 *
 * For builds targeting API ≥ 26 we also expose the "Private DNS" deep-link
 * so the user can configure it system-wide.
 */
enum class DohProvider(val label: String, val url: String) {
    CLOUDFLARE(
        label = "Cloudflare (1.1.1.1)",
        url = "https://cloudflare-dns.com/dns-query"
    ),
    GOOGLE(
        label = "Google (8.8.8.8)",
        url = "https://dns.google/dns-query"
    ),
    QUAD9(
        label = "Quad9 (9.9.9.9)",
        url = "https://dns.quad9.net/dns-query"
    ),
    ADGUARD(
        label = "AdGuard DNS",
        url = "https://dns.adguard-dns.com/dns-query"
    ),
    MULLVAD(
        label = "Mullvad DNS",
        url = "https://doh.mullvad.net/dns-query"
    ),
    SYSTEM(
        label = "System default",
        url = ""
    );

    companion object {
        fun fromUrl(url: String): DohProvider =
            values().firstOrNull { it.url == url } ?: SYSTEM

        /** Smart rotation: picks providers that offer filtering (adblock). */
        val PRIVACY_PRESET = listOf(ADGUARD, MULLVAD, QUAD9)
    }
}
