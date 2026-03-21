/*
 * Copyright 2026 Vion Browser Contributors. GPL v3.
 */
package com.vion.browser.ui.panel

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class PanelPagerAdapter(
    fragment: Fragment,
    val onNavigate: ((String) -> Unit)?
) : FragmentStateAdapter(fragment) {

    override fun getItemCount() = 4

    override fun createFragment(position: Int): Fragment = when (position) {
        0 -> BookmarksTabFragment.newInstance(onNavigate)
        1 -> HistoryTabFragment.newInstance(onNavigate)
        2 -> SavedPagesTabFragment.newInstance()
        3 -> DownloadsTabFragment.newInstance()
        else -> BookmarksTabFragment.newInstance(onNavigate)
    }
}
