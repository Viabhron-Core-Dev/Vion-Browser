/*
 * Copyright 2026 Vion Browser Contributors. GPL v3.
 */
package com.vion.browser.ui.panel

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.vion.browser.databinding.ItemPanelEntryBinding

/**
 * Generic single-column list adapter reused by Bookmarks, History,
 * Saved Pages, and Downloads tabs.
 *
 * @param T  the data type held in each row
 * @param titleOf     primary text extractor
 * @param subtitleOf  secondary (grey) text extractor
 * @param onClick     click handler
 * @param onLongClick long-click handler, receives the item and the anchor view
 */
class PanelListAdapter<T : Any>(
    private val titleOf: (T) -> String,
    private val subtitleOf: (T) -> String,
    private val onClick: (T) -> Unit,
    private val onLongClick: (T, View) -> Unit
) : ListAdapter<T, PanelListAdapter<T>.VH>(object : DiffUtil.ItemCallback<T>() {
    override fun areItemsTheSame(a: T, b: T) = a === b || a == b
    override fun areContentsTheSame(a: T, b: T) = a == b
}) {

    inner class VH(val binding: ItemPanelEntryBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        VH(ItemPanelEntryBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = getItem(position)
        holder.binding.panelEntryTitle.text = titleOf(item)
        holder.binding.panelEntrySubtitle.text = subtitleOf(item)
        holder.binding.root.setOnClickListener { onClick(item) }
        holder.binding.root.setOnLongClickListener {
            onLongClick(item, holder.binding.root)
            true
        }
    }
}
