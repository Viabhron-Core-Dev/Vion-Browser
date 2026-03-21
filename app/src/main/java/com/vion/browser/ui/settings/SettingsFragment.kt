/*
 * Copyright 2016 Soren Stoutner <soren@stoutner.com>.
 * Copyright 2026 Vion Browser Contributors.
 *
 * This file is part of Vion Browser (GPL v3).
 */

package com.vion.browser.ui.settings

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.vion.browser.R

/**
 * PreferenceFragmentCompat — loads preferences.xml and handles live changes
 * via the SharedPreferences listener registered in MainWebViewActivity.
 */
class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
    }
}
