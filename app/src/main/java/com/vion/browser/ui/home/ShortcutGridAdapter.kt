/*
 * Copyright 2026 Vion Browser Contributors. GPL v3.
 */
package com.vion.browser.ui.home

import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.vion.browser.data.db.ShortcutEntity
import com.vion.browser.databinding.ItemShortcutBinding

class ShortcutGridAdapter(
    private val onItemClick: (ShortcutEntity) -> Unit,
    private val onItemLongClick: (ShortcutEntity) -> Unit
) : ListAdapter<ShortcutEntity, ShortcutGridAdapter.VH>(DIFF) {

    inner class VH(val binding: ItemShortcutBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        VH(ItemShortcutBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = getItem(position)
        holder.binding.shortcutLabel.text = item.label

        // Try to decode saved favicon
        if (item.iconBase64 != null) {
            try {
                val bytes = Base64.decode(item.iconBase64, Base64.DEFAULT)
                val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                if (bmp != null) holder.binding.shortcutIcon.setImageBitmap(bmp)
            } catch (_: Exception) { /* use default */ }
        }

        holder.binding.root.setOnClickListener { onItemClick(item) }
        holder.binding.root.setOnLongClickListener { onItemLongClick(item); true }
    }

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<ShortcutEntity>() {
            override fun areItemsTheSame(a: ShortcutEntity, b: ShortcutEntity) = a.id == b.id
            override fun areContentsTheSame(a: ShortcutEntity, b: ShortcutEntity) = a == b
        }
    }
}
