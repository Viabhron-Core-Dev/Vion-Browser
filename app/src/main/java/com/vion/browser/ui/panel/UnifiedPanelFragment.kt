/*
 * Copyright 2026 Vion Browser Contributors. GPL v3.
 */
package com.vion.browser.ui.panel

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.tabs.TabLayoutMediator
import com.vion.browser.R
import com.vion.browser.databinding.FragmentUnifiedPanelBinding

/**
 * Unified side-panel / bottom-sheet that aggregates four tabs:
 *   [Bookmarks] [History] [Saved Pages] [Downloads]
 *
 * Shown as a full-screen bottom sheet from MainWebViewActivity
 * when the user taps the bookmarks or history menu items.
 */
class UnifiedPanelFragment : Fragment() {

    private var _binding: FragmentUnifiedPanelBinding? = null
    private val binding get() = _binding!!

    /** Callback to navigate to a URL in the main browser. */
    var onNavigate: ((String) -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUnifiedPanelBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val pagerAdapter = PanelPagerAdapter(this, onNavigate)
        binding.panelViewPager.adapter = pagerAdapter

        TabLayoutMediator(binding.panelTabLayout, binding.panelViewPager) { tab, position ->
            tab.text = when (position) {
                0 -> getString(R.string.panel_bookmarks)
                1 -> getString(R.string.panel_history)
                2 -> getString(R.string.panel_saved)
                3 -> getString(R.string.panel_downloads)
                else -> ""
            }
        }.attach()

        // Jump to initial tab if requested
        val initialTab = arguments?.getInt(ARG_INITIAL_TAB, 0) ?: 0
        if (initialTab in 0..3) binding.panelViewPager.setCurrentItem(initialTab, false)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val ARG_INITIAL_TAB = "initial_tab"
        const val TAB_BOOKMARKS = 0
        const val TAB_HISTORY   = 1
        const val TAB_SAVED     = 2
        const val TAB_DOWNLOADS = 3

        fun newInstance(initialTab: Int = 0, onNavigate: ((String) -> Unit)? = null) =
            UnifiedPanelFragment().apply {
                this.onNavigate = onNavigate
                arguments = Bundle().also { it.putInt(ARG_INITIAL_TAB, initialTab) }
            }
    }
}
