/*
 * Copyright 2026 Vion Browser Contributors.
 * Based on Privacy Browser by Soren Stoutner (GPL v3).
 *
 * This file is part of Vion Browser (GPL v3).
 */

package com.vion.browser.ui.menu

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.viewpager2.widget.ViewPager2
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
import com.vion.browser.R
import com.vion.browser.model.FloatingMenuItem

/**
 * Self-contained floating menu widget.
 * Inflates layout_floating_menu.xml which contains a ViewPager2 for pages
 * and a dot indicator row at the bottom.
 *
 * Call [show] / [hide] to toggle visibility.
 * Call [setItems] to supply the menu item list (sorted by page then position).
 */
class FloatingMenuView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {

    private val viewPager: ViewPager2
    private val pageIndicator: TabLayout
    private val pagerAdapter: FloatingMenuPagerAdapter

    var onItemClick: ((FloatingMenuItem) -> Unit)? = null
    var onItemLongClick: ((FloatingMenuItem, View) -> Unit)? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.layout_floating_menu, this, true)
        viewPager = findViewById(R.id.floating_menu_pager)
        pageIndicator = findViewById(R.id.floating_menu_indicator)

        pagerAdapter = FloatingMenuPagerAdapter(
            onItemClick = { item -> onItemClick?.invoke(item) },
            onItemLongClick = { item, v -> onItemLongClick?.invoke(item, v) }
        )
        viewPager.adapter = pagerAdapter

        // Connect dots to ViewPager2
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                pageIndicator.getTabAt(position)?.select()
            }
        })
        pageIndicator.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) { viewPager.currentItem = tab.position }
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        visibility = View.GONE
    }

    fun setItems(allItems: List<FloatingMenuItem>) {
        // Group into pages
        val grouped = allItems.groupBy { it.page }
        val pageCount = (grouped.keys.maxOrNull() ?: 0) + 1
        val pages = (0 until pageCount).map { p ->
            (grouped[p] ?: emptyList()).sortedBy { it.position }
        }
        pagerAdapter.setPages(pages)

        // Rebuild dot indicators
        pageIndicator.removeAllTabs()
        repeat(pageCount) { pageIndicator.addTab(pageIndicator.newTab()) }
        pageIndicator.visibility = if (pageCount > 1) View.VISIBLE else View.GONE
    }

    fun show() { visibility = View.VISIBLE }
    fun hide() { visibility = View.GONE }
    fun toggle() { if (visibility == View.VISIBLE) hide() else show() }
    val isShowing get() = visibility == View.VISIBLE
}
