/*
 * Copyright 2026 Vion Browser Contributors.
 * Based on Privacy Browser by Soren Stoutner (GPL v3).
 */

package com.vion.browser.ui.about

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class AboutPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {

    override fun getItemCount() = TAB_TITLES.size

    override fun createFragment(position: Int): Fragment = AboutTabFragment.newInstance(position)

    companion object {
        val TAB_TITLES = arrayOf(
            "Version", "Permissions", "Privacy Policy",
            "Changelog", "License", "Contributors", "Links"
        )
    }
}
