/*
 * Copyright 2015-2016 Soren Stoutner <soren@stoutner.com>.
 * Copyright 2026 Vion Browser Contributors.
 *
 * This file is part of Vion Browser (GPL v3).
 */

package com.vion.browser.ui.about

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.vion.browser.R

class AboutTabFragment : Fragment() {

    companion object {
        private const val ARG_TAB = "tab_position"
        fun newInstance(position: Int) = AboutTabFragment().apply {
            arguments = Bundle().apply { putInt(ARG_TAB, position) }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val position = arguments?.getInt(ARG_TAB, 0) ?: 0
        return if (position == 0) {
            // Version tab — show device info as text
            inflater.inflate(R.layout.about_tab_version, container, false)
        } else {
            // All other tabs — WebView with HTML asset
            inflater.inflate(R.layout.about_tab_webview, container, false)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val position = arguments?.getInt(ARG_TAB, 0) ?: 0

        if (position == 0) {
            populateVersionTab(view)
        } else {
            val webView = view.findViewById<WebView>(R.id.about_webview)
            val asset = when (position) {
                1 -> "about_permissions.html"
                2 -> "about_privacy_policy.html"
                3 -> "about_changelog.html"
                4 -> "about_license.html"
                5 -> "about_contributors.html"
                6 -> "about_links.html"
                else -> "about_changelog.html"
            }
            webView?.loadUrl("file:///android_asset/$asset")
        }
    }

    private fun populateVersionTab(view: View) {
        val ctx = requireContext()
        fun tv(id: Int) = view.findViewById<TextView>(id)

        try {
            val pi = ctx.packageManager.getPackageInfo(ctx.packageName, 0)
            tv(R.id.version_name)?.text = "${getString(R.string.version)} ${pi.versionName}"
            tv(R.id.version_code_text)?.text = "${getString(R.string.version_code)} ${pi.longVersionCode}"
        } catch (_: Exception) {}

        tv(R.id.brand_text)?.text          = "${getString(R.string.brand)} ${Build.BRAND}"
        tv(R.id.manufacturer_text)?.text   = "${getString(R.string.manufacturer)} ${Build.MANUFACTURER}"
        tv(R.id.model_text)?.text          = "${getString(R.string.model)} ${Build.MODEL}"
        tv(R.id.device_text)?.text         = "${getString(R.string.device)} ${Build.DEVICE}"
        tv(R.id.bootloader_text)?.text     = "${getString(R.string.bootloader)} ${Build.BOOTLOADER}"
        tv(R.id.android_text)?.text        = "${getString(R.string.android_version)} ${Build.VERSION.RELEASE}"
        tv(R.id.api_text)?.text            = "${getString(R.string.api)} ${Build.VERSION.SDK_INT}"
        tv(R.id.build_text)?.text          = "${getString(R.string.build)} ${Build.DISPLAY}"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            tv(R.id.security_patch_text)?.text = "${getString(R.string.security_patch)} ${Build.VERSION.SECURITY_PATCH}"
        }
    }
}
