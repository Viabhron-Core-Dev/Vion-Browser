/*
 * Copyright 2026 Vion Browser Contributors. GPL v3.
 */
package com.vion.browser.ui.ai

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.vion.browser.databinding.ItemAiMessageBinding

class AiChatAdapter : ListAdapter<AiMessage, AiChatAdapter.VH>(DIFF) {

    inner class VH(val binding: ItemAiMessageBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        VH(ItemAiMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        val msg = getItem(position)
        holder.binding.aiMessageText.text = msg.content
        val isUser = msg.role == "user"
        holder.binding.root.isActivated = isUser   // used by background selector
        holder.binding.aiMessageText.textAlignment =
            if (isUser) android.view.View.TEXT_ALIGNMENT_TEXT_END
            else        android.view.View.TEXT_ALIGNMENT_TEXT_START
        // Tint the avatar dot: purple for AI, teal for user
        holder.binding.aiAvatarDot.setBackgroundColor(
            if (isUser) 0xFF26C6DA.toInt() else 0xFF6C63FF.toInt()
        )
    }

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<AiMessage>() {
            override fun areItemsTheSame(a: AiMessage, b: AiMessage) = a === b
            override fun areContentsTheSame(a: AiMessage, b: AiMessage) = a == b
        }
    }
}
