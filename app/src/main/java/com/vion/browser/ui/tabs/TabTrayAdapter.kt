/*
 * Copyright 2026 Vion Browser Contributors.
 * Based on Privacy Browser by Soren Stoutner (GPL v3).
 *
 * This file is part of Vion Browser (GPL v3).
 */

package com.vion.browser.ui.tabs

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.vion.browser.R
import com.vion.browser.model.BrowserTab

/**
 * RecyclerView adapter for the tab tray.
 * Supports tap-to-switch, long-press context menu, and drag-to-reorder (via ItemTouchHelper outside).
 */
class TabTrayAdapter(
    private val onTabClick: (Int) -> Unit,
    private val onTabClose: (Int) -> Unit,
    private val onTabLongClick: (Int, View) -> Unit
) : ListAdapter<BrowserTab, TabTrayAdapter.TabViewHolder>(DIFF_CALLBACK) {

    var activeIndex: Int = 0
        set(value) {
            val old = field
            field = value
            notifyItemChanged(old)
            notifyItemChanged(value)
        }

    inner class TabViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.tab_title)
        val url: TextView = itemView.findViewById(R.id.tab_url)
        val favicon: ImageView = itemView.findViewById(R.id.tab_favicon)
        val closeBtn: ImageView = itemView.findViewById(R.id.tab_close)
        val activeIndicator: View = itemView.findViewById(R.id.tab_active_indicator)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TabViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_tab, parent, false)
        return TabViewHolder(view)
    }

    override fun onBindViewHolder(holder: TabViewHolder, position: Int) {
        val tab = getItem(position)
        holder.title.text = tab.title.ifBlank { "New tab" }
        holder.url.text = tab.url
        tab.favicon?.let { holder.favicon.setImageBitmap(it) }
            ?: holder.favicon.setImageResource(R.drawable.ic_tab_default)
        holder.activeIndicator.visibility =
            if (position == activeIndex) View.VISIBLE else View.INVISIBLE
        holder.itemView.isSelected = (position == activeIndex)
        holder.itemView.setOnClickListener { onTabClick(position) }
        holder.closeBtn.setOnClickListener { onTabClose(position) }
        holder.itemView.setOnLongClickListener {
            onTabLongClick(position, it)
            true
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<BrowserTab>() {
            override fun areItemsTheSame(a: BrowserTab, b: BrowserTab) = a.id == b.id
            override fun areContentsTheSame(a: BrowserTab, b: BrowserTab) = a == b
        }
    }
}
