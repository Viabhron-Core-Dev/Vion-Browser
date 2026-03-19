/*
 * Copyright 2026 Vion Browser Contributors. GPL v3.
 */
package com.vion.browser.security

import android.content.Context
import androidx.preference.PreferenceManager

/**
 * Preference keys and getters for Phase 8 security features.
 */
object SecurityPrefs {

    // Keys
    const val KEY_FINGERPRINT_RESISTANCE = "fingerprint_resistance"
    const val KEY_PAGE_VISIBILITY_SPOOF  = "page_visibility_spoof"
    const val KEY_STRIP_TRACKING_PARAMS  = "strip_tracking_params"
    const val KEY_HTTPS_ONLY             = "https_only_mode"
    const val KEY_WEBRTC_MODE            = "webrtc_mode"
    const val KEY_DOH_ENABLED            = "doh_enabled"
    const val KEY_DOH_PROVIDER           = "doh_provider"

    // WebRTC modes
    const val WEBRTC_DISABLE       = "disable"
    const val WEBRTC_BLOCK_NON_PROXIED = "block_non_proxied"
    const val WEBRTC_ALLOW         = "allow"

    fun get(context: Context) = PreferenceManager.getDefaultSharedPreferences(context)

    fun isFingerprintResistanceEnabled(context: Context) =
        get(context).getBoolean(KEY_FINGERPRINT_RESISTANCE, true)

    fun isPageVisibilitySpoofEnabled(context: Context) =
        get(context).getBoolean(KEY_PAGE_VISIBILITY_SPOOF, true)

    fun isStripTrackingParamsEnabled(context: Context) =
        get(context).getBoolean(KEY_STRIP_TRACKING_PARAMS, true)

    fun isHttpsOnly(context: Context) =
        get(context).getBoolean(KEY_HTTPS_ONLY, false)

    fun webRtcMode(context: Context) =
        get(context).getString(KEY_WEBRTC_MODE, WEBRTC_DISABLE) ?: WEBRTC_DISABLE

    fun isDohEnabled(context: Context) =
        get(context).getBoolean(KEY_DOH_ENABLED, false)

    fun dohProviderUrl(context: Context): String {
        val stored = get(context).getString(KEY_DOH_PROVIDER, DohProvider.CLOUDFLARE.url)
            ?: DohProvider.CLOUDFLARE.url
        return stored
    }
}
