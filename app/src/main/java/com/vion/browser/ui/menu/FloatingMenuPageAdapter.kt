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
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.vion.browser.R
import com.vion.browser.model.FloatingMenuItem

/**
 * Adapter for one page (10 slots) of the floating 2×5 menu grid.
 */
class FloatingMenuPageAdapter(
    private val items: List<FloatingMenuItem>,
    private val onItemClick: (FloatingMenuItem) -> Unit,
    private val onItemLongClick: (FloatingMenuItem, View) -> Unit
) : RecyclerView.Adapter<FloatingMenuPageAdapter.ItemVH>() {

    inner class ItemVH(v: View) : RecyclerView.ViewHolder(v) {
        val icon: ImageView = v.findViewById(R.id.menu_item_icon)
        val label: TextView = v.findViewById(R.id.menu_item_label)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ItemVH(LayoutInflater.from(parent.context)
            .inflate(R.layout.item_floating_menu, parent, false))

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ItemVH, position: Int) {
        val item = items[position]
        holder.icon.setImageResource(item.iconRes)
        holder.label.setText(item.labelRes)
        holder.itemView.alpha = if (item.isEnabled) 1f else 0.4f
        holder.itemView.setOnClickListener { if (item.isEnabled) onItemClick(item) }
        holder.itemView.setOnLongClickListener {
            onItemLongClick(item, it)
            true
        }
    }
}
