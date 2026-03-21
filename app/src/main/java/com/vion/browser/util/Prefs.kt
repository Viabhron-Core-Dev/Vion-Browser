/*
 * Copyright 2026 Vion Browser Contributors.
 * Based on Privacy Browser by Soren Stoutner (GPL v3).
 *
 * This file is part of Vion Browser (GPL v3).
 */

package com.vion.browser.util

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager

/**
 * Centralised preference keys and helpers.
 * All preference reads/writes go through here.
 */
object Prefs {

    // Keys
    const val KEY_JAVASCRIPT_ENABLED = "javascript_enabled"
    const val KEY_FIRST_PARTY_COOKIES = "first_party_cookies_enabled"
    const val KEY_THIRD_PARTY_COOKIES = "third_party_cookies_enabled"
    const val KEY_DOM_STORAGE = "dom_storage_enabled"
    const val KEY_USER_AGENT = "user_agent"
    const val KEY_CUSTOM_USER_AGENT = "custom_user_agent"
    const val KEY_JS_DISABLED_SEARCH = "javascript_disabled_search"
    const val KEY_JS_DISABLED_SEARCH_CUSTOM = "javascript_disabled_search_custom_url"
    const val KEY_JS_ENABLED_SEARCH = "javascript_enabled_search"
    const val KEY_JS_ENABLED_SEARCH_CUSTOM = "javascript_enabled_search_custom_url"
    const val KEY_HOMEPAGE = "homepage"
    const val KEY_SWIPE_TO_REFRESH = "swipe_to_refresh_enabled"

    // Bottom toolbar long-press
    const val KEY_BACK_LONG = "back_button_long_press"
    const val KEY_FORWARD_LONG = "forward_button_long_press"
    const val KEY_HOME_LONG = "home_button_long_press"
    const val KEY_TABS_LONG = "tabs_button_long_press"
    const val KEY_MENU_LONG = "menu_button_long_press"

    // Defaults
    const val DEFAULT_HOMEPAGE = "https://duckduckgo.com"
    const val DEFAULT_SEARCH_JS_OFF = "https://duckduckgo.com/html/?q="
    const val DEFAULT_SEARCH_JS_ON = "https://duckduckgo.com/?q="
    const val DEFAULT_USER_AGENT = "Default user agent"
    const val DEFAULT_CUSTOM_UA = "VionBrowser/1.0"
    const val CUSTOM_URL_SENTINEL = "Custom URL"
    const val DEFAULT_USER_AGENT_SENTINEL = "Default user agent"
    const val CUSTOM_USER_AGENT_SENTINEL = "Custom user agent"

    fun get(context: Context): SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(context)

    fun isJavaScriptEnabled(context: Context) =
        get(context).getBoolean(KEY_JAVASCRIPT_ENABLED, false)

    fun isFirstPartyCookiesEnabled(context: Context) =
        get(context).getBoolean(KEY_FIRST_PARTY_COOKIES, false)

    fun isThirdPartyCookiesEnabled(context: Context) =
        get(context).getBoolean(KEY_THIRD_PARTY_COOKIES, false)

    fun isDomStorageEnabled(context: Context) =
        get(context).getBoolean(KEY_DOM_STORAGE, false)

    fun isSwipeToRefresh(context: Context) =
        get(context).getBoolean(KEY_SWIPE_TO_REFRESH, true)

    fun homepage(context: Context) =
        get(context).getString(KEY_HOMEPAGE, DEFAULT_HOMEPAGE) ?: DEFAULT_HOMEPAGE

    fun resolveSearchUrl(context: Context, jsEnabled: Boolean): String {
        val key = if (jsEnabled) KEY_JS_ENABLED_SEARCH else KEY_JS_DISABLED_SEARCH
        val defaultVal = if (jsEnabled) DEFAULT_SEARCH_JS_ON else DEFAULT_SEARCH_JS_OFF
        val customKey = if (jsEnabled) KEY_JS_ENABLED_SEARCH_CUSTOM else KEY_JS_DISABLED_SEARCH_CUSTOM
        val stored = get(context).getString(key, defaultVal) ?: defaultVal
        return if (stored == CUSTOM_URL_SENTINEL)
            get(context).getString(customKey, "") ?: ""
        else stored
    }

    fun resolveUserAgent(context: Context): String? {
        val prefs = get(context)
        return when (val ua = prefs.getString(KEY_USER_AGENT, DEFAULT_USER_AGENT_SENTINEL)) {
            DEFAULT_USER_AGENT_SENTINEL -> null // use WebView default
            CUSTOM_USER_AGENT_SENTINEL  -> prefs.getString(KEY_CUSTOM_USER_AGENT, DEFAULT_CUSTOM_UA)
            else                        -> ua
        }
    }

    fun longPressAction(context: Context, key: String): String =
        get(context).getString(key, "none") ?: "none"
}
