/*
 * Copyright 2026 Vion Browser Contributors.
 * Based on Privacy Browser by Soren Stoutner (GPL v3).
 *
 * This file is part of Vion Browser (GPL v3).
 */

package com.vion.browser.ui.menu

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vion.browser.R
import com.vion.browser.model.FloatingMenuItem

/**
 * Pager adapter — each page is a 2×5 RecyclerView grid of FloatingMenuItems.
 */
class FloatingMenuPagerAdapter(
    private val onItemClick: (FloatingMenuItem) -> Unit,
    private val onItemLongClick: (FloatingMenuItem, View) -> Unit
) : RecyclerView.Adapter<FloatingMenuPagerAdapter.PageVH>() {

    private val pages = mutableListOf<List<FloatingMenuItem>>()

    fun setPages(newPages: List<List<FloatingMenuItem>>) {
        pages.clear()
        pages.addAll(newPages)
        notifyDataSetChanged()
    }

    inner class PageVH(val recyclerView: RecyclerView) : RecyclerView.ViewHolder(recyclerView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PageVH {
        val rv = RecyclerView(parent.context).apply {
            layoutManager = GridLayoutManager(context, 2)
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
        return PageVH(rv)
    }

    override fun getItemCount() = pages.size

    override fun onBindViewHolder(holder: PageVH, position: Int) {
        holder.recyclerView.adapter = FloatingMenuPageAdapter(
            items = pages[position],
            onItemClick = onItemClick,
            onItemLongClick = onItemLongClick
        )
    }
}
